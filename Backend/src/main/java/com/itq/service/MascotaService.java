package com.itq.service;

import com.itq.dao.MascotaDAO;
import com.itq.model.Mascota;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MascotaService {
    private final MascotaDAO dao = new MascotaDAO();
    public List<Mascota> listar() throws SQLException { return dao.listar(); }
    public Optional<Mascota> buscarPorId(UUID idMascota) throws SQLException { return dao.buscarPorId(idMascota); }
    public Mascota crear(Mascota obj) throws SQLException { validar(obj); return dao.insertar(obj); }
    public boolean actualizar(Mascota obj) throws SQLException { validar(obj); return dao.actualizar(obj); }
    public boolean eliminar(UUID idMascota) throws SQLException { return dao.eliminar(idMascota); }
    private void validar(Mascota obj) {
        if (obj == null) throw new IllegalArgumentException("Los datos de la mascota son obligatorios");
        if (obj.getIdEmpresa() == null) throw new IllegalArgumentException("La empresa es obligatoria");
        if (obj.getIdCliente() == null) throw new IllegalArgumentException("El cliente es obligatorio");
        if (obj.getIdRaza() == null || obj.getIdRaza() <= 0) throw new IllegalArgumentException("La raza es obligatoria");
        if (vacio(obj.getNombre())) throw new IllegalArgumentException("El nombre es obligatorio");
        if (obj.getFechaNacimiento() == null) throw new IllegalArgumentException("La fecha de nacimiento es obligatoria");
        if (obj.getFechaNacimiento().isAfter(LocalDate.now())) throw new IllegalArgumentException("La fecha de nacimiento no puede ser futura");
        obj.setNombre(obj.getNombre().trim());
    }
    private boolean vacio(String valor) { return valor == null || valor.trim().isEmpty(); }
}
