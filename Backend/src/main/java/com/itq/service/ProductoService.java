package com.itq.service;

import com.itq.dao.ProductoDAO;
import com.itq.model.Producto;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ProductoService {
    private final ProductoDAO dao = new ProductoDAO();
    public List<Producto> listar() throws SQLException { return dao.listar(); }
    public Optional<Producto> buscarPorId(UUID idProducto) throws SQLException { return dao.buscarPorId(idProducto); }
    public Producto crear(Producto obj) throws SQLException { validar(obj); return dao.insertar(obj); }
    public boolean actualizar(Producto obj) throws SQLException { validar(obj); return dao.actualizar(obj); }
    public boolean eliminar(UUID idProducto) throws SQLException { return dao.eliminar(idProducto); }
    private void validar(Producto obj) {
        if (obj == null) throw new IllegalArgumentException("Los datos del producto son obligatorios");
        if (obj.getIdEmpresa() == null) throw new IllegalArgumentException("La empresa es obligatoria");
        if (obj.getIdCategoria() == null || obj.getIdCategoria() <= 0) throw new IllegalArgumentException("La categoría es obligatoria");
        if (obj.getIdIva() == null || obj.getIdIva() <= 0) throw new IllegalArgumentException("El IVA es obligatorio");
        if (vacio(obj.getNombre())) throw new IllegalArgumentException("El nombre del producto es obligatorio");
        if (obj.getPrecioUnitario() == null || obj.getPrecioUnitario().compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("El precio unitario no puede ser negativo");
        if (obj.getStockActual() == null || obj.getStockActual() < 0) throw new IllegalArgumentException("El stock actual no puede ser negativo");
        if (obj.getStockMinimo() == null || obj.getStockMinimo() < 0) throw new IllegalArgumentException("El stock mínimo no puede ser negativo");
        obj.setNombre(obj.getNombre().trim());
    }
    private boolean vacio(String valor) { return valor == null || valor.trim().isEmpty(); }
}
