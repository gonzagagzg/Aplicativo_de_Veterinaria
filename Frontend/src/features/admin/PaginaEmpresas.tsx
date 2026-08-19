import { useMemo, useState, type FormEvent } from 'react'
import type { ColumnDef } from '@tanstack/react-table'
import { BarChart3, Building2, Plus, Power, PowerOff } from 'lucide-react'
import { TablaDatos } from '@/shared/components/TablaDatos'
import {
  Badge,
  Boton,
  CabeceraPagina,
  Campo,
  Cargando,
  Input,
  MensajeError,
  Modal,
} from '@/shared/components/ui'
import { empresaAdminApi, empresasApi } from '@/shared/api/recursos'
import type { Empresa, EmpresaConAdmin, Uuid } from '@/shared/types/api'
import { formatearMoneda } from '@/shared/lib/utils'

/**
 * Panel exclusivo de SuperUsuario: todas las veterinarias del SaaS,
 * alta de nuevas y activar/desactivar (bloqueo por falta de pago).
 * El backend ya restringe todo /api/empresas/* a rol SuperUsuario
 * (ver EmpresaServlet.exigirSuperUsuario) y la ruta está protegida en
 * el router (`RequiereSuperUsuario`).
 */
export function PaginaEmpresas() {
  const lista = empresasApi.useLista()
  const activar = empresaAdminApi.useActivar()
  const desactivar = empresaAdminApi.useDesactivar()

  const [modalNuevaAbierto, setModalNuevaAbierto] = useState(false)
  const [idResumen, setIdResumen] = useState<Uuid | null>(null)

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
          row.original.activo ? (
            <Badge tono="exito">Activa</Badge>
          ) : (
            <Badge tono="peligro">Inactiva</Badge>
          ),
      },
      {
        id: 'acciones',
        header: '',
        cell: ({ row }) => {
          const empresa = row.original
          const enCurso = activar.isPending || desactivar.isPending
          return (
            <div className="flex justify-end gap-1.5">
              <button
                type="button"
                onClick={() => setIdResumen(empresa.idEmpresa)}
                className="rounded-lg p-1.5 text-slate-400 hover:bg-slate-100 hover:text-brand-600"
                title="Ver resumen de uso"
              >
                <BarChart3 className="h-4 w-4" />
              </button>
              {empresa.activo ? (
                <button
                  type="button"
                  disabled={enCurso}
                  onClick={() => desactivar.mutate(empresa.idEmpresa)}
                  className="rounded-lg p-1.5 text-slate-400 hover:bg-red-50 hover:text-red-600 disabled:opacity-40"
                  title="Desactivar (bloquear por falta de pago)"
                >
                  <PowerOff className="h-4 w-4" />
                </button>
              ) : (
                <button
                  type="button"
                  disabled={enCurso}
                  onClick={() => activar.mutate(empresa.idEmpresa)}
                  className="rounded-lg p-1.5 text-slate-400 hover:bg-emerald-50 hover:text-emerald-600 disabled:opacity-40"
                  title="Activar"
                >
                  <Power className="h-4 w-4" />
                </button>
              )}
            </div>
          )
        },
      },
    ],
    [activar, desactivar],
  )

  return (
    <div>
      <CabeceraPagina
        titulo="Veterinarias"
        descripcion="Todas las veterinarias registradas en el sistema (SaaS). Desactivar bloquea el acceso completo por falta de pago."
        acciones={
          <Boton onClick={() => setModalNuevaAbierto(true)}>
            <Plus className="h-4 w-4" />
            Nueva veterinaria
          </Boton>
        }
      />

      {lista.isLoading ? (
        <Cargando />
      ) : lista.error ? (
        <MensajeError error={lista.error} />
      ) : (
        <TablaDatos
          datos={lista.data ?? []}
          columnas={columnas}
          buscarPlaceholder="Buscar por RUC o razón social…"
        />
      )}

      {(activar.error || desactivar.error) && (
        <div className="mt-3">
          <MensajeError error={activar.error ?? desactivar.error} />
        </div>
      )}

      <ModalNuevaEmpresa abierto={modalNuevaAbierto} onCerrar={() => setModalNuevaAbierto(false)} />
      <ModalResumenEmpresa idEmpresa={idResumen} onCerrar={() => setIdResumen(null)} />
    </div>
  )
}

function ModalNuevaEmpresa({ abierto, onCerrar }: { abierto: boolean; onCerrar: () => void }) {
  const crear = empresasApi.useCrear()
  const [ruc, setRuc] = useState('')
  const [razonSocial, setRazonSocial] = useState('')
  const [direccion, setDireccion] = useState('')
  const [adminUsuario, setAdminUsuario] = useState('')
  const [adminNombres, setAdminNombres] = useState('')
  const [adminContrasena, setAdminContrasena] = useState('')
  const [confirmarContrasena, setConfirmarContrasena] = useState('')

  function cerrarYLimpiar() {
    setRuc('')
    setRazonSocial('')
    setDireccion('')
    setAdminUsuario('')
    setAdminNombres('')
    setAdminContrasena('')
    setConfirmarContrasena('')
    crear.reset()
    onCerrar()
  }

  const contrasenaCorta = adminContrasena.length > 0 && adminContrasena.length < 6
  const noCoincide =
    confirmarContrasena.length > 0 && confirmarContrasena !== adminContrasena

  function enviar(e: FormEvent) {
    e.preventDefault()
    // El backend crea la veterinaria y su administrador ("Administrador Local")
    // en una sola transacción (EmpresaService.crearConAdmin).
    const payload = {
      ruc,
      razonSocial,
      direccion,
      activo: true,
      adminUsuario,
      adminNombres,
      adminContrasena,
    } satisfies Omit<EmpresaConAdmin, 'idEmpresa'>
    crear.mutate(payload as unknown as Partial<Empresa>, { onSuccess: cerrarYLimpiar })
  }

  return (
    <Modal abierto={abierto} titulo="Nueva veterinaria" onCerrar={cerrarYLimpiar}>
      <form className="space-y-4" onSubmit={enviar}>
        <div className="space-y-4 rounded-lg border border-slate-200 p-4">
          <p className="text-xs font-semibold uppercase tracking-wide text-slate-500">
            Datos de la veterinaria
          </p>
          <Campo etiqueta="RUC" requerido ayuda="13 dígitos">
            <Input value={ruc} onChange={(e) => setRuc(e.target.value)} maxLength={13} required />
          </Campo>
          <Campo etiqueta="Razón social" requerido>
            <Input
              value={razonSocial}
              onChange={(e) => setRazonSocial(e.target.value)}
              maxLength={150}
              required
            />
          </Campo>
          <Campo etiqueta="Dirección" requerido>
            <Input
              value={direccion}
              onChange={(e) => setDireccion(e.target.value)}
              maxLength={255}
              required
            />
          </Campo>
        </div>

        <div className="space-y-4 rounded-lg border border-slate-200 p-4">
          <p className="text-xs font-semibold uppercase tracking-wide text-slate-500">
            Usuario administrador
          </p>
          <p className="text-xs text-slate-400">
            Se creará con el rol Administrador Local, dueño de esta veterinaria.
          </p>
          <Campo etiqueta="Usuario" requerido ayuda="Nombre de acceso al sistema (único)">
            <Input
              value={adminUsuario}
              onChange={(e) => setAdminUsuario(e.target.value)}
              maxLength={50}
              required
            />
          </Campo>
          <Campo etiqueta="Nombres" requerido>
            <Input
              value={adminNombres}
              onChange={(e) => setAdminNombres(e.target.value)}
              maxLength={100}
              required
            />
          </Campo>
          <Campo
            etiqueta="Contraseña"
            requerido
            ayuda="Mínimo 6 caracteres. El backend la cifra con BCrypt."
            error={contrasenaCorta ? 'La contraseña debe tener al menos 6 caracteres' : undefined}
          >
            <Input
              type="password"
              value={adminContrasena}
              onChange={(e) => setAdminContrasena(e.target.value)}
              maxLength={255}
              required
            />
          </Campo>
          <Campo
            etiqueta="Confirmar contraseña"
            requerido
            error={noCoincide ? 'Las contraseñas no coinciden' : undefined}
          >
            <Input
              type="password"
              value={confirmarContrasena}
              onChange={(e) => setConfirmarContrasena(e.target.value)}
              maxLength={255}
              required
            />
          </Campo>
        </div>

        {crear.error && <MensajeError error={crear.error} />}

        <div className="flex justify-end gap-2 pt-2">
          <Boton type="button" variante="secundario" onClick={cerrarYLimpiar}>
            Cancelar
          </Boton>
          <Boton
            type="submit"
            cargando={crear.isPending}
            disabled={contrasenaCorta || noCoincide}
          >
            Crear veterinaria
          </Boton>
        </div>
      </form>
    </Modal>
  )
}

function ModalResumenEmpresa({
  idEmpresa,
  onCerrar,
}: {
  idEmpresa: Uuid | null
  onCerrar: () => void
}) {
  const resumen = empresaAdminApi.useResumen(idEmpresa ?? undefined)

  return (
    <Modal
      abierto={!!idEmpresa}
      titulo="Resumen de uso"
      descripcion={resumen.data?.razonSocial}
      onCerrar={onCerrar}
    >
      {resumen.isLoading ? (
        <Cargando />
      ) : resumen.error ? (
        <MensajeError error={resumen.error} />
      ) : resumen.data ? (
        <div className="grid grid-cols-2 gap-3">
          <Metrica etiqueta="Usuarios" valor={resumen.data.totalUsuarios} />
          <Metrica etiqueta="Veterinarios" valor={resumen.data.totalVeterinarios} />
          <Metrica etiqueta="Clientes" valor={resumen.data.totalClientes} />
          <Metrica etiqueta="Mascotas" valor={resumen.data.totalMascotas} />
          <Metrica etiqueta="Citas" valor={resumen.data.totalCitas} />
          <Metrica etiqueta="Historiales" valor={resumen.data.totalHistoriales} />
          <Metrica etiqueta="Productos" valor={resumen.data.totalProductos} />
          <Metrica etiqueta="Productos bajo stock" valor={resumen.data.productosBajoStock} />
          <Metrica etiqueta="Recetas" valor={resumen.data.totalRecetas} />
          <Metrica etiqueta="Facturas" valor={resumen.data.totalFacturas} />
          <Metrica
            etiqueta="Total facturado"
            valor={formatearMoneda(resumen.data.totalFacturado ?? 0)}
          />
          <Metrica etiqueta="Movimientos de inventario" valor={resumen.data.totalMovimientosInventario} />
        </div>
      ) : (
        <div className="flex items-center gap-2 py-8 text-sm text-slate-400">
          <Building2 className="h-4 w-4" />
          Sin datos
        </div>
      )}
    </Modal>
  )
}

function Metrica({ etiqueta, valor }: { etiqueta: string; valor: number | string }) {
  return (
    <div className="rounded-lg bg-slate-50 p-3">
      <p className="text-xs font-medium uppercase tracking-wide text-slate-500">{etiqueta}</p>
      <p className="mt-1 text-lg font-bold text-slate-900">{valor}</p>
    </div>
  )
}
