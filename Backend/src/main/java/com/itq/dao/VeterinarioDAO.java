package com.itq.dao;

import com.itq.config.ConexionBD;
import com.itq.model.Veterinario;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class VeterinarioDAO {
    public List<Veterinario> listar() throws SQLException {
        String sql = "SELECT id_veterinario, id_usuario, id_empresa, especialidad FROM veterinario ORDER BY id_veterinario";
        List<Veterinario> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public Optional<Veterinario> buscarPorId(UUID idVeterinario) throws SQLException {
        String sql = "SELECT id_veterinario, id_usuario, id_empresa, especialidad FROM veterinario WHERE id_veterinario = ?";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, idVeterinario);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? Optional.of(mapear(rs)) : Optional.empty(); }
        }
    }

    public Veterinario insertar(Veterinario obj) throws SQLException {
        String sql = "INSERT INTO veterinario (id_usuario, id_empresa, especialidad) VALUES (?, ?, ?) RETURNING id_veterinario";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, obj.getIdUsuario());
            ps.setObject(2, obj.getIdEmpresa());
            ps.setObject(3, obj.getEspecialidad());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("No se generó la clave primaria");
                obj.setIdVeterinario(rs.getObject("id_veterinario", UUID.class));
            }
        }
        return obj;
    }

    public boolean actualizar(Veterinario obj) throws SQLException {
        String sql = "UPDATE veterinario SET id_usuario = ?, id_empresa = ?, especialidad = ? WHERE id_veterinario = ?";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, obj.getIdUsuario());
            ps.setObject(2, obj.getIdEmpresa());
            ps.setObject(3, obj.getEspecialidad());
            ps.setObject(4, obj.getIdVeterinario());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(UUID idVeterinario) throws SQLException {
        String sql = "DELETE FROM veterinario WHERE id_veterinario = ?";
        try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setObject(1, idVeterinario);
            return ps.executeUpdate() > 0;
        }
    }

    private Veterinario mapear(ResultSet rs) throws SQLException {
        Veterinario obj = new Veterinario();
        obj.setIdVeterinario(rs.getObject("id_veterinario", UUID.class));
        obj.setIdUsuario(rs.getObject("id_usuario", UUID.class));
        obj.setIdEmpresa(rs.getObject("id_empresa", UUID.class));
        obj.setEspecialidad(rs.getString("especialidad"));
        return obj;
    }
}
