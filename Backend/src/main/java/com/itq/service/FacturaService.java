package com.itq.service;

import com.itq.dao.FacturaDAO;
import com.itq.model.Factura;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class FacturaService {
    private final FacturaDAO dao = new FacturaDAO();
    public List<Factura> listar() throws SQLException { return dao.listar(); }
    public Optional<Factura> buscarPorId(UUID idFactura) throws SQLException { return dao.buscarPorId(idFactura); }
    public Factura crear(Factura obj) throws SQLException { validar(obj); return dao.insertar(obj); }
    public boolean actualizar(Factura obj) throws SQLException { validar(obj); return dao.actualizar(obj); }
    public boolean eliminar(UUID idFactura) throws SQLException { return dao.eliminar(idFactura); }
    private void validar(Factura obj) {
        if (obj == null) throw new IllegalArgumentException("Los datos de la factura son obligatorios");
        if (obj.getIdEmpresa() == null) throw new IllegalArgumentException("La empresa es obligatoria");
        if (obj.getIdCliente() == null) throw new IllegalArgumentException("El cliente es obligatorio");
        if (obj.getIdUsuario() == null) throw new IllegalArgumentException("El usuario es obligatorio");
        if (obj.getTotal() == null || obj.getTotal().compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("El total no puede ser negativo");
        if (vacio(obj.getEstado())) throw new IllegalArgumentException("El estado es obligatorio");
        if (obj.getFecha() == null) throw new IllegalArgumentException("La fecha es obligatoria");
        obj.setEstado(obj.getEstado().trim().toUpperCase());
    }
    private boolean vacio(String valor) { return valor == null || valor.trim().isEmpty(); }
}
