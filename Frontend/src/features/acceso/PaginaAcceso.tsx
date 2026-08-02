import { useEffect, useMemo, useState } from 'react'
import { Navigate, useNavigate } from 'react-router-dom'
import { Dog, TriangleAlert } from 'lucide-react'
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

  if (idUsuario) return <Navigate to="/" replace />

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
    navigate('/', { replace: true })
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-100 p-4">
      <div className="w-full max-w-md rounded-2xl border border-slate-200 bg-white p-7 shadow-sm">
        <div className="mb-6 flex items-center gap-3">
          <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-brand-600 text-white">
            <Dog className="h-6 w-6" />
          </div>
          <div>
            <h1 className="text-lg font-semibold text-slate-900">Veterinaria ITQ</h1>
            <p className="text-sm text-slate-500">Seleccione su contexto de trabajo</p>
          </div>
        </div>

        <div className="mb-5 flex gap-2.5 rounded-lg border border-amber-200 bg-amber-50 p-3 text-xs text-amber-900">
          <TriangleAlert className="h-4 w-4 shrink-0 text-amber-600" />
          <p>
            El backend aún no expone un endpoint de autenticación, por lo que esta pantalla{' '}
            <strong>no verifica contraseñas</strong>. Solo define la empresa y el usuario con los
            que trabajará la aplicación.
          </p>
        </div>

        {cargando ? (
          <Cargando texto="Consultando empresas y usuarios…" />
        ) : error ? (
          <MensajeError error={error} />
        ) : (
          <div className="space-y-4">
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

            <Boton className="w-full" disabled={!empresaSel || !usuarioSel} onClick={entrar}>
              Entrar
            </Boton>
          </div>
        )}
      </div>
    </div>
  )
}
