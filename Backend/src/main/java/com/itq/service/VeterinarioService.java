package com.itq.service;

import com.itq.dao.VeterinarioDAO;
import com.itq.model.Veterinario;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class VeterinarioService {
    private final VeterinarioDAO dao = new VeterinarioDAO();
    public List<Veterinario> listar() throws SQLException { return dao.listar(); }
    public Optional<Veterinario> buscarPorId(UUID idVeterinario) throws SQLException { return dao.buscarPorId(idVeterinario); }
    public Veterinario crear(Veterinario obj) throws SQLException { validar(obj); return dao.insertar(obj); }
    public boolean actualizar(Veterinario obj) throws SQLException { validar(obj); return dao.actualizar(obj); }
    public boolean eliminar(UUID idVeterinario) throws SQLException { return dao.eliminar(idVeterinario); }
    private void validar(Veterinario obj) {
        if (obj == null) throw new IllegalArgumentException("Los datos del veterinario son obligatorios");
        if (obj.getIdUsuario() == null) throw new IllegalArgumentException("El usuario es obligatorio");
        if (obj.getIdEmpresa() == null) throw new IllegalArgumentException("La empresa es obligatoria");
        if (vacio(obj.getEspecialidad())) throw new IllegalArgumentException("La especialidad es obligatoria");
        obj.setEspecialidad(obj.getEspecialidad().trim());
    }
    private boolean vacio(String valor) { return valor == null || valor.trim().isEmpty(); }
}
