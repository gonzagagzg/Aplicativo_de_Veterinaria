import { useMemo, useState } from 'react'
import type { ColumnDef } from '@tanstack/react-table'
import { Building2 } from 'lucide-react'
import { PaginaCrud } from '@/shared/components/crud/PaginaCrud'
import { TablaDatos } from '@/shared/components/TablaDatos'
import { Badge, CabeceraPagina, Campo, Cargando, MensajeError, Select } from '@/shared/components/ui'
import { indexarPor } from '@/shared/lib/utils'
import { empresaAdminApi, empresasApi, rolesApi, usuariosApi } from '@/shared/api/recursos'
import { useSesion } from '@/shared/session/sesion'
import type { Usuario, Uuid } from '@/shared/types/api'

export function PaginaUsuarios() {
  const rol = useSesion((s) => s.rol)
  const esSuper = rol?.toLowerCase() === 'superusuario'

  return esSuper ? <UsuariosPorVeterinaria /> : <UsuariosDeMiEmpresa />
}

/**
 * SuperUsuario: primero elige una veterinaria y solo entonces ve sus
 * usuarios (GET /api/usuarios/empresa/{idEmpresa}). Es una vista de solo
 * lectura — el alta/edición de usuarios se hace desde la propia veterinaria
 * con un Administrador Local.
 */
function UsuariosPorVeterinaria() {
  const empresas = empresasApi.useLista()
  const roles = rolesApi.useLista()
  const [idEmpresa, setIdEmpresa] = useState<Uuid | ''>('')

  const usuarios = empresaAdminApi.useUsuariosDeEmpresa(idEmpresa || undefined)
  const porRol = useMemo(() => indexarPor(roles.data, 'idRol'), [roles.data])

  const columnas = useMemo<ColumnDef<Usuario, unknown>[]>(
    () => [
      { accessorKey: 'usuario', header: 'Usuario' },
      { accessorKey: 'nombres', header: 'Nombres' },
      {
        id: 'rol',
        header: 'Rol',
        accessorFn: (u) => porRol.get(String(u.idRol))?.nombre ?? '—',
      },
      {
        id: 'activo',
        header: 'Estado',
        accessorFn: (u) => (u.activo ? 'Activo' : 'Inactivo'),
        cell: ({ row }) =>
          row.original.activo ? <Badge tono="exito">Activo</Badge> : <Badge>Inactivo</Badge>,
      },
    ],
    [porRol],
  )

  return (
    <div>
      <CabeceraPagina
        titulo="Usuarios"
        descripcion="Selecciona una veterinaria para ver únicamente sus usuarios."
      />

      <div className="mb-5 max-w-sm">
        <Campo etiqueta="Veterinaria">
          <Select value={idEmpresa} onChange={(e) => setIdEmpresa(e.target.value)}>
            <option value="">— Seleccionar veterinaria —</option>
            {(empresas.data ?? []).map((e) => (
              <option key={e.idEmpresa} value={e.idEmpresa}>
                {e.razonSocial}
              </option>
            ))}
          </Select>
        </Campo>
      </div>

      {!idEmpresa ? (
        <div className="flex items-center gap-2 py-16 text-sm text-slate-400">
          <Building2 className="h-4 w-4" />
          Elige una veterinaria para ver sus usuarios
        </div>
      ) : usuarios.isLoading ? (
        <Cargando />
      ) : usuarios.error ? (
        <MensajeError error={usuarios.error} />
      ) : (
        <TablaDatos
          datos={usuarios.data ?? []}
          columnas={columnas}
          buscarPlaceholder="Buscar por usuario o nombres…"
        />
      )}
    </div>
  )
}

/** Administrador Local (y demás roles): CRUD completo, acotado a su propia empresa. */
function UsuariosDeMiEmpresa() {
  const roles = rolesApi.useLista()
  const porRol = useMemo(() => indexarPor(roles.data, 'idRol'), [roles.data])

  const columnas = useMemo<ColumnDef<Usuario, unknown>[]>(
    () => [
      { accessorKey: 'usuario', header: 'Usuario' },
      { accessorKey: 'nombres', header: 'Nombres' },
      {
        id: 'rol',
        header: 'Rol',
        accessorFn: (u) => porRol.get(String(u.idRol))?.nombre ?? '—',
      },
      {
        id: 'activo',
        header: 'Estado',
        accessorFn: (u) => (u.activo ? 'Activo' : 'Inactivo'),
        cell: ({ row }) =>
          row.original.activo ? <Badge tono="exito">Activo</Badge> : <Badge>Inactivo</Badge>,
      },
    ],
    [porRol],
  )

  return (
    <PaginaCrud<Usuario>
      titulo="Usuarios"
      singular="usuario"
      descripcion="Cuentas de acceso al sistema"
      api={usuariosApi}
      campoId="idUsuario"
      multiEmpresa
      columnas={columnas}
      campos={[
        { nombre: 'usuario', etiqueta: 'Usuario', tipo: 'texto', requerido: true, maxLength: 50 },
        { nombre: 'nombres', etiqueta: 'Nombres', tipo: 'texto', requerido: true, maxLength: 100 },
        {
          nombre: 'idRol',
          etiqueta: 'Rol',
          tipo: 'select',
          requerido: true,
          opcionesNumericas: true,
          opciones: (roles.data ?? []).map((r) => ({ valor: r.idRol, etiqueta: r.nombre })),
        },
        { nombre: 'activo', etiqueta: 'Activo', tipo: 'booleano' },
        {
          nombre: 'claveHash',
          etiqueta: 'Contraseña',
          tipo: 'contrasena',
          requerido: true,
          maxLength: 255,
          anchoCompleto: true,
          ayuda: 'El backend la cifra con BCrypt antes de guardarla y nunca la devuelve.',
        },
      ]}
    />
  )
}
