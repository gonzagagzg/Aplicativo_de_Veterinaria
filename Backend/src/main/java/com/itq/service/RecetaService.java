package com.itq.service;

import com.itq.dao.RecetaDAO;
import com.itq.model.Receta;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class RecetaService {
    private final RecetaDAO dao = new RecetaDAO();
    public List<Receta> listar() throws SQLException { return dao.listar(); }
    public Optional<Receta> buscarPorId(UUID idReceta) throws SQLException { return dao.buscarPorId(idReceta); }
    public Receta crear(Receta obj) throws SQLException { validar(obj); return dao.insertar(obj); }
    public boolean actualizar(Receta obj) throws SQLException { validar(obj); return dao.actualizar(obj); }
    public boolean eliminar(UUID idReceta) throws SQLException { return dao.eliminar(idReceta); }
    private void validar(Receta obj) {
        if (obj == null) throw new IllegalArgumentException("Los datos de la receta son obligatorios");
        if (obj.getIdEmpresa() == null) throw new IllegalArgumentException("La empresa es obligatoria");
        if (obj.getIdHistorial() == null) throw new IllegalArgumentException("El historial clínico es obligatorio");
        if (vacio(obj.getIndicacionesGenerales())) throw new IllegalArgumentException("Las indicaciones generales son obligatorias");
        obj.setIndicacionesGenerales(obj.getIndicacionesGenerales().trim());
    }
    private boolean vacio(String valor) { return valor == null || valor.trim().isEmpty(); }
}
