package com.itq.dao;

import com.itq.config.ConexionBD;
import com.itq.model.Cliente;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ClienteDAO {
    public List<Cliente> listar() throws SQLException {
        String sql = "SELECT id_cliente, id_empresa, identificacion, nombres FROM cliente ORDER BY id_cliente";
        List<Cliente> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public Optional<Cliente> buscarPorId(UUID idCliente) throws SQLException {
        String sql = "SELECT id_cliente, id_empresa, identificacion, nombres FROM cliente WHERE id_cliente = ?";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(mapear(rs)) : Optional.empty(); }
        }
    }

    public Cliente insertar(Cliente obj) throws SQLException {
        String sql = "INSERT INTO cliente (id_empresa, identificacion, nombres) VALUES (?, ?, ?) RETURNING id_cliente";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, obj.getIdEmpresa());
            ps.setObject(2, obj.getIdentificacion());
            ps.setObject(3, obj.getNombres());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("No se generó la clave primaria");
                obj.setIdCliente(rs.getObject("id_cliente", UUID.class));
            }
        }
        return obj;
    }

    public boolean actualizar(Cliente obj) throws SQLException {
        String sql = "UPDATE cliente SET id_empresa = ?, identificacion = ?, nombres = ? WHERE id_cliente = ?";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, obj.getIdEmpresa());
            ps.setObject(2, obj.getIdentificacion());
            ps.setObject(3, obj.getNombres());
            ps.setObject(4, obj.getIdCliente());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(UUID idCliente) throws SQLException {
        String sql = "DELETE FROM cliente WHERE id_cliente = ?";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, idCliente);
            return ps.executeUpdate() > 0;
        }
    }

    private Cliente mapear(ResultSet rs) throws SQLException {
        Cliente obj = new Cliente();
        obj.setIdCliente(rs.getObject("id_cliente", UUID.class));
        obj.setIdEmpresa(rs.getObject("id_empresa", UUID.class));
        obj.setIdentificacion(rs.getString("identificacion"));
        obj.setNombres(rs.getString("nombres"));
        return obj;
    }
}
