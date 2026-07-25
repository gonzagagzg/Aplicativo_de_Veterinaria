package com.itq.service;

import com.itq.dao.CategoriaDAO;
import com.itq.model.Categoria;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;


public class CategoriaService {
    private final CategoriaDAO dao = new CategoriaDAO();
    public List<Categoria> listar() throws SQLException { return dao.listar(); }
    public Optional<Categoria> buscarPorId(Integer idCategoria) throws SQLException { return dao.buscarPorId(idCategoria); }
    public Categoria crear(Categoria obj) throws SQLException { return dao.insertar(obj); }
    public boolean actualizar(Categoria obj) throws SQLException { return dao.actualizar(obj); }
    public boolean eliminar(Integer idCategoria) throws SQLException { return dao.eliminar(idCategoria); }
}
