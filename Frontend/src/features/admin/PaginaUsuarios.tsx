import { useMemo } from 'react'
import type { ColumnDef } from '@tanstack/react-table'
import { PaginaCrud } from '@/shared/components/crud/PaginaCrud'
import { Badge } from '@/shared/components/ui'
import { indexarPor } from '@/shared/lib/utils'
import { rolesApi, usuariosApi } from '@/shared/api/recursos'
import type { Usuario } from '@/shared/types/api'

export function PaginaUsuarios() {
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
          etiqueta: 'Hash de contraseña',
          tipo: 'texto',
          requerido: true,
          maxLength: 255,
          anchoCompleto: true,
          ayuda:
            'El backend almacena el valor tal cual se envía: no lo cifra. Mientras no exista un endpoint de autenticación, este campo es un marcador de posición.',
        },
      ]}
    />
  )
}
