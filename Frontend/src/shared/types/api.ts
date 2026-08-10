/**
 * Tipos espejo de los modelos Java del backend (com.itq.model).
 *
 * Convenciones que respeta el backend:
 *  - Gson serializa los campos en camelCase, igual que en Java.
 *  - `UUID`           -> string
 *  - `Integer`        -> number   (llaves SERIAL de catálogos)
 *  - `BigDecimal`     -> number   (Gson lo emite como número JSON)
 *  - `LocalDate`      -> string 'YYYY-MM-DD'
 *  - `OffsetDateTime` -> string ISO-8601 con offset
 *
 * IMPORTANTE: no confundir llaves. Los catálogos usan number, las
 * entidades de negocio usan string (UUID). Los alias de abajo lo hacen explícito.
 */

export type Uuid = string
export type IsoDate = string // 'YYYY-MM-DD'
export type IsoDateTime = string // ISO-8601 con offset

/** Envoltorio uniforme que devuelven todos los servlets. */
export interface ApiResponse<T> {
  exito: boolean
  mensaje: string
  datos: T
}

/* ---------------------------------------------------------------- catálogos */

export interface SriIva {
  idIva: number
  porcentaje: number
  codigoSri: string | null
}

export interface Especie {
  idEspecie: number
  nombre: string
}

export interface Raza {
  idRaza: number
  idEspecie: number
  nombre: string
}

export interface Vacuna {
  idVacuna: number
  nombre: string
}

export interface Categoria {
  idCategoria: number
  idEmpresa: Uuid
  nombre: string
}

/* ------------------------------------------------------- seguridad / acceso */

export interface LoginRequest {
  usuario: string
  clave: string
}

export interface LoginResponse {
  token: string
  idUsuario: Uuid
  idEmpresa: Uuid | null
  idRol: number
  rol: string
  nombres: string
  usuario: string
}

/** Respuesta paginada genérica (ver PaginaResponse<T> en el backend). */
export interface PaginaResponse<T> {
  contenido: T[]
  totalRegistros: number
  pagina: number
  tamanio: number
  totalPaginas: number
}

export interface ResumenEmpresa {
  idEmpresa: Uuid
  ruc: string
  razonSocial: string
  direccion: string
  activo: boolean
  totalUsuarios: number
  totalVeterinarios: number
  totalClientes: number
  totalMascotas: number
  totalCitas: number
  totalHistoriales: number
  totalProductos: number
  totalRecetas: number
  totalFacturas: number
  totalMovimientosInventario: number
  totalFacturado: number
  productosBajoStock: number
}

export interface Rol {
  idRol: number
  nombre: string
}

export interface Permiso {
  idPermiso: number
  modulo: string
  accion: string
}

/** Llave compuesta: el servlet la recibe por query string, no por path. */
export interface RolPermiso {
  idRol: number
  idPermiso: number
}

export interface Empresa {
  idEmpresa: Uuid
  ruc: string
  razonSocial: string
  direccion: string
  activo: boolean
}

export interface Usuario {
  idUsuario: Uuid
  idEmpresa: Uuid
  idRol: number
  usuario: string
  /** El backend expone este campo; nunca lo mostramos en la UI. */
  claveHash?: string
  nombres: string
  activo: boolean
}

export interface Veterinario {
  idVeterinario: Uuid
  idUsuario: Uuid
  idEmpresa: Uuid
  especialidad: string | null
}

/* ----------------------------------------------------------------- clínica */

export interface Cliente {
  idCliente: Uuid
  idEmpresa: Uuid
  identificacion: string
  nombres: string
}

export interface Mascota {
  idMascota: Uuid
  idEmpresa: Uuid
  idCliente: Uuid
  idRaza: number
  nombre: string
  fechaNacimiento: IsoDate | null
}

export interface MascotaVacuna {
  idMascotaVacuna: Uuid
  idEmpresa: Uuid
  idMascota: Uuid
  idVacuna: number
  fechaAplicacion: IsoDate
}

export const ESTADOS_CITA = ['Pendiente', 'Atendida', 'Cancelada'] as const
export type EstadoCita = (typeof ESTADOS_CITA)[number]

export interface Cita {
  idCita: Uuid
  idEmpresa: Uuid
  idMascota: Uuid
  idVeterinario: Uuid
  fechaHora: IsoDateTime
  estado: EstadoCita | string
}

export interface HistorialClinico {
  idHistorial: Uuid
  idEmpresa: Uuid
  idCita: Uuid
  pesoKg: number | null
  temperaturaC: number | null
  anamnesis: string | null
  diagnostico: string | null
}

export interface Receta {
  idReceta: Uuid
  idEmpresa: Uuid
  idHistorial: Uuid
  indicacionesGenerales: string | null
}

export interface RecetaDetalle {
  idDetalleReceta: Uuid
  idReceta: Uuid
  idProducto: Uuid
  dosis: string
  frecuencia: string
  duracionDias: number
}

/* ------------------------------------------------- inventario / facturación */

export interface Producto {
  idProducto: Uuid
  idEmpresa: Uuid
  idCategoria: number
  idIva: number
  nombre: string
  precioUnitario: number
  stockActual: number
  stockMinimo: number
  fechaCaducidad: IsoDate | null
}

export const ESTADOS_FACTURA = ['Emitida', 'Anulada'] as const
export type EstadoFactura = (typeof ESTADOS_FACTURA)[number]

export interface Factura {
  idFactura: Uuid
  idEmpresa: Uuid
  idCliente: Uuid
  idUsuario: Uuid
  total: number
  estado: EstadoFactura | string
  fecha: IsoDateTime
}

export interface FacturaDetalle {
  idDetalle: Uuid
  idFactura: Uuid
  idProducto: Uuid
  idIva: number
  cantidad: number
  precioUnitario: number
  subtotal: number
}

/** Body de POST /api/facturas/emitir — emisión transaccional (cabecera + detalle). */
export interface FacturaEmitirRequest {
  idCliente: Uuid
  detalles: Array<Pick<FacturaDetalle, 'idProducto' | 'idIva' | 'cantidad' | 'precioUnitario' | 'subtotal'>>
}

export const TIPOS_MOVIMIENTO = ['Ingreso', 'Egreso', 'Venta', 'Ajuste'] as const
export type TipoMovimiento = (typeof TIPOS_MOVIMIENTO)[number]

export interface MovimientoInventario {
  idMovimiento: Uuid
  idEmpresa: Uuid
  idProducto: Uuid
  idFactura: Uuid | null
  tipo: TipoMovimiento | string
  cantidad: number
  fecha: IsoDateTime
}

/* --------------------------------------------------------------- utilidades */

/** Payload de creación: la llave primaria la genera la BD. */
export type NuevoRegistro<T, K extends keyof T> = Omit<T, K>
