import { useMemo } from 'react'
import type { ColumnDef } from '@tanstack/react-table'
import { PaginaCrud } from '@/shared/components/crud/PaginaCrud'
import { indexarPor } from '@/shared/lib/utils'
import { usuariosApi, veterinariosApi } from '@/shared/api/recursos'
import { filtrarPorEmpresa, useSesion } from '@/shared/session/sesion'
import type { Veterinario } from '@/shared/types/api'

export function PaginaVeterinarios() {
  const idEmpresa = useSesion((s) => s.idEmpresa)

  const usuarios = usuariosApi.useLista()
  const veterinarios = veterinariosApi.useLista()

  const porUsuario = useMemo(() => indexarPor(usuarios.data, 'idUsuario'), [usuarios.data])

  const columnas = useMemo<ColumnDef<Veterinario, unknown>[]>(
    () => [
      {
        id: 'nombre',
        header: 'Nombre',
        accessorFn: (v) => porUsuario.get(v.idUsuario)?.nombres ?? '—',
      },
      {
        id: 'usuario',
        header: 'Usuario',
        accessorFn: (v) => porUsuario.get(v.idUsuario)?.usuario ?? '—',
      },
      { accessorKey: 'especialidad', header: 'Especialidad' },
    ],
    [porUsuario],
  )

  // La BD impone id_usuario UNIQUE (relación 1:1), así que solo se ofrecen
  // usuarios de la empresa que todavía no están vinculados a un veterinario.
  const yaVinculados = new Set((veterinarios.data ?? []).map((v) => v.idUsuario))
  const usuariosDisponibles = filtrarPorEmpresa(usuarios.data, idEmpresa).filter(
    (u) => u.activo && !yaVinculados.has(u.idUsuario),
  )

  return (
    <PaginaCrud<Veterinario>
      titulo="Veterinarios"
      singular="veterinario"
      descripcion="Profesionales que atienden las citas"
      api={veterinariosApi}
      campoId="idVeterinario"
      multiEmpresa
      columnas={columnas}
      campos={[
        {
          nombre: 'idUsuario',
          etiqueta: 'Usuario asociado',
          tipo: 'select',
          requerido: true,
          soloCreacion: true,
          ayuda: 'Relación 1 a 1: cada usuario puede ser un único veterinario',
          opciones: usuariosDisponibles.map((u) => ({
            valor: u.idUsuario,
            etiqueta: `${u.nombres} (${u.usuario})`,
          })),
        },
        {
          nombre: 'especialidad',
          etiqueta: 'Especialidad',
          tipo: 'texto',
          maxLength: 100,
          placeholder: 'Medicina interna, cirugía…',
        },
      ]}
    />
  )
}
