import { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { Boton, Campo, Input, MensajeError, Select, TextArea } from '../ui'
import { aInputDateTime, desdeInputDateTime } from '@/shared/lib/utils'
import type { CampoFormulario } from './tipos'

/**
 * Formulario generado a partir de la configuración de campos.
 *
 * Se apoya en la validación nativa de React Hook Form (required/min/max/
 * maxLength) en lugar de un esquema Zod por entidad: los servlets ya validan
 * en servidor y devuelven mensajes en español, así que aquí solo se cubre el
 * feedback inmediato y la conversión de tipos correcta (number vs string).
 */
export function FormularioCrud<T extends object>({
  campos,
  valoresIniciales,
  edicion,
  enviando,
  error,
  onEnviar,
  onCancelar,
}: {
  campos: CampoFormulario<T>[]
  valoresIniciales?: Partial<T>
  edicion: boolean
  enviando: boolean
  error: unknown
  onEnviar: (datos: Partial<T>) => void
  onCancelar: () => void
}) {
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<Record<string, unknown>>()

  // Al abrir el modal (crear o editar) se rehidrata el formulario.
  useEffect(() => {
    const valores: Record<string, unknown> = {}
    for (const campo of campos) {
      const bruto = valoresIniciales?.[campo.nombre]
      valores[campo.nombre] =
        campo.tipo === 'fechaHora' ? aInputDateTime(bruto as string) : (bruto ?? '')
    }
    reset(valores)
  }, [valoresIniciales, campos, reset])

  function alEnviar(valores: Record<string, unknown>) {
    const payload: Record<string, unknown> = {}

    for (const campo of campos) {
      if (edicion && campo.soloCreacion) continue

      const valor = valores[campo.nombre]

      // Campo opcional vacío -> null explícito (Gson lo acepta y la BD también).
      if (valor === '' || valor === undefined) {
        payload[campo.nombre] = campo.requerido ? '' : null
        continue
      }

      switch (campo.tipo) {
        case 'numero':
          payload[campo.nombre] = Number.parseInt(String(valor), 10)
          break
        case 'decimal':
          payload[campo.nombre] = Number.parseFloat(String(valor))
          break
        case 'select':
          payload[campo.nombre] = campo.opcionesNumericas
            ? Number.parseInt(String(valor), 10)
            : valor
          break
        case 'booleano':
          payload[campo.nombre] = valor === true || valor === 'true'
          break
        case 'fechaHora':
          payload[campo.nombre] = desdeInputDateTime(String(valor))
          break
        default:
          payload[campo.nombre] = valor
      }
    }

    onEnviar(payload as Partial<T>)
  }

  return (
    <form onSubmit={handleSubmit(alEnviar)} className="space-y-4">
      {Boolean(error) && <MensajeError error={error} />}

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        {campos.map((campo) => {
          const bloqueado = edicion && campo.soloCreacion
          const mensajeError = errors[campo.nombre]?.message as string | undefined

          const reglas = {
            required: campo.requerido && !bloqueado ? 'Este campo es obligatorio' : false,
            maxLength: campo.maxLength
              ? { value: campo.maxLength, message: `Máximo ${campo.maxLength} caracteres` }
              : undefined,
            min:
              campo.min !== undefined
                ? { value: campo.min, message: `El valor mínimo es ${campo.min}` }
                : undefined,
            max:
              campo.max !== undefined
                ? { value: campo.max, message: `El valor máximo es ${campo.max}` }
                : undefined,
          }

          return (
            <div
              key={campo.nombre}
              className={campo.anchoCompleto || campo.tipo === 'textarea' ? 'sm:col-span-2' : ''}
            >
              <Campo
                etiqueta={campo.etiqueta}
                requerido={campo.requerido && !bloqueado}
                error={mensajeError}
                ayuda={bloqueado ? 'No editable después de la creación' : campo.ayuda}
              >
                {campo.tipo === 'select' ? (
                  <Select disabled={bloqueado} {...register(campo.nombre, reglas)}>
                    <option value="">— Seleccionar —</option>
                    {campo.opciones?.map((o) => (
                      <option key={o.valor} value={o.valor}>
                        {o.etiqueta}
                      </option>
                    ))}
                  </Select>
                ) : campo.tipo === 'textarea' ? (
                  <TextArea
                    disabled={bloqueado}
                    placeholder={campo.placeholder}
                    {...register(campo.nombre, reglas)}
                  />
                ) : campo.tipo === 'booleano' ? (
                  <Select disabled={bloqueado} {...register(campo.nombre)}>
                    <option value="true">Sí</option>
                    <option value="false">No</option>
                  </Select>
                ) : (
                  <Input
                    type={
                      campo.tipo === 'fecha'
                        ? 'date'
                        : campo.tipo === 'fechaHora'
                          ? 'datetime-local'
                          : campo.tipo === 'numero' || campo.tipo === 'decimal'
                            ? 'number'
                            : 'text'
                    }
                    step={campo.tipo === 'decimal' ? '0.01' : undefined}
                    disabled={bloqueado}
                    placeholder={campo.placeholder}
                    {...register(campo.nombre, reglas)}
                  />
                )}
              </Campo>
            </div>
          )
        })}
      </div>

      <div className="flex justify-end gap-2 border-t border-slate-200 pt-4">
        <Boton type="button" variante="secundario" onClick={onCancelar}>
          Cancelar
        </Boton>
        <Boton type="submit" cargando={enviando}>
          {edicion ? 'Guardar cambios' : 'Crear'}
        </Boton>
      </div>
    </form>
  )
}
