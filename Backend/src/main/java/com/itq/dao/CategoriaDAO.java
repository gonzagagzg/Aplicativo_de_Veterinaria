package com.itq.dao;

import com.itq.config.ConexionBD;
import com.itq.model.Categoria;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CategoriaDAO {
    public List<Categoria> listar() throws SQLException {
        String sql = "SELECT id_categoria, id_empresa, nombre FROM categoria ORDER BY id_categoria";
        List<Categoria> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public Optional<Categoria> buscarPorId(Integer idCategoria) throws SQLException {
        String sql = "SELECT id_categoria, id_empresa, nombre FROM categoria WHERE id_categoria = ?";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, idCategoria);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(mapear(rs)) : Optional.empty(); }
        }
    }

    public Categoria insertar(Categoria obj) throws SQLException {
        String sql = "INSERT INTO categoria (id_empresa, nombre) VALUES (?, ?) RETURNING id_categoria";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, obj.getIdEmpresa());
            ps.setObject(2, obj.getNombre());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("No se generó la clave primaria");
                obj.setIdCategoria((Integer) rs.getObject("id_categoria"));
            }
        }
        return obj;
    }

    public boolean actualizar(Categoria obj) throws SQLException {
        String sql = "UPDATE categoria SET id_empresa = ?, nombre = ? WHERE id_categoria = ?";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, obj.getIdEmpresa());
            ps.setObject(2, obj.getNombre());
            ps.setObject(3, obj.getIdCategoria());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(Integer idCategoria) throws SQLException {
        String sql = "DELETE FROM categoria WHERE id_categoria = ?";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, idCategoria);
            return ps.executeUpdate() > 0;
        }
    }

    private Categoria mapear(ResultSet rs) throws SQLException {
        Categoria obj = new Categoria();
        obj.setIdCategoria((Integer) rs.getObject("id_categoria"));
        obj.setIdEmpresa(rs.getObject("id_empresa", UUID.class));
        obj.setNombre(rs.getString("nombre"));
        return obj;
    }
}
