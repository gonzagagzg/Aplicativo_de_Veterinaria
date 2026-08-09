package com.itq.servlet;

import com.itq.dto.ApiResponse;
import com.itq.dto.FacturaEmitirRequest;
import com.itq.model.Factura;
import com.itq.security.Autorizacion;
import com.itq.service.FacturaService;
import com.itq.service.FacturacionTransaccionalService;
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

@WebServlet("/api/facturas/*")
public class FacturaServlet extends HttpServlet {

    private final FacturaService service =
            new FacturaService();

    private final FacturacionTransaccionalService
            facturacionService =
            new FacturacionTransaccionalService();

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

            if (raw == null) {

                Autorizacion.exigir(
                        req,
                        "FACTURAS",
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

            if (raw.equalsIgnoreCase("emitir")) {

                HttpUtil.error(
                        resp,
                        405,
                        "Método no permitido"
                );

                return;
            }

            Autorizacion.exigir(
                    req,
                    "FACTURAS",
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

            UUID idEmpresa =
                    obtenerIdEmpresa(req);

            UUID idUsuario =
                    obtenerIdUsuario(req);

            boolean superUsuario =
                    esSuperUsuario(req);

            String path =
                    req.getPathInfo();

            // POST /api/facturas/emitir
            if (path != null &&
                    path.equalsIgnoreCase("/emitir")) {

                Autorizacion.exigir(
                        req,
                        "FACTURAS",
                        "EMITIR"
                );

                FacturaEmitirRequest request =
                        JsonUtil.gson()
                                .fromJson(
                                        req.getReader(),
                                        FacturaEmitirRequest.class
                                );

                Factura factura =
                        facturacionService.emitir(
                                request,
                                idEmpresa,
                                idUsuario,
                                superUsuario
                        );

                HttpUtil.json(
                        resp,
                        201,
                        ApiResponse.ok(
                                "Factura emitida correctamente",
                                factura
                        )
                );

                return;
            }

            // POST /api/facturas
            Autorizacion.exigir(
                    req,
                    "FACTURAS",
                    "CREAR"
            );

            Factura obj =
                    JsonUtil.gson()
                            .fromJson(
                                    req.getReader(),
                                    Factura.class
                            );

            Factura creada =
                    service.crear(
                            obj,
                            idEmpresa,
                            idUsuario,
                            superUsuario
                    );

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
                    "FACTURAS",
                    "EDITAR"
            );

            UUID idEmpresa =
                    obtenerIdEmpresa(req);

            UUID idUsuario =
                    obtenerIdUsuario(req);

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

            if (raw.equalsIgnoreCase("emitir")) {

                HttpUtil.error(
                        resp,
                        405,
                        "Método no permitido"
                );

                return;
            }

            UUID id =
                    UUID.fromString(raw);

            Factura obj =
                    JsonUtil.gson()
                            .fromJson(
                                    req.getReader(),
                                    Factura.class
                            );

            if (obj == null) {

                HttpUtil.error(
                        resp,
                        400,
                        "Los datos de la factura son obligatorios"
                );

                return;
            }

            obj.setIdFactura(id);

            if (!service.actualizar(
                    obj,
                    idEmpresa,
                    idUsuario,
                    superUsuario
            )) {

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
                    "FACTURAS",
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

            if (raw.equalsIgnoreCase("emitir")) {

                HttpUtil.error(
                        resp,
                        405,
                        "Método no permitido"
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

    private UUID obtenerIdEmpresa(
            HttpServletRequest req
    ) {

        return (UUID)
                req.getAttribute("idEmpresa");
    }

    private UUID obtenerIdUsuario(
            HttpServletRequest req
    ) {

        return (UUID)
                req.getAttribute("idUsuario");
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