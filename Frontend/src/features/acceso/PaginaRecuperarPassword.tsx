import { useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { HeartPulse, MailCheck } from 'lucide-react'
import { useRecuperarPassword } from '@/shared/api/auth'
import { Boton, Campo, Input, MensajeError } from '@/shared/components/ui'

/**
 * Solicita el envío del correo de recuperación (POST /api/auth/recuperar-password).
 *
 * El backend siempre responde igual exista o no el correo (para no filtrar
 * qué correos están registrados), así que aquí solo se muestra un mensaje
 * de confirmación tras el envío.
 */
export function PaginaRecuperarPassword() {
  const recuperar = useRecuperarPassword()
  const [correo, setCorreo] = useState('')

  function enviar(e: FormEvent) {
    e.preventDefault()
    if (!correo.trim()) return
    recuperar.mutate({ correo: correo.trim() })
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-brand-50 p-8">
      <div className="w-full max-w-sm">
        <Link to="/" className="mb-8 flex items-center gap-3">
          <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-brand-600 text-white">
            <HeartPulse className="h-5 w-5" />
          </div>
          <span className="text-sm font-semibold text-slate-900">Veterinaria ITQ</span>
        </Link>

        <h2 className="text-2xl font-bold tracking-tight text-slate-900">Recuperar contraseña</h2>
        <p className="mt-1 text-sm text-slate-500">
          Ingresa el correo asociado a tu cuenta y te enviaremos un enlace para restablecerla.
        </p>

        {recuperar.isSuccess ? (
          <div className="mt-6 flex gap-2 rounded-lg border border-emerald-200 bg-emerald-50 p-4 text-sm text-emerald-700">
            <MailCheck className="mt-0.5 h-4 w-4 shrink-0" />
            <p>
              Si el correo está registrado, recibirás un mensaje con las instrucciones para
              restablecer tu contraseña.
            </p>
          </div>
        ) : (
          <form className="mt-6 space-y-4" onSubmit={enviar}>
            <Campo etiqueta="Correo" requerido>
              <Input
                type="email"
                autoFocus
                autoComplete="email"
                value={correo}
                onChange={(e) => setCorreo(e.target.value)}
                placeholder="correo@veterinaria.com"
              />
            </Campo>

            {recuperar.error && <MensajeError error={recuperar.error} />}

            <Boton
              type="submit"
              className="w-full"
              disabled={!correo.trim() || recuperar.isPending}
              cargando={recuperar.isPending}
            >
              Enviar enlace de recuperación
            </Boton>
          </form>
        )}

        <Link to="/acceso" className="mt-6 block text-center text-sm text-brand-600 hover:underline">
          Volver a iniciar sesión
        </Link>
      </div>
    </div>
  )
}
