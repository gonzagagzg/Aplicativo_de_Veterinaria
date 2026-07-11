--Ver BDs
SELECT datname AS base_de_datos 
FROM pg_database 
WHERE datistemplate = false;

CREATE DATABASE aplicacion_veterinaria;

--ver tablas de BD
SELECT table_name AS tabla
FROM information_schema.tables
WHERE table_schema = 'public' 
  AND table_type = 'BASE TABLE'
ORDER BY table_name;

/*
 * Sistemas Multiempresa o SaaS
El sistema está diseñado para e muchas Empresas 
compartan la misma base de datos si usaras números simples (1, 2, 3), 
la Empresa A tendría al Cliente 1 y la Empresa B tendría al Cliente 2.
Si en el futuro necesitas mover datos, sincronizar sucursales offline o 
fusionar información, los números chocarían de inmediato. Los UUID son 
únicos en todo el universo; jamás se van a repetir entre empresas.*/


-- Activar extensión para la generación automática de UUIDs
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =========================================================================
-- 1. TABLAS MAESTRAS GLOBALES (No dependen de Empresa)
-- =========================================================================

CREATE TABLE sri_iva (
    id_iva SERIAL PRIMARY KEY,
    porcentaje NUMERIC(5,2) NOT NULL,
    codigo_sri VARCHAR(10) 
);

CREATE TABLE especie (
    id_especie SERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE raza (
    id_raza SERIAL PRIMARY KEY,
    id_especie INTEGER NOT NULL,
    nombre VARCHAR(50) NOT NULL,
    CONSTRAINT fk_raza_especie FOREIGN KEY (id_especie) REFERENCES especie(id_especie) ON DELETE RESTRICT
);

CREATE TABLE vacuna (
    id_vacuna SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE
);

-- =========================================================================
-- 2. SEGURIDAD Y ROLES (RBAC)
-- =========================================================================

CREATE TABLE rol (
    id_rol SERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE permiso (
    id_permiso SERIAL PRIMARY KEY,
    modulo VARCHAR(50) NOT NULL,
    accion VARCHAR(50) NOT NULL,
    CONSTRAINT uq_modulo_accion UNIQUE (modulo, accion)
);

CREATE TABLE rol_permiso (
    id_rol INTEGER NOT NULL,
    id_permiso INTEGER NOT NULL,
    PRIMARY KEY (id_rol, id_permiso),
    CONSTRAINT fk_rp_rol FOREIGN KEY (id_rol) REFERENCES rol(id_rol) ON DELETE CASCADE,
    CONSTRAINT fk_rp_permiso FOREIGN KEY (id_permiso) REFERENCES permiso(id_permiso) ON DELETE CASCADE
);

-- =========================================================================
-- 3. MULTIEMPRESA (Eje Central SaaS)
-- =========================================================================

CREATE TABLE empresa (
    id_empresa UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    ruc VARCHAR(13) NOT NULL UNIQUE,
    razon_social VARCHAR(150) NOT NULL,
    direccion VARCHAR(255) NOT NULL,
    activo BOOLEAN DEFAULT TRUE NOT NULL
);

-- =========================================================================
-- 4. USUARIOS, PERSONAL Y CLIENTES
-- =========================================================================

CREATE TABLE usuario (
    id_usuario UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    id_empresa UUID NOT NULL,
    id_rol INTEGER NOT NULL,
    usuario VARCHAR(50) NOT NULL,
    clave_hash VARCHAR(255) NOT NULL,
    nombres VARCHAR(100) NOT NULL,
    activo BOOLEAN DEFAULT TRUE NOT NULL,
    CONSTRAINT uq_usuario_empresa UNIQUE (id_empresa, usuario), -- El usuario es único por empresa
    CONSTRAINT fk_usuario_empresa FOREIGN KEY (id_empresa) REFERENCES empresa(id_empresa) ON DELETE RESTRICT,
    CONSTRAINT fk_usuario_rol FOREIGN KEY (id_rol) REFERENCES rol(id_rol) ON DELETE RESTRICT
);

CREATE TABLE veterinario (
    id_veterinario UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    id_usuario UUID NOT NULL UNIQUE, -- Relación 1 a 1
    id_empresa UUID NOT NULL,
    especialidad VARCHAR(100),
    CONSTRAINT fk_vet_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_vet_empresa FOREIGN KEY (id_empresa) REFERENCES empresa(id_empresa) ON DELETE RESTRICT
);

CREATE TABLE cliente (
    id_cliente UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    id_empresa UUID NOT NULL,
    identificacion VARCHAR(20) NOT NULL,
    nombres VARCHAR(150) NOT NULL,
    CONSTRAINT uq_cliente_empresa UNIQUE (id_empresa, identificacion),
    CONSTRAINT fk_cliente_empresa FOREIGN KEY (id_empresa) REFERENCES empresa(id_empresa) ON DELETE RESTRICT
);

-- =========================================================================
-- 5. MÓDULO CLÍNICO MÍNIMO
-- =========================================================================

CREATE TABLE mascota (
    id_mascota UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    id_empresa UUID NOT NULL,
    id_cliente UUID NOT NULL,
    id_raza INTEGER NOT NULL,
    nombre VARCHAR(50) NOT NULL,
    fecha_nacimiento DATE,
    CONSTRAINT fk_mascota_empresa FOREIGN KEY (id_empresa) REFERENCES empresa(id_empresa) ON DELETE RESTRICT,
    CONSTRAINT fk_mascota_cliente FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente) ON DELETE RESTRICT,
    CONSTRAINT fk_mascota_raza FOREIGN KEY (id_raza) REFERENCES raza(id_raza) ON DELETE RESTRICT
);

CREATE TABLE mascota_vacuna (
    id_mascota_vacuna UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    id_empresa UUID NOT NULL,
    id_mascota UUID NOT NULL,
    id_vacuna INTEGER NOT NULL,
    fecha_aplicacion DATE NOT NULL,
    CONSTRAINT fk_mv_empresa FOREIGN KEY (id_empresa) REFERENCES empresa(id_empresa) ON DELETE RESTRICT,
    CONSTRAINT fk_mv_mascota FOREIGN KEY (id_mascota) REFERENCES mascota(id_mascota) ON DELETE CASCADE,
    CONSTRAINT fk_mv_vacuna FOREIGN KEY (id_vacuna) REFERENCES vacuna(id_vacuna) ON DELETE RESTRICT
);

CREATE TABLE cita (
    id_cita UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    id_empresa UUID NOT NULL,
    id_mascota UUID NOT NULL,
    id_veterinario UUID NOT NULL,
    fecha_hora TIMESTAMP WITH TIME ZONE NOT NULL,
    estado VARCHAR(20) DEFAULT 'Pendiente' NOT NULL, -- P.ej: Pendiente, Atendida, Cancelada
    CONSTRAINT fk_cita_empresa FOREIGN KEY (id_empresa) REFERENCES empresa(id_empresa) ON DELETE RESTRICT,
    CONSTRAINT fk_cita_mascota FOREIGN KEY (id_mascota) REFERENCES mascota(id_mascota) ON DELETE RESTRICT,
    CONSTRAINT fk_cita_vet FOREIGN KEY (id_veterinario) REFERENCES veterinario(id_veterinario) ON DELETE RESTRICT
);

CREATE TABLE historial_clinico (
    id_historial UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    id_empresa UUID NOT NULL,
    id_cita UUID NOT NULL UNIQUE, -- Relación 1 a 1 originada por cita
    peso_kg NUMERIC(6,2),
    temperatura_c NUMERIC(4,1),
    anamnesis TEXT,
    diagnostico TEXT,
    CONSTRAINT fk_hc_empresa FOREIGN KEY (id_empresa) REFERENCES empresa(id_empresa) ON DELETE RESTRICT,
    CONSTRAINT fk_hc_cita FOREIGN KEY (id_cita) REFERENCES cita(id_cita) ON DELETE RESTRICT
);

-- =========================================================================
-- 6. MÓDULO INVENTARIO Y PRODUCTOS
-- =========================================================================

CREATE TABLE categoria (
    id_categoria SERIAL PRIMARY KEY,
    id_empresa UUID NOT NULL,
    nombre VARCHAR(50) NOT NULL,
    CONSTRAINT uq_cat_empresa UNIQUE (id_empresa, nombre),
    CONSTRAINT fk_cat_empresa FOREIGN KEY (id_empresa) REFERENCES empresa(id_empresa) ON DELETE RESTRICT
);

CREATE TABLE producto (
    id_producto UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    id_empresa UUID NOT NULL,
    id_categoria INTEGER NOT NULL,
    id_iva INTEGER NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    precio_unitario NUMERIC(10,2) NOT NULL,
    stock_actual INTEGER DEFAULT 0 NOT NULL,
    stock_minimo INTEGER DEFAULT 0 NOT NULL,
    fecha_caducidad DATE,
    CONSTRAINT fk_prod_empresa FOREIGN KEY (id_empresa) REFERENCES empresa(id_empresa) ON DELETE RESTRICT,
    CONSTRAINT fk_prod_categoria FOREIGN KEY (id_categoria) REFERENCES categoria(id_categoria) ON DELETE RESTRICT,
    CONSTRAINT fk_prod_iva FOREIGN KEY (id_iva) REFERENCES sri_iva(id_iva) ON DELETE RESTRICT
);

-- Se crean las recetas clínicas tras conocer los productos
CREATE TABLE receta (
    id_receta UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    id_empresa UUID NOT NULL,
    id_historial UUID NOT NULL UNIQUE, -- Relación 1 a 1 con historial
    indicaciones_generales TEXT,
    CONSTRAINT fk_receta_empresa FOREIGN KEY (id_empresa) REFERENCES empresa(id_empresa) ON DELETE RESTRICT,
    CONSTRAINT fk_receta_hc FOREIGN KEY (id_historial) REFERENCES historial_clinico(id_historial) ON DELETE RESTRICT
);

CREATE TABLE receta_detalle (
    id_detalle_receta UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    id_receta UUID NOT NULL,
    id_producto UUID NOT NULL,
    dosis VARCHAR(100) NOT NULL,
    frecuencia VARCHAR(100) NOT NULL,
    duracion_dias INTEGER NOT NULL,
    CONSTRAINT fk_rd_receta FOREIGN KEY (id_receta) REFERENCES receta(id_receta) ON DELETE CASCADE,
    CONSTRAINT fk_rd_producto FOREIGN KEY (id_producto) REFERENCES producto(id_producto) ON DELETE RESTRICT
);

-- =========================================================================
-- 7. MÓDULO FACTURACIÓN Y AUDITORÍA
-- =========================================================================

CREATE TABLE factura (
    id_factura UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    id_empresa UUID NOT NULL,
    id_cliente UUID NOT NULL,
    id_usuario UUID NOT NULL, -- Usuario que cobra/emite
    total NUMERIC(10,2) DEFAULT 0.00 NOT NULL,
    estado VARCHAR(20) DEFAULT 'Emitida' NOT NULL, -- Emitida, Anulada
    fecha TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_fact_empresa FOREIGN KEY (id_empresa) REFERENCES empresa(id_empresa) ON DELETE RESTRICT,
    CONSTRAINT fk_fact_cliente FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente) ON DELETE RESTRICT,
    CONSTRAINT fk_fact_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE RESTRICT
);

CREATE TABLE factura_detalle (
    id_detalle UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    id_factura UUID NOT NULL,
    id_producto UUID NOT NULL,
    id_iva INTEGER NOT NULL, -- Se guarda histórico del IVA aplicado al momento de vender
    cantidad INTEGER NOT NULL,
    precio_unitario NUMERIC(10,2) NOT NULL,
    subtotal NUMERIC(10,2) NOT NULL,
    CONSTRAINT fk_fd_factura FOREIGN KEY (id_factura) REFERENCES factura(id_factura) ON DELETE CASCADE,
    CONSTRAINT fk_fd_producto FOREIGN KEY (id_producto) REFERENCES producto(id_producto) ON DELETE RESTRICT,
    CONSTRAINT fk_fd_iva FOREIGN KEY (id_iva) REFERENCES sri_iva(id_iva) ON DELETE RESTRICT
);

CREATE TABLE movimiento_inventario (
    id_movimiento UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    id_empresa UUID NOT NULL,
    id_producto UUID NOT NULL,
    id_factura UUID, -- Opcional: Puede ser nulo si es un ajuste manual o ingreso por proveedor
    tipo VARCHAR(20) NOT NULL, -- P.ej: 'Ingreso', 'Egreso', 'Venta', 'Ajuste'
    cantidad INTEGER NOT NULL,
    fecha TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_mov_empresa FOREIGN KEY (id_empresa) REFERENCES empresa(id_empresa) ON DELETE RESTRICT,
    CONSTRAINT fk_mov_producto FOREIGN KEY (id_producto) REFERENCES producto(id_producto) ON DELETE CASCADE,
    CONSTRAINT fk_mov_factura FOREIGN KEY (id_factura) REFERENCES factura(id_factura) ON DELETE SET NULL
);

-- =========================================================================
-- 8. ÍNDICES DE RENDIMIENTO 
-- =========================================================================
CREATE INDEX idx_usuario_empresa ON usuario(id_empresa);
CREATE INDEX idx_cliente_empresa ON cliente(id_empresa);
CREATE INDEX idx_mascota_empresa ON mascota(id_empresa);
CREATE INDEX idx_producto_empresa ON producto(id_empresa);
CREATE INDEX idx_factura_empresa ON factura(id_empresa);
CREATE INDEX idx_movimiento_producto ON movimiento_inventario(id_producto);


-- =========================================================================
-- DATOS DE PRUEBA PARA EL APLICATIVO
-- =========================================================================
-- =========================================================================
-- 1. TABLAS MAESTRAS GLOBALES
-- =========================================================================

INSERT INTO sri_iva (porcentaje, codigo_sri) VALUES
(0.00, '0'),
(15.00, '4'), -- IVA 15% (Ecuador actual)
(5.00, '5');

INSERT INTO especie (nombre) VALUES
('Canina'),
('Felina'),
('Ave'),
('Roedor'),
('Reptil');

INSERT INTO raza (id_especie, nombre) VALUES
(1, 'Golden Retriever'),
(1, 'Pastor Alemán'),
(2, 'Siamés'),
(2, 'Persa'),
(3, 'Canario');

INSERT INTO vacuna (nombre) VALUES
('Antirrábica'),
('Parvovirus'),
('Triple Felina'),
('Quíntuple Canina'),
('Leucemia Felina');

-- =========================================================================
-- 2. SEGURIDAD Y ROLES 
-- =========================================================================

INSERT INTO rol (nombre) VALUES
('Administrador Global'),
('Administrador Local'),
('Veterinario'),
('Recepcionista'),
('Asistente Clínico');

INSERT INTO permiso (modulo, accion) VALUES
('Usuarios', 'Crear'),
('Mascotas', 'Ver'),
('Historial', 'Editar'),
('Facturas', 'Emitir'),
('Inventario', 'Ajustar');

INSERT INTO rol_permiso (id_rol, id_permiso) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), -- Admin Global tiene todo
(3, 2), (3, 3),                         -- Veterinario: Ver mascotas y Editar historial
(4, 2), (4, 4);                         -- Recepcionista: Ver mascotas y Emitir facturas

-- =========================================================================
-- 3. MULTIEMPRESA 
-- =========================================================================

-- Creamos variables simuladas para los IDs principales usando CTEs en cascada
-- Para que el script corra directo, insertamos con UUIDs fijos predecibles creados al azar:

INSERT INTO empresa (id_empresa, ruc, razon_social, direccion) VALUES
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '1792457812001', 'Corporación VetCare S.A.', 'Av. Amazonas y Shyris, Quito'),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', '0992345678001', 'PetSmile S.A.', 'Av. Carlos Julio Arosemena, Guayaquil'),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', '0102345678001', 'Clínica Veterinaria El Establo', 'Calle Larga, Cuenca'),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a44', '1891234567001', 'Huellas Clinic', 'Av. Cevallos, Ambato'),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a55', '1391234567001', 'Dany Pets', 'Malecón Tarqui, Manta');

-- =========================================================================
-- 4. USUARIOS, PERSONAL Y CLIENTES
-- =========================================================================

INSERT INTO usuario (id_usuario, id_empresa, id_rol, usuario, clave_hash, nombres) VALUES
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b11', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 2, 'admin_quito', '$2b$12$ExampleHash1...', 'Carlos Mendoza'),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b22', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 3, 'doc_juarez', '$2b$12$ExampleHash2...', 'Ana Juárez'),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b33', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 2, 'admin_gye', '$2b$12$ExampleHash3...', 'Pedro Morales'),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b44', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 3, 'doc_silva', '$2b$12$ExampleHash4...', 'Luis Silva'),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b55', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 4, 'recep_lucia', '$2b$12$ExampleHash5...', 'Lucía Torres');

INSERT INTO veterinario (id_veterinario, id_usuario, id_empresa, especialidad) VALUES
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c11', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b22', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Cirugía de Tejidos Blandos'),
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c22', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b44', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'Dermatología Felina'),
-- Generamos otros 3 aleatorios vinculados a los administradores para llenar la tabla (fines didácticos)
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c33', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b11', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Medicina General'),
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c44', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b33', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'Cardiología'),
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c55', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b55', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Nutrición Animal');

INSERT INTO cliente (id_cliente, id_empresa, identificacion, nombres) VALUES
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380d11', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '1723456789', 'Juan Pérez'),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380d22', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '1755543210', 'María Flores'),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380d33', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', '0912345678', 'Roberto Gómez'),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380d44', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', '0928765432', 'Elena Castro'),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380d55', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', '0104567890', 'Diego Andrade');

-- =========================================================================
-- 5. MÓDULO CLÍNICO
-- =========================================================================

INSERT INTO mascota (id_mascota, id_empresa, id_cliente, id_raza, nombre, fecha_nacimiento) VALUES
('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e11', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380d11', 1, 'Max', '2021-05-10'),
('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e22', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380d22', 3, 'Luna', '2022-11-18'),
('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e33', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380d33', 2, 'Rocky', '2019-02-03'),
('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e44', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380d44', 4, 'Mimi', '2023-01-25'),
('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e55', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380d11', 1, 'Toby', '2024-06-14');

INSERT INTO mascota_vacuna (id_empresa, id_mascota, id_vacuna, fecha_aplicacion) VALUES
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e11', 1, '2025-01-10'),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e11', 4, '2025-02-10'),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e22', 3, '2025-03-15'),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e33', 1, '2025-01-20'),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e44', 5, '2025-04-02');

INSERT INTO cita (id_cita, id_empresa, id_mascota, id_veterinario, fecha_hora, estado) VALUES
('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380f11', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e11', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c11', '2026-07-10 09:30:00 -05', 'Atendida'),
('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380f22', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e22', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c11', '2026-07-11 10:00:00 -05', 'Atendida'),
('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380f33', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e33', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c22', '2026-07-12 14:00:00 -05', 'Pendiente'),
('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380f44', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e44', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c22', '2026-07-12 15:00:00 -05', 'Pendiente'),
('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380f55', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380e55', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380c33', '2026-07-13 11:30:00 -05', 'Pendiente');

INSERT INTO historial_clinico (id_historial, id_empresa, id_cita, peso_kg, temperatura_c, anamnesis, diagnostico) VALUES
('10eebc99-9c0b-4ef8-bb6d-6bb9bd380111', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'f0eebc99-9c0b-4ef8-bb6d-6bb9bd380f11', 25.50, 38.5, 'Presenta vómito y decaimiento desde hace 24 horas.', 'Gastroenteritis bacteriana leve.'),
('20eebc99-9c0b-4ef8-bb6d-6bb9bd380222', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'f0eebc99-9c0b-4ef8-bb6d-6bb9bd380f22', 4.20, 39.1, 'Prurito excesivo en la zona de las orejas y descamación.', 'Otitis externa por ácaros.'),
-- Agregamos tres registros dummy en base a IDs manuales para cumplir la cuota sin romper el UNIQUE en id_cita
('30eebc99-9c0b-4ef8-bb6d-6bb9bd380333', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'f0eebc99-9c0b-4ef8-bb6d-6bb9bd380f33', 12.00, 38.2, 'Control rutinario anual.', 'Mascota sana clínicamente.'),
('40eebc99-9c0b-4ef8-bb6d-6bb9bd380444', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'f0eebc99-9c0b-4ef8-bb6d-6bb9bd380f44', 3.10, 38.8, 'Cachorro para primera evaluación.', 'Sano, se sugiere plan vacunal.'),
('50eebc99-9c0b-4ef8-bb6d-6bb9bd380555', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'f0eebc99-9c0b-4ef8-bb6d-6bb9bd380f55', 8.40, 37.9, 'Cojera en miembro posterior izquierdo.', 'Sospecha de luxación patelar.');

-- =========================================================================
-- 6. MÓDULO INVENTARIO Y PRODUCTOS
-- =========================================================================

INSERT INTO categoria (id_categoria, id_empresa, nombre) VALUES
(1, 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Fármacos y Antibióticos'),
(2, 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Alimentos Premium'),
(3, 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'Accesorios y Juguetes'),
(4, 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'Antiparasitarios'),
(5, 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'Estética Canina');

INSERT INTO producto (id_producto, id_empresa, id_categoria, id_iva, nombre, precio_unitario, stock_actual, stock_minimo, fecha_caducidad) VALUES
('60eebc99-9c0b-4ef8-bb6d-6bb9bd380611', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 1, 2, 'Apoquel 5.4mg x 20 tabletas', 45.00, 15, 3, '2027-12-31'),
('60eebc99-9c0b-4ef8-bb6d-6bb9bd380622', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 2, 1, 'Hills Metabolic Canino 3kg', 38.50, 8, 2, '2026-10-15'),
('60eebc99-9c0b-4ef8-bb6d-6bb9bd380633', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 4, 2, 'Bravecto Perros 10-20kg', 32.00, 24, 5, '2028-04-20'),
('60eebc99-9c0b-4ef8-bb6d-6bb9bd380644', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 3, 2, 'Collar Reflectivo Ajustable', 8.50, 50, 10, NULL),
('60eebc99-9c0b-4ef8-bb6d-6bb9bd380655', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 1, 2, 'Amoxicilina Suspensión Oral', 12.00, 10, 4, '2026-09-01');

INSERT INTO receta (id_receta, id_empresa, id_historial, indicaciones_generales) VALUES
('70eebc99-9c0b-4ef8-bb6d-6bb9bd380711', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '10eebc99-9c0b-4ef8-bb6d-6bb9bd380111', 'Mantener en reposo, dar agua hervida a libre acceso.'),
('70eebc99-9c0b-4ef8-bb6d-6bb9bd380722', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '20eebc99-9c0b-4ef8-bb6d-6bb9bd380222', 'Limpiar el pabellón auricular antes de aplicar el fármaco.'),
('70eebc99-9c0b-4ef8-bb6d-6bb9bd380733', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', '30eebc99-9c0b-4ef8-bb6d-6bb9bd380333', 'Continuar con dieta habitual basada en croquetas premium.'),
('70eebc99-9c0b-4ef8-bb6d-6bb9bd380744', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', '40eebc99-9c0b-4ef8-bb6d-6bb9bd380444', 'Evitar contacto con otras mascotas no vacunadas.'),
('70eebc99-9c0b-4ef8-bb6d-6bb9bd380755', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '50eebc99-9c0b-4ef8-bb6d-6bb9bd380555', 'Restringir saltos y ejercicios bruscos.');

INSERT INTO receta_detalle (id_receta, id_producto, dosis, frecuencia, duracion_dias) VALUES
('70eebc99-9c0b-4ef8-bb6d-6bb9bd380711', '60eebc99-9c0b-4ef8-bb6d-6bb9bd380655', '2.5 ml', 'Cada 12 horas', 7),
('70eebc99-9c0b-4ef8-bb6d-6bb9bd380722', '60eebc99-9c0b-4ef8-bb6d-6bb9bd380611', '1/2 tableta', 'Cada 24 horas', 10),
('70eebc99-9c0b-4ef8-bb6d-6bb9bd380733', '60eebc99-9c0b-4ef8-bb6d-6bb9bd380622', '200 gramos', 'Cada 8 horas', 30),
('70eebc99-9c0b-4ef8-bb6d-6bb9bd380744', '60eebc99-9c0b-4ef8-bb6d-6bb9bd380633', '1 tableta masticable', 'Dosis única', 1),
('70eebc99-9c0b-4ef8-bb6d-6bb9bd380755', '60eebc99-9c0b-4ef8-bb6d-6bb9bd380655', '1.5 ml', 'Cada 24 horas', 5);

-- =========================================================================
-- 7. MÓDULO FACTURACIÓN Y AUDITORÍA
-- =========================================================================

INSERT INTO factura (id_factura, id_empresa, id_cliente, id_usuario, total, estado) VALUES
('80eebc99-9c0b-4ef8-bb6d-6bb9bd380811', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380d11', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b55', 57.00, 'Emitida'),
('80eebc99-9c0b-4ef8-bb6d-6bb9bd380822', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380d22', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b55', 38.50, 'Emitida'),
('80eebc99-9c0b-4ef8-bb6d-6bb9bd380833', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380d33', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b33', 32.00, 'Emitida'),
('80eebc99-9c0b-4ef8-bb6d-6bb9bd380844', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380d44', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b33', 17.00, 'Anulada'),
('80eebc99-9c0b-4ef8-bb6d-6bb9bd380855', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380d11', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380b55', 45.00, 'Emitida');

INSERT INTO factura_detalle (id_factura, id_producto, id_iva, cantidad, precio_unitario, subtotal) VALUES
('80eebc99-9c0b-4ef8-bb6d-6bb9bd380811', '60eebc99-9c0b-4ef8-bb6d-6bb9bd380655', 2, 1, 12.00, 12.00),
('80eebc99-9c0b-4ef8-bb6d-6bb9bd380811', '60eebc99-9c0b-4ef8-bb6d-6bb9bd380611', 2, 1, 45.00, 45.00),
('80eebc99-9c0b-4ef8-bb6d-6bb9bd380822', '60eebc99-9c0b-4ef8-bb6d-6bb9bd380622', 1, 1, 38.50, 38.50),
('80eebc99-9c0b-4ef8-bb6d-6bb9bd380833', '60eebc99-9c0b-4ef8-bb6d-6bb9bd380633', 2, 1, 32.00, 32.00),
('80eebc99-9c0b-4ef8-bb6d-6bb9bd380844', '60eebc99-9c0b-4ef8-bb6d-6bb9bd380644', 2, 2, 8.50, 17.00);

INSERT INTO movimiento_inventario (id_empresa, id_producto, id_factura, tipo, cantidad) VALUES
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '60eebc99-9c0b-4ef8-bb6d-6bb9bd380655', '80eebc99-9c0b-4ef8-bb6d-6bb9bd380811', 'Venta', 1),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '60eebc99-9c0b-4ef8-bb6d-6bb9bd380611', '80eebc99-9c0b-4ef8-bb6d-6bb9bd380811', 'Venta', 1),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '60eebc99-9c0b-4ef8-bb6d-6bb9bd380622', '80eebc99-9c0b-4ef8-bb6d-6bb9bd380822', 'Venta', 1),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', '60eebc99-9c0b-4ef8-bb6d-6bb9bd380633', '80eebc99-9c0b-4ef8-bb6d-6bb9bd380833', 'Venta', 1),
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '60eebc99-9c0b-4ef8-bb6d-6bb9bd380655', NULL, 'Ingreso', 50); 



--ver tablas de BD
SELECT table_name AS tabla
FROM information_schema.tables
WHERE table_schema = 'public' 
  AND table_type = 'BASE TABLE'
ORDER BY table_name;



-- =========================================================================
-- SELECT POR CADA TABLA
-- =========================================================================

-- 1. SRI_IVA
SELECT id_iva, porcentaje, codigo_sri FROM sri_iva;

-- 2. ESPECIE
SELECT id_especie, nombre FROM especie;

-- 3. RAZA
SELECT id_raza, id_especie, nombre FROM raza;

-- 4. VACUNA
SELECT id_vacuna, nombre FROM vacuna;

-- 5. ROL
SELECT id_rol, nombre FROM rol;

-- 6. PERMISO
SELECT id_permiso, modulo, accion FROM permiso;

-- 7. ROL_PERMISO
SELECT id_rol, id_permiso FROM rol_permiso;

-- 8. EMPRESA
SELECT id_empresa, ruc, razon_social, direccion, activo FROM empresa;

-- 9. USUARIO
SELECT id_usuario, id_empresa, id_rol, usuario, clave_hash, nombres, activo FROM usuario;

-- 10. VETERINARIO
SELECT id_veterinario, id_usuario, id_empresa, especialidad FROM veterinario;

-- 11. CLIENTE
SELECT id_cliente, id_empresa, identificacion, nombres FROM cliente;

-- 12. MASCOTA
SELECT id_mascota, id_empresa, id_cliente, id_raza, nombre, fecha_nacimiento FROM mascota;

-- 13. MASCOTA_VACUNA
SELECT id_mascota_vacuna, id_empresa, id_mascota, id_vacuna, fecha_aplicacion FROM mascota_vacuna;

-- 14. CITA
SELECT id_cita, id_empresa, id_mascota, id_veterinario, fecha_hora, estado FROM cita;

-- 15. HISTORIAL_CLINICO
SELECT id_historial, id_empresa, id_cita, peso_kg, temperatura_c, anamnesis, diagnostico FROM historial_clinico;

-- 16. RECETA
SELECT id_receta, id_empresa, id_historial, indicaciones_generales FROM receta;

-- 17. RECETA_DETALLE
SELECT id_detalle_receta, id_receta, id_producto, dosis, frecuencia, duracion_dias FROM receta_detalle;

-- 18. CATEGORIA
SELECT id_categoria, id_empresa, nombre FROM categoria;

-- 19. PRODUCTO
SELECT id_producto, id_empresa, id_categoria, id_iva, nombre, precio_unitario, stock_actual, stock_minimo, fecha_caducidad FROM producto;

-- 20. FACTURA
SELECT id_factura, id_empresa, id_cliente, id_usuario, total, estado, fecha FROM factura;

-- 21. FACTURA_DETALLE
SELECT id_detalle, id_factura, id_producto, id_iva, cantidad, precio_unitario, subtotal FROM factura_detalle;

-- 22. MOVIMIENTO_INVENTARIO
SELECT id_movimiento, id_empresa, id_producto, id_factura, tipo, cantidad, fecha FROM movimiento_inventario;


-- =========================================================================
-- SELECT PARA RELACIONAR DATOS DE FORMA FÁCIL
-- =========================================================================

-- Listar las razas junto con el nombre de su especie (JOIN)
SELECT r.id_raza, r.nombre AS raza, e.nombre AS especie 
FROM raza r
INNER JOIN especie e ON r.id_especie = e.id_especie;

--Seguridad: Usuarios con sus Roles y Permisos

SELECT 
    u.usuario,
    u.nombres AS nombre_usuario,
    r.nombre AS rol_asignado,
    p.modulo,
    p.accion
FROM usuario u
INNER JOIN rol r ON u.id_rol = r.id_rol
INNER JOIN rol_permiso rp ON r.id_rol = rp.id_rol
INNER JOIN permiso p ON rp.id_permiso = p.id_permiso
WHERE u.id_empresa = 'Inserta la empresa';


--Clínico: Expediente Completo de la Mascota

SELECT 
    m.id_mascota,
    m.nombre AS nombre_mascota,
    m.fecha_nacimiento,
    c.nombres AS nombre_dueno,
    c.identificacion AS cedula_dueno,
    r.nombre AS raza,
    e.nombre AS especie
FROM mascota m
INNER JOIN cliente c ON m.id_cliente = c.id_cliente
INNER JOIN raza r ON m.id_raza = r.id_raza
INNER JOIN especie e ON r.id_especie = e.id_especie
WHERE m.id_empresa = 'Inserta la empresa';

--Citas: Agenda de Atenciones
SELECT 
    c.id_cita,
    c.fecha_hora,
    c.estado AS estado_cita,
    m.nombre AS nombre_mascota,
    uv.nombres AS nombre_veterinario,
    v.especialidad
FROM cita c
INNER JOIN mascota m ON c.id_mascota = m.id_mascota
INNER JOIN veterinario v ON c.id_veterinario = v.id_veterinario
INNER JOIN usuario uv ON v.id_usuario = uv.id_usuario
WHERE c.id_empresa = 'inserta la empresa'
ORDER BY c.fecha_hora ASC;

--Citas: Agenda de Atenciones

SELECT 
    c.id_cita,
    c.fecha_hora,
    c.estado AS estado_cita,
    m.nombre AS nombre_mascota,
    uv.nombres AS nombre_veterinario,
    v.especialidad
FROM cita c
INNER JOIN mascota m ON c.id_mascota = m.id_mascota
INNER JOIN veterinario v ON c.id_veterinario = v.id_veterinario
INNER JOIN usuario uv ON v.id_usuario = uv.id_usuario
WHERE c.id_empresa = 'inserta la empresa'
ORDER BY c.fecha_hora ASC;


