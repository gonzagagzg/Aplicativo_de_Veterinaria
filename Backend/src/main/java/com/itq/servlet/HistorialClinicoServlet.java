package com.itq.servlet;

import com.itq.dto.ApiResponse;
import com.itq.model.HistorialClinico;
import com.itq.service.HistorialClinicoService;
import com.itq.util.HttpUtil;
import com.itq.util.JsonUtil;
import com.itq.util.SqlErrorUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

@WebServlet("/api/historiales-clinicos/*")
public class HistorialClinicoServlet extends HttpServlet {
    private final HistorialClinicoService service = new HistorialClinicoService();

    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String raw = valorId(req);
            if (raw == null) { HttpUtil.json(resp, 200, ApiResponse.ok("Listado", service.listar())); return; }
            UUID id = UUID.fromString(raw);
            var encontrado = service.buscarPorId(id);
            if (encontrado.isEmpty()) { HttpUtil.error(resp, 404, "Registro no encontrado"); return; }
            HttpUtil.json(resp, 200, ApiResponse.ok("Registro encontrado", encontrado.get()));
        } catch (IllegalArgumentException e) { HttpUtil.error(resp, 400, "Identificador inválido"); }
          catch (SQLException e) { HttpUtil.error(resp, SqlErrorUtil.estadoHttp(e), e.getMessage()); }
    }

    @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            HistorialClinico obj = JsonUtil.gson().fromJson(req.getReader(), HistorialClinico.class);
            HttpUtil.json(resp, 201, ApiResponse.ok("Registro creado", service.crear(obj)));
        } catch (SQLException e) { HttpUtil.error(resp, SqlErrorUtil.estadoHttp(e), e.getMessage()); }
          catch (Exception e) { HttpUtil.error(resp, 400, "JSON o datos inválidos: " + e.getMessage()); }
    }

    @Override protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            UUID id = UUID.fromString(valorId(req));
            HistorialClinico obj = JsonUtil.gson().fromJson(req.getReader(), HistorialClinico.class);
            obj.setIdHistorial(id);
            if (!service.actualizar(obj)) { HttpUtil.error(resp, 404, "Registro no encontrado"); return; }
            HttpUtil.json(resp, 200, ApiResponse.ok("Registro actualizado", obj));
        } catch (SQLException e) { HttpUtil.error(resp, SqlErrorUtil.estadoHttp(e), e.getMessage()); }
          catch (Exception e) { HttpUtil.error(resp, 400, "Datos inválidos: " + e.getMessage()); }
    }

    @Override protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            UUID id = UUID.fromString(valorId(req));
            if (!service.eliminar(id)) { HttpUtil.error(resp, 404, "Registro no encontrado"); return; }
            resp.setStatus(204);
        } catch (SQLException e) { HttpUtil.error(resp, SqlErrorUtil.estadoHttp(e), e.getMessage()); }
          catch (Exception e) { HttpUtil.error(resp, 400, "Identificador inválido"); }
    }

    private String valorId(HttpServletRequest req) {
        String path = req.getPathInfo();
        if (path == null || path.equals("/") || path.isBlank()) return null;
        return path.substring(1).split("/")[0];
    }
}
