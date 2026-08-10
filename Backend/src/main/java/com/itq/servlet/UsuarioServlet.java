package com.itq.servlet;

import com.itq.dto.ApiResponse;
import com.itq.model.Usuario;
import com.itq.security.Autorizacion;
import com.itq.service.UsuarioService;
import com.itq.util.HttpUtil;
import com.itq.util.JsonUtil;
import com.itq.util.SqlErrorUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

@WebServlet("/api/usuarios/*")
public class UsuarioServlet extends HttpServlet {

    private final UsuarioService service =
            new UsuarioService();

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws IOException {

        try {

            UUID idEmpresa =
                    obtenerIdEmpresa(req);

            boolean superUsuario =
                    esSuperUsuario(req);

            String raw =
                    valorId(req);

            // GET /api/usuarios
            if (raw == null) {

                Autorizacion.exigir(
                        req,
                        "USUARIOS",
                        "LISTAR"
                );

                HttpUtil.json(
                        resp,
                        200,
                        ApiResponse.ok(
                                "Listado",
                                service.listar(
                                        idEmpresa,
                                        superUsuario
                                )
                        )
                );

                return;
            }

            // GET /api/usuarios/{id}
            Autorizacion.exigir(
                    req,
                    "USUARIOS",
                    "VER"
            );

            UUID id =
                    UUID.fromString(raw);

            var encontrado =
                    service.buscarPorId(
                            id,
                            idEmpresa,
                            superUsuario
                    );

            if (encontrado.isEmpty()) {

                HttpUtil.error(
                        resp,
                        404,
                        "Registro no encontrado"
                );

                return;
            }

            HttpUtil.json(
                    resp,
                    200,
                    ApiResponse.ok(
                            "Registro encontrado",
                            encontrado.get()
                    )
            );

        } catch (SecurityException e) {

            HttpUtil.error(
                    resp,
                    403,
                    e.getMessage()
            );

        } catch (IllegalArgumentException e) {

            HttpUtil.error(
                    resp,
                    400,
                    "Identificador inválido"
            );

        } catch (SQLException e) {

            HttpUtil.error(
                    resp,
                    SqlErrorUtil.estadoHttp(e),
                    e.getMessage()
            );

        } catch (IllegalStateException e) {

            HttpUtil.error(
                    resp,
                    500,
                    e.getMessage()
            );
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws IOException {

        try {

            Autorizacion.exigir(
                    req,
                    "USUARIOS",
                    "CREAR"
            );

            UUID idEmpresa =
                    obtenerIdEmpresa(req);

            boolean superUsuario =
                    esSuperUsuario(req);

            Usuario obj =
                    JsonUtil.gson()
                            .fromJson(
                                    req.getReader(),
                                    Usuario.class
                            );

            HttpUtil.json(
                    resp,
                    201,
                    ApiResponse.ok(
                            "Registro creado",
                            service.crear(
                                    obj,
                                    idEmpresa,
                                    superUsuario
                            )
                    )
            );

        } catch (SecurityException e) {

            HttpUtil.error(
                    resp,
                    403,
                    e.getMessage()
            );

        } catch (SQLException e) {

            HttpUtil.error(
                    resp,
                    SqlErrorUtil.estadoHttp(e),
                    e.getMessage()
            );

        } catch (IllegalStateException e) {

            HttpUtil.error(
                    resp,
                    500,
                    e.getMessage()
            );

        } catch (Exception e) {

            HttpUtil.error(
                    resp,
                    400,
                    "JSON o datos inválidos: "
                            + e.getMessage()
            );
        }
    }

    @Override
    protected void doPut(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws IOException {

        try {

            Autorizacion.exigir(
                    req,
                    "USUARIOS",
                    "EDITAR"
            );

            UUID idEmpresa =
                    obtenerIdEmpresa(req);

            boolean superUsuario =
                    esSuperUsuario(req);

            String raw =
                    valorId(req);

            if (raw == null) {

                HttpUtil.error(
                        resp,
                        400,
                        "Identificador obligatorio"
                );

                return;
            }

            UUID id =
                    UUID.fromString(raw);

            Usuario obj =
                    JsonUtil.gson()
                            .fromJson(
                                    req.getReader(),
                                    Usuario.class
                            );

            if (obj == null) {

                HttpUtil.error(
                        resp,
                        400,
                        "Los datos del usuario son obligatorios"
                );

                return;
            }

            obj.setIdUsuario(id);

            if (!service.actualizar(
                    obj,
                    idEmpresa,
                    superUsuario
            )) {

                HttpUtil.error(
                        resp,
                        404,
                        "Registro no encontrado"
                );

                return;
            }

            obj.setClaveHash(null);

            HttpUtil.json(
                    resp,
                    200,
                    ApiResponse.ok(
                            "Registro actualizado",
                            obj
                    )
            );

        } catch (SecurityException e) {

            HttpUtil.error(
                    resp,
                    403,
                    e.getMessage()
            );

        } catch (SQLException e) {

            HttpUtil.error(
                    resp,
                    SqlErrorUtil.estadoHttp(e),
                    e.getMessage()
            );

        } catch (IllegalStateException e) {

            HttpUtil.error(
                    resp,
                    500,
                    e.getMessage()
            );

        } catch (Exception e) {

            HttpUtil.error(
                    resp,
                    400,
                    "Datos inválidos: "
                            + e.getMessage()
            );
        }
    }

    @Override
    protected void doDelete(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws IOException {

        try {

            Autorizacion.exigir(
                    req,
                    "USUARIOS",
                    "ELIMINAR"
            );

            UUID idEmpresa =
                    obtenerIdEmpresa(req);

            boolean superUsuario =
                    esSuperUsuario(req);

            String raw =
                    valorId(req);

            if (raw == null) {

                HttpUtil.error(
                        resp,
                        400,
                        "Identificador obligatorio"
                );

                return;
            }

            UUID id =
                    UUID.fromString(raw);

            if (!service.eliminar(
                    id,
                    idEmpresa,
                    superUsuario
            )) {

                HttpUtil.error(
                        resp,
                        404,
                        "Registro no encontrado"
                );

                return;
            }

            resp.setStatus(
                    HttpServletResponse.SC_NO_CONTENT
            );

        } catch (SecurityException e) {

            HttpUtil.error(
                    resp,
                    403,
                    e.getMessage()
            );

        } catch (SQLException e) {

            HttpUtil.error(
                    resp,
                    SqlErrorUtil.estadoHttp(e),
                    e.getMessage()
            );

        } catch (IllegalStateException e) {

            HttpUtil.error(
                    resp,
                    500,
                    e.getMessage()
            );

        } catch (Exception e) {

            HttpUtil.error(
                    resp,
                    400,
                    "Identificador inválido"
            );
        }
    }

    private UUID obtenerIdEmpresa(
            HttpServletRequest req
    ) {

        return (UUID)
                req.getAttribute("idEmpresa");
    }

    private boolean esSuperUsuario(
            HttpServletRequest req
    ) {

        String rol =
                (String)
                        req.getAttribute("rol");

        return rol != null &&
                rol.equalsIgnoreCase(
                        "SuperUsuario"
                );
    }

    private String valorId(
            HttpServletRequest req
    ) {

        String path =
                req.getPathInfo();

        if (path == null ||
                path.equals("/") ||
                path.isBlank()) {

            return null;
        }

        String valor =
                path.substring(1)
                        .split("/")[0];

        return valor.isBlank()
                ? null
                : valor;
    }
}