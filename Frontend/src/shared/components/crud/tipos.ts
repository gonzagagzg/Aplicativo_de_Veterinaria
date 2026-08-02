import type { ColumnDef } from '@tanstack/react-table'
import type { CrudHooks } from '@/shared/api/crud'

export type TipoCampo =
  | 'texto'
  | 'numero'
  | 'decimal'
  | 'fecha'
  | 'fechaHora'
  | 'textarea'
  | 'select'
  | 'booleano'

export interface OpcionSelect {
  valor: string | number
  etiqueta: string
}

export interface CampoFormulario<T> {
  nombre: keyof T & string
  etiqueta: string
  tipo: TipoCampo
  requerido?: boolean
  /** Solo se puede definir al crear; se bloquea al editar (FKs estructurales). */
  soloCreacion?: boolean
  /** Opciones para `tipo: 'select'`. Las llaves numéricas se convierten a number. */
  opciones?: OpcionSelect[]
  /** El select devuelve number en vez de string (llaves SERIAL). */
  opcionesNumericas?: boolean
  placeholder?: string
  ayuda?: string
  maxLength?: number
  min?: number
  max?: number
  /** Ocupa las dos columnas de la rejilla. */
  anchoCompleto?: boolean
}

export interface ConfiguracionCrud<T> {
  /** Plural, para títulos: "Clientes". */
  titulo: string
  /** Singular, para el modal: "cliente". */
  singular: string
  descripcion?: string
  api: CrudHooks<T>
  campoId: keyof T & string
  campos: CampoFormulario<T>[]
  columnas: ColumnDef<T, unknown>[]
  /**
   * Si el registro lleva `idEmpresa`, se inyecta automáticamente desde la
   * sesión al crear y se filtra el listado por empresa activa.
   */
  multiEmpresa?: boolean
  /** Valores fijos añadidos al payload de creación. */
  valoresBase?: Partial<T>
}
