package com.itq.servlet;

import com.itq.dto.ApiResponse;
import com.itq.model.Empresa;
import com.itq.security.Autorizacion;
import com.itq.service.EmpresaService;
import com.itq.service.SuperUsuarioService;
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

@WebServlet("/api/empresas/*")
public class EmpresaServlet extends HttpServlet {

    private final EmpresaService service =
            new EmpresaService();

    private final SuperUsuarioService superUsuarioService =
            new SuperUsuarioService();

    // =========================================================
    // GET
    // =========================================================

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws IOException {

        try {

            // Todo el módulo de empresas queda reservado
            // exclusivamente para el SuperUsuario.
            exigirSuperUsuario(req);

            String[] partes =
                    partesRuta(req);

            // =================================================
            // GET /api/empresas
            // =================================================

            if (partes.length == 0) {

                Autorizacion.exigir(
                        req,
                        "EMPRESAS",
                        "LISTAR"
                );

                HttpUtil.json(
                        resp,
                        200,
                        ApiResponse.ok(
                                "Listado",
                                service.listar()
                        )
                );

                return;
            }

            // =================================================
            // GET /api/empresas/{id}/resumen
            // =================================================

            if (partes.length >= 2 &&
                    partes[1].equalsIgnoreCase("resumen")) {

                Autorizacion.exigir(
                        req,
                        "EMPRESAS",
                        "VER"
                );

                UUID idEmpresa =
                        UUID.fromString(
                                partes[0]
                        );

                var resumen =
                        superUsuarioService
                                .obtenerResumen(
                                        idEmpresa
                                );

                if (resumen.isEmpty()) {

                    HttpUtil.error(
                            resp,
                            404,
                            "Empresa no encontrada"
                    );

                    return;
                }

                HttpUtil.json(
                        resp,
                        200,
                        ApiResponse.ok(
                                "Resumen de empresa",
                                resumen.get()
                        )
                );

                return;
            }

            // =================================================
            // GET /api/empresas/{id}
            // =================================================

            if (partes.length == 1) {

                Autorizacion.exigir(
                        req,
                        "EMPRESAS",
                        "VER"
                );

                UUID idEmpresa =
                        UUID.fromString(
                                partes[0]
                        );

                var encontrado =
                        service.buscarPorId(
                                idEmpresa
                        );

                if (encontrado.isEmpty()) {

                    HttpUtil.error(
                            resp,
                            404,
                            "Empresa no encontrada"
                    );

                    return;
                }

                HttpUtil.json(
                        resp,
                        200,
                        ApiResponse.ok(
                                "Empresa encontrada",
                                encontrado.get()
                        )
                );

                return;
            }

            // Si llega una ruta no contemplada.
            HttpUtil.error(
                    resp,
                    404,
                    "Ruta no encontrada"
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

    // =========================================================
    // POST
    // =========================================================

    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws IOException {

        try {

            exigirSuperUsuario(req);

            Autorizacion.exigir(
                    req,
                    "EMPRESAS",
                    "CREAR"
            );

            Empresa obj =
                    JsonUtil.gson()
                            .fromJson(
                                    req.getReader(),
                                    Empresa.class
                            );

            if (obj == null) {

                HttpUtil.error(
                        resp,
                        400,
                        "Los datos de la empresa son obligatorios"
                );

                return;
            }

            Empresa creada =
                    service.crear(obj);

            HttpUtil.json(
                    resp,
                    201,
                    ApiResponse.ok(
                            "Empresa creada",
                            creada
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

    // =========================================================
    // PUT
    // =========================================================

    @Override
    protected void doPut(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws IOException {

        try {

            exigirSuperUsuario(req);

            String[] partes =
                    partesRuta(req);

            if (partes.length == 0) {

                HttpUtil.error(
                        resp,
                        400,
                        "Identificador obligatorio"
                );

                return;
            }

            UUID idEmpresa =
                    UUID.fromString(
                            partes[0]
                    );

            // =================================================
            // PUT /api/empresas/{id}/activar
            // =================================================

            if (partes.length == 2 &&
                    partes[1].equalsIgnoreCase("activar")) {

                Autorizacion.exigir(
                        req,
                        "EMPRESAS",
                        "ACTIVAR"
                );

                if (!service.activar(
                        idEmpresa
                )) {

                    HttpUtil.error(
                            resp,
                            404,
                            "Empresa no encontrada"
                    );

                    return;
                }

                HttpUtil.json(
                        resp,
                        200,
                        ApiResponse.ok(
                                "Empresa activada",
                                null
                        )
                );

                return;
            }

            // =================================================
            // PUT /api/empresas/{id}/desactivar
            // =================================================

            if (partes.length == 2 &&
                    partes[1].equalsIgnoreCase("desactivar")) {

                Autorizacion.exigir(
                        req,
                        "EMPRESAS",
                        "DESACTIVAR"
                );

                if (!service.desactivar(
                        idEmpresa
                )) {

                    HttpUtil.error(
                            resp,
                            404,
                            "Empresa no encontrada"
                    );

                    return;
                }

                HttpUtil.json(
                        resp,
                        200,
                        ApiResponse.ok(
                                "Empresa desactivada",
                                null
                        )
                );

                return;
            }

            // =================================================
            // PUT /api/empresas/{id}
            // =================================================

            if (partes.length == 1) {

                Autorizacion.exigir(
                        req,
                        "EMPRESAS",
                        "EDITAR"
                );

                Empresa obj =
                        JsonUtil.gson()
                                .fromJson(
                                        req.getReader(),
                                        Empresa.class
                                );

                if (obj == null) {

                    HttpUtil.error(
                            resp,
                            400,
                            "Los datos de la empresa son obligatorios"
                    );

                    return;
                }

                obj.setIdEmpresa(
                        idEmpresa
                );

                if (!service.actualizar(obj)) {

                    HttpUtil.error(
                            resp,
                            404,
                            "Empresa no encontrada"
                    );

                    return;
                }

                HttpUtil.json(
                        resp,
                        200,
                        ApiResponse.ok(
                                "Empresa actualizada",
                                obj
                        )
                );

                return;
            }

            HttpUtil.error(
                    resp,
                    404,
                    "Ruta no encontrada"
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
                    "Datos inválidos: "
                            + e.getMessage()
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

    // =========================================================
    // DELETE
    // =========================================================

    @Override
    protected void doDelete(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws IOException {

        try {

            exigirSuperUsuario(req);

            HttpUtil.error(
                    resp,
                    405,
                    "Las empresas no se eliminan. Utilice activar o desactivar."
            );

        } catch (SecurityException e) {

            HttpUtil.error(
                    resp,
                    403,
                    e.getMessage()
            );
        }
    }

    // =========================================================
    // SOLO SUPERUSUARIO
    // =========================================================

    private void exigirSuperUsuario(
            HttpServletRequest req
    ) {

        String rol =
                (String)
                        req.getAttribute("rol");

        if (rol == null ||
                !rol.equalsIgnoreCase(
                        "SuperUsuario"
                )) {

            throw new SecurityException(
                    "Esta operación requiere SuperUsuario"
            );
        }
    }

    // =========================================================
    // PARSEO DE RUTA
    //
    // /                      -> []
    // /UUID                  -> [UUID]
    // /UUID/resumen          -> [UUID, resumen]
    // /UUID/activar          -> [UUID, activar]
    // /UUID/desactivar       -> [UUID, desactivar]
    // =========================================================

    private String[] partesRuta(
            HttpServletRequest req
    ) {

        String path =
                req.getPathInfo();

        if (path == null ||
                path.equals("/") ||
                path.isBlank()) {

            return new String[0];
        }

        String limpio =
                path.trim();

        if (limpio.startsWith("/")) {
            limpio =
                    limpio.substring(1);
        }

        if (limpio.endsWith("/")) {
            limpio =
                    limpio.substring(
                            0,
                            limpio.length() - 1
                    );
        }

        if (limpio.isBlank()) {
            return new String[0];
        }

        return limpio.split("/");
    }
}