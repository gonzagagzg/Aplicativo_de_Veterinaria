package com.itq.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class ConexionBD {

    private static final String URL =
            "jdbc:postgresql://localhost:5432/aplicacion_veterinaria";

    private static final String USUARIO = "postgres";

    private static final String CONTRASENA =
            "Izabela123";

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(
                    "No se encontró el driver JDBC de PostgreSQL: " + e.getMessage()
            );
        }
    }

    private ConexionBD() {
    }

    public static Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CONTRASENA);
    }
}