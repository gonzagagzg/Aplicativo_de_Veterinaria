package com.itq.service;

import com.itq.dao.EspecieDAO;
import com.itq.model.Especie;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;


public class EspecieService {
    private final EspecieDAO dao = new EspecieDAO();
    public List<Especie> listar() throws SQLException { return dao.listar(); }
    public Optional<Especie> buscarPorId(Integer idEspecie) throws SQLException { return dao.buscarPorId(idEspecie); }
    public Especie crear(Especie obj) throws SQLException { return dao.insertar(obj); }
    public boolean actualizar(Especie obj) throws SQLException { return dao.actualizar(obj); }
    public boolean eliminar(Integer idEspecie) throws SQLException { return dao.eliminar(idEspecie); }
}
