-- ============================================================
-- APLICATIVO DE VETERINARIA
-- ACTUALIZACIÓN DE BASE DE DATOS - CAMBIOS SOLICITADOS
-- ============================================================
-- Este script contiene únicamente los cambios de Base de Datos
-- correspondientes a las mejoras implementadas en Backend.
--
-- Incluye:
-- 1. Correo electrónico para usuarios
-- 2. Recuperación de contraseña
-- 3. Código de veterinario
-- 4. Permisos de mensualidades para Administrador Local
--
-- No crea pago_aplicacion porque se reutiliza el módulo
-- existente mensualidad_empresa.
-- ============================================================


-- ============================================================
-- 1. CORREO ELECTRÓNICO EN USUARIO
-- ============================================================

ALTER TABLE usuario
ADD COLUMN IF NOT EXISTS correo VARCHAR(120);

CREATE UNIQUE INDEX IF NOT EXISTS ux_usuario_correo_normalizado
ON usuario (LOWER(TRIM(correo)))
WHERE correo IS NOT NULL
  AND TRIM(correo) <> '';


-- ============================================================
-- 2. RECUPERACIÓN DE CONTRASEÑA
-- ============================================================

CREATE TABLE IF NOT EXISTS recuperacion_password (
    id_recuperacion UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_usuario UUID NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_expiracion TIMESTAMP NOT NULL,
    utilizado BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_recuperacion_password_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuario(id_usuario)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_recuperacion_password_token
ON recuperacion_password(token);


-- ============================================================
-- 3. CÓDIGO DE VETERINARIO
-- ============================================================

ALTER TABLE veterinario
ADD COLUMN IF NOT EXISTS codigo_veterinario VARCHAR(50);

CREATE UNIQUE INDEX IF NOT EXISTS ux_veterinario_codigo_empresa
ON veterinario (
    id_empresa,
    LOWER(TRIM(codigo_veterinario))
)
WHERE codigo_veterinario IS NOT NULL
  AND TRIM(codigo_veterinario) <> '';


-- ============================================================
-- 4. PERMISOS DE MENSUALIDADES PARA ADMINISTRADOR LOCAL
-- ============================================================
-- Rol 2 = Administrador Local
--
-- Se otorgan únicamente:
-- LISTAR
-- VER
--
-- La creación, edición y eliminación continúa restringida
-- al SuperUsuario.
-- ============================================================

INSERT INTO rol_permiso (
    id_rol,
    id_permiso
)
SELECT
    2,
    p.id_permiso
FROM permiso p
WHERE UPPER(p.modulo) = 'MENSUALIDADES'
  AND UPPER(p.accion) IN ('LISTAR', 'VER')
  AND NOT EXISTS (
      SELECT 1
      FROM rol_permiso rp
      WHERE rp.id_rol = 2
        AND rp.id_permiso = p.id_permiso
  );


-- ============================================================
-- 5. VERIFICACIÓN DE CAMBIOS
-- ============================================================

-- Verificar columna correo
SELECT
    column_name,
    data_type
FROM information_schema.columns
WHERE table_name = 'usuario'
  AND column_name = 'correo';


-- Verificar tabla de recuperación
SELECT
    to_regclass('public.recuperacion_password')
    AS tabla_recuperacion_password;


-- Verificar código de veterinario
SELECT
    column_name,
    data_type
FROM information_schema.columns
WHERE table_name = 'veterinario'
  AND column_name = 'codigo_veterinario';


-- Verificar permisos del Administrador Local
SELECT
    rp.id_rol,
    p.id_permiso,
    p.modulo,
    p.accion
FROM rol_permiso rp
INNER JOIN permiso p
    ON p.id_permiso = rp.id_permiso
WHERE rp.id_rol = 2
  AND UPPER(p.modulo) = 'MENSUALIDADES'
ORDER BY p.id_permiso;


-- ============================================================
-- FIN DE ACTUALIZACIÓN
-- ============================================================