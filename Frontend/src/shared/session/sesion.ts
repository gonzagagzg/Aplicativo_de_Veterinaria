import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import type { Uuid } from '@/shared/types/api'

/**
 * Sesión local.
 *
 * ADVERTENCIA DELIBERADA: el backend actual no expone /api/auth/login ni emite
 * ningún token; tampoco resuelve `id_empresa` en el servidor. Por eso esta
 * "sesión" es únicamente un contexto de trabajo del lado del cliente:
 * selecciona la empresa activa (para filtrar y para prellenar `idEmpresa` en
 * los formularios) y el usuario que opera.
 *
 * NO es un control de seguridad: cualquiera puede cambiarlo desde el navegador.
 * Cuando el backend incorpore JWT + AuthFilter, este store se reemplaza por el
 * contenido del token y `idEmpresa` deja de enviarse desde el cliente.
 * El resto de la aplicación no necesita cambiar: todo lee `useSesion()`.
 */

interface EstadoSesion {
  idEmpresa: Uuid | null
  idUsuario: Uuid | null
  nombreEmpresa: string | null
  nombreUsuario: string | null
  establecer: (datos: {
    idEmpresa: Uuid
    idUsuario: Uuid
    nombreEmpresa: string
    nombreUsuario: string
  }) => void
  limpiar: () => void
}

export const useSesion = create<EstadoSesion>()(
  persist(
    (set) => ({
      idEmpresa: null,
      idUsuario: null,
      nombreEmpresa: null,
      nombreUsuario: null,
      establecer: (datos) => set(datos),
      limpiar: () =>
        set({
          idEmpresa: null,
          idUsuario: null,
          nombreEmpresa: null,
          nombreUsuario: null,
        }),
    }),
    { name: 'vet-sesion' },
  ),
)

/** Empresa activa garantizada; usar solo dentro de rutas protegidas. */
export function useEmpresaActiva(): Uuid {
  const idEmpresa = useSesion((s) => s.idEmpresa)
  if (!idEmpresa) throw new Error('No hay empresa activa en la sesión')
  return idEmpresa
}

/**
 * Filtra por empresa activa en el cliente.
 *
 * El backend devuelve todos los registros sin filtrar por tenant, así que el
 * recorte ocurre aquí. Es una limitación conocida — ver nota superior.
 */
export function filtrarPorEmpresa<T extends { idEmpresa?: Uuid }>(
  registros: T[] | undefined,
  idEmpresa: Uuid | null,
): T[] {
  if (!registros) return []
  if (!idEmpresa) return registros
  return registros.filter((r) => !r.idEmpresa || r.idEmpresa === idEmpresa)
}
