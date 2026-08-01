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
    public Categoria crear(Categoria obj) throws SQLException { validar(obj); return dao.insertar(obj); }
    public boolean actualizar(Categoria obj) throws SQLException { validar(obj); return dao.actualizar(obj); }
    public boolean eliminar(Integer idCategoria) throws SQLException { return dao.eliminar(idCategoria); }
    private void validar(Categoria obj) {
        if (obj == null) throw new IllegalArgumentException("Los datos de la categoría son obligatorios");
        if (obj.getIdEmpresa() == null) throw new IllegalArgumentException("La empresa es obligatoria");
        if (vacio(obj.getNombre())) throw new IllegalArgumentException("El nombre de la categoría es obligatorio");
        obj.setNombre(obj.getNombre().trim());
    }
    private boolean vacio(String valor) { return valor == null || valor.trim().isEmpty(); }
}
