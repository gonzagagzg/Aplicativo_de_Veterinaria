import { Link } from 'react-router-dom'
import {
  ArrowRight,
  Boxes,
  CalendarDays,
  ClipboardList,
  HeartPulse,
  Package,
  Receipt,
  ShieldCheck,
  Stethoscope,
  Syringe,
} from 'lucide-react'
import { format } from 'date-fns'
import { es } from 'date-fns/locale'

const MODULOS = [
  {
    Icono: CalendarDays,
    nombre: 'Agenda de citas',
    descripcion: 'Calendario semanal por veterinario con estados de atención y filtros.',
    color: 'bg-brand-50 text-brand-600',
  },
  {
    Icono: Stethoscope,
    nombre: 'Historias clínicas',
    descripcion: 'Registro completo por paciente: diagnóstico, tratamiento y evolución.',
    color: 'bg-emerald-50 text-emerald-600',
  },
  {
    Icono: Syringe,
    nombre: 'Vacunación',
    descripcion: 'Control de esquemas de vacunación y seguimiento de próximas dosis.',
    color: 'bg-violet-50 text-violet-600',
  },
  {
    Icono: ClipboardList,
    nombre: 'Recetas médicas',
    descripcion: 'Prescripciones vinculadas directamente a la historia clínica.',
    color: 'bg-sky-50 text-sky-600',
  },
  {
    Icono: Boxes,
    nombre: 'Inventario',
    descripcion: 'Stock de productos con alertas automáticas por mínimos críticos.',
    color: 'bg-amber-50 text-amber-600',
  },
  {
    Icono: Receipt,
    nombre: 'Facturación',
    descripcion: 'Emisión de facturas, cálculo de impuestos y control de anulaciones.',
    color: 'bg-rose-50 text-rose-600',
  },
]

export function PaginaInicio() {
  const hoy = format(new Date(), "EEEE d 'de' MMMM yyyy", { locale: es })

  return (
    <div className="min-h-screen">
      {/* ── HEADER ─────────────────────────────────── */}
      <header className="fixed top-0 z-50 w-full border-b border-brand-900 bg-brand-950/95 backdrop-blur-sm">
        <div className="mx-auto flex max-w-7xl items-center justify-between px-6 py-4">
          <div className="flex items-center gap-3">
            <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-gold-500 text-brand-950">
              <HeartPulse className="h-4 w-4" />
            </div>
            <span className="text-sm font-semibold text-white">Veterinaria ITQ</span>
          </div>

          <nav className="hidden items-center gap-8 md:flex">
            <a href="#servicios" className="text-sm text-brand-300 transition-colors hover:text-white">
              Servicios
            </a>
            <a href="#modulos" className="text-sm text-brand-300 transition-colors hover:text-white">
              Módulos
            </a>
          </nav>

          <Link
            to="/acceso"
            className="inline-flex items-center gap-2 rounded-lg bg-gold-500 px-4 py-2 text-sm font-bold text-brand-950 transition-colors hover:bg-gold-400"
          >
            Iniciar sesión
            <ArrowRight className="h-3.5 w-3.5" />
          </Link>
        </div>
      </header>

      {/* ── HERO ───────────────────────────────────── */}
      <section className="flex min-h-screen flex-col justify-center bg-brand-950 px-6 pt-20">
        <div className="mx-auto w-full max-w-7xl">
          <p
            className="itq-fade-up mb-6 text-[11px] font-semibold uppercase tracking-[0.2em] text-brand-400"
            style={{ animationDelay: '0ms' }}
          >
            Sistema de gestión clínica veterinaria
          </p>

          <h1
            className="itq-fade-up mb-6 max-w-4xl text-5xl font-extrabold leading-[1.05] tracking-tight text-white md:text-7xl"
            style={{ animationDelay: '120ms' }}
          >
            Cada paciente.
            <br />
            Cada visita.
            <br />
            <span className="text-gold-400">Todo registrado.</span>
          </h1>

          <p
            className="itq-fade-up mb-10 max-w-lg text-lg leading-relaxed text-brand-300"
            style={{ animationDelay: '240ms' }}
          >
            Control clínico completo para su equipo: agenda, historiales, vacunación, recetas, inventario y facturación — integrados.
          </p>

          <div
            className="itq-fade-up flex flex-wrap gap-4"
            style={{ animationDelay: '360ms' }}
          >
            <Link
              to="/acceso"
              className="inline-flex items-center gap-2 rounded-lg bg-gold-500 px-6 py-3 text-sm font-bold text-brand-950 transition-colors hover:bg-gold-400"
            >
              Iniciar sesión
              <ArrowRight className="h-4 w-4" />
            </Link>
            <a
              href="#modulos"
              className="inline-flex items-center gap-2 rounded-lg border border-brand-700 px-6 py-3 text-sm font-medium text-brand-300 transition-colors hover:border-brand-500 hover:text-white"
            >
              Ver módulos
            </a>
          </div>

          {/* Tira de datos clínicos — el elemento firma */}
          <div
            className="itq-fade-up mt-16 flex flex-wrap items-center gap-x-8 gap-y-2 border-t border-brand-800 pt-6"
            style={{ animationDelay: '480ms' }}
          >
            <span className="text-xs capitalize text-brand-500">{hoy}</span>
            <span className="flex items-center gap-2 text-xs text-brand-500">
              <span className="h-1.5 w-1.5 rounded-full bg-emerald-400" />
              Sistema operativo
            </span>
            <span className="text-xs text-brand-500">15 módulos activos</span>
            <span className="text-xs text-brand-500">Multi-empresa</span>
          </div>
        </div>
      </section>

      {/* ── PILARES ────────────────────────────────── */}
      <section id="servicios" className="bg-brand-50 px-6 py-24">
        <div className="mx-auto max-w-7xl">
          <p className="mb-3 text-[11px] font-semibold uppercase tracking-[0.2em] text-brand-500">
            Áreas de gestión
          </p>
          <h2 className="mb-12 text-3xl font-bold tracking-tight text-slate-900 md:text-4xl">
            Todo lo que necesita su clínica
          </h2>
          <div className="grid grid-cols-1 gap-6 md:grid-cols-3">
            <PilarCard
              Icono={Stethoscope}
              titulo="Clínica"
              descripcion="Gestión completa del flujo de atención médica veterinaria."
              items={['Agenda de citas', 'Historias clínicas', 'Vacunación', 'Recetas médicas']}
              iconBg="bg-brand-100"
              iconColor="text-brand-600"
              acento="border-brand-500"
            />
            <PilarCard
              Icono={Package}
              titulo="Comercial"
              descripcion="Control de productos, stock y facturación al cliente."
              items={['Catálogo de productos', 'Control de inventario', 'Alertas de stock', 'Facturación con IVA']}
              iconBg="bg-gold-100"
              iconColor="text-gold-700"
              acento="border-gold-500"
            />
            <PilarCard
              Icono={ShieldCheck}
              titulo="Administración"
              descripcion="Configuración de usuarios, roles y estructura multi-empresa."
              items={['Gestión de usuarios', 'Roles y permisos', 'Perfiles veterinarios', 'Multi-empresa']}
              iconBg="bg-emerald-100"
              iconColor="text-emerald-600"
              acento="border-emerald-500"
            />
          </div>
        </div>
      </section>

      {/* ── MÓDULOS ────────────────────────────────── */}
      <section id="modulos" className="bg-white px-6 py-24">
        <div className="mx-auto max-w-7xl">
          <p className="mb-3 text-[11px] font-semibold uppercase tracking-[0.2em] text-brand-500">
            Módulos del sistema
          </p>
          <h2 className="mb-4 text-3xl font-bold tracking-tight text-slate-900 md:text-4xl">
            Funcionalidad integrada
          </h2>
          <p className="mb-12 max-w-xl text-base leading-relaxed text-slate-500">
            Cada módulo está diseñado para el flujo real de una clínica veterinaria, conectado con el resto del sistema.
          </p>
          <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-3">
            {MODULOS.map((m) => (
              <ModuloCard key={m.nombre} {...m} />
            ))}
          </div>
        </div>
      </section>

      {/* ── STATS ──────────────────────────────────── */}
      <section className="bg-brand-900 px-6 py-16">
        <div className="mx-auto max-w-7xl">
          <div className="flex flex-wrap items-center justify-around gap-10 text-center">
            <StatItem valor="15+" etiqueta="Módulos" />
            <StatItem valor="360°" etiqueta="Visión del paciente" />
            <StatItem valor="∞" etiqueta="Registros clínicos" />
            <StatItem valor="1" etiqueta="Sistema unificado" />
          </div>
        </div>
      </section>

      {/* ── CTA FINAL ──────────────────────────────── */}
      <section className="bg-brand-950 px-6 py-24 text-center">
        <div className="mx-auto max-w-2xl">
          <p className="mb-3 text-[11px] font-semibold uppercase tracking-[0.2em] text-brand-400">
            Listo para empezar
          </p>
          <h2 className="mb-4 text-3xl font-bold text-white md:text-4xl">
            Su clínica, organizada.
          </h2>
          <p className="mb-8 text-base leading-relaxed text-brand-300">
            Acceda al sistema con su usuario y comience a gestionar su clínica veterinaria de forma profesional.
          </p>
          <Link
            to="/acceso"
            className="inline-flex items-center gap-2 rounded-lg bg-gold-500 px-8 py-3.5 text-sm font-bold text-brand-950 transition-colors hover:bg-gold-400"
          >
            Iniciar sesión
            <ArrowRight className="h-4 w-4" />
          </Link>
        </div>
      </section>

      {/* ── FOOTER ─────────────────────────────────── */}
      <footer className="border-t border-brand-900 bg-brand-950 px-6 py-8">
        <div className="mx-auto flex max-w-7xl flex-wrap items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <div className="flex h-7 w-7 items-center justify-center rounded-lg bg-gold-500 text-brand-950">
              <HeartPulse className="h-3.5 w-3.5" />
            </div>
            <span className="text-sm font-semibold text-white">Veterinaria ITQ</span>
          </div>
          <div className="flex items-center gap-6">
            <Link to="/acceso" className="text-xs text-brand-500 transition-colors hover:text-brand-300">
              Iniciar sesión
            </Link>
            <span className="text-xs text-brand-600">
              © {new Date().getFullYear()} · Sistema de gestión clínica veterinaria
            </span>
          </div>
        </div>
      </footer>
    </div>
  )
}

function PilarCard({
  Icono,
  titulo,
  descripcion,
  items,
  iconBg,
  iconColor,
  acento,
}: {
  Icono: React.ComponentType<{ className?: string }>
  titulo: string
  descripcion: string
  items: string[]
  iconBg: string
  iconColor: string
  acento: string
}) {
  return (
    <div className={`rounded-xl border-t-4 bg-white p-6 shadow-sm ${acento}`}>
      <div className={`mb-4 flex h-11 w-11 items-center justify-center rounded-lg ${iconBg}`}>
        <Icono className={`h-5 w-5 ${iconColor}`} />
      </div>
      <h3 className="mb-2 text-lg font-bold text-slate-900">{titulo}</h3>
      <p className="mb-5 text-sm leading-relaxed text-slate-500">{descripcion}</p>
      <ul className="space-y-2">
        {items.map((item) => (
          <li key={item} className="flex items-center gap-2.5 text-sm text-slate-600">
            <span className="h-1 w-1 shrink-0 rounded-full bg-gold-500" />
            {item}
          </li>
        ))}
      </ul>
    </div>
  )
}

function ModuloCard({
  Icono,
  nombre,
  descripcion,
  color,
}: {
  Icono: React.ComponentType<{ className?: string }>
  nombre: string
  descripcion: string
  color: string
}) {
  return (
    <div className="group rounded-xl border border-slate-200 p-5 transition-all hover:border-brand-300 hover:shadow-sm">
      <div className={`mb-4 flex h-10 w-10 items-center justify-center rounded-lg ${color}`}>
        <Icono className="h-5 w-5" />
      </div>
      <h3 className="mb-1.5 text-sm font-semibold text-slate-900">{nombre}</h3>
      <p className="text-sm leading-relaxed text-slate-500">{descripcion}</p>
    </div>
  )
}

function StatItem({ valor, etiqueta }: { valor: string; etiqueta: string }) {
  return (
    <div>
      <p className="text-4xl font-extrabold tracking-tight text-white">{valor}</p>
      <p className="mt-1 text-sm text-brand-400">{etiqueta}</p>
    </div>
  )
}

