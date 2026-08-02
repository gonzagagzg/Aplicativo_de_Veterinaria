import { forwardRef, type ReactNode } from 'react'
import { Loader2, X } from 'lucide-react'
import { cn } from '@/shared/lib/utils'

/* --------------------------------------------------------------- Button */

type VarianteBoton = 'primario' | 'secundario' | 'peligro' | 'fantasma'

const estilosBoton: Record<VarianteBoton, string> = {
  primario: 'bg-brand-600 text-white hover:bg-brand-700 focus-visible:ring-brand-500',
  secundario:
    'bg-white text-slate-700 border border-slate-300 hover:bg-slate-50 focus-visible:ring-slate-400',
  peligro: 'bg-red-600 text-white hover:bg-red-700 focus-visible:ring-red-500',
  fantasma: 'text-slate-600 hover:bg-slate-100 focus-visible:ring-slate-400',
}

interface PropsBoton extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variante?: VarianteBoton
  cargando?: boolean
}

export function Boton({
  variante = 'primario',
  cargando,
  className,
  children,
  disabled,
  ...props
}: PropsBoton) {
  return (
    <button
      className={cn(
        'inline-flex items-center justify-center gap-2 rounded-lg px-3.5 py-2 text-sm font-medium transition-colors',
        'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-1',
        'disabled:pointer-events-none disabled:opacity-50',
        estilosBoton[variante],
        className,
      )}
      disabled={disabled || cargando}
      {...props}
    >
      {cargando && <Loader2 className="h-4 w-4 animate-spin" />}
      {children}
    </button>
  )
}

/* ------------------------------------------------------- Campos de formulario */

const claseControl =
  'w-full rounded-lg border border-slate-300 bg-white px-3 py-2 text-sm text-slate-900 ' +
  'placeholder:text-slate-400 focus:border-brand-500 focus:outline-none focus:ring-2 ' +
  'focus:ring-brand-500/20 disabled:bg-slate-50 disabled:text-slate-500'

export const Input = forwardRef<HTMLInputElement, React.InputHTMLAttributes<HTMLInputElement>>(
  ({ className, ...props }, ref) => (
    <input ref={ref} className={cn(claseControl, className)} {...props} />
  ),
)
Input.displayName = 'Input'

export const Select = forwardRef<HTMLSelectElement, React.SelectHTMLAttributes<HTMLSelectElement>>(
  ({ className, ...props }, ref) => (
    <select ref={ref} className={cn(claseControl, 'pr-8', className)} {...props} />
  ),
)
Select.displayName = 'Select'

export const TextArea = forwardRef<
  HTMLTextAreaElement,
  React.TextareaHTMLAttributes<HTMLTextAreaElement>
>(({ className, ...props }, ref) => (
  <textarea ref={ref} rows={3} className={cn(claseControl, 'resize-y', className)} {...props} />
))
TextArea.displayName = 'TextArea'

export function Campo({
  etiqueta,
  error,
  requerido,
  ayuda,
  children,
}: {
  etiqueta: string
  error?: string
  requerido?: boolean
  ayuda?: string
  children: ReactNode
}) {
  return (
    <label className="block space-y-1.5">
      <span className="text-sm font-medium text-slate-700">
        {etiqueta}
        {requerido && <span className="ml-0.5 text-red-500">*</span>}
      </span>
      {children}
      {ayuda && !error && <span className="block text-xs text-slate-500">{ayuda}</span>}
      {error && <span className="block text-xs font-medium text-red-600">{error}</span>}
    </label>
  )
}

/* ---------------------------------------------------------------- Badge */

type TonoBadge = 'neutro' | 'exito' | 'alerta' | 'peligro' | 'info'

const estilosBadge: Record<TonoBadge, string> = {
  neutro: 'bg-slate-100 text-slate-700',
  exito: 'bg-emerald-100 text-emerald-800',
  alerta: 'bg-amber-100 text-amber-800',
  peligro: 'bg-red-100 text-red-800',
  info: 'bg-sky-100 text-sky-800',
}

export function Badge({ tono = 'neutro', children }: { tono?: TonoBadge; children: ReactNode }) {
  return (
    <span
      className={cn(
        'inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium',
        estilosBadge[tono],
      )}
    >
      {children}
    </span>
  )
}

/* ---------------------------------------------------------------- Modal */

export function Modal({
  abierto,
  titulo,
  descripcion,
  onCerrar,
  children,
  ancho = 'max-w-lg',
}: {
  abierto: boolean
  titulo: string
  descripcion?: string
  onCerrar: () => void
  children: ReactNode
  ancho?: string
}) {
  if (!abierto) return null

  return (
    <div className="fixed inset-0 z-50 flex items-start justify-center overflow-y-auto bg-slate-900/40 p-4 sm:p-8">
      <div
        role="dialog"
        aria-modal="true"
        aria-label={titulo}
        className={cn('w-full rounded-xl bg-white shadow-xl', ancho)}
      >
        <div className="flex items-start justify-between border-b border-slate-200 px-5 py-4">
          <div>
            <h2 className="text-base font-semibold text-slate-900">{titulo}</h2>
            {descripcion && <p className="mt-0.5 text-sm text-slate-500">{descripcion}</p>}
          </div>
          <button
            onClick={onCerrar}
            aria-label="Cerrar"
            className="rounded-lg p-1 text-slate-400 hover:bg-slate-100 hover:text-slate-600"
          >
            <X className="h-5 w-5" />
          </button>
        </div>
        <div className="px-5 py-4">{children}</div>
      </div>
    </div>
  )
}

/* --------------------------------------------------------- Estados vacíos */

export function Cargando({ texto = 'Cargando…' }: { texto?: string }) {
  return (
    <div className="flex items-center justify-center gap-2 py-12 text-sm text-slate-500">
      <Loader2 className="h-4 w-4 animate-spin" />
      {texto}
    </div>
  )
}

export function MensajeError({ error }: { error: unknown }) {
  const mensaje = error instanceof Error ? error.message : 'Ocurrió un error inesperado'
  return (
    <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-800">
      {mensaje}
    </div>
  )
}

export function SinDatos({ mensaje = 'No hay registros para mostrar' }: { mensaje?: string }) {
  return (
    <div className="py-12 text-center text-sm text-slate-500">{mensaje}</div>
  )
}

/* -------------------------------------------------------------- Cabecera */

export function CabeceraPagina({
  titulo,
  descripcion,
  acciones,
}: {
  titulo: string
  descripcion?: string
  acciones?: ReactNode
}) {
  return (
    <div className="mb-6 flex flex-wrap items-end justify-between gap-4">
      <div>
        <h1 className="text-xl font-semibold text-slate-900">{titulo}</h1>
        {descripcion && <p className="mt-1 text-sm text-slate-500">{descripcion}</p>}
      </div>
      {acciones && <div className="flex gap-2">{acciones}</div>}
    </div>
  )
}

export function Tarjeta({
  titulo,
  acciones,
  children,
  className,
}: {
  titulo?: string
  acciones?: ReactNode
  children: ReactNode
  className?: string
}) {
  return (
    <section className={cn('rounded-xl border border-slate-200 bg-white', className)}>
      {(titulo || acciones) && (
        <header className="flex items-center justify-between border-b border-slate-200 px-5 py-3">
          {titulo && <h2 className="text-sm font-semibold text-slate-900">{titulo}</h2>}
          {acciones}
        </header>
      )}
      <div className="p-5">{children}</div>
    </section>
  )
}
