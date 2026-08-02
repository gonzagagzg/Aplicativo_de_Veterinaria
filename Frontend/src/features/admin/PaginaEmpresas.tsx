import { useMemo } from 'react'
import type { ColumnDef } from '@tanstack/react-table'
import { PaginaCrud } from '@/shared/components/crud/PaginaCrud'
import { Badge } from '@/shared/components/ui'
import { empresasApi } from '@/shared/api/recursos'
import type { Empresa } from '@/shared/types/api'

export function PaginaEmpresas() {
  const columnas = useMemo<ColumnDef<Empresa, unknown>[]>(
    () => [
      { accessorKey: 'ruc', header: 'RUC' },
      { accessorKey: 'razonSocial', header: 'Razón social' },
      { accessorKey: 'direccion', header: 'Dirección' },
      {
        id: 'activo',
        header: 'Estado',
        accessorFn: (e) => (e.activo ? 'Activa' : 'Inactiva'),
        cell: ({ row }) =>
          row.original.activo ? <Badge tono="exito">Activa</Badge> : <Badge>Inactiva</Badge>,
      },
    ],
    [],
  )

  return (
    <PaginaCrud<Empresa>
      titulo="Empresas"
      singular="empresa"
      descripcion="Cada empresa es un tenant independiente del sistema"
      api={empresasApi}
      campoId="idEmpresa"
      columnas={columnas}
      campos={[
        {
          nombre: 'ruc',
          etiqueta: 'RUC',
          tipo: 'texto',
          requerido: true,
          maxLength: 13,
          ayuda: '13 dígitos',
        },
        {
          nombre: 'razonSocial',
          etiqueta: 'Razón social',
          tipo: 'texto',
          requerido: true,
          maxLength: 150,
        },
        {
          nombre: 'direccion',
          etiqueta: 'Dirección',
          tipo: 'texto',
          requerido: true,
          maxLength: 255,
          anchoCompleto: true,
        },
        { nombre: 'activo', etiqueta: 'Activa', tipo: 'booleano' },
      ]}
    />
  )
}
