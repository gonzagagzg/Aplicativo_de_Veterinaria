import { useState, type FormEvent } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { HeartPulse, LockKeyhole } from 'lucide-react'
import { useLogin } from '@/shared/api/auth'
import { ApiError } from '@/shared/api/client'
import { Boton, Campo, Input } from '@/shared/components/ui'
import { useSesion } from '@/shared/session/sesion'

/**
 * Login real contra POST /api/auth/login.
 *
 * El backend valida usuario/contraseña, empresa y usuario activos, y
 * devuelve un JWT con idEmpresa/idRol/rol embebidos — ya no se elige la
 * empresa a mano: el token la trae resuelta (ver AuthServlet/AuthFilter).
 */
export function PaginaAcceso() {
  const navigate = useNavigate()
  const { token, establecer } = useSesion()
  const login = useLogin()

  const [usuario, setUsuario] = useState('')
  const [clave, setClave] = useState('')

  if (token) return <Navigate to="/app" replace />

  function enviar(e: FormEvent) {
    e.preventDefault()
    if (!usuario.trim() || !clave) return

    login.mutate(
      { usuario: usuario.trim(), clave },
      {
        onSuccess: (r) => {
          establecer({
            token: r.token,
            idEmpresa: r.idEmpresa,
            idUsuario: r.idUsuario,
            idRol: r.idRol,
            rol: r.rol,
            nombreUsuario: r.nombres,
          })
          navigate('/app', { replace: true })
        },
      },
    )
  }

  const error = login.error
  const mensajeError =
    error instanceof ApiError
      ? error.message
      : error
        ? 'No se pudo iniciar sesión. Intente nuevamente.'
        : null

  return (
    <div className="flex min-h-screen">
      {/* Panel izquierdo: identidad clínica */}
      <div className="hidden flex-col justify-between bg-brand-950 p-12 lg:flex lg:w-5/12">
        <Link to="/" className="flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-gold-500 text-brand-950">
            <HeartPulse className="h-6 w-6" />
          </div>
          <span className="text-base font-semibold text-white">Veterinaria ITQ</span>
        </Link>

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
          <Link to="/" className="mb-8 flex items-center gap-3 lg:hidden">
            <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-brand-600 text-white">
              <HeartPulse className="h-5 w-5" />
            </div>
            <span className="text-sm font-semibold text-slate-900">Veterinaria ITQ</span>
          </Link>

          <h2 className="text-2xl font-bold tracking-tight text-slate-900">Iniciar sesión</h2>
          <p className="mt-1 text-sm text-slate-500">Ingrese sus credenciales de acceso.</p>

          <form className="mt-6 space-y-4" onSubmit={enviar}>
            <Campo etiqueta="Usuario" requerido>
              <Input
                autoFocus
                autoComplete="username"
                value={usuario}
                onChange={(e) => setUsuario(e.target.value)}
                placeholder="usuario"
              />
            </Campo>

            <Campo etiqueta="Contraseña" requerido>
              <Input
                type="password"
                autoComplete="current-password"
                value={clave}
                onChange={(e) => setClave(e.target.value)}
                placeholder="••••••••"
              />
            </Campo>

            {mensajeError && (
              <div className="flex gap-2 rounded-lg border border-red-200 bg-red-50 p-3 text-xs text-red-700">
                <LockKeyhole className="mt-0.5 h-3.5 w-3.5 shrink-0" />
                <p>{mensajeError}</p>
              </div>
            )}

            <Boton
              type="submit"
              className="mt-2 w-full"
              disabled={!usuario.trim() || !clave || login.isPending}
            >
              {login.isPending ? 'Ingresando…' : 'Entrar al sistema'}
            </Boton>
          </form>
        </div>
      </div>
    </div>
  )
}
