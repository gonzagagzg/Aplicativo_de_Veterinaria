package com.itq.service;

import com.itq.dao.ProductoDAO;
import com.itq.model.Producto;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ProductoService {
    private final ProductoDAO dao = new ProductoDAO();
    public List<Producto> listar() throws SQLException { return dao.listar(); }
    public Optional<Producto> buscarPorId(UUID idProducto) throws SQLException { return dao.buscarPorId(idProducto); }
    public Producto crear(Producto obj) throws SQLException { return dao.insertar(obj); }
    public boolean actualizar(Producto obj) throws SQLException { return dao.actualizar(obj); }
    public boolean eliminar(UUID idProducto) throws SQLException { return dao.eliminar(idProducto); }
}
