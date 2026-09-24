package com.itq.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public final class ConexionBD {

    // =========================================================
    // DATOS DE CONEXIÓN
    // =========================================================

    /*
     * La configuración se obtiene de variables de entorno.
     *
     * Cada integrante del equipo puede usar sus propias
     * credenciales locales de PostgreSQL sin modificar este
     * archivo ni subir contraseñas a GitHub.
     */
    private static final String URL =
            obtenerVariable(
                    "VET_DB_URL",
                    "jdbc:postgresql://localhost:5432/Aplicativo_de_Veterinaria"
            );

    private static final String USUARIO =
            obtenerVariable(
                    "VET_DB_USER",
                    "postgres"
            );

    /*
     * La contraseña NO tiene valor por defecto por seguridad.
     * Debe existir VET_DB_PASSWORD en la computadora donde
     * se ejecuta Tomcat.
     */
    private static final String CONTRASENA =
            obtenerVariableObligatoria(
                    "VET_DB_PASSWORD"
            );

    // =========================================================
    // POOL ÚNICO DE CONEXIONES
    // =========================================================

    private static final HikariDataSource DATA_SOURCE;

    static {

        try {

            HikariConfig config =
                    new HikariConfig();

            // -------------------------------------------------
            // CONEXIÓN
            // -------------------------------------------------

            config.setJdbcUrl(
                    URL
            );

            config.setUsername(
                    USUARIO
            );

            config.setPassword(
                    CONTRASENA
            );

            config.setDriverClassName(
                    "org.postgresql.Driver"
            );

            // -------------------------------------------------
            // NOMBRE DEL POOL
            // -------------------------------------------------

            config.setPoolName(
                    "VETERINARIAITQ-Pool"
            );

            // -------------------------------------------------
            // TAMAÑO DEL POOL
            // -------------------------------------------------

            config.setMaximumPoolSize(
                    10
            );

            config.setMinimumIdle(
                    2
            );

            // -------------------------------------------------
            // TIEMPOS
            // -------------------------------------------------

            config.setConnectionTimeout(
                    10_000
            );

            config.setIdleTimeout(
                    300_000
            );

            config.setMaxLifetime(
                    1_800_000
            );

            // -------------------------------------------------
            // VALIDACIÓN
            // -------------------------------------------------

            config.setConnectionTestQuery(
                    "SELECT 1"
            );

            config.setAutoCommit(
                    true
            );

            // -------------------------------------------------
            // CREAR DATASOURCE
            // -------------------------------------------------

            DATA_SOURCE =
                    new HikariDataSource(
                            config
                    );

        } catch (Exception e) {

            throw new ExceptionInInitializerError(
                    "No se pudo inicializar el pool de conexiones: "
                            + e.getMessage()
            );
        }
    }

    private ConexionBD() {
    }

    // =========================================================
    // VARIABLES DE ENTORNO
    // =========================================================

    private static String obtenerVariable(
            String nombre,
            String valorPorDefecto
    ) {

        String valor =
                System.getenv(
                        nombre
                );

        if (valor == null ||
                valor.isBlank()) {

            return valorPorDefecto;
        }

        return valor.trim();
    }

    private static String obtenerVariableObligatoria(
            String nombre
    ) {

        String valor =
                System.getenv(
                        nombre
                );

        if (valor == null ||
                valor.isBlank()) {

            throw new IllegalStateException(
                    "Falta configurar la variable de entorno "
                            + nombre
            );
        }

        return valor;
    }

    // =========================================================
    // OBTENER CONEXIÓN
    // =========================================================

    public static Connection obtenerConexion()
            throws SQLException {

        return DATA_SOURCE.getConnection();
    }

    // =========================================================
    // CERRAR POOL
    // =========================================================

    public static void cerrarPool() {

        if (DATA_SOURCE != null &&
                !DATA_SOURCE.isClosed()) {

            DATA_SOURCE.close();
        }
    }
}
