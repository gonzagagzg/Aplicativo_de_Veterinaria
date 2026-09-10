import { useEffect, useMemo, useState, type FormEvent } from 'react'
import type { ColumnDef } from '@tanstack/react-table'
import { BarChart3, Building2, Eye, EyeOff, Pencil, Plus, Power, PowerOff, Users } from 'lucide-react'
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
import { empresaAdminApi, empresasApi, rolesApi, usuarioAdminApi } from '@/shared/api/recursos'
import type { Empresa, EmpresaConAdmin, Rol, Uuid, Usuario } from '@/shared/types/api'
import { formatearMoneda } from '@/shared/lib/utils'
import { rucEcuatorianoValido } from '@/shared/lib/sri'

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
  const [empresaAEditar, setEmpresaAEditar] = useState<Empresa | null>(null)
  const [empresaUsuarios, setEmpresaUsuarios] = useState<Empresa | null>(null)

  const columnas = useMemo<ColumnDef<Empresa, unknown>[]>(
    () => [
      { accessorKey: 'ruc', header: 'RUC' },
      { accessorKey: 'razonSocial', header: 'Razón social' },
      { accessorKey: 'direccion', header: 'Dirección' },
      {
        accessorKey: 'correo',
        header: 'Correo',
        cell: ({ getValue }) => (getValue() as string | null) || '—',
      },
      {
        accessorKey: 'telefono',
        header: 'Teléfono',
        cell: ({ getValue }) => (getValue() as string | null) || '—',
      },
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
                onClick={() => setEmpresaAEditar(empresa)}
                className="rounded-lg p-1.5 text-slate-400 hover:bg-slate-100 hover:text-brand-600"
                title="Editar veterinaria"
              >
                <Pencil className="h-4 w-4" />
              </button>
              <button
                type="button"
                onClick={() => setEmpresaUsuarios(empresa)}
                className="rounded-lg p-1.5 text-slate-400 hover:bg-slate-100 hover:text-brand-600"
                title="Ver usuarios"
              >
                <Users className="h-4 w-4" />
              </button>
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
      <ModalEditarEmpresa empresa={empresaAEditar} onCerrar={() => setEmpresaAEditar(null)} />
      <ModalUsuariosEmpresa empresa={empresaUsuarios} onCerrar={() => setEmpresaUsuarios(null)} />
    </div>
  )
}

function ModalNuevaEmpresa({ abierto, onCerrar }: { abierto: boolean; onCerrar: () => void }) {
  const crear = empresasApi.useCrear()
  const [ruc, setRuc] = useState('')
  const [razonSocial, setRazonSocial] = useState('')
  const [direccion, setDireccion] = useState('')
  const [correo, setCorreo] = useState('')
  const [telefono, setTelefono] = useState('')
  const [adminUsuario, setAdminUsuario] = useState('')
  const [adminNombres, setAdminNombres] = useState('')
  const [adminContrasena, setAdminContrasena] = useState('')
  const [confirmarContrasena, setConfirmarContrasena] = useState('')
  const [mostrarContrasena, setMostrarContrasena] = useState(false)
  const [mostrarConfirmar, setMostrarConfirmar] = useState(false)

  function cerrarYLimpiar() {
    setRuc('')
    setRazonSocial('')
    setDireccion('')
    setCorreo('')
    setTelefono('')
    setAdminUsuario('')
    setAdminNombres('')
    setAdminContrasena('')
    setConfirmarContrasena('')
    setMostrarContrasena(false)
    setMostrarConfirmar(false)
    crear.reset()
    onCerrar()
  }

  const rucCompleto = ruc.length === 13
  const rucValido = rucCompleto && rucEcuatorianoValido(ruc)
  const errorRuc = rucCompleto && !rucValido ? 'El RUC ecuatoriano no es válido' : undefined

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
      correo: correo || undefined,
      telefono: telefono || undefined,
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
          <Campo etiqueta="RUC" requerido ayuda="13 dígitos, RUC ecuatoriano (validación SRI)" error={errorRuc}>
            <Input
              value={ruc}
              onChange={(e) => setRuc(e.target.value.replace(/\D/g, ''))}
              maxLength={13}
              inputMode="numeric"
              required
            />
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
          <Campo etiqueta="Correo">
            <Input
              type="email"
              value={correo}
              onChange={(e) => setCorreo(e.target.value)}
              maxLength={100}
              placeholder="contacto@veterinaria.com"
            />
          </Campo>
          <Campo etiqueta="Teléfono">
            <Input
              value={telefono}
              onChange={(e) => setTelefono(e.target.value)}
              maxLength={15}
              inputMode="tel"
              placeholder="0991234567"
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
            <div className="relative">
              <Input
                type={mostrarContrasena ? 'text' : 'password'}
                value={adminContrasena}
                onChange={(e) => setAdminContrasena(e.target.value)}
                maxLength={255}
                required
                className="pr-10"
              />
              <button
                type="button"
                onClick={() => setMostrarContrasena((v) => !v)}
                className="absolute inset-y-0 right-0 flex items-center px-3 text-slate-400 hover:text-slate-600"
                tabIndex={-1}
                aria-label={mostrarContrasena ? 'Ocultar contraseña' : 'Mostrar contraseña'}
              >
                {mostrarContrasena ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
              </button>
            </div>
          </Campo>
          <Campo
            etiqueta="Confirmar contraseña"
            requerido
            error={noCoincide ? 'Las contraseñas no coinciden' : undefined}
          >
            <div className="relative">
              <Input
                type={mostrarConfirmar ? 'text' : 'password'}
                value={confirmarContrasena}
                onChange={(e) => setConfirmarContrasena(e.target.value)}
                maxLength={255}
                required
                className="pr-10"
              />
              <button
                type="button"
                onClick={() => setMostrarConfirmar((v) => !v)}
                className="absolute inset-y-0 right-0 flex items-center px-3 text-slate-400 hover:text-slate-600"
                tabIndex={-1}
                aria-label={mostrarConfirmar ? 'Ocultar contraseña' : 'Mostrar contraseña'}
              >
                {mostrarConfirmar ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
              </button>
            </div>
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
            disabled={contrasenaCorta || noCoincide || (rucCompleto && !rucValido)}
          >
            Crear veterinaria
          </Boton>
        </div>
      </form>
    </Modal>
  )
}

function ModalEditarEmpresa({
  empresa,
  onCerrar,
}: {
  empresa: Empresa | null
  onCerrar: () => void
}) {
  const actualizar = empresasApi.useActualizar()
  const [ruc, setRuc] = useState('')
  const [razonSocial, setRazonSocial] = useState('')
  const [direccion, setDireccion] = useState('')
  const [correo, setCorreo] = useState('')
  const [telefono, setTelefono] = useState('')

  useEffect(() => {
    if (empresa) {
      setRuc(empresa.ruc)
      setRazonSocial(empresa.razonSocial)
      setDireccion(empresa.direccion)
      setCorreo(empresa.correo ?? '')
      setTelefono(empresa.telefono ?? '')
    }
  }, [empresa])

  function cerrarYLimpiar() {
    actualizar.reset()
    onCerrar()
  }

  const rucCompleto = ruc.length === 13
  const rucValido = rucCompleto && rucEcuatorianoValido(ruc)
  const errorRuc = rucCompleto && !rucValido ? 'El RUC ecuatoriano no es válido' : undefined

  function enviar(e: FormEvent) {
    e.preventDefault()
    if (!empresa) return
    actualizar.mutate(
      {
        id: empresa.idEmpresa,
        datos: {
          ruc,
          razonSocial,
          direccion,
          correo: correo || undefined,
          telefono: telefono || undefined,
        },
      },
      { onSuccess: cerrarYLimpiar },
    )
  }

  return (
    <Modal abierto={!!empresa} titulo="Editar veterinaria" onCerrar={cerrarYLimpiar}>
      <form className="space-y-4" onSubmit={enviar}>
        <Campo etiqueta="RUC" requerido ayuda="13 dígitos, RUC ecuatoriano (validación SRI)" error={errorRuc}>
          <Input
            value={ruc}
            onChange={(e) => setRuc(e.target.value.replace(/\D/g, ''))}
            maxLength={13}
            inputMode="numeric"
            required
          />
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
        <Campo etiqueta="Correo">
          <Input
            type="email"
            value={correo}
            onChange={(e) => setCorreo(e.target.value)}
            maxLength={100}
            placeholder="contacto@veterinaria.com"
          />
        </Campo>
        <Campo etiqueta="Teléfono">
          <Input
            value={telefono}
            onChange={(e) => setTelefono(e.target.value)}
            maxLength={15}
            inputMode="tel"
            placeholder="0991234567"
          />
        </Campo>

        {actualizar.error && <MensajeError error={actualizar.error} />}

        <div className="flex justify-end gap-2 pt-2">
          <Boton type="button" variante="secundario" onClick={cerrarYLimpiar}>
            Cancelar
          </Boton>
          <Boton type="submit" cargando={actualizar.isPending} disabled={rucCompleto && !rucValido}>
            Guardar cambios
          </Boton>
        </div>
      </form>
    </Modal>
  )
}

function ModalUsuariosEmpresa({
  empresa,
  onCerrar,
}: {
  empresa: Empresa | null
  onCerrar: () => void
}) {
  const usuarios = empresaAdminApi.useUsuariosDeEmpresa(empresa?.idEmpresa)
  const roles = rolesApi.useLista()
  const desbloquear = usuarioAdminApi.useDesbloquear()
  const [usuarioBloqueando, setUsuarioBloqueando] = useState<Usuario | null>(null)

  const nombreRol = (idRol: number) =>
    (roles.data ?? []).find((r: Rol) => r.idRol === idRol)?.nombre ?? `Rol #${idRol}`

  return (
    <>
      <Modal
        abierto={!!empresa}
        titulo="Usuarios de la veterinaria"
        descripcion={empresa?.razonSocial}
        onCerrar={onCerrar}
        ancho="max-w-4xl"
      >
        {usuarios.isLoading ? (
          <Cargando />
        ) : usuarios.error ? (
          <MensajeError error={usuarios.error} />
        ) : (usuarios.data ?? []).length === 0 ? (
          <div className="flex items-center gap-2 py-8 text-sm text-slate-400">
            <Users className="h-4 w-4" />
            Sin usuarios registrados
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="bg-slate-50 text-left">
                <tr>
                  <th className="whitespace-nowrap px-3 py-2 text-xs font-semibold uppercase tracking-wide text-slate-500">
                    Usuario
                  </th>
                  <th className="whitespace-nowrap px-3 py-2 text-xs font-semibold uppercase tracking-wide text-slate-500">
                    Nombres
                  </th>
                  <th className="whitespace-nowrap px-3 py-2 text-xs font-semibold uppercase tracking-wide text-slate-500">
                    Rol
                  </th>
                  <th className="whitespace-nowrap px-3 py-2 text-xs font-semibold uppercase tracking-wide text-slate-500">
                    Estado
                  </th>
                  <th className="whitespace-nowrap px-3 py-2 text-xs font-semibold uppercase tracking-wide text-slate-500">
                    Acciones
                  </th>
                  <th className="whitespace-nowrap px-3 py-2 text-xs font-semibold uppercase tracking-wide text-slate-500">
                    Notificación
                  </th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {(usuarios.data ?? []).map((u) => (
                  <tr
                    key={u.idUsuario}
                    className={
                      u.notificacion === 'pago'
                        ? 'bg-red-50'
                        : u.notificacion === 'tecnico'
                          ? 'bg-amber-50'
                          : ''
                    }
                  >
                    <td className="px-3 py-2 text-slate-700">{u.usuario}</td>
                    <td className="px-3 py-2 text-slate-700">{u.nombres}</td>
                    <td className="px-3 py-2 text-slate-700">{nombreRol(u.idRol)}</td>
                    <td className="px-3 py-2">
                      {EstadoUsuario({ usuario: u })}
                    </td>
                    <td className="flex justify-center px-3 py-2">
                      {u.activo ? (
                        <button
                          type="button"
                          onClick={() => setUsuarioBloqueando(u)}
                          className="inline-flex items-center justify-center rounded-lg p-1.5 text-slate-400 hover:bg-red-50 hover:text-red-600"
                          title="Bloquear usuario"
                        >
                          <PowerOff className="h-4 w-4" />
                        </button>
                      ) : (
                        <button
                          type="button"
                          disabled={desbloquear.isPending}
                          onClick={() => desbloquear.mutate(u.idUsuario)}
                          className="inline-flex items-center justify-center rounded-lg p-1.5 text-slate-400 hover:bg-emerald-50 hover:text-emerald-600 disabled:opacity-40"
                          title="Desbloquear (pone activo=true y tipobloqueo=null)"
                        >
                          <Power className="h-4 w-4" />
                        </button>
                      )}
                    </td>
                    <td className="px-3 py-2 text-slate-700">
                      {NotificacionUsuario({ usuario: u })}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Modal>

      <ModalBloqueoUsuario usuario={usuarioBloqueando} onCerrar={() => setUsuarioBloqueando(null)} />
    </>
  )
}

function NotificacionUsuario({ usuario }: { usuario: Usuario }) {
  if (usuario.notificacion === 'pago') return 'Pago'
  if (usuario.notificacion === 'tecnico') return 'Técnico'
  return 'Ninguna'
}

function EstadoUsuario({ usuario }: { usuario: Usuario }) {
  if (usuario.activo) return <Badge tono="exito">Activo</Badge>
  if (usuario.tipobloqueo === 'pago') return <Badge tono="peligro">Bloqueado Pago</Badge>
  if (usuario.tipobloqueo === 'tecnico') return <Badge tono="alerta">Bloqueado Técnico</Badge>
  return <Badge tono="peligro">Inactivo</Badge>
}

function ModalBloqueoUsuario({
  usuario,
  onCerrar,
}: {
  usuario: Usuario | null
  onCerrar: () => void
}) {
  const bloquear = usuarioAdminApi.useBloquear()
  const [tipo, setTipo] = useState<'pago' | 'tecnico'>('pago')

  function cerrarYLimpiar() {
    setTipo('pago')
    bloquear.reset()
    onCerrar()
  }

  return (
    <Modal
      abierto={!!usuario}
      titulo="Tipo de desactivación"
      descripcion={usuario ? `Se bloqueará el usuario "${usuario.usuario}"` : undefined}
      onCerrar={cerrarYLimpiar}
    >
      <div className="space-y-3">
        <p className="text-sm text-slate-600">Seleccione el motivo del bloqueo:</p>
        {(
          [
            { valor: 'pago', etiqueta: 'Por falta de pago', descripcion: 'El usuario no ha regularizado el pago' },
            { valor: 'tecnico', etiqueta: 'Técnico', descripcion: 'Bloqueo por motivos técnicos del sistema' },
          ] as const
        ).map(({ valor, etiqueta, descripcion }) => (
          <label
            key={valor}
            className={`flex cursor-pointer items-start gap-3 rounded-lg border p-3 transition-colors ${
              tipo === valor
                ? 'border-brand-600 bg-brand-50'
                : 'border-slate-200 hover:border-slate-300'
            }`}
          >
            <input
              type="radio"
              name="tipoBloqueo"
              value={valor}
              checked={tipo === valor}
              onChange={() => setTipo(valor)}
              className="mt-0.5 accent-brand-600"
            />
            <span>
              <span className="block text-sm font-semibold text-slate-800">{etiqueta}</span>
              <span className="block text-xs text-slate-500">{descripcion}</span>
            </span>
          </label>
        ))}

        {bloquear.error && (
          <div className="mt-3">
            <MensajeError error={bloquear.error} />
          </div>
        )}

        <div className="flex justify-end gap-2 pt-2">
          <Boton type="button" variante="secundario" onClick={cerrarYLimpiar}>
            Cancelar
          </Boton>
          <Boton
            type="button"
            variante="peligro"
            cargando={bloquear.isPending}
            disabled={!usuario}
            onClick={() =>
              usuario &&
              bloquear.mutate({ idUsuario: usuario.idUsuario, tipoBloqueo: tipo }, {
                onSuccess: cerrarYLimpiar,
              })
            }
          >
            Bloquear usuario
          </Boton>
        </div>
      </div>
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
