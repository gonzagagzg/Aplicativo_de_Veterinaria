import { crearHooksCrud } from './crud'
import type {
  Categoria,
  Cita,
  Cliente,
  Empresa,
  Especie,
  Factura,
  FacturaDetalle,
  HistorialClinico,
  Mascota,
  MascotaVacuna,
  MovimientoInventario,
  Permiso,
  Producto,
  Raza,
  Receta,
  RecetaDetalle,
  Rol,
  SriIva,
  Usuario,
  Vacuna,
  Veterinario,
} from '@/shared/types/api'

/**
 * Punto único donde se declara cada recurso de la API.
 * La ruta corresponde exactamente al @WebServlet del backend.
 *
 * Nota: `rol-permisos` NO se declara aquí porque usa llave compuesta por
 * query string en vez de path; vive en features/admin/api/rolPermisos.ts.
 */

export const especiesApi = crearHooksCrud<Especie>('especies', 'idEspecie')
export const razasApi = crearHooksCrud<Raza>('razas', 'idRaza')
export const vacunasApi = crearHooksCrud<Vacuna>('vacunas', 'idVacuna')
export const categoriasApi = crearHooksCrud<Categoria>('categorias', 'idCategoria')
export const sriIvaApi = crearHooksCrud<SriIva>('sri-iva', 'idIva')

export const rolesApi = crearHooksCrud<Rol>('roles', 'idRol')
export const permisosApi = crearHooksCrud<Permiso>('permisos', 'idPermiso')
export const empresasApi = crearHooksCrud<Empresa>('empresas', 'idEmpresa')
export const usuariosApi = crearHooksCrud<Usuario>('usuarios', 'idUsuario')
export const veterinariosApi = crearHooksCrud<Veterinario>('veterinarios', 'idVeterinario')

export const clientesApi = crearHooksCrud<Cliente>('clientes', 'idCliente')
export const mascotasApi = crearHooksCrud<Mascota>('mascotas', 'idMascota')
export const mascotaVacunasApi = crearHooksCrud<MascotaVacuna>('mascota-vacunas', 'idMascotaVacuna')
export const citasApi = crearHooksCrud<Cita>('citas', 'idCita')
export const historialesApi = crearHooksCrud<HistorialClinico>('historiales-clinicos', 'idHistorial')
export const recetasApi = crearHooksCrud<Receta>('recetas', 'idReceta')
export const recetaDetallesApi = crearHooksCrud<RecetaDetalle>('receta-detalles', 'idDetalleReceta')

export const productosApi = crearHooksCrud<Producto>('productos', 'idProducto')
export const facturasApi = crearHooksCrud<Factura>('facturas', 'idFactura')
export const facturaDetallesApi = crearHooksCrud<FacturaDetalle>('factura-detalles', 'idDetalle')
export const movimientosApi = crearHooksCrud<MovimientoInventario>(
  'movimientos-inventario',
  'idMovimiento',
)
