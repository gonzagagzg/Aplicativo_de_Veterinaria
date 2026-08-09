package com.itq.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public final class ConexionBD {

    // =========================================================
    // DATOS DE CONEXIÓN
    // =========================================================

    private static final String URL =
            "jdbc:postgresql://localhost:5432/aplicacion_veterinaria";

    private static final String USUARIO =
            "postgres";

    private static final String CONTRASENA =
            "Izabela123";

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

            /*
             * Máximo de conexiones simultáneas
             * abiertas hacia PostgreSQL.
             */
            config.setMaximumPoolSize(
                    10
            );

            /*
             * Mínimo de conexiones disponibles
             * en estado idle.
             */
            config.setMinimumIdle(
                    2
            );

            // -------------------------------------------------
            // TIEMPOS
            // -------------------------------------------------

            /*
             * Máximo tiempo esperando una conexión
             * disponible del pool.
             *
             * 10 segundos.
             */
            config.setConnectionTimeout(
                    10_000
            );

            /*
             * Tiempo máximo de inactividad
             * antes de liberar una conexión idle.
             *
             * 5 minutos.
             */
            config.setIdleTimeout(
                    300_000
            );

            /*
             * Vida máxima de una conexión.
             *
             * 30 minutos.
             */
            config.setMaxLifetime(
                    1_800_000
            );

            // -------------------------------------------------
            // VALIDACIÓN
            // -------------------------------------------------

            config.setConnectionTestQuery(
                    "SELECT 1"
            );

            /*
             * JDBC usa autoCommit=true por defecto.
             *
             * Lo mantenemos porque todos los DAO actuales
             * fueron construidos con ese comportamiento.
             *
             * La facturación transaccional cambia
             * temporalmente a false cuando corresponde.
             */
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
    // OBTENER CONEXIÓN
    // =========================================================

    public static Connection obtenerConexion()
            throws SQLException {

        /*
         * Ahora NO se crea una conexión TCP nueva.
         *
         * Hikari entrega una conexión del pool.
         *
         * Cuando un DAO ejecuta:
         *
         * try (Connection cn = obtenerConexion()) { ... }
         *
         * el cn.close() NO destruye realmente la conexión;
         * la devuelve al pool.
         */
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