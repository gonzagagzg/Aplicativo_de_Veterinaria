package com.itq.dao;

import com.itq.config.ConexionBD;
import com.itq.model.Empresa;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class EmpresaDAO {
    public List<Empresa> listar() throws SQLException {
        String sql = "SELECT id_empresa, ruc, razon_social, direccion, activo FROM empresa ORDER BY id_empresa";
        List<Empresa> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public Optional<Empresa> buscarPorId(UUID idEmpresa) throws SQLException {
        String sql = "SELECT id_empresa, ruc, razon_social, direccion, activo FROM empresa WHERE id_empresa = ?";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, idEmpresa);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(mapear(rs)) : Optional.empty(); }
        }
    }

    public Empresa insertar(Empresa obj) throws SQLException {
        String sql = "INSERT INTO empresa (ruc, razon_social, direccion, activo) VALUES (?, ?, ?, ?) RETURNING id_empresa";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, obj.getRuc());
            ps.setObject(2, obj.getRazonSocial());
            ps.setObject(3, obj.getDireccion());
            ps.setObject(4, obj.isActivo());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("No se generó la clave primaria");
                obj.setIdEmpresa(rs.getObject("id_empresa", UUID.class));
            }
        }
        return obj;
    }

    public boolean actualizar(Empresa obj) throws SQLException {
        String sql = "UPDATE empresa SET ruc = ?, razon_social = ?, direccion = ?, activo = ? WHERE id_empresa = ?";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, obj.getRuc());
            ps.setObject(2, obj.getRazonSocial());
            ps.setObject(3, obj.getDireccion());
            ps.setObject(4, obj.isActivo());
            ps.setObject(5, obj.getIdEmpresa());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(UUID idEmpresa) throws SQLException {
        String sql = "DELETE FROM empresa WHERE id_empresa = ?";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, idEmpresa);
            return ps.executeUpdate() > 0;
        }
    }

    private Empresa mapear(ResultSet rs) throws SQLException {
        Empresa obj = new Empresa();
        obj.setIdEmpresa(rs.getObject("id_empresa", UUID.class));
        obj.setRuc(rs.getString("ruc"));
        obj.setRazonSocial(rs.getString("razon_social"));
        obj.setDireccion(rs.getString("direccion"));
        obj.setActivo((Boolean) rs.getObject("activo"));
        return obj;
    }
}
