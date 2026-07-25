package com.itq.dao;

import com.itq.config.ConexionBD;
import com.itq.model.Usuario;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UsuarioDAO {
    public List<Usuario> listar() throws SQLException {
        String sql = "SELECT id_usuario, id_empresa, id_rol, usuario, clave_hash, nombres, activo FROM usuario ORDER BY id_usuario";
        List<Usuario> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public Optional<Usuario> buscarPorId(UUID idUsuario) throws SQLException {
        String sql = "SELECT id_usuario, id_empresa, id_rol, usuario, clave_hash, nombres, activo FROM usuario WHERE id_usuario = ?";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(mapear(rs)) : Optional.empty(); }
        }
    }

    public Usuario insertar(Usuario obj) throws SQLException {
        String sql = "INSERT INTO usuario (id_empresa, id_rol, usuario, clave_hash, nombres, activo) VALUES (?, ?, ?, ?, ?, ?) RETURNING id_usuario";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, obj.getIdEmpresa());
            ps.setObject(2, obj.getIdRol());
            ps.setObject(3, obj.getUsuario());
            ps.setObject(4, obj.getClaveHash());
            ps.setObject(5, obj.getNombres());
            ps.setObject(6, obj.isActivo());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("No se generó la clave primaria");
                obj.setIdUsuario(rs.getObject("id_usuario", UUID.class));
            }
        }
        return obj;
    }

    public boolean actualizar(Usuario obj) throws SQLException {
        String sql = "UPDATE usuario SET id_empresa = ?, id_rol = ?, usuario = ?, clave_hash = ?, nombres = ?, activo = ? WHERE id_usuario = ?";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, obj.getIdEmpresa());
            ps.setObject(2, obj.getIdRol());
            ps.setObject(3, obj.getUsuario());
            ps.setObject(4, obj.getClaveHash());
            ps.setObject(5, obj.getNombres());
            ps.setObject(6, obj.isActivo());
            ps.setObject(7, obj.getIdUsuario());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(UUID idUsuario) throws SQLException {
        String sql = "DELETE FROM usuario WHERE id_usuario = ?";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, idUsuario);
            return ps.executeUpdate() > 0;
        }
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario obj = new Usuario();
        obj.setIdUsuario(rs.getObject("id_usuario", UUID.class));
        obj.setIdEmpresa(rs.getObject("id_empresa", UUID.class));
        obj.setIdRol((Integer) rs.getObject("id_rol"));
        obj.setUsuario(rs.getString("usuario"));
        obj.setClaveHash(rs.getString("clave_hash"));
        obj.setNombres(rs.getString("nombres"));
        obj.setActivo((Boolean) rs.getObject("activo"));
        return obj;
    }
}
