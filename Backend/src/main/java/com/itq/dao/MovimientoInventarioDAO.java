package com.itq.dao;

import com.itq.config.ConexionBD;
import com.itq.model.MovimientoInventario;
import java.sql.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MovimientoInventarioDAO {
    public List<MovimientoInventario> listar() throws SQLException {
        String sql = "SELECT id_movimiento, id_empresa, id_producto, id_factura, tipo, cantidad, fecha FROM movimiento_inventario ORDER BY id_movimiento";
        List<MovimientoInventario> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public Optional<MovimientoInventario> buscarPorId(UUID idMovimiento) throws SQLException {
        String sql = "SELECT id_movimiento, id_empresa, id_producto, id_factura, tipo, cantidad, fecha FROM movimiento_inventario WHERE id_movimiento = ?";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, idMovimiento);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(mapear(rs)) : Optional.empty(); }
        }
    }

    public MovimientoInventario insertar(MovimientoInventario obj) throws SQLException {
        String sql = "INSERT INTO movimiento_inventario (id_empresa, id_producto, id_factura, tipo, cantidad, fecha) VALUES (?, ?, ?, ?, ?, ?) RETURNING id_movimiento";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, obj.getIdEmpresa());
            ps.setObject(2, obj.getIdProducto());
            ps.setObject(3, obj.getIdFactura());
            ps.setObject(4, obj.getTipo());
            ps.setObject(5, obj.getCantidad());
            ps.setObject(6, obj.getFecha());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("No se generó la clave primaria");
                obj.setIdMovimiento(rs.getObject("id_movimiento", UUID.class));
            }
        }
        return obj;
    }

    public boolean actualizar(MovimientoInventario obj) throws SQLException {
        String sql = "UPDATE movimiento_inventario SET id_empresa = ?, id_producto = ?, id_factura = ?, tipo = ?, cantidad = ?, fecha = ? WHERE id_movimiento = ?";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, obj.getIdEmpresa());
            ps.setObject(2, obj.getIdProducto());
            ps.setObject(3, obj.getIdFactura());
            ps.setObject(4, obj.getTipo());
            ps.setObject(5, obj.getCantidad());
            ps.setObject(6, obj.getFecha());
            ps.setObject(7, obj.getIdMovimiento());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(UUID idMovimiento) throws SQLException {
        String sql = "DELETE FROM movimiento_inventario WHERE id_movimiento = ?";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, idMovimiento);
            return ps.executeUpdate() > 0;
        }
    }

    private MovimientoInventario mapear(ResultSet rs) throws SQLException {
        MovimientoInventario obj = new MovimientoInventario();
        obj.setIdMovimiento(rs.getObject("id_movimiento", UUID.class));
        obj.setIdEmpresa(rs.getObject("id_empresa", UUID.class));
        obj.setIdProducto(rs.getObject("id_producto", UUID.class));
        obj.setIdFactura(rs.getObject("id_factura", UUID.class));
        obj.setTipo(rs.getString("tipo"));
        obj.setCantidad((Integer) rs.getObject("cantidad"));
        obj.setFecha(rs.getObject("fecha", OffsetDateTime.class));
        return obj;
    }
}
