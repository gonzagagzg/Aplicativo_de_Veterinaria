package com.itq.servlet;

import com.itq.dto.ApiResponse;
import com.itq.dto.LoginRequest;
import com.itq.dto.LoginResponse;
import com.itq.service.AuthService;
import com.itq.util.HttpUtil;
import com.itq.util.JsonUtil;
import com.itq.util.SqlErrorUtil;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/api/auth/login")
public class AuthServlet extends HttpServlet {

    private final AuthService service = new AuthService();

    @Override
    protected void doPost(
            HttpServletRequest req,
            HttpServletResponse resp
    ) throws IOException {

        try {

            LoginRequest login = JsonUtil.gson()
                    .fromJson(req.getReader(), LoginRequest.class);

            if (login == null) {
                HttpUtil.error(
                        resp,
                        400,
                        "Los datos de inicio de sesión son obligatorios"
                );
                return;
            }

            LoginResponse resultado = service.login(
                    login.getUsuario(),
                    login.getClave()
            );

            HttpUtil.json(
                    resp,
                    200,
                    ApiResponse.ok(
                            "Inicio de sesión correcto",
                            resultado
                    )
            );

        } catch (SecurityException e) {

            HttpUtil.error(
                    resp,
                    401,
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
                    500,
                    "Error interno al iniciar sesión"
            );
        }
    }
}