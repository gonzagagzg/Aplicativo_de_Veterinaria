import { useEffect, useMemo, useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { ChevronLeft, ChevronRight, Pencil, Plus, Trash2 } from 'lucide-react'
import { flexRender, getCoreRowModel, useReactTable, type ColumnDef } from '@tanstack/react-table'
import {
  Badge,
  Boton,
  CabeceraPagina,
  Campo,
  Cargando,
  Input,
  MensajeError,
  Modal,
  Select,
  SinDatos,
} from '@/shared/components/ui'
import { clientesApi, empresasApi, mascotasApi } from '@/shared/api/recursos'
import { useClientesPaginado } from './api'
import { cedulaEcuatorianaValida } from '@/shared/lib/sri'
import { esSuperUsuario, useSesion } from '@/shared/session/sesion'
import type { Cliente } from '@/shared/types/api'

const TAMANIO_PAGINA = 20

/**
 * Clientes con paginación real de servidor (GET /api/clientes?pagina&tamanio,
 * ver ClienteService.listarPaginado) — este módulo, a diferencia del resto,
 * ya no puede usar TablaDatos/PaginaCrud genéricos porque esos paginan en
 * cliente sobre una lista completa que el backend ya no entrega.
 *
 * También muestra en el formulario las validaciones SRI (cédula ecuatoriana,
 * 10 dígitos) que el backend aplica en ClienteService.validar().
 */
export function PaginaClientes() {
  const [pagina, setPagina] = useState(0)
  const paginaResp = useClientesPaginado(pagina, TAMANIO_PAGINA)
  const mascotas = mascotasApi.useLista()

  const [modalAbierto, setModalAbierto] = useState(false)
  const [registroEditado, setRegistroEditado] = useState<Cliente | null>(null)
  const [registroAEliminar, setRegistroAEliminar] = useState<Cliente | null>(null)

  const eliminar = clientesApi.useEliminar()

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
      {
        id: 'acciones',
        header: '',
        cell: ({ row }) => (
          <div className="flex justify-end gap-1">
            <button
              onClick={() => {
                setRegistroEditado(row.original)
                setModalAbierto(true)
              }}
              className="rounded-lg p-1.5 text-slate-500 hover:bg-slate-100 hover:text-brand-700"
              aria-label="Editar cliente"
            >
              <Pencil className="h-4 w-4" />
            </button>
            <button
              onClick={() => setRegistroAEliminar(row.original)}
              className="rounded-lg p-1.5 text-slate-500 hover:bg-red-50 hover:text-red-600"
              aria-label="Eliminar cliente"
            >
              <Trash2 className="h-4 w-4" />
            </button>
          </div>
        ),
      },
    ],
    [mascotasPorCliente],
  )

  const tabla = useReactTable({
    data: paginaResp.data?.contenido ?? [],
    columns: columnas as ColumnDef<Cliente, unknown>[],
    getCoreRowModel: getCoreRowModel(),
  })

  function abrirCreacion() {
    setRegistroEditado(null)
    setModalAbierto(true)
  }

  return (
    <div>
      <CabeceraPagina
        titulo="Clientes"
        descripcion="Propietarios registrados en la clínica"
        acciones={
          <Boton onClick={abrirCreacion}>
            <Plus className="h-4 w-4" />
            Nuevo
          </Boton>
        }
      />

      {paginaResp.isLoading ? (
        <Cargando />
      ) : paginaResp.isError ? (
        <MensajeError error={paginaResp.error} />
      ) : (
        <div className="rounded-xl border border-slate-200 bg-white">
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="bg-slate-50 text-left">
                {tabla.getHeaderGroups().map((grupo) => (
                  <tr key={grupo.id}>
                    {grupo.headers.map((header) => (
                      <th
                        key={header.id}
                        className="whitespace-nowrap px-4 py-2.5 text-xs font-semibold uppercase tracking-wide text-slate-500"
                      >
                        {header.isPlaceholder
                          ? null
                          : flexRender(header.column.columnDef.header, header.getContext())}
                      </th>
                    ))}
                  </tr>
                ))}
              </thead>
              <tbody className="divide-y divide-slate-100">
                {tabla.getRowModel().rows.map((fila) => (
                  <tr key={fila.id} className="hover:bg-slate-50/70">
                    {fila.getVisibleCells().map((celda) => (
                      <td key={celda.id} className="px-4 py-2.5 text-slate-700">
                        {flexRender(celda.column.columnDef.cell, celda.getContext())}
                      </td>
                    ))}
                  </tr>
                ))}
              </tbody>
            </table>
            {(paginaResp.data?.contenido.length ?? 0) === 0 && <SinDatos />}
          </div>

          <div className="flex items-center justify-between border-t border-slate-200 px-4 py-2.5 text-sm text-slate-600">
            <span>
              Página {(paginaResp.data?.pagina ?? 0) + 1} de {Math.max(paginaResp.data?.totalPaginas ?? 1, 1)}{' '}
              · {paginaResp.data?.totalRegistros ?? 0} clientes
            </span>
            <div className="flex gap-1">
              <button
                onClick={() => setPagina((p) => Math.max(0, p - 1))}
                disabled={pagina === 0}
                className="rounded-lg p-1.5 hover:bg-slate-100 disabled:opacity-40"
                aria-label="Página anterior"
              >
                <ChevronLeft className="h-4 w-4" />
              </button>
              <button
                onClick={() => setPagina((p) => p + 1)}
                disabled={pagina + 1 >= (paginaResp.data?.totalPaginas ?? 1)}
                className="rounded-lg p-1.5 hover:bg-slate-100 disabled:opacity-40"
                aria-label="Página siguiente"
              >
                <ChevronRight className="h-4 w-4" />
              </button>
            </div>
          </div>
        </div>
      )}

      <ModalCliente
        abierto={modalAbierto}
        registro={registroEditado}
        onCerrar={() => {
          setModalAbierto(false)
          setRegistroEditado(null)
        }}
      />

      <Modal
        abierto={Boolean(registroAEliminar)}
        titulo="Eliminar cliente"
        onCerrar={() => setRegistroAEliminar(null)}
      >
        <p className="text-sm text-slate-600">
          Esta acción no se puede deshacer. Si el cliente tiene mascotas o facturas asociadas, el
          servidor rechazará la operación.
        </p>
        {eliminar.error && (
          <div className="mt-3">
            <MensajeError error={eliminar.error} />
          </div>
        )}
        <div className="mt-5 flex justify-end gap-2">
          <Boton variante="secundario" onClick={() => setRegistroAEliminar(null)}>
            Cancelar
          </Boton>
          <Boton
            variante="peligro"
            cargando={eliminar.isPending}
            onClick={() =>
              registroAEliminar &&
              eliminar.mutate(registroAEliminar.idCliente, {
                onSuccess: () => setRegistroAEliminar(null),
              })
            }
          >
            Eliminar
          </Boton>
        </div>
      </Modal>
    </div>
  )
}

function ModalCliente({
  abierto,
  registro,
  onCerrar,
}: {
  abierto: boolean
  registro: Cliente | null
  onCerrar: () => void
}) {
  const crear = clientesApi.useCrear()
  const actualizar = clientesApi.useActualizar()

  // El SuperUsuario no tiene una empresa propia en su token (ve todas las
  // veterinarias), así que el backend le exige `idEmpresa` explícito en el
  // body de creación/edición (ver ClienteService.crear/actualizar). Para
  // cualquier otro rol el backend lo resuelve solo desde el token y este
  // selector ni se muestra.
  const idEmpresaSesion = useSesion((s) => s.idEmpresa)
  const rol = useSesion((s) => s.rol)
  const esSuper = esSuperUsuario(rol)
  const empresas = empresasApi.useLista({ enabled: esSuper })

  const [identificacion, setIdentificacion] = useState('')
  const [nombres, setNombres] = useState('')
  const [idEmpresa, setIdEmpresa] = useState('')

  // Sincroniza el formulario cada vez que se abre el modal (nuevo o edición).
  useEffect(() => {
    if (abierto) {
      setIdentificacion(registro?.identificacion ?? '')
      setNombres(registro?.nombres ?? '')
      setIdEmpresa(registro?.idEmpresa ?? '')
    }
  }, [abierto, registro])

  function cerrarYLimpiar() {
    setIdentificacion('')
    setNombres('')
    setIdEmpresa('')
    crear.reset()
    actualizar.reset()
    onCerrar()
  }

  const cedulaLimpia = identificacion.replace(/\D/g, '')
  const cedulaCompleta = cedulaLimpia.length === 10
  const cedulaValida = cedulaCompleta && cedulaEcuatorianaValida(identificacion)
  const errorCedula = cedulaCompleta && !cedulaValida ? 'La cédula ecuatoriana no es válida' : undefined

  function enviar(e: FormEvent) {
    e.preventDefault()
    // El backend exige `idEmpresa` en el body cuando quien crea/edita es
    // SuperUsuario (ve clientes de todas las veterinarias, no puede inferir
    // el tenant); para el resto de roles lo ignora y usa el de su token.
    const empresaEnviada = esSuper ? idEmpresa : (registro?.idEmpresa ?? idEmpresaSesion)

    if (registro) {
      actualizar.mutate(
        {
          id: registro.idCliente,
          datos: { identificacion, nombres, idEmpresa: empresaEnviada ?? undefined },
        },
        { onSuccess: cerrarYLimpiar },
      )
    } else {
      crear.mutate(
        { identificacion, nombres, idEmpresa: empresaEnviada || undefined } as Partial<Cliente>,
        { onSuccess: cerrarYLimpiar },
      )
    }
  }

  const error = crear.error ?? actualizar.error
  const enviando = crear.isPending || actualizar.isPending
  const faltaEmpresa = esSuper && !idEmpresa

  return (
    <Modal
      abierto={abierto}
      titulo={registro ? 'Editar cliente' : 'Nuevo cliente'}
      onCerrar={cerrarYLimpiar}
      ancho="max-w-lg"
    >
      <form className="space-y-4" onSubmit={enviar}>
        {esSuper && (
          <Campo etiqueta="Veterinaria" requerido ayuda="Como SuperUsuario, elige a qué veterinaria pertenece">
            <Select value={idEmpresa} onChange={(e) => setIdEmpresa(e.target.value)}>
              <option value="">— Seleccionar —</option>
              {(empresas.data ?? []).map((emp) => (
                <option key={emp.idEmpresa} value={emp.idEmpresa}>
                  {emp.razonSocial} · {emp.ruc}
                </option>
              ))}
            </Select>
          </Campo>
        )}

        <Campo
          etiqueta="Cédula"
          requerido
          ayuda="10 dígitos, cédula ecuatoriana (validación SRI)"
          error={errorCedula}
        >
          <Input
            value={identificacion}
            onChange={(e) => setIdentificacion(e.target.value)}
            maxLength={10}
            inputMode="numeric"
            placeholder="1712345678"
          />
        </Campo>

        <Campo etiqueta="Nombres completos" requerido>
          <Input value={nombres} onChange={(e) => setNombres(e.target.value)} maxLength={150} />
        </Campo>

        {error && <MensajeError error={error} />}

        <div className="flex justify-end gap-2 pt-2">
          <Boton type="button" variante="secundario" onClick={cerrarYLimpiar}>
            Cancelar
          </Boton>
          <Boton
            type="submit"
            cargando={enviando}
            disabled={(cedulaCompleta && !cedulaValida) || faltaEmpresa}
          >
            {registro ? 'Guardar cambios' : 'Crear cliente'}
          </Boton>
        </div>
      </form>
    </Modal>
  )
}
