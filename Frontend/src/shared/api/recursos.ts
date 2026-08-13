import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { api } from './client'
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
  ResumenEmpresa,
  Rol,
  SriIva,
  Usuario,
  Uuid,
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

/**
 * Extensiones del módulo Empresas reservadas a SuperUsuario.
 * `empresasApi` (arriba) ya cubre GET/POST/PUT genéricos de /api/empresas;
 * aquí solo lo que no encaja en el CRUD genérico: activar/desactivar y el
 * resumen de uso por empresa (EmpresaServlet en el backend).
 */
export const empresaAdminApi = {
  useResumen: (idEmpresa: Uuid | undefined) =>
    useQuery<ResumenEmpresa>({
      queryKey: ['empresas', 'resumen', idEmpresa],
      queryFn: () => api.get<ResumenEmpresa>(`/api/empresas/${idEmpresa}/resumen`),
      enabled: !!idEmpresa,
    }),

  useActivar: () => {
    const qc = useQueryClient()
    return useMutation<void, Error, Uuid>({
      mutationFn: (idEmpresa) => api.put<void>(`/api/empresas/${idEmpresa}/activar`, undefined),
      onSuccess: () => qc.invalidateQueries({ queryKey: ['empresas'] }),
    })
  },

  useDesactivar: () => {
    const qc = useQueryClient()
    return useMutation<void, Error, Uuid>({
      mutationFn: (idEmpresa) => api.put<void>(`/api/empresas/${idEmpresa}/desactivar`, undefined),
      onSuccess: () => qc.invalidateQueries({ queryKey: ['empresas'] }),
    })
  },
}
