# Frontend — Veterinaria ITQ

SPA en React + TypeScript que consume la API de servlets del backend
(`VETERINARIAITQ.war` sobre Tomcat 10.1).

## Puesta en marcha

1. Levanta el backend en Tomcat. Debe responder en
   `http://localhost:8080/VETERINARIAITQ/api/...`
2. Instala e inicia:

```bash
npm install
npm run dev          # http://localhost:5173
```

El servidor de desarrollo hace proxy de `/api` hacia Tomcat
([vite.config.ts](vite.config.ts)), así que no se depende de la configuración
CORS del backend durante el desarrollo.

### Producción

```bash
npm run build        # genera dist/
```

Si el frontend se sirve desde un origen distinto al backend, define la URL
absoluta al construir:

```bash
VITE_API_URL=http://servidor:8080/VETERINARIAITQ npm run build
```

Alternativa sin CORS: copiar `dist/` dentro de `Backend/src/main/webapp/` para
que Tomcat sirva ambos desde el mismo origen.

## Stack

| Área | Herramienta | Motivo |
|---|---|---|
| Build | Vite 6 | El backend es un `.war`; no hay Node en producción, así que SSR no aplica |
| Lenguaje | TypeScript | 22 entidades con FKs mezcladas `SERIAL`(number) y `UUID`(string) |
| Datos | TanStack Query v5 | La API es CRUD plano sin endpoints compuestos: cachea, deduplica e invalida |
| Ruteo | React Router v6 | Rutas anidadas bajo un layout común |
| Tablas | TanStack Table v8 | Orden, filtro y paginación en cliente |
| Formularios | React Hook Form | Validación inmediata; el servidor valida en serio |
| Estado UI | Zustand | Solo la sesión local; el resto lo gobierna Query |
| Estilos | Tailwind CSS 3 | Componentes propios en `shared/components` |

## Arquitectura

```
src/
├─ app/               router, layout, providers
├─ shared/
│  ├─ api/
│  │  ├─ client.ts    fetch + desempaqueta {exito, mensaje, datos} + ApiError
│  │  ├─ crud.ts      crearHooksCrud<T>() — fábrica de hooks
│  │  └─ recursos.ts  los 21 recursos declarados en un solo lugar
│  ├─ types/api.ts    tipos espejo de com.itq.model
│  ├─ components/     UI + TablaDatos + motor PaginaCrud
│  ├─ session/        sesión local (empresa + usuario activos)
│  └─ lib/utils.ts    formato de fecha/moneda, indexarPor
└─ features/          un directorio por módulo de negocio
```

### La pieza clave: `crearHooksCrud`

Los 22 servlets son estructuralmente idénticos, así que la capa de datos se
genera una sola vez:

```ts
export const clientesApi = crearHooksCrud<Cliente>('clientes', 'idCliente')
// → useLista(), useDetalle(id), useCrear(), useActualizar(), useEliminar()
```

Y sobre eso, `PaginaCrud` construye la pantalla completa (tabla + búsqueda +
alta + edición + baja) a partir de una configuración declarativa de campos.
Un módulo de catálogo cuesta ~20 líneas. Ver
[PaginaCatalogos.tsx](src/features/catalogos/PaginaCatalogos.tsx).

Las pantallas con lógica real **no** usan el motor genérico:

- [Agenda de citas](src/features/citas/PaginaCitas.tsx) — vista semanal, cambio de estado
- [Consulta clínica](src/features/historiales/PaginaHistoriales.tsx) — cita → historial → receta → medicamentos
- [Facturación](src/features/facturacion/PaginaFacturas.tsx) — carrito, cálculo de IVA, anulación
- [Panel](src/features/panel/PaginaPanel.tsx) — métricas del día y alertas de stock

## Limitaciones heredadas del backend

Ninguna se puede resolver desde el frontend. Están documentadas en el código,
en el punto exacto donde impactan.

1. **No hay autenticación.** No existe `/api/auth/login` ni token. La pantalla
   `/acceso` solo selecciona empresa y usuario; **no verifica contraseñas** y no
   es un control de seguridad. Ver
   [sesion.ts](src/shared/session/sesion.ts).

2. **El filtro multi-empresa ocurre en el cliente.** Los servlets devuelven
   todos los registros de todas las empresas; el recorte por `idEmpresa` se hace
   en el navegador. Los datos de otros tenants viajan por la red.

3. **Los permisos no se aplican.** `/accesos` administra la matriz
   rol↔permiso, pero como el backend no informa los permisos del usuario en
   sesión, la interfaz no restringe nada. Ocultar menús daría una falsa
   sensación de seguridad.

4. **La facturación no es transaccional.** No hay endpoint que reciba cabecera
   y detalle juntos, así que se emite en N+1 llamadas. Si falla una línea, la
   factura queda incompleta y no hay rollback.

5. **Sin paginación en servidor.** Se descarga el listado completo y se pagina
   en el navegador. Funciona bien hasta unos pocos miles de filas.

6. **El stock no se descuenta solo.** Facturar no genera el movimiento de
   inventario; hay que registrarlo desde `/inventario`.

Cuando el backend incorpore JWT + `AuthFilter`, el cambio en el frontend se
limita a `shared/session` y `shared/api/client.ts`: el resto de la aplicación
ya lee la sesión a través de `useSesion()`.
