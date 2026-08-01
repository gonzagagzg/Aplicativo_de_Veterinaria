package com.itq.service;

import com.itq.dao.RecetaDetalleDAO;
import com.itq.model.RecetaDetalle;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class RecetaDetalleService {
    private final RecetaDetalleDAO dao = new RecetaDetalleDAO();
    public List<RecetaDetalle> listar() throws SQLException { return dao.listar(); }
    public Optional<RecetaDetalle> buscarPorId(UUID idDetalleReceta) throws SQLException { return dao.buscarPorId(idDetalleReceta); }
    public RecetaDetalle crear(RecetaDetalle obj) throws SQLException { validar(obj); return dao.insertar(obj); }
    public boolean actualizar(RecetaDetalle obj) throws SQLException { validar(obj); return dao.actualizar(obj); }
    public boolean eliminar(UUID idDetalleReceta) throws SQLException { return dao.eliminar(idDetalleReceta); }
    private void validar(RecetaDetalle obj) {
        if (obj == null) throw new IllegalArgumentException("Los datos del detalle de receta son obligatorios");
        if (obj.getIdReceta() == null) throw new IllegalArgumentException("La receta es obligatoria");
        if (obj.getIdProducto() == null) throw new IllegalArgumentException("El producto es obligatorio");
        if (vacio(obj.getDosis())) throw new IllegalArgumentException("La dosis es obligatoria");
        if (vacio(obj.getFrecuencia())) throw new IllegalArgumentException("La frecuencia es obligatoria");
        if (obj.getDuracionDias() == null || obj.getDuracionDias() <= 0) throw new IllegalArgumentException("La duración debe ser mayor que cero");
        obj.setDosis(obj.getDosis().trim());
        obj.setFrecuencia(obj.getFrecuencia().trim());
    }
    private boolean vacio(String valor) { return valor == null || valor.trim().isEmpty(); }
}
