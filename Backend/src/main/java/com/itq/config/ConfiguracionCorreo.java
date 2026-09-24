package com.itq.config;

public final class ConfiguracionCorreo {

    private ConfiguracionCorreo() {
        // Evita instancias.
    }

    // =========================================================
    // CONFIGURACIÓN SMTP DEL PROYECTO
    // =========================================================

    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final int SMTP_PUERTO = 587;

    private static final String SMTP_USUARIO =
            "itqveterinaria@gmail.com";

    /*
     * Proyecto académico.
     * Credencial SMTP utilizada por la cuenta remitente.
     */
    private static final String SMTP_CLAVE =
            "llnhoprqhainbvny";

    private static final String CORREO_REMITENTE =
            "itqveterinaria@gmail.com";

    private static final String NOMBRE_REMITENTE =
            "Veterinaria ITQ";

    private static final boolean SMTP_STARTTLS = true;
    private static final boolean SMTP_AUTH = true;

    /*
     * URL del frontend local utilizada dentro
     * del enlace enviado por correo.
     */
    private static final String FRONTEND_URL =
            "http://localhost:5174";

    // =========================================================
    // SMTP
    // =========================================================

    public static String getSmtpHost() {
        return SMTP_HOST;
    }

    public static int getSmtpPuerto() {
        return SMTP_PUERTO;
    }

    public static String getSmtpUsuario() {
        return SMTP_USUARIO;
    }

    public static String getSmtpClave() {
        return SMTP_CLAVE;
    }

    // =========================================================
    // REMITENTE
    // =========================================================

    public static String getCorreoRemitente() {
        return CORREO_REMITENTE;
    }

    public static String getNombreRemitente() {
        return NOMBRE_REMITENTE;
    }

    // =========================================================
    // SEGURIDAD SMTP
    // =========================================================

    public static boolean usarStartTls() {
        return SMTP_STARTTLS;
    }

    public static boolean usarAutenticacion() {
        return SMTP_AUTH;
    }

    // =========================================================
    // FRONTEND
    // =========================================================

    public static String getFrontendUrl() {
        return FRONTEND_URL;
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