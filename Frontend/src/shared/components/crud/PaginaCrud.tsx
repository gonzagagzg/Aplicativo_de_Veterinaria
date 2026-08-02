import { useMemo, useState } from 'react'
import { Pencil, Plus, Trash2 } from 'lucide-react'
import type { ColumnDef } from '@tanstack/react-table'
import { TablaDatos } from '../TablaDatos'
import { Boton, CabeceraPagina, Cargando, MensajeError, Modal } from '../ui'
import { FormularioCrud } from './FormularioCrud'
import { filtrarPorEmpresa, useSesion } from '@/shared/session/sesion'
import type { ConfiguracionCrud } from './tipos'

/**
 * Página CRUD completa a partir de una configuración declarativa.
 *
 * Cubre listado + búsqueda + alta + edición + baja con confirmación.
 * Los módulos con lógica de negocio real (citas, consulta clínica,
 * facturación) NO usan esto: tienen su propia pantalla.
 */
export function PaginaCrud<T extends object>({
  titulo,
  singular,
  descripcion,
  api,
  campoId,
  campos,
  columnas,
  multiEmpresa,
  valoresBase,
}: ConfiguracionCrud<T>) {
  const idEmpresa = useSesion((s) => s.idEmpresa)

  const [modalAbierto, setModalAbierto] = useState(false)
  const [registroEditado, setRegistroEditado] = useState<T | null>(null)
  const [registroAEliminar, setRegistroAEliminar] = useState<T | null>(null)

  const lista = api.useLista()
  const crear = api.useCrear()
  const actualizar = api.useActualizar()
  const eliminar = api.useEliminar()

  const datos = useMemo(() => {
    const registros = (lista.data ?? []) as T[]
    return multiEmpresa
      ? (filtrarPorEmpresa(registros as { idEmpresa?: string }[], idEmpresa) as T[])
      : registros
  }, [lista.data, multiEmpresa, idEmpresa])

  const columnasConAcciones = useMemo<ColumnDef<T, unknown>[]>(
    () => [
      ...columnas,
      {
        id: 'acciones',
        header: '',
        enableSorting: false,
        cell: ({ row }) => (
          <div className="flex justify-end gap-1">
            <button
              onClick={() => {
                setRegistroEditado(row.original)
                setModalAbierto(true)
              }}
              className="rounded-lg p-1.5 text-slate-500 hover:bg-slate-100 hover:text-brand-700"
              aria-label={`Editar ${singular}`}
            >
              <Pencil className="h-4 w-4" />
            </button>
            <button
              onClick={() => setRegistroAEliminar(row.original)}
              className="rounded-lg p-1.5 text-slate-500 hover:bg-red-50 hover:text-red-600"
              aria-label={`Eliminar ${singular}`}
            >
              <Trash2 className="h-4 w-4" />
            </button>
          </div>
        ),
      } as ColumnDef<T, unknown>,
    ],
    [columnas, singular],
  )

  function abrirCreacion() {
    setRegistroEditado(null)
    crear.reset()
    actualizar.reset()
    setModalAbierto(true)
  }

  function cerrarModal() {
    setModalAbierto(false)
    setRegistroEditado(null)
  }

  function enviar(datosFormulario: Partial<T>) {
    if (registroEditado) {
      actualizar.mutate(
        {
          id: registroEditado[campoId] as string | number,
          // Se conserva el registro original para no perder campos que el
          // formulario no expone (p. ej. claveHash o idEmpresa).
          datos: { ...registroEditado, ...datosFormulario },
        },
        { onSuccess: cerrarModal },
      )
    } else {
      const payload: Record<string, unknown> = { ...valoresBase, ...datosFormulario }
      if (multiEmpresa && idEmpresa) payload.idEmpresa = idEmpresa
      crear.mutate(payload as Partial<T>, { onSuccess: cerrarModal })
    }
  }

  if (lista.isLoading) return <Cargando />

  return (
    <>
      <CabeceraPagina
        titulo={titulo}
        descripcion={descripcion}
        acciones={
          <Boton onClick={abrirCreacion}>
            <Plus className="h-4 w-4" />
            Nuevo
          </Boton>
        }
      />

      {lista.isError && (
        <div className="mb-4">
          <MensajeError error={lista.error} />
        </div>
      )}

      <TablaDatos
        datos={datos}
        columnas={columnasConAcciones}
        buscarPlaceholder={`Buscar en ${titulo.toLowerCase()}…`}
      />

      <Modal
        abierto={modalAbierto}
        titulo={registroEditado ? `Editar ${singular}` : `Nuevo ${singular}`}
        onCerrar={cerrarModal}
        ancho="max-w-2xl"
      >
        <FormularioCrud<T>
          campos={campos}
          valoresIniciales={registroEditado ?? undefined}
          edicion={Boolean(registroEditado)}
          enviando={crear.isPending || actualizar.isPending}
          error={crear.error ?? actualizar.error}
          onEnviar={enviar}
          onCancelar={cerrarModal}
        />
      </Modal>

      <Modal
        abierto={Boolean(registroAEliminar)}
        titulo={`Eliminar ${singular}`}
        onCerrar={() => setRegistroAEliminar(null)}
      >
        <p className="text-sm text-slate-600">
          Esta acción no se puede deshacer. Si el registro está referenciado por otros datos, el
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
              eliminar.mutate(registroAEliminar[campoId] as string | number, {
                onSuccess: () => setRegistroAEliminar(null),
              })
            }
          >
            Eliminar
          </Boton>
        </div>
      </Modal>
    </>
  )
}
