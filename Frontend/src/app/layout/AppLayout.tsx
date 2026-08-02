import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import {
  Boxes,
  Building2,
  CalendarDays,
  ClipboardList,
  Dog,
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
import { useSesion } from '@/shared/session/sesion'

interface ItemNav {
  ruta: string
  etiqueta: string
  Icono: React.ComponentType<{ className?: string }>
  exacto?: boolean
}

const SECCIONES: { titulo: string; items: ItemNav[] }[] = [
  {
    titulo: 'General',
    items: [{ ruta: '/', etiqueta: 'Panel', Icono: LayoutDashboard, exacto: true }],
  },
  {
    titulo: 'Clínica',
    items: [
      { ruta: '/citas', etiqueta: 'Agenda de citas', Icono: CalendarDays },
      { ruta: '/clientes', etiqueta: 'Clientes', Icono: Users },
      { ruta: '/mascotas', etiqueta: 'Mascotas', Icono: Dog },
      { ruta: '/historiales', etiqueta: 'Historias clínicas', Icono: Stethoscope },
      { ruta: '/vacunacion', etiqueta: 'Vacunación', Icono: Syringe },
      { ruta: '/recetas', etiqueta: 'Recetas', Icono: ClipboardList },
    ],
  },
  {
    titulo: 'Comercial',
    items: [
      { ruta: '/productos', etiqueta: 'Productos', Icono: Package },
      { ruta: '/inventario', etiqueta: 'Inventario', Icono: Boxes },
      { ruta: '/facturas', etiqueta: 'Facturación', Icono: Receipt },
    ],
  },
  {
    titulo: 'Configuración',
    items: [
      { ruta: '/catalogos', etiqueta: 'Catálogos', Icono: Tags },
      { ruta: '/usuarios', etiqueta: 'Usuarios', Icono: UserSquare },
      { ruta: '/veterinarios', etiqueta: 'Veterinarios', Icono: Stethoscope },
      { ruta: '/accesos', etiqueta: 'Roles y permisos', Icono: ShieldCheck },
      { ruta: '/empresas', etiqueta: 'Empresas', Icono: Building2 },
    ],
  },
]

export function AppLayout() {
  const navigate = useNavigate()
  const { nombreEmpresa, nombreUsuario, limpiar } = useSesion()

  return (
    <div className="flex min-h-screen">
      <aside className="hidden w-64 shrink-0 flex-col border-r border-slate-200 bg-white lg:flex">
        <div className="flex items-center gap-2.5 border-b border-slate-200 px-5 py-4">
          <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-brand-600 text-white">
            <Dog className="h-5 w-5" />
          </div>
          <div className="min-w-0">
            <p className="truncate text-sm font-semibold text-slate-900">Veterinaria ITQ</p>
            <p className="truncate text-xs text-slate-500">{nombreEmpresa ?? 'Sin empresa'}</p>
          </div>
        </div>

        <nav className="flex-1 space-y-5 overflow-y-auto px-3 py-4">
          {SECCIONES.map((seccion) => (
            <div key={seccion.titulo}>
              <p className="px-2 pb-1.5 text-[11px] font-semibold uppercase tracking-wider text-slate-400">
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
                        'flex items-center gap-2.5 rounded-lg px-2.5 py-2 text-sm transition-colors',
                        isActive
                          ? 'bg-brand-50 font-medium text-brand-700'
                          : 'text-slate-600 hover:bg-slate-100',
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

        <div className="border-t border-slate-200 p-3">
          <div className="mb-2 px-2">
            <p className="truncate text-sm font-medium text-slate-700">{nombreUsuario}</p>
            <p className="text-xs text-slate-500">Sesión local</p>
          </div>
          <button
            onClick={() => {
              limpiar()
              navigate('/acceso')
            }}
            className="flex w-full items-center gap-2.5 rounded-lg px-2.5 py-2 text-sm text-slate-600 hover:bg-slate-100"
          >
            <LogOut className="h-4 w-4" />
            Cambiar de sesión
          </button>
        </div>
      </aside>

      <main className="min-w-0 flex-1">
        <div className="mx-auto max-w-7xl px-5 py-6 lg:px-8">
          <Outlet />
        </div>
      </main>
    </div>
  )
}
