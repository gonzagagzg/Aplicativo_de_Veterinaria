import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import {
  Boxes,
  Building2,
  CalendarDays,
  ClipboardList,
  Dog,
  HeartPulse,
  LayoutDashboard,
  LogOut,
  Package,
  Receipt,
  ShieldCheck,
  Stethoscope,
  Syringe,
  Tags,
  Users,
  UserSquare,
} from 'lucide-react'
import { cn } from '@/shared/lib/utils'
import { esSuperUsuario, useSesion } from '@/shared/session/sesion'
import { usePermisos } from '@/shared/session/permisos'
import { BloqueoEmpresa } from './BloqueoEmpresa'

interface ItemNav {
  ruta: string
  etiqueta: string
  Icono: React.ComponentType<{ className?: string }>
  exacto?: boolean
  /**
   * Módulo RBAC que protege esta ruta en el backend (ver Autorizacion.exigir
   * en cada Servlet). `null` = sin permiso dedicado, visible para cualquier
   * usuario autenticado. `soloSuperUsuario` = reservado (ver EmpresaServlet).
   */
  modulo: string | null
  soloSuperUsuario?: boolean
}

const SECCIONES: { titulo: string; items: ItemNav[] }[] = [
  {
    titulo: 'General',
    items: [{ ruta: '/app', etiqueta: 'Panel', Icono: LayoutDashboard, exacto: true, modulo: null }],
  },
  {
    titulo: 'Clínica',
    items: [
      { ruta: '/app/citas', etiqueta: 'Agenda de citas', Icono: CalendarDays, modulo: 'CITAS' },
      { ruta: '/app/clientes', etiqueta: 'Clientes', Icono: Users, modulo: 'CLIENTES' },
      { ruta: '/app/mascotas', etiqueta: 'Mascotas', Icono: Dog, modulo: 'MASCOTAS' },
      {
        ruta: '/app/historiales',
        etiqueta: 'Historias clínicas',
        Icono: Stethoscope,
        modulo: 'HISTORIALES',
      },
      { ruta: '/app/vacunacion', etiqueta: 'Vacunación', Icono: Syringe, modulo: 'MASCOTAS' },
      { ruta: '/app/recetas', etiqueta: 'Recetas', Icono: ClipboardList, modulo: null },
    ],
  },
  {
    titulo: 'Comercial',
    items: [
      { ruta: '/app/productos', etiqueta: 'Productos', Icono: Package, modulo: 'PRODUCTOS' },
      { ruta: '/app/inventario', etiqueta: 'Inventario', Icono: Boxes, modulo: 'INVENTARIO' },
      { ruta: '/app/facturas', etiqueta: 'Facturación', Icono: Receipt, modulo: 'FACTURAS' },
    ],
  },
  {
    titulo: 'Configuración',
    items: [
      { ruta: '/app/catalogos', etiqueta: 'Catálogos', Icono: Tags, modulo: 'CATEGORIAS' },
      { ruta: '/app/usuarios', etiqueta: 'Usuarios', Icono: UserSquare, modulo: 'USUARIOS' },
      {
        ruta: '/app/veterinarios',
        etiqueta: 'Veterinarios',
        Icono: Stethoscope,
        modulo: 'VETERINARIOS',
      },
      { ruta: '/app/accesos', etiqueta: 'Roles y permisos', Icono: ShieldCheck, modulo: 'USUARIOS' },
      {
        ruta: '/app/empresas',
        etiqueta: 'Veterinarias (SaaS)',
        Icono: Building2,
        modulo: 'EMPRESAS',
        soloSuperUsuario: true,
      },
    ],
  },
]

export function AppLayout() {
  const navigate = useNavigate()
  const { rol, nombreUsuario, limpiar } = useSesion()
  const permisos = usePermisos()
  const esSuper = esSuperUsuario(rol)

  const secciones = SECCIONES.map((seccion) => ({
    ...seccion,
    items: seccion.items.filter((item) => {
      if (item.soloSuperUsuario) return esSuper
      if (item.modulo === null) return true
      return permisos.tieneAccesoAModulo(item.modulo)
    }),
  })).filter((seccion) => seccion.items.length > 0)

  return (
    <>
      <BloqueoEmpresa />
    <div className="flex min-h-screen">
      <aside className="hidden w-64 shrink-0 flex-col bg-brand-950 lg:flex">
        <div className="flex items-center gap-3 border-b border-brand-900 px-5 py-5">
          <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-gold-500 text-brand-950">
            <HeartPulse className="h-5 w-5" />
          </div>
          <div className="min-w-0">
            <p className="truncate text-sm font-semibold text-white">Veterinaria ITQ</p>
            <p className="truncate text-xs text-brand-400">{rol ?? 'Sin rol'}</p>
          </div>
        </div>

        <nav className="flex-1 space-y-6 overflow-y-auto px-3 py-5">
          {secciones.map((seccion) => (
            <div key={seccion.titulo}>
              <p className="px-2 pb-2 text-[10px] font-semibold uppercase tracking-widest text-brand-500">
                {seccion.titulo}
              </p>
              <div className="space-y-0.5">
                {seccion.items.map(({ ruta, etiqueta, Icono, exacto }) => (
                  <NavLink
                    key={ruta}
                    to={ruta}
                    end={exacto}
                    className={({ isActive }) =>
                      cn(
                        'flex items-center gap-3 rounded-lg px-3 py-2 text-sm transition-colors',
                        isActive
                          ? 'bg-brand-800 font-semibold text-gold-400'
                          : 'text-brand-300 hover:bg-brand-900 hover:text-white',
                      )
                    }
                  >
                    <Icono className="h-4 w-4 shrink-0" />
                    {etiqueta}
                  </NavLink>
                ))}
              </div>
            </div>
          ))}
        </nav>

        <div className="border-t border-brand-900 p-3">
          <div className="mb-2 rounded-lg px-3 py-2">
            <p className="truncate text-sm font-semibold text-white">{nombreUsuario}</p>
            <p className="text-xs text-brand-400">Sesión activa</p>
          </div>
          <button
            onClick={() => {
              limpiar()
              navigate('/acceso')
            }}
            className="flex w-full items-center gap-3 rounded-lg px-3 py-2 text-sm text-brand-300 transition-colors hover:bg-brand-900 hover:text-white"
          >
            <LogOut className="h-4 w-4" />
            Cerrar sesión
          </button>
        </div>
      </aside>

      <main className="min-w-0 flex-1">
        <div className="mx-auto max-w-7xl px-5 py-8 lg:px-8">
          <Outlet />
        </div>
      </main>
    </div>
    </>
  )
}
