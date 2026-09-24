package com.itq.config;

public final class ConfiguracionCorreo {

    private ConfiguracionCorreo() {
        // Evita instancias.
    }

    // =========================================================
    // VARIABLES DE ENTORNO
    // =========================================================

    private static String obtenerVariable(
            String nombre
    ) {

        String valor =
                System.getenv(nombre);

        if (valor == null ||
                valor.isBlank()) {

            throw new IllegalStateException(
                    "No está configurada la variable de entorno: "
                            + nombre
            );
        }

        return valor.trim();
    }

    private static String obtenerVariableOpcional(
            String nombre,
            String valorDefecto
    ) {

        String valor =
                System.getenv(nombre);

        if (valor == null ||
                valor.isBlank()) {

            return valorDefecto;
        }

        return valor.trim();
    }

    // =========================================================
    // SMTP
    // =========================================================

    public static String getSmtpHost() {

        return obtenerVariable(
                "VET_SMTP_HOST"
        );
    }

    public static int getSmtpPuerto() {

        String puerto =
                obtenerVariableOpcional(
                        "VET_SMTP_PORT",
                        "587"
                );

        try {

            return Integer.parseInt(
                    puerto
            );

        } catch (NumberFormatException e) {

            throw new IllegalStateException(
                    "VET_SMTP_PORT debe ser un número válido"
            );
        }
    }

    public static String getSmtpUsuario() {

        return obtenerVariable(
                "VET_SMTP_USER"
        );
    }

    public static String getSmtpClave() {

        return obtenerVariable(
                "VET_SMTP_PASSWORD"
        );
    }

    // =========================================================
    // REMITENTE
    // =========================================================

    public static String getCorreoRemitente() {

        return obtenerVariableOpcional(
                "VET_SMTP_FROM",
                getSmtpUsuario()
        );
    }

    public static String getNombreRemitente() {

        return obtenerVariableOpcional(
                "VET_SMTP_FROM_NAME",
                "Sistema Veterinario"
        );
    }

    // =========================================================
    // SEGURIDAD SMTP
    // =========================================================

    public static boolean usarStartTls() {

        return Boolean.parseBoolean(
                obtenerVariableOpcional(
                        "VET_SMTP_STARTTLS",
                        "true"
                )
        );
    }

    public static boolean usarAutenticacion() {

        return Boolean.parseBoolean(
                obtenerVariableOpcional(
                        "VET_SMTP_AUTH",
                        "true"
                )
        );
    }

    // =========================================================
    // FRONTEND
    // =========================================================

    public static String getFrontendUrl() {

        String url =
                obtenerVariableOpcional(
                        "VET_FRONTEND_URL",
                        "http://localhost:5174"
                );

        while (url.endsWith("/")) {

            url =
                    url.substring(
                            0,
                            url.length() - 1
                    );
        }

        return url;
    }

    // =========================================================
    // ENLACE DE RECUPERACIÓN
    // =========================================================

    public static String crearUrlRecuperacion(
            String token
    ) {

        if (token == null ||
                token.isBlank()) {

            throw new IllegalArgumentException(
                    "El token de recuperación es obligatorio"
            );
        }

        return getFrontendUrl()
                + "/restablecer-password?token="
                + token;
    }
}