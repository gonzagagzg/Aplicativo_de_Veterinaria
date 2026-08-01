package com.itq.service;

import com.itq.dao.MovimientoInventarioDAO;
import com.itq.model.MovimientoInventario;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MovimientoInventarioService {
    private final MovimientoInventarioDAO dao = new MovimientoInventarioDAO();
    public List<MovimientoInventario> listar() throws SQLException { return dao.listar(); }
    public Optional<MovimientoInventario> buscarPorId(UUID idMovimiento) throws SQLException { return dao.buscarPorId(idMovimiento); }
    public MovimientoInventario crear(MovimientoInventario obj) throws SQLException { validar(obj); return dao.insertar(obj); }
    public boolean actualizar(MovimientoInventario obj) throws SQLException { validar(obj); return dao.actualizar(obj); }
    public boolean eliminar(UUID idMovimiento) throws SQLException { return dao.eliminar(idMovimiento); }
    private void validar(MovimientoInventario obj) {
        if (obj == null) throw new IllegalArgumentException("Los datos del movimiento son obligatorios");
        if (obj.getIdEmpresa() == null) throw new IllegalArgumentException("La empresa es obligatoria");
        if (obj.getIdProducto() == null) throw new IllegalArgumentException("El producto es obligatorio");
        if (vacio(obj.getTipo())) throw new IllegalArgumentException("El tipo de movimiento es obligatorio");
        if (obj.getCantidad() == null || obj.getCantidad() <= 0) throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        if (obj.getFecha() == null) throw new IllegalArgumentException("La fecha es obligatoria");
        obj.setTipo(obj.getTipo().trim().toUpperCase());
    }
    private boolean vacio(String valor) { return valor == null || valor.trim().isEmpty(); }
}
