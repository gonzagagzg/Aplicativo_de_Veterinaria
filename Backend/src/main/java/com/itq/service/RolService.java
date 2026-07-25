package com.itq.service;

import com.itq.dao.RolDAO;
import com.itq.model.Rol;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;


public class RolService {
    private final RolDAO dao = new RolDAO();
    public List<Rol> listar() throws SQLException { return dao.listar(); }
    public Optional<Rol> buscarPorId(Integer idRol) throws SQLException { return dao.buscarPorId(idRol); }
    public Rol crear(Rol obj) throws SQLException { return dao.insertar(obj); }
    public boolean actualizar(Rol obj) throws SQLException { return dao.actualizar(obj); }
    public boolean eliminar(Integer idRol) throws SQLException { return dao.eliminar(idRol); }
}
