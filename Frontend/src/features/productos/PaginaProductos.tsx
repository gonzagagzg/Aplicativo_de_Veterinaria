import { useMemo } from 'react'
import type { ColumnDef } from '@tanstack/react-table'
import { PaginaCrud } from '@/shared/components/crud/PaginaCrud'
import { Badge } from '@/shared/components/ui'
import { formatearFecha, formatearMoneda, indexarPor } from '@/shared/lib/utils'
import { categoriasApi, productosApi, sriIvaApi } from '@/shared/api/recursos'
import { filtrarPorEmpresa, useSesion } from '@/shared/session/sesion'
import type { Producto } from '@/shared/types/api'

export function PaginaProductos() {
  const idEmpresa = useSesion((s) => s.idEmpresa)

  const categorias = categoriasApi.useLista()
  const ivas = sriIvaApi.useLista()

  const porCategoria = useMemo(() => indexarPor(categorias.data, 'idCategoria'), [categorias.data])
  const porIva = useMemo(() => indexarPor(ivas.data, 'idIva'), [ivas.data])

  const columnas = useMemo<ColumnDef<Producto, unknown>[]>(
    () => [
      { accessorKey: 'nombre', header: 'Producto' },
      {
        id: 'categoria',
        header: 'Categoría',
        accessorFn: (p) => porCategoria.get(String(p.idCategoria))?.nombre ?? '—',
      },
      {
        id: 'precio',
        header: 'Precio',
        accessorFn: (p) => p.precioUnitario,
        cell: ({ row }) => formatearMoneda(row.original.precioUnitario),
      },
      {
        id: 'iva',
        header: 'IVA',
        accessorFn: (p) => {
          const iva = porIva.get(String(p.idIva))
          return iva ? `${Number(iva.porcentaje).toFixed(0)} %` : '—'
        },
      },
      {
        id: 'stock',
        header: 'Stock',
        accessorFn: (p) => p.stockActual,
        cell: ({ row }) => {
          const { stockActual, stockMinimo } = row.original
          const bajo = stockActual <= stockMinimo
          return (
            <span className="inline-flex items-center gap-2">
              <span className={bajo ? 'font-semibold text-amber-700' : ''}>{stockActual}</span>
              {bajo && <Badge tono="alerta">Bajo mínimo</Badge>}
            </span>
          )
        },
      },
      {
        id: 'caducidad',
        header: 'Caducidad',
        accessorFn: (p) => formatearFecha(p.fechaCaducidad),
      },
    ],
    [porCategoria, porIva],
  )

  const categoriasEmpresa = filtrarPorEmpresa(categorias.data, idEmpresa)

  return (
    <PaginaCrud<Producto>
      titulo="Productos"
      singular="producto"
      descripcion="Medicamentos, insumos y servicios facturables"
      api={productosApi}
      campoId="idProducto"
      multiEmpresa
      columnas={columnas}
      campos={[
        {
          nombre: 'nombre',
          etiqueta: 'Nombre',
          tipo: 'texto',
          requerido: true,
          maxLength: 100,
          anchoCompleto: true,
        },
        {
          nombre: 'idCategoria',
          etiqueta: 'Categoría',
          tipo: 'select',
          requerido: true,
          opcionesNumericas: true,
          opciones: categoriasEmpresa.map((c) => ({ valor: c.idCategoria, etiqueta: c.nombre })),
        },
        {
          nombre: 'idIva',
          etiqueta: 'Tarifa de IVA',
          tipo: 'select',
          requerido: true,
          opcionesNumericas: true,
          opciones: (ivas.data ?? []).map((i) => ({
            valor: i.idIva,
            etiqueta: `${Number(i.porcentaje).toFixed(2)} %${i.codigoSri ? ` (${i.codigoSri})` : ''}`,
          })),
        },
        {
          nombre: 'precioUnitario',
          etiqueta: 'Precio unitario',
          tipo: 'decimal',
          requerido: true,
          min: 0,
        },
        {
          nombre: 'stockActual',
          etiqueta: 'Stock actual',
          tipo: 'numero',
          requerido: true,
          min: 0,
          ayuda: 'Los movimientos de inventario lo ajustan después',
        },
        { nombre: 'stockMinimo', etiqueta: 'Stock mínimo', tipo: 'numero', requerido: true, min: 0 },
        { nombre: 'fechaCaducidad', etiqueta: 'Fecha de caducidad', tipo: 'fecha' },
      ]}
    />
  )
}
