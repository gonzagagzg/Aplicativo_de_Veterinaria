package com.itq.service;

import com.itq.dao.EmpresaDAO;
import com.itq.dao.RolDAO;
import com.itq.dao.UsuarioDAO;
import com.itq.dto.LoginResponse;
import com.itq.model.Empresa;
import com.itq.model.Rol;
import com.itq.model.Usuario;
import com.itq.util.JwtUtil;
import com.itq.util.PasswordUtil;

import java.sql.SQLException;

public class AuthService {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final RolDAO rolDAO = new RolDAO();
    private final EmpresaDAO empresaDAO = new EmpresaDAO();

    public LoginResponse login(String usuario, String clave)
            throws SQLException {

        if (usuario == null || usuario.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El usuario es obligatorio"
            );
        }

        if (clave == null || clave.isEmpty()) {
            throw new IllegalArgumentException(
                    "La contraseña es obligatoria"
            );
        }

        Usuario encontrado = usuarioDAO
                .buscarPorUsuarioParaLogin(usuario)
                .orElseThrow(() ->
                        new SecurityException(
                                "Usuario o contraseña incorrectos"
                        )
                );

        if (!Boolean.TRUE.equals(encontrado.isActivo())) {
            throw new SecurityException(
                    "El usuario se encuentra inactivo"
            );
        }

        if (!PasswordUtil.verificar(
                clave,
                encontrado.getClaveHash()
        )) {
            throw new SecurityException(
                    "Usuario o contraseña incorrectos"
            );
        }

        Rol rol = rolDAO
                .buscarPorId(encontrado.getIdRol())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "El usuario no tiene un rol válido"
                        )
                );

        /*
         * El superusuario global puede tener una empresa
         * técnica/matriz. Los demás usuarios deben pertenecer
         * a una empresa activa.
         */
        if (encontrado.getIdEmpresa() != null) {

            Empresa empresa = empresaDAO
                    .buscarPorId(encontrado.getIdEmpresa())
                    .orElseThrow(() ->
                            new IllegalStateException(
                                    "La empresa del usuario no existe"
                            )
                    );

            if (!Boolean.TRUE.equals(empresa.isActivo())) {
                throw new SecurityException(
                        "La empresa se encuentra inactiva"
                );
            }
        }

        String token = JwtUtil.generarToken(
                encontrado.getIdUsuario(),
                encontrado.getIdEmpresa(),
                encontrado.getIdRol(),
                rol.getNombre()
        );

        return new LoginResponse(
                token,
                encontrado.getIdUsuario(),
                encontrado.getIdEmpresa(),
                encontrado.getIdRol(),
                rol.getNombre(),
                encontrado.getNombres(),
                encontrado.getUsuario()
        );
    }
}