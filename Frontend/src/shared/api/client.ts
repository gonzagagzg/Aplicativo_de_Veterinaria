import type { ApiResponse } from '@/shared/types/api'

/**
 * Cliente HTTP único contra la API de servlets.
 *
 * Responsabilidades:
 *  - Desempaquetar el envoltorio {exito, mensaje, datos} para que las capas
 *    superiores trabajen directamente con los datos.
 *  - Normalizar los errores del backend (que llegan como
 *    {exito:false, mensaje:"..."} con status 400/404/409/500) en una
 *    excepción `ApiError` con mensaje legible.
 *  - Tratar el 204 de DELETE como respuesta vacía válida.
 */

const BASE_URL = import.meta.env.VITE_API_URL ?? ''

export class ApiError extends Error {
  constructor(
    readonly status: number,
    message: string,
  ) {
    super(message)
    this.name = 'ApiError'
  }

  /** El backend usa 409 para violaciones de FK / UNIQUE (ver SqlErrorUtil). */
  get esConflicto() {
    return this.status === 409
  }

  get esNoEncontrado() {
    return this.status === 404
  }
}

type QueryParams = Record<string, string | number | boolean | null | undefined>

function construirUrl(path: string, params?: QueryParams) {
  const url = `${BASE_URL}${path}`
  if (!params) return url

  const qs = new URLSearchParams()
  for (const [clave, valor] of Object.entries(params)) {
    if (valor !== undefined && valor !== null && valor !== '') {
      qs.append(clave, String(valor))
    }
  }
  const cadena = qs.toString()
  return cadena ? `${url}?${cadena}` : url
}

async function solicitar<T>(
  method: string,
  path: string,
  opciones: { body?: unknown; params?: QueryParams } = {},
): Promise<T> {
  let respuesta: Response

  try {
    respuesta = await fetch(construirUrl(path, opciones.params), {
      method,
      headers: opciones.body !== undefined ? { 'Content-Type': 'application/json' } : undefined,
      body: opciones.body !== undefined ? JSON.stringify(opciones.body) : undefined,
    })
  } catch {
    throw new ApiError(0, 'No se pudo contactar al servidor. Verifica que Tomcat esté ejecutándose.')
  }

  // DELETE exitoso devuelve 204 sin cuerpo.
  if (respuesta.status === 204) return undefined as T

  const texto = await respuesta.text()
  let cuerpo: ApiResponse<T> | null = null

  if (texto) {
    try {
      cuerpo = JSON.parse(texto) as ApiResponse<T>
    } catch {
      throw new ApiError(respuesta.status, texto.slice(0, 200))
    }
  }

  if (!respuesta.ok || (cuerpo && cuerpo.exito === false)) {
    throw new ApiError(respuesta.status, cuerpo?.mensaje ?? `Error ${respuesta.status}`)
  }

  return (cuerpo?.datos ?? (undefined as T)) as T
}

export const api = {
  get: <T>(path: string, params?: QueryParams) => solicitar<T>('GET', path, { params }),
  post: <T>(path: string, body: unknown) => solicitar<T>('POST', path, { body }),
  put: <T>(path: string, body: unknown, params?: QueryParams) =>
    solicitar<T>('PUT', path, { body, params }),
  delete: (path: string, params?: QueryParams) => solicitar<void>('DELETE', path, { params }),
}
