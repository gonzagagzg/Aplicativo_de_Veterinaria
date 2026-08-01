package com.itq.service;

import com.itq.dao.HistorialClinicoDAO;
import com.itq.model.HistorialClinico;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class HistorialClinicoService {
    private final HistorialClinicoDAO dao = new HistorialClinicoDAO();
    public List<HistorialClinico> listar() throws SQLException { return dao.listar(); }
    public Optional<HistorialClinico> buscarPorId(UUID idHistorial) throws SQLException { return dao.buscarPorId(idHistorial); }
    public HistorialClinico crear(HistorialClinico obj) throws SQLException { validar(obj); return dao.insertar(obj); }
    public boolean actualizar(HistorialClinico obj) throws SQLException { validar(obj); return dao.actualizar(obj); }
    public boolean eliminar(UUID idHistorial) throws SQLException { return dao.eliminar(idHistorial); }
    private void validar(HistorialClinico obj) {
        if (obj == null) throw new IllegalArgumentException("Los datos del historial clínico son obligatorios");
        if (obj.getIdEmpresa() == null) throw new IllegalArgumentException("La empresa es obligatoria");
        if (obj.getIdCita() == null) throw new IllegalArgumentException("La cita es obligatoria");
        if (obj.getPesoKg() != null && obj.getPesoKg().compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("El peso debe ser mayor que cero");
        if (obj.getTemperaturaC() != null && obj.getTemperaturaC().compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("La temperatura debe ser mayor que cero");
        if (vacio(obj.getAnamnesis())) throw new IllegalArgumentException("La anamnesis es obligatoria");
        if (vacio(obj.getDiagnostico())) throw new IllegalArgumentException("El diagnóstico es obligatorio");
        obj.setAnamnesis(obj.getAnamnesis().trim());
        obj.setDiagnostico(obj.getDiagnostico().trim());
    }
    private boolean vacio(String valor) { return valor == null || valor.trim().isEmpty(); }
}
