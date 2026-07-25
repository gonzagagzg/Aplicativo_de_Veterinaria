package com.itq.dao;

import com.itq.config.ConexionBD;
import com.itq.model.Producto;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ProductoDAO {
    public List<Producto> listar() throws SQLException {
        String sql = "SELECT id_producto, id_empresa, id_categoria, id_iva, nombre, precio_unitario, stock_actual, stock_minimo, fecha_caducidad FROM producto ORDER BY id_producto";
        List<Producto> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public Optional<Producto> buscarPorId(UUID idProducto) throws SQLException {
        String sql = "SELECT id_producto, id_empresa, id_categoria, id_iva, nombre, precio_unitario, stock_actual, stock_minimo, fecha_caducidad FROM producto WHERE id_producto = ?";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, idProducto);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(mapear(rs)) : Optional.empty(); }
        }
    }

    public Producto insertar(Producto obj) throws SQLException {
        String sql = "INSERT INTO producto (id_empresa, id_categoria, id_iva, nombre, precio_unitario, stock_actual, stock_minimo, fecha_caducidad) VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id_producto";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, obj.getIdEmpresa());
            ps.setObject(2, obj.getIdCategoria());
            ps.setObject(3, obj.getIdIva());
            ps.setObject(4, obj.getNombre());
            ps.setObject(5, obj.getPrecioUnitario());
            ps.setObject(6, obj.getStockActual());
            ps.setObject(7, obj.getStockMinimo());
            ps.setObject(8, obj.getFechaCaducidad());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("No se generó la clave primaria");
                obj.setIdProducto(rs.getObject("id_producto", UUID.class));
            }
        }
        return obj;
    }

    public boolean actualizar(Producto obj) throws SQLException {
        String sql = "UPDATE producto SET id_empresa = ?, id_categoria = ?, id_iva = ?, nombre = ?, precio_unitario = ?, stock_actual = ?, stock_minimo = ?, fecha_caducidad = ? WHERE id_producto = ?";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, obj.getIdEmpresa());
            ps.setObject(2, obj.getIdCategoria());
            ps.setObject(3, obj.getIdIva());
            ps.setObject(4, obj.getNombre());
            ps.setObject(5, obj.getPrecioUnitario());
            ps.setObject(6, obj.getStockActual());
            ps.setObject(7, obj.getStockMinimo());
            ps.setObject(8, obj.getFechaCaducidad());
            ps.setObject(9, obj.getIdProducto());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(UUID idProducto) throws SQLException {
        String sql = "DELETE FROM producto WHERE id_producto = ?";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, idProducto);
            return ps.executeUpdate() > 0;
        }
    }

    private Producto mapear(ResultSet rs) throws SQLException {
        Producto obj = new Producto();
        obj.setIdProducto(rs.getObject("id_producto", UUID.class));
        obj.setIdEmpresa(rs.getObject("id_empresa", UUID.class));
        obj.setIdCategoria((Integer) rs.getObject("id_categoria"));
        obj.setIdIva((Integer) rs.getObject("id_iva"));
        obj.setNombre(rs.getString("nombre"));
        obj.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
        obj.setStockActual((Integer) rs.getObject("stock_actual"));
        obj.setStockMinimo((Integer) rs.getObject("stock_minimo"));
        obj.setFechaCaducidad(rs.getObject("fecha_caducidad", LocalDate.class));
        return obj;
    }
}
