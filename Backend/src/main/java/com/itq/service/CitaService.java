package com.itq.service;

import com.itq.dao.CitaDAO;
import com.itq.model.Cita;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CitaService {
    private final CitaDAO dao = new CitaDAO();
    public List<Cita> listar() throws SQLException { return dao.listar(); }
    public Optional<Cita> buscarPorId(UUID idCita) throws SQLException { return dao.buscarPorId(idCita); }
    public Cita crear(Cita obj) throws SQLException { validar(obj); return dao.insertar(obj); }
    public boolean actualizar(Cita obj) throws SQLException { validar(obj); return dao.actualizar(obj); }
    public boolean eliminar(UUID idCita) throws SQLException { return dao.eliminar(idCita); }
    private void validar(Cita obj) {
        if (obj == null) throw new IllegalArgumentException("Los datos de la cita son obligatorios");
        if (obj.getIdEmpresa() == null) throw new IllegalArgumentException("La empresa es obligatoria");
        if (obj.getIdMascota() == null) throw new IllegalArgumentException("La mascota es obligatoria");
        if (obj.getIdVeterinario() == null) throw new IllegalArgumentException("El veterinario es obligatorio");
        if (obj.getFechaHora() == null) throw new IllegalArgumentException("La fecha y hora son obligatorias");
        if (vacio(obj.getEstado())) throw new IllegalArgumentException("El estado es obligatorio");
        obj.setEstado(obj.getEstado().trim().toUpperCase());
    }
    private boolean vacio(String valor) { return valor == null || valor.trim().isEmpty(); }
}
