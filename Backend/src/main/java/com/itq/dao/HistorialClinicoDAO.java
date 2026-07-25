package com.itq.dao;

import com.itq.config.ConexionBD;
import com.itq.model.HistorialClinico;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class HistorialClinicoDAO {
    public List<HistorialClinico> listar() throws SQLException {
        String sql = "SELECT id_historial, id_empresa, id_cita, peso_kg, temperatura_c, anamnesis, diagnostico FROM historial_clinico ORDER BY id_historial";
        List<HistorialClinico> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public Optional<HistorialClinico> buscarPorId(UUID idHistorial) throws SQLException {
        String sql = "SELECT id_historial, id_empresa, id_cita, peso_kg, temperatura_c, anamnesis, diagnostico FROM historial_clinico WHERE id_historial = ?";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, idHistorial);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(mapear(rs)) : Optional.empty(); }
        }
    }

    public HistorialClinico insertar(HistorialClinico obj) throws SQLException {
        String sql = "INSERT INTO historial_clinico (id_empresa, id_cita, peso_kg, temperatura_c, anamnesis, diagnostico) VALUES (?, ?, ?, ?, ?, ?) RETURNING id_historial";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, obj.getIdEmpresa());
            ps.setObject(2, obj.getIdCita());
            ps.setObject(3, obj.getPesoKg());
            ps.setObject(4, obj.getTemperaturaC());
            ps.setObject(5, obj.getAnamnesis());
            ps.setObject(6, obj.getDiagnostico());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("No se generó la clave primaria");
                obj.setIdHistorial(rs.getObject("id_historial", UUID.class));
            }
        }
        return obj;
    }

    public boolean actualizar(HistorialClinico obj) throws SQLException {
        String sql = "UPDATE historial_clinico SET id_empresa = ?, id_cita = ?, peso_kg = ?, temperatura_c = ?, anamnesis = ?, diagnostico = ? WHERE id_historial = ?";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, obj.getIdEmpresa());
            ps.setObject(2, obj.getIdCita());
            ps.setObject(3, obj.getPesoKg());
            ps.setObject(4, obj.getTemperaturaC());
            ps.setObject(5, obj.getAnamnesis());
            ps.setObject(6, obj.getDiagnostico());
            ps.setObject(7, obj.getIdHistorial());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(UUID idHistorial) throws SQLException {
        String sql = "DELETE FROM historial_clinico WHERE id_historial = ?";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, idHistorial);
            return ps.executeUpdate() > 0;
        }
    }

    private HistorialClinico mapear(ResultSet rs) throws SQLException {
        HistorialClinico obj = new HistorialClinico();
        obj.setIdHistorial(rs.getObject("id_historial", UUID.class));
        obj.setIdEmpresa(rs.getObject("id_empresa", UUID.class));
        obj.setIdCita(rs.getObject("id_cita", UUID.class));
        obj.setPesoKg(rs.getBigDecimal("peso_kg"));
        obj.setTemperaturaC(rs.getBigDecimal("temperatura_c"));
        obj.setAnamnesis(rs.getString("anamnesis"));
        obj.setDiagnostico(rs.getString("diagnostico"));
        return obj;
    }
}
