import { useMemo } from 'react'
import { Link } from 'react-router-dom'
import type { ColumnDef } from '@tanstack/react-table'
import { PaginaCrud } from '@/shared/components/crud/PaginaCrud'
import { Badge } from '@/shared/components/ui'
import { clientesApi, mascotasApi } from '@/shared/api/recursos'
import type { Cliente } from '@/shared/types/api'

export function PaginaClientes() {
  const mascotas = mascotasApi.useLista()

  /** Conteo de mascotas por cliente: el backend no lo agrega, se calcula aquí. */
  const mascotasPorCliente = useMemo(() => {
    const conteo = new Map<string, number>()
    for (const m of mascotas.data ?? []) {
      conteo.set(m.idCliente, (conteo.get(m.idCliente) ?? 0) + 1)
    }
    return conteo
  }, [mascotas.data])

  const columnas = useMemo<ColumnDef<Cliente, unknown>[]>(
    () => [
      { accessorKey: 'identificacion', header: 'Identificación' },
      { accessorKey: 'nombres', header: 'Nombres' },
      {
        id: 'mascotas',
        header: 'Mascotas',
        accessorFn: (c) => mascotasPorCliente.get(c.idCliente) ?? 0,
        cell: ({ row }) => {
          const total = mascotasPorCliente.get(row.original.idCliente) ?? 0
          return total === 0 ? (
            <Badge>Sin mascotas</Badge>
          ) : (
            <Link
              to={`/mascotas?cliente=${row.original.idCliente}`}
              className="text-brand-700 hover:underline"
            >
              {total} {total === 1 ? 'mascota' : 'mascotas'}
            </Link>
          )
        },
      },
    ],
    [mascotasPorCliente],
  )

  return (
    <PaginaCrud<Cliente>
      titulo="Clientes"
      singular="cliente"
      descripcion="Propietarios registrados en la clínica"
      api={clientesApi}
      campoId="idCliente"
      multiEmpresa
      columnas={columnas}
      campos={[
        {
          nombre: 'identificacion',
          etiqueta: 'Identificación',
          tipo: 'texto',
          requerido: true,
          maxLength: 20,
          ayuda: 'Cédula, RUC o pasaporte',
        },
        {
          nombre: 'nombres',
          etiqueta: 'Nombres completos',
          tipo: 'texto',
          requerido: true,
          maxLength: 150,
        },
      ]}
    />
  )
}
