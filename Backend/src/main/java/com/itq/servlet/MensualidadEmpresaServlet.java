package com.itq.servlet;

import com.itq.dto.ApiResponse;
import com.itq.model.MensualidadEmpresa;
import com.itq.security.Autorizacion;
import com.itq.service.MensualidadEmpresaService;
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

@WebServlet("/api/mensualidades/*")
public class MensualidadEmpresaServlet extends HttpServlet {

    private final MensualidadEmpresaService service =
            new MensualidadEmpresaService();

    // =========================================================
    // GET
    // =========================================================

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws IOException {

        try {

            String raw =
                    valorId(req);

            // -------------------------------------------------
            // GET /api/mensualidades
            // -------------------------------------------------

            if (raw == null) {

                Autorizacion.exigir(
                        req,
                        "MENSUALIDADES",
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

            // -------------------------------------------------
            // GET /api/mensualidades/empresa/{idEmpresa}
            // -------------------------------------------------

            if (raw.equalsIgnoreCase("empresa")) {

                Autorizacion.exigir(
                        req,
                        "MENSUALIDADES",
                        "LISTAR"
                );

                String idEmpresaRaw =
                        segundoValor(req);

                if (idEmpresaRaw == null) {

                    HttpUtil.error(
                            resp,
                            400,
                            "La empresa es obligatoria"
                    );

                    return;
                }

                UUID idEmpresa =
                        UUID.fromString(
                                idEmpresaRaw
                        );

                HttpUtil.json(
                        resp,
                        200,
                        ApiResponse.ok(
                                "Listado",
                                service.listarPorEmpresa(
                                        idEmpresa
                                )
                        )
                );

                return;
            }

            // -------------------------------------------------
            // GET /api/mensualidades/{id}
            // -------------------------------------------------

            Autorizacion.exigir(
                    req,
                    "MENSUALIDADES",
                    "VER"
            );

            UUID id =
                    UUID.fromString(raw);

            var encontrado =
                    service.buscarPorId(id);

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
        }
    }

    // =========================================================
    // POST - CREAR
    // =========================================================

    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws IOException {

        try {

            Autorizacion.exigir(
                    req,
                    "MENSUALIDADES",
                    "CREAR"
            );

            MensualidadEmpresa obj =
                    JsonUtil.gson()
                            .fromJson(
                                    req.getReader(),
                                    MensualidadEmpresa.class
                            );

            MensualidadEmpresa creada =
                    service.crear(obj);

            HttpUtil.json(
                    resp,
                    201,
                    ApiResponse.ok(
                            "Registro creado",
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
    // PUT - EDITAR / REACTIVAR
    // =========================================================

    @Override
    protected void doPut(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws IOException {

        try {

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

            String accion =
                    segundoValor(req);

            // -------------------------------------------------
            // PUT /api/mensualidades/{id}/reactivar
            // -------------------------------------------------

            if (accion != null &&
                    accion.equalsIgnoreCase("reactivar")) {

                Autorizacion.exigir(
                        req,
                        "MENSUALIDADES",
                        "EDITAR"
                );

                if (!service.reactivar(id)) {

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
                                "Registro reactivado",
                                null
                        )
                );

                return;
            }

            // -------------------------------------------------
            // PUT /api/mensualidades/{id}
            // -------------------------------------------------

            Autorizacion.exigir(
                    req,
                    "MENSUALIDADES",
                    "EDITAR"
            );

            MensualidadEmpresa obj =
                    JsonUtil.gson()
                            .fromJson(
                                    req.getReader(),
                                    MensualidadEmpresa.class
                            );

            if (obj == null) {

                HttpUtil.error(
                        resp,
                        400,
                        "Los datos de la mensualidad son obligatorios"
                );

                return;
            }

            obj.setIdMensualidad(id);

            if (!service.actualizar(obj)) {

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
    // DELETE - SOFT DELETE
    // =========================================================

    @Override
    protected void doDelete(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws IOException {

        try {

            Autorizacion.exigir(
                    req,
                    "MENSUALIDADES",
                    "ELIMINAR"
            );

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

            if (!service.eliminar(id)) {

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
                            "Registro desactivado",
                            null
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
        }
    }

    // =========================================================
    // UTILIDADES PATH
    // =========================================================

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

        String[] partes =
                path.substring(1)
                        .split("/");

        if (partes.length == 0 ||
                partes[0].isBlank()) {

            return null;
        }

        return partes[0];
    }

    private String segundoValor(
            HttpServletRequest req
    ) {

        String path =
                req.getPathInfo();

        if (path == null ||
                path.equals("/") ||
                path.isBlank()) {

            return null;
        }

        String[] partes =
                path.substring(1)
                        .split("/");

        if (partes.length < 2 ||
                partes[1].isBlank()) {

            return null;
        }

        return partes[1];
    }
}