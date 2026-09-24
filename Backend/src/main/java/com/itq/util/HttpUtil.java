package com.itq.util;

import com.itq.dto.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public final class HttpUtil {
    private HttpUtil() {}
    public static void json(HttpServletResponse response, int status, Object data) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.getWriter().write(JsonUtil.gson().toJson(data));
    }
    public static void error(HttpServletResponse response, int status, String mensaje) throws IOException {
        json(response, status, ApiResponse.error(mensaje));
    }
}
