package com.itq.servlet;

import com.itq.dto.ApiResponse;
import com.itq.model.Mascota;
import com.itq.service.MascotaService;
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

@WebServlet("/api/mascotas/*")
public class MascotaServlet extends HttpServlet {

    private final MascotaService service = new MascotaService();

    @Override
    protected void doGet(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws IOException {

        try {
            String raw = valorId(req);

            if (raw == null) {
                HttpUtil.json(
                        resp,
                        200,
                        ApiResponse.ok("Listado", service.listar())
                );
                return;
            }

            UUID id = UUID.fromString(raw);

            var encontrado = service.buscarPorId(id);

            if (encontrado.isEmpty()) {
                HttpUtil.error(resp, 404, "Registro no encontrado");
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

        } catch (IllegalArgumentException e) {
            HttpUtil.error(resp, 400, "Identificador inválido");

        } catch (SQLException e) {
            HttpUtil.error(
                    resp,
                    SqlErrorUtil.estadoHttp(e),
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
            Mascota obj = JsonUtil.gson()
                    .fromJson(req.getReader(), Mascota.class);

            Mascota creada = service.crear(obj);

            HttpUtil.json(
                    resp,
                    201,
                    ApiResponse.ok("Registro creado", creada)
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
                    "JSON o datos inválidos: " + e.getMessage()
            );
        }
    }

    @Override
    protected void doPut(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws IOException {

        try {
            String raw = valorId(req);

            if (raw == null) {
                HttpUtil.error(
                        resp,
                        400,
                        "Identificador obligatorio"
                );
                return;
            }

            UUID id = UUID.fromString(raw);

            Mascota obj = JsonUtil.gson()
                    .fromJson(req.getReader(), Mascota.class);

            if (obj == null) {
                HttpUtil.error(
                        resp,
                        400,
                        "Los datos de la mascota son obligatorios"
                );
                return;
            }

            obj.setIdMascota(id);

            boolean actualizado = service.actualizar(obj);

            if (!actualizado) {
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
                    ApiResponse.ok("Registro actualizado", obj)
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
                    "Datos inválidos: " + e.getMessage()
            );
        }
    }

    @Override
    protected void doDelete(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws IOException {

        try {
            String raw = valorId(req);

            if (raw == null) {
                HttpUtil.error(
                        resp,
                        400,
                        "Identificador obligatorio"
                );
                return;
            }

            UUID id = UUID.fromString(raw);

            boolean eliminado = service.eliminar(id);

            if (!eliminado) {
                HttpUtil.error(
                        resp,
                        404,
                        "Registro no encontrado"
                );
                return;
            }

            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);

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
        }
    }

    private String valorId(HttpServletRequest req) {
        String path = req.getPathInfo();

        if (path == null || path.equals("/") || path.isBlank()) {
            return null;
        }

        String valor = path.substring(1).split("/")[0];

        if (valor.isBlank()) {
            return null;
        }

        return valor;
    }
}