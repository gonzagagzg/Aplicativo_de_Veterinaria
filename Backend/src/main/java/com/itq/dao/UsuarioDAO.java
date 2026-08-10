package com.itq.dao;

import com.itq.config.ConexionBD;
import com.itq.model.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UsuarioDAO {

    // =========================================================
    // LISTADOS SEGUROS - NUNCA DEVUELVEN clave_hash
    // =========================================================

    public List<Usuario> listar() throws SQLException {

        String sql = """
                SELECT id_usuario, id_empresa, id_rol,
                       usuario, nombres, activo
                FROM usuario
                ORDER BY id_usuario
                """;

        List<Usuario> lista = new ArrayList<>();

        try (
                Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                lista.add(mapearSeguro(rs));
            }
        }

        return lista;
    }

    public List<Usuario> listarPorEmpresa(UUID idEmpresa)
            throws SQLException {

        String sql = """
                SELECT id_usuario, id_empresa, id_rol,
                       usuario, nombres, activo
                FROM usuario
                WHERE id_empresa = ?
                ORDER BY id_usuario
                """;

        List<Usuario> lista = new ArrayList<>();

        try (
                Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setObject(1, idEmpresa);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearSeguro(rs));
                }
            }
        }

        return lista;
    }

    public Optional<Usuario> buscarPorId(UUID idUsuario)
            throws SQLException {

        String sql = """
                SELECT id_usuario, id_empresa, id_rol,
                       usuario, nombres, activo
                FROM usuario
                WHERE id_usuario = ?
                """;

        try (
                Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setObject(1, idUsuario);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next()
                        ? Optional.of(mapearSeguro(rs))
                        : Optional.empty();
            }
        }
    }

    public Optional<Usuario> buscarPorIdYEmpresa(
            UUID idUsuario,
            UUID idEmpresa
    ) throws SQLException {

        String sql = """
                SELECT id_usuario, id_empresa, id_rol,
                       usuario, nombres, activo
                FROM usuario
                WHERE id_usuario = ?
                  AND id_empresa = ?
                """;

        try (
                Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setObject(1, idUsuario);
            ps.setObject(2, idEmpresa);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next()
                        ? Optional.of(mapearSeguro(rs))
                        : Optional.empty();
            }
        }
    }

    // =========================================================
    // CONSULTAS INTERNAS CON HASH
    // Solo deben usarse para autenticación/cambio de contraseña
    // =========================================================

    public Optional<Usuario> buscarPorUsuarioParaLogin(String usuario)
            throws SQLException {

        String sql = """
                SELECT id_usuario, id_empresa, id_rol,
                       usuario, clave_hash, nombres, activo
                FROM usuario
                WHERE LOWER(usuario) = LOWER(?)
                """;

        try (
                Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setString(1, usuario.trim());

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next()
                        ? Optional.of(mapearConHash(rs))
                        : Optional.empty();
            }
        }
    }

    public Optional<Usuario> buscarPorIdConHash(UUID idUsuario)
            throws SQLException {

        String sql = """
                SELECT id_usuario, id_empresa, id_rol,
                       usuario, clave_hash, nombres, activo
                FROM usuario
                WHERE id_usuario = ?
                """;

        try (
                Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setObject(1, idUsuario);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next()
                        ? Optional.of(mapearConHash(rs))
                        : Optional.empty();
            }
        }
    }

    // =========================================================
    // CREAR
    // =========================================================

    public Usuario insertar(Usuario obj) throws SQLException {

        String sql = """
                INSERT INTO usuario
                (id_empresa, id_rol, usuario, clave_hash, nombres, activo)
                VALUES (?, ?, ?, ?, ?, ?)
                RETURNING id_usuario
                """;

        try (
                Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setObject(1, obj.getIdEmpresa());
            ps.setObject(2, obj.getIdRol());
            ps.setString(3, obj.getUsuario());
            ps.setString(4, obj.getClaveHash());
            ps.setString(5, obj.getNombres());
            ps.setBoolean(6, obj.isActivo());

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    throw new SQLException(
                            "No se generó la clave primaria"
                    );
                }

                obj.setIdUsuario(
                        rs.getObject("id_usuario", UUID.class)
                );
            }
        }

        return obj;
    }

    // =========================================================
    // ACTUALIZAR
    // =========================================================

    public boolean actualizar(Usuario obj)
            throws SQLException {

        String sql = """
                UPDATE usuario
                SET id_empresa = ?,
                    id_rol = ?,
                    usuario = ?,
                    clave_hash = ?,
                    nombres = ?,
                    activo = ?
                WHERE id_usuario = ?
                """;

        try (
                Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setObject(1, obj.getIdEmpresa());
            ps.setObject(2, obj.getIdRol());
            ps.setString(3, obj.getUsuario());
            ps.setString(4, obj.getClaveHash());
            ps.setString(5, obj.getNombres());
            ps.setBoolean(6, obj.isActivo());
            ps.setObject(7, obj.getIdUsuario());

            return ps.executeUpdate() > 0;
        }
    }

    public boolean actualizarPorEmpresa(
            Usuario obj,
            UUID idEmpresa
    ) throws SQLException {

        String sql = """
                UPDATE usuario
                SET id_rol = ?,
                    usuario = ?,
                    clave_hash = ?,
                    nombres = ?,
                    activo = ?
                WHERE id_usuario = ?
                  AND id_empresa = ?
                """;

        try (
                Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setObject(1, obj.getIdRol());
            ps.setString(2, obj.getUsuario());
            ps.setString(3, obj.getClaveHash());
            ps.setString(4, obj.getNombres());
            ps.setBoolean(5, obj.isActivo());
            ps.setObject(6, obj.getIdUsuario());
            ps.setObject(7, idEmpresa);

            return ps.executeUpdate() > 0;
        }
    }

    // =========================================================
    // ELIMINAR
    // =========================================================

    public boolean eliminar(UUID idUsuario)
            throws SQLException {

        String sql = """
                DELETE FROM usuario
                WHERE id_usuario = ?
                """;

        try (
                Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setObject(1, idUsuario);

            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminarPorEmpresa(
            UUID idUsuario,
            UUID idEmpresa
    ) throws SQLException {

        String sql = """
                DELETE FROM usuario
                WHERE id_usuario = ?
                  AND id_empresa = ?
                """;

        try (
                Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setObject(1, idUsuario);
            ps.setObject(2, idEmpresa);

            return ps.executeUpdate() > 0;
        }
    }

    // =========================================================
    // MAPEO
    // =========================================================

    private Usuario mapearSeguro(ResultSet rs)
            throws SQLException {

        Usuario obj = new Usuario();

        obj.setIdUsuario(
                rs.getObject("id_usuario", UUID.class)
        );

        obj.setIdEmpresa(
                rs.getObject("id_empresa", UUID.class)
        );

        obj.setIdRol(
                (Integer) rs.getObject("id_rol")
        );

        obj.setUsuario(
                rs.getString("usuario")
        );

        obj.setNombres(
                rs.getString("nombres")
        );

        obj.setActivo(
                (Boolean) rs.getObject("activo")
        );

        // IMPORTANTE:
        // claveHash queda null y no sale por la API.

        return obj;
    }

    private Usuario mapearConHash(ResultSet rs)
            throws SQLException {

        Usuario obj = mapearSeguro(rs);

        obj.setClaveHash(
                rs.getString("clave_hash")
        );

        return obj;
    }
}