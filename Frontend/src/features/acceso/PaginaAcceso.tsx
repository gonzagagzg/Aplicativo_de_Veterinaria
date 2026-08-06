import { useEffect, useMemo, useState } from 'react'
import { Navigate, useNavigate } from 'react-router-dom'
import { HeartPulse, TriangleAlert } from 'lucide-react'
import { empresasApi, usuariosApi } from '@/shared/api/recursos'
import { Boton, Campo, Cargando, MensajeError, Select } from '@/shared/components/ui'
import { useSesion } from '@/shared/session/sesion'

/**
 * Selector de contexto de trabajo.
 *
 * El backend no expone /api/auth/login, así que no hay autenticación real:
 * se elige la empresa y el usuario con los que se va a operar. La advertencia
 * en pantalla es intencional para que nadie confunda esto con un login.
 */
export function PaginaAcceso() {
  const navigate = useNavigate()
  const { idUsuario, establecer } = useSesion()

  const empresas = empresasApi.useLista()
  const usuarios = usuariosApi.useLista()

  const [empresaSel, setEmpresaSel] = useState('')
  const [usuarioSel, setUsuarioSel] = useState('')

  // Con una sola empresa activa, la preselecciona: evita un clic inútil.
  useEffect(() => {
    const activas = (empresas.data ?? []).filter((e) => e.activo)
    if (!empresaSel && activas.length === 1) setEmpresaSel(activas[0].idEmpresa)
  }, [empresas.data, empresaSel])

  const usuariosDeEmpresa = useMemo(
    () => (usuarios.data ?? []).filter((u) => u.idEmpresa === empresaSel && u.activo),
    [usuarios.data, empresaSel],
  )

  if (idUsuario) return <Navigate to="/app" replace />

  const cargando = empresas.isLoading || usuarios.isLoading
  const error = empresas.error ?? usuarios.error

  function entrar() {
    const empresa = empresas.data?.find((e) => e.idEmpresa === empresaSel)
    const usuario = usuarios.data?.find((u) => u.idUsuario === usuarioSel)
    if (!empresa || !usuario) return

    establecer({
      idEmpresa: empresa.idEmpresa,
      idUsuario: usuario.idUsuario,
      nombreEmpresa: empresa.razonSocial,
      nombreUsuario: usuario.nombres,
    })
    navigate('/app', { replace: true })
  }

  return (
    <div className="flex min-h-screen">
      {/* Panel izquierdo: identidad clínica */}
      <div className="hidden flex-col justify-between bg-brand-950 p-12 lg:flex lg:w-5/12">
        <div className="flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-gold-500 text-brand-950">
            <HeartPulse className="h-6 w-6" />
          </div>
          <span className="text-base font-semibold text-white">Veterinaria ITQ</span>
        </div>

        <div>
          <p className="text-4xl font-bold leading-snug text-white">
            Gestión clínica<br />
            <span className="text-gold-400">de precisión.</span>
          </p>
          <p className="mt-4 text-base leading-relaxed text-brand-300">
            Agenda, historiales, vacunación e inventario en un solo sistema diseñado para el equipo veterinario.
          </p>
        </div>

        <div className="flex gap-6 text-xs text-brand-500">
          <span>Clínica · Comercial · Administración</span>
        </div>
      </div>

      {/* Panel derecho: formulario */}
      <div className="flex flex-1 flex-col items-center justify-center bg-brand-50 p-8">
        <div className="w-full max-w-sm">
          {/* Logo móvil */}
          <div className="mb-8 flex items-center gap-3 lg:hidden">
            <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-brand-600 text-white">
              <HeartPulse className="h-5 w-5" />
            </div>
            <span className="text-sm font-semibold text-slate-900">Veterinaria ITQ</span>
          </div>

          <h2 className="text-2xl font-bold tracking-tight text-slate-900">Iniciar sesión</h2>
          <p className="mt-1 text-sm text-slate-500">Seleccione su empresa y usuario de trabajo.</p>

          <div className="mt-6 flex gap-2.5 rounded-lg border border-amber-200 bg-amber-50 p-3 text-xs text-amber-800">
            <TriangleAlert className="mt-0.5 h-3.5 w-3.5 shrink-0 text-amber-600" />
            <p>Sin verificación de contraseña — solo selección de contexto de trabajo.</p>
          </div>

          {cargando ? (
            <Cargando texto="Consultando empresas y usuarios…" />
          ) : error ? (
            <MensajeError error={error} />
          ) : (
            <div className="mt-6 space-y-4">
              <Campo etiqueta="Empresa" requerido>
                <Select
                  value={empresaSel}
                  onChange={(e) => {
                    setEmpresaSel(e.target.value)
                    setUsuarioSel('')
                  }}
                >
                  <option value="">— Seleccionar —</option>
                  {(empresas.data ?? [])
                    .filter((e) => e.activo)
                    .map((e) => (
                      <option key={e.idEmpresa} value={e.idEmpresa}>
                        {e.razonSocial} · {e.ruc}
                      </option>
                    ))}
                </Select>
              </Campo>

              <Campo
                etiqueta="Usuario"
                requerido
                ayuda={
                  empresaSel && usuariosDeEmpresa.length === 0
                    ? 'Esta empresa no tiene usuarios activos'
                    : undefined
                }
              >
                <Select
                  value={usuarioSel}
                  disabled={!empresaSel}
                  onChange={(e) => setUsuarioSel(e.target.value)}
                >
                  <option value="">— Seleccionar —</option>
                  {usuariosDeEmpresa.map((u) => (
                    <option key={u.idUsuario} value={u.idUsuario}>
                      {u.nombres} ({u.usuario})
                    </option>
                  ))}
                </Select>
              </Campo>

              <Boton className="mt-2 w-full" disabled={!empresaSel || !usuarioSel} onClick={entrar}>
                Entrar al sistema
              </Boton>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
