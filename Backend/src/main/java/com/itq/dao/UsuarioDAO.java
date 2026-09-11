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
                       usuario, nombres, activo, tipobloqueo, notificacion
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
                       usuario, nombres, activo, tipobloqueo, notificacion
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
                       usuario, nombres, activo, tipobloqueo, notificacion
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
                       usuario, nombres, activo, tipobloqueo, notificacion
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
                       usuario, clave_hash, nombres, activo, tipobloqueo, notificacion
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
                       usuario, clave_hash, nombres, activo, tipobloqueo, notificacion
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
                (id_empresa, id_rol, usuario, clave_hash, nombres, activo, tipobloqueo, notificacion)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
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
            ps.setString(7, obj.getTipobloqueo());
            ps.setString(8, obj.getNotificacion());

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
                    activo = ?,
                    tipobloqueo = ?,
                    notificacion = ?
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
            ps.setString(7, obj.getTipobloqueo());
            ps.setString(8, obj.getNotificacion());
            ps.setObject(9, obj.getIdUsuario());

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
                    activo = ?,
                    tipobloqueo = ?,
                    notificacion = ?
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
            ps.setString(6, obj.getTipobloqueo());
            ps.setString(7, obj.getNotificacion());
            ps.setObject(8, obj.getIdUsuario());
            ps.setObject(9, idEmpresa);

            return ps.executeUpdate() > 0;
        }
    }

    // =========================================================
    // BLOQUEO / DESBLOQUEO
    // Solo actualiza activo, tipobloqueo y notificacion.
    // No toca clave_hash ni demás datos.
    // =========================================================

    public boolean actualizarBloqueo(
            UUID idUsuario,
            Boolean activo,
            String tipobloqueo
    ) throws SQLException {

        String sql = """
                UPDATE usuario
                SET activo = ?,
                    tipobloqueo = ?,
                    notificacion = NULL
                WHERE id_usuario = ?
                """;

        try (
                Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setBoolean(1, activo);
            ps.setString(2, tipobloqueo);
            ps.setObject(3, idUsuario);

            return ps.executeUpdate() > 0;
        }
    }

    // =========================================================
    // NOTIFICACIÓN DE BLOQUEO
    // Solo actualiza notificacion; no toca activo ni tipobloqueo.
    // =========================================================

    public boolean actualizarNotificacion(
            UUID idUsuario,
            String notificacion
    ) throws SQLException {

        String sql = """
                UPDATE usuario
                SET notificacion = ?
                WHERE id_usuario = ?
                """;

        try (
                Connection cn = ConexionBD.obtenerConexion();
                PreparedStatement ps = cn.prepareStatement(sql)
        ) {

            ps.setString(1, notificacion);
            ps.setObject(2, idUsuario);

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

        obj.setTipobloqueo(
                rs.getString("tipobloqueo")
        );

        obj.setNotificacion(
                rs.getString("notificacion")
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