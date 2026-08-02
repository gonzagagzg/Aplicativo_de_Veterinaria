import { Navigate, createBrowserRouter } from 'react-router-dom'
import { AppLayout } from './layout/AppLayout'
import { PaginaAcceso } from '@/features/acceso/PaginaAcceso'
import { PaginaPanel } from '@/features/panel/PaginaPanel'
import { PaginaCitas } from '@/features/citas/PaginaCitas'
import { PaginaClientes } from '@/features/clientes/PaginaClientes'
import { PaginaMascotas } from '@/features/mascotas/PaginaMascotas'
import { PaginaHistoriales } from '@/features/historiales/PaginaHistoriales'
import { PaginaVacunacion } from '@/features/vacunacion/PaginaVacunacion'
import { PaginaRecetas } from '@/features/recetas/PaginaRecetas'
import { PaginaProductos } from '@/features/productos/PaginaProductos'
import { PaginaInventario } from '@/features/inventario/PaginaInventario'
import { PaginaFacturas } from '@/features/facturacion/PaginaFacturas'
import { PaginaCatalogos } from '@/features/catalogos/PaginaCatalogos'
import { PaginaUsuarios } from '@/features/admin/PaginaUsuarios'
import { PaginaVeterinarios } from '@/features/admin/PaginaVeterinarios'
import { PaginaAccesos } from '@/features/admin/PaginaAccesos'
import { PaginaEmpresas } from '@/features/admin/PaginaEmpresas'
import { useSesion } from '@/shared/session/sesion'

/**
 * Redirige al selector de contexto si no hay empresa/usuario elegidos.
 * NO es un guard de seguridad — ver la nota en shared/session/sesion.ts.
 */
function RequiereSesion({ children }: { children: React.ReactNode }) {
  const idEmpresa = useSesion((s) => s.idEmpresa)
  if (!idEmpresa) return <Navigate to="/acceso" replace />
  return <>{children}</>
}

export const router = createBrowserRouter([
  { path: '/acceso', element: <PaginaAcceso /> },
  {
    path: '/',
    element: (
      <RequiereSesion>
        <AppLayout />
      </RequiereSesion>
    ),
    children: [
      { index: true, element: <PaginaPanel /> },
      { path: 'citas', element: <PaginaCitas /> },
      { path: 'clientes', element: <PaginaClientes /> },
      { path: 'mascotas', element: <PaginaMascotas /> },
      { path: 'historiales', element: <PaginaHistoriales /> },
      { path: 'vacunacion', element: <PaginaVacunacion /> },
      { path: 'recetas', element: <PaginaRecetas /> },
      { path: 'productos', element: <PaginaProductos /> },
      { path: 'inventario', element: <PaginaInventario /> },
      { path: 'facturas', element: <PaginaFacturas /> },
      { path: 'catalogos', element: <PaginaCatalogos /> },
      { path: 'usuarios', element: <PaginaUsuarios /> },
      { path: 'veterinarios', element: <PaginaVeterinarios /> },
      { path: 'accesos', element: <PaginaAccesos /> },
      { path: 'empresas', element: <PaginaEmpresas /> },
      { path: '*', element: <Navigate to="/" replace /> },
    ],
  },
])
