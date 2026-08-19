package com.itq.service;

import com.itq.config.ConexionBD;
import com.itq.dao.EmpresaDAO;
import com.itq.dto.EmpresaConAdminRequest;
import com.itq.model.Empresa;
import com.itq.util.PasswordUtil;
import com.itq.validation.EcuadorValidator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class EmpresaService {

    private final EmpresaDAO dao =
            new EmpresaDAO();

    // =========================================================
    // LISTAR
    // =========================================================

    public List<Empresa> listar()
            throws SQLException {

        return dao.listar();
    }

    // =========================================================
    // BUSCAR POR ID
    // =========================================================

    public Optional<Empresa> buscarPorId(
            UUID idEmpresa
    ) throws SQLException {

        if (idEmpresa == null) {

            throw new IllegalArgumentException(
                    "La empresa es obligatoria"
            );
        }

        return dao.buscarPorId(
                idEmpresa
        );
    }

    // =========================================================
    // CREAR
    // =========================================================

    public Empresa crear(
            Empresa obj
    ) throws SQLException {

        if (obj == null) {

            throw new IllegalArgumentException(
                    "Los datos de la empresa son obligatorios"
            );
        }

        /*
         * Toda veterinaria nueva queda activa
         * por defecto.
         */
        if (obj.isActivo() == null) {

            obj.setActivo(true);
        }

        validar(obj);

        return dao.insertar(obj);
    }

    // =========================================================
    // CREAR CON ADMINISTRADOR
    // =========================================================

    /*
     * Alta completa de una veterinaria: crea la empresa y su usuario
     * administrador ("Administrador Local") en UNA transacción, de modo
     * que si falla la creación del usuario no queda la empresa huérfana.
     */
    public Empresa crearConAdmin(
            EmpresaConAdminRequest request
    ) throws SQLException {

        if (request == null) {

            throw new IllegalArgumentException(
                    "Los datos de la empresa son obligatorios"
            );
        }

        Empresa empresa =
                new Empresa();

        empresa.setRuc(
                request.getRuc()
        );

        empresa.setRazonSocial(
                request.getRazonSocial()
        );

        empresa.setDireccion(
                request.getDireccion()
        );

        empresa.setActivo(
                request.getActivo()
        );

        /*
         * Toda veterinaria nueva queda activa
         * por defecto.
         */
        if (empresa.isActivo() == null) {

            empresa.setActivo(true);
        }

        validar(empresa);

        validarAdmin(request);

        try (Connection cn =
                     ConexionBD.obtenerConexion()) {

            cn.setAutoCommit(false);

            try {

                UUID idEmpresa =
                        insertarEmpresa(
                                cn,
                                empresa
                        );

                empresa.setIdEmpresa(
                        idEmpresa
                );

                Integer idRolAdmin =
                        buscarRolAdministrador(cn);

                if (idRolAdmin == null) {

                    throw new IllegalArgumentException(
                            "No se encontró el rol de administrador en la base de datos"
                    );
                }

                String hash =
                        PasswordUtil.hash(
                                request.getAdminContrasena()
                                        .trim()
                        );

                insertarUsuarioAdmin(
                        cn,
                        idEmpresa,
                        idRolAdmin,
                        request,
                        hash
                );

                cn.commit();

                return empresa;

            } catch (Exception e) {

                try {
                    cn.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(
                            rollbackError
                    );
                }

                if (e instanceof IllegalArgumentException) {
                    throw (IllegalArgumentException) e;
                }

                if (e instanceof SecurityException) {
                    throw (SecurityException) e;
                }

                if (e instanceof SQLException) {
                    throw (SQLException) e;
                }

                throw new SQLException(
                        "No se pudo registrar la veterinaria",
                        e
                );

            } finally {

                try {
                    cn.setAutoCommit(true);
                } catch (SQLException ignored) {
                }
            }
        }
    }

    // =========================================================
    // ACTUALIZAR
    // =========================================================

    public boolean actualizar(
            Empresa obj
    ) throws SQLException {

        if (obj == null ||
                obj.getIdEmpresa() == null) {

            throw new IllegalArgumentException(
                    "Los datos de la empresa son obligatorios"
            );
        }

        validar(obj);

        return dao.actualizar(obj);
    }

    // =========================================================
    // ACTIVAR
    // =========================================================

    public boolean activar(
            UUID idEmpresa
    ) throws SQLException {

        if (idEmpresa == null) {

            throw new IllegalArgumentException(
                    "La empresa es obligatoria"
            );
        }

        return dao.cambiarEstado(
                idEmpresa,
                true
        );
    }

    // =========================================================
    // DESACTIVAR
    // =========================================================

    public boolean desactivar(
            UUID idEmpresa
    ) throws SQLException {

        if (idEmpresa == null) {

            throw new IllegalArgumentException(
                    "La empresa es obligatoria"
            );
        }

        return dao.cambiarEstado(
                idEmpresa,
                false
        );
    }

    // =========================================================
    // VALIDACIONES
    // =========================================================

    private void validar(
            Empresa obj
    ) {

        // -----------------------------------------------------
        // RUC obligatorio
        // -----------------------------------------------------

        if (vacio(obj.getRuc())) {

            throw new IllegalArgumentException(
                    "El RUC es obligatorio"
            );
        }

        /*
         * Limpiamos espacios, guiones u otros caracteres
         * antes de validar.
         *
         * Ejemplo:
         * 1792457812-001
         * se convierte a:
         * 1792457812001
         */
        String rucLimpio =
                EcuadorValidator.limpiarNumero(
                        obj.getRuc()
                );

        if (rucLimpio == null ||
                rucLimpio.length() != 13) {

            throw new IllegalArgumentException(
                    "El RUC debe contener 13 dígitos"
            );
        }

        /*
         * Validación ecuatoriana:
         * - provincia
         * - tercer dígito
         * - dígito verificador
         * - establecimiento
         */
        if (!EcuadorValidator.rucValido(
                rucLimpio
        )) {

            throw new IllegalArgumentException(
                    "El RUC ecuatoriano no es válido"
            );
        }

        // Guardamos siempre el RUC normalizado.
        obj.setRuc(
                rucLimpio
        );

        // -----------------------------------------------------
        // RAZÓN SOCIAL
        // -----------------------------------------------------

        if (vacio(
                obj.getRazonSocial()
        )) {

            throw new IllegalArgumentException(
                    "La razón social es obligatoria"
            );
        }

        // -----------------------------------------------------
        // DIRECCIÓN
        // -----------------------------------------------------

        if (vacio(
                obj.getDireccion()
        )) {

            throw new IllegalArgumentException(
                    "La dirección es obligatoria"
            );
        }

        // -----------------------------------------------------
        // ESTADO
        // -----------------------------------------------------

        if (obj.isActivo() == null) {

            throw new IllegalArgumentException(
                    "El estado activo es obligatorio"
            );
        }

        // -----------------------------------------------------
        // NORMALIZACIÓN
        // -----------------------------------------------------

        obj.setRazonSocial(
                obj.getRazonSocial()
                        .trim()
        );

        obj.setDireccion(
                obj.getDireccion()
                        .trim()
        );
    }

    // =========================================================
    // VALIDAR ADMINISTRADOR
    // =========================================================

    private void validarAdmin(
            EmpresaConAdminRequest request
    ) {

        if (vacio(
                request.getAdminUsuario()
        )) {

            throw new IllegalArgumentException(
                    "El usuario administrador es obligatorio"
            );
        }

        if (vacio(
                request.getAdminNombres()
        )) {

            throw new IllegalArgumentException(
                    "Los nombres del administrador son obligatorios"
            );
        }

        if (vacio(
                request.getAdminContrasena()
        )) {

            throw new IllegalArgumentException(
                    "La contraseña del administrador es obligatoria"
            );
        }

        if (request.getAdminContrasena()
                .trim()
                .length() < 6) {

            throw new IllegalArgumentException(
                    "La contraseña debe tener al menos 6 caracteres"
            );
        }
    }

    // =========================================================
    // INSERTAR EMPRESA (MISMA CONEXIÓN)
    // =========================================================

    private UUID insertarEmpresa(
            Connection cn,
            Empresa empresa
    ) throws SQLException {

        String sql = """
                INSERT INTO empresa
                (
                    ruc,
                    razon_social,
                    direccion,
                    activo
                )
                VALUES (?, ?, ?, ?)
                RETURNING id_empresa
                """;

        try (PreparedStatement ps =
                     cn.prepareStatement(sql)) {

            ps.setString(
                    1,
                    empresa.getRuc()
            );

            ps.setString(
                    2,
                    empresa.getRazonSocial()
            );

            ps.setString(
                    3,
                    empresa.getDireccion()
            );

            ps.setBoolean(
                    4,
                    empresa.isActivo()
            );

            try (ResultSet rs =
                         ps.executeQuery()) {

                if (!rs.next()) {

                    throw new SQLException(
                            "No se generó la clave primaria de la empresa"
                    );
                }

                return rs.getObject(
                        "id_empresa",
                        UUID.class
                );
            }
        }
    }

    // =========================================================
    // ROL ADMINISTRADOR DE UNA VETERINARIA
    // =========================================================

    private Integer buscarRolAdministrador(
            Connection cn
    ) throws SQLException {

        String sql = """
                SELECT id_rol
                FROM rol
                WHERE UPPER(nombre) = UPPER(?)
                """;

        try (PreparedStatement ps =
                     cn.prepareStatement(sql)) {

            ps.setString(
                    1,
                    "Administrador Local"
            );

            try (ResultSet rs =
                         ps.executeQuery()) {

                return rs.next()
                        ? (Integer) rs.getObject(
                        "id_rol"
                )
                        : null;
            }
        }
    }

    // =========================================================
    // INSERTAR USUARIO ADMINISTRADOR (MISMA CONEXIÓN)
    // =========================================================

    private void insertarUsuarioAdmin(
            Connection cn,
            UUID idEmpresa,
            Integer idRol,
            EmpresaConAdminRequest request,
            String hash
    ) throws SQLException {

        String sql = """
                INSERT INTO usuario
                (
                    id_empresa,
                    id_rol,
                    usuario,
                    clave_hash,
                    nombres,
                    activo
                )
                VALUES (?, ?, ?, ?, ?, TRUE)
                """;

        try (PreparedStatement ps =
                     cn.prepareStatement(sql)) {

            ps.setObject(
                    1,
                    idEmpresa
            );

            ps.setObject(
                    2,
                    idRol
            );

            ps.setString(
                    3,
                    request.getAdminUsuario()
                            .trim()
            );

            ps.setString(
                    4,
                    hash
            );

            ps.setString(
                    5,
                    request.getAdminNombres()
                            .trim()
            );

            if (ps.executeUpdate() != 1) {

                throw new SQLException(
                        "No se pudo crear el usuario administrador"
                );
            }
        }
    }

    // =========================================================
    // UTILIDAD
    // =========================================================

    private boolean vacio(
            String valor
    ) {

        return valor == null ||
                valor.trim().isEmpty();
    }
}