import { useMemo } from 'react'
import type { ColumnDef } from '@tanstack/react-table'
import { PaginaCrud } from '@/shared/components/crud/PaginaCrud'
import { calcularEdad, formatearFecha, indexarPor } from '@/shared/lib/utils'
import { especiesApi, mascotasApi, razasApi } from '@/shared/api/recursos'
import { useClientesTodos } from '@/features/clientes/api'
import { filtrarPorEmpresa, useSesion } from '@/shared/session/sesion'
import type { Mascota } from '@/shared/types/api'

export function PaginaMascotas() {
  const idEmpresa = useSesion((s) => s.idEmpresa)

  const clientes = useClientesTodos()
  const razas = razasApi.useLista()
  const especies = especiesApi.useLista()

  const porCliente = useMemo(() => indexarPor(clientes.data, 'idCliente'), [clientes.data])
  const porRaza = useMemo(() => indexarPor(razas.data, 'idRaza'), [razas.data])
  const porEspecie = useMemo(() => indexarPor(especies.data, 'idEspecie'), [especies.data])

  const columnas = useMemo<ColumnDef<Mascota, unknown>[]>(
    () => [
      { accessorKey: 'nombre', header: 'Nombre' },
      {
        id: 'especie',
        header: 'Especie',
        accessorFn: (m) => {
          const raza = porRaza.get(String(m.idRaza))
          return raza ? (porEspecie.get(String(raza.idEspecie))?.nombre ?? '—') : '—'
        },
      },
      {
        id: 'raza',
        header: 'Raza',
        accessorFn: (m) => porRaza.get(String(m.idRaza))?.nombre ?? '—',
      },
      {
        id: 'cliente',
        header: 'Propietario',
        accessorFn: (m) => porCliente.get(m.idCliente)?.nombres ?? '—',
      },
      {
        id: 'nacimiento',
        header: 'Nacimiento',
        accessorFn: (m) => formatearFecha(m.fechaNacimiento),
      },
      {
        id: 'edad',
        header: 'Edad',
        accessorFn: (m) => calcularEdad(m.fechaNacimiento),
      },
    ],
    [porCliente, porRaza, porEspecie],
  )

  // Solo se ofrecen clientes de la empresa activa: crear una mascota apuntando
  // a un cliente de otra empresa sería un dato inconsistente.
  const clientesEmpresa = filtrarPorEmpresa(clientes.data, idEmpresa)

  return (
    <PaginaCrud<Mascota>
      titulo="Mascotas"
      singular="mascota"
      descripcion="Pacientes de la clínica"
      api={mascotasApi}
      campoId="idMascota"
      multiEmpresa
      columnas={columnas}
      campos={[
        { nombre: 'nombre', etiqueta: 'Nombre', tipo: 'texto', requerido: true, maxLength: 50 },
        {
          nombre: 'idCliente',
          etiqueta: 'Propietario',
          tipo: 'select',
          requerido: true,
          opciones: clientesEmpresa.map((c) => ({
            valor: c.idCliente,
            etiqueta: `${c.nombres} — ${c.identificacion}`,
          })),
        },
        {
          nombre: 'idRaza',
          etiqueta: 'Raza',
          tipo: 'select',
          requerido: true,
          opcionesNumericas: true,
          opciones: (razas.data ?? []).map((r) => ({
            valor: r.idRaza,
            etiqueta: `${porEspecie.get(String(r.idEspecie))?.nombre ?? '?'} · ${r.nombre}`,
          })),
        },
        { nombre: 'fechaNacimiento', etiqueta: 'Fecha de nacimiento', tipo: 'fecha' },
      ]}
    />
  )
}
