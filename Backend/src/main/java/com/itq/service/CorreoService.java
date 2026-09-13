package com.itq.service;

import com.itq.config.ConfiguracionCorreo;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class CorreoService {

    // =========================================================
    // ENVIAR CORREO DE RECUPERACIÓN
    // =========================================================

    public void enviarCorreoRecuperacion(
            String destinatario,
            String nombreUsuario,
            String token
    ) {

        if (destinatario == null ||
                destinatario.isBlank()) {

            throw new IllegalArgumentException(
                    "El correo destinatario es obligatorio"
            );
        }

        if (token == null ||
                token.isBlank()) {

            throw new IllegalArgumentException(
                    "El token de recuperación es obligatorio"
            );
        }

        String enlace =
                ConfiguracionCorreo.crearUrlRecuperacion(
                        token
                );

        String asunto =
                "Recuperación de contraseña";

        String html =
                crearContenidoHtml(
                        nombreUsuario,
                        enlace
                );

        enviarCorreoHtml(
                destinatario,
                asunto,
                html
        );
    }

    // =========================================================
    // ENVÍO SMTP
    // =========================================================

    private void enviarCorreoHtml(
            String destinatario,
            String asunto,
            String contenidoHtml
    ) {

        Properties propiedades =
                new Properties();

        propiedades.put(
                "mail.smtp.host",
                ConfiguracionCorreo.getSmtpHost()
        );

        propiedades.put(
                "mail.smtp.port",
                String.valueOf(
                        ConfiguracionCorreo.getSmtpPuerto()
                )
        );

        propiedades.put(
                "mail.smtp.auth",
                String.valueOf(
                        ConfiguracionCorreo.usarAutenticacion()
                )
        );

        propiedades.put(
                "mail.smtp.starttls.enable",
                String.valueOf(
                        ConfiguracionCorreo.usarStartTls()
                )
        );

        propiedades.put(
                "mail.smtp.starttls.required",
                String.valueOf(
                        ConfiguracionCorreo.usarStartTls()
                )
        );

        propiedades.put(
                "mail.smtp.connectiontimeout",
                "10000"
        );

        propiedades.put(
                "mail.smtp.timeout",
                "10000"
        );

        propiedades.put(
                "mail.smtp.writetimeout",
                "10000"
        );

        Session sesion =
                Session.getInstance(
                        propiedades,
                        new Authenticator() {

                            @Override
                            protected PasswordAuthentication
                            getPasswordAuthentication() {

                                return new PasswordAuthentication(
                                        ConfiguracionCorreo.getSmtpUsuario(),
                                        ConfiguracionCorreo.getSmtpClave()
                                );
                            }
                        }
                );

        try {

            MimeMessage mensaje =
                    new MimeMessage(
                            sesion
                    );

            configurarRemitente(
                    mensaje
            );

            mensaje.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(
                            destinatario,
                            false
                    )
            );

            mensaje.setSubject(
                    asunto,
                    StandardCharsets.UTF_8.name()
            );

            mensaje.setContent(
                    contenidoHtml,
                    "text/html; charset=UTF-8"
            );

            Transport.send(
                    mensaje
            );

        } catch (MessagingException |
                 UnsupportedEncodingException e) {

            throw new IllegalStateException(
                    "No se pudo enviar el correo de recuperación",
                    e
            );
        }
    }

    // =========================================================
    // REMITENTE
    // =========================================================

    private void configurarRemitente(
            MimeMessage mensaje
    )
            throws MessagingException,
            UnsupportedEncodingException {

        mensaje.setFrom(
                new InternetAddress(
                        ConfiguracionCorreo.getCorreoRemitente(),
                        ConfiguracionCorreo.getNombreRemitente(),
                        StandardCharsets.UTF_8.name()
                )
        );
    }

    // =========================================================
    // HTML DEL CORREO
    // =========================================================

    private String crearContenidoHtml(
            String nombreUsuario,
            String enlace
    ) {

        String nombre =
                nombreUsuario == null ||
                        nombreUsuario.isBlank()
                        ? "Usuario"
                        : escaparHtml(
                                nombreUsuario
                        );

        String url =
                escaparHtml(
                        enlace
                );

        return """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport"
                          content="width=device-width, initial-scale=1.0">
                    <title>Recuperación de contraseña</title>
                </head>

                <body style="
                    margin:0;
                    padding:0;
                    background-color:#f4f4f4;
                    font-family:Arial, Helvetica, sans-serif;
                ">

                    <table width="100%%"
                           cellpadding="0"
                           cellspacing="0"
                           border="0">

                        <tr>
                            <td align="center"
                                style="padding:40px 20px;">

                                <table width="600"
                                       cellpadding="0"
                                       cellspacing="0"
                                       border="0"
                                       style="
                                           max-width:600px;
                                           width:100%%;
                                           background:#ffffff;
                                           border-radius:12px;
                                           overflow:hidden;
                                       ">

                                    <tr>
                                        <td style="
                                            padding:30px;
                                            text-align:center;
                                            background:#1f2937;
                                            color:#ffffff;
                                        ">

                                            <h1 style="
                                                margin:0;
                                                font-size:24px;
                                            ">
                                                Sistema Veterinario
                                            </h1>

                                        </td>
                                    </tr>

                                    <tr>
                                        <td style="
                                            padding:35px;
                                            color:#333333;
                                        ">

                                            <h2 style="
                                                margin-top:0;
                                            ">
                                                Restablecer contraseña
                                            </h2>

                                            <p>
                                                Hola %s,
                                            </p>

                                            <p>
                                                Recibimos una solicitud para
                                                cambiar la contraseña de tu cuenta.
                                            </p>

                                            <p>
                                                Presiona el siguiente botón para
                                                crear una nueva contraseña:
                                            </p>

                                            <p style="
                                                text-align:center;
                                                margin:35px 0;
                                            ">

                                                <a href="%s"
                                                   style="
                                                       display:inline-block;
                                                       padding:14px 24px;
                                                       background:#2563eb;
                                                       color:#ffffff;
                                                       text-decoration:none;
                                                       border-radius:8px;
                                                       font-weight:bold;
                                                   ">
                                                    Restablecer contraseña
                                                </a>

                                            </p>

                                            <p>
                                                Este enlace estará disponible
                                                durante 15 minutos.
                                            </p>

                                            <p>
                                                Si no solicitaste este cambio,
                                                puedes ignorar este mensaje.
                                            </p>

                                            <hr style="
                                                border:none;
                                                border-top:1px solid #dddddd;
                                                margin:30px 0;
                                            ">

                                            <p style="
                                                font-size:12px;
                                                color:#777777;
                                            ">
                                                Si el botón no funciona,
                                                copia y pega este enlace
                                                en tu navegador:
                                            </p>

                                            <p style="
                                                font-size:12px;
                                                word-break:break-all;
                                                color:#555555;
                                            ">
                                                %s
                                            </p>

                                        </td>
                                    </tr>

                                </table>

                            </td>
                        </tr>

                    </table>

                </body>
                </html>
                """.formatted(
                nombre,
                url,
                url
        );
    }

    // =========================================================
    // ESCAPAR HTML
    // =========================================================

    private String escaparHtml(
            String texto
    ) {

        if (texto == null) {
            return "";
        }

        return texto
                .replace(
                        "&",
                        "&amp;"
                )
                .replace(
                        "<",
                        "&lt;"
                )
                .replace(
                        ">",
                        "&gt;"
                )
                .replace(
                        "\"",
                        "&quot;"
                )
                .replace(
                        "'",
                        "&#39;"
                );
    }
}