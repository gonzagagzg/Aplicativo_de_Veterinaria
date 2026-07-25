package com.itq.service;

import com.itq.dao.RazaDAO;
import com.itq.model.Raza;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;


public class RazaService {
    private final RazaDAO dao = new RazaDAO();
    public List<Raza> listar() throws SQLException { return dao.listar(); }
    public Optional<Raza> buscarPorId(Integer idRaza) throws SQLException { return dao.buscarPorId(idRaza); }
    public Raza crear(Raza obj) throws SQLException { return dao.insertar(obj); }
    public boolean actualizar(Raza obj) throws SQLException { return dao.actualizar(obj); }
    public boolean eliminar(Integer idRaza) throws SQLException { return dao.eliminar(idRaza); }
}
