package com.itq.service;

import com.itq.dao.PermisoDAO;
import com.itq.model.Permiso;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;


public class PermisoService {
    private final PermisoDAO dao = new PermisoDAO();
    public List<Permiso> listar() throws SQLException { return dao.listar(); }
    public Optional<Permiso> buscarPorId(Integer idPermiso) throws SQLException { return dao.buscarPorId(idPermiso); }
    public Permiso crear(Permiso obj) throws SQLException { return dao.insertar(obj); }
    public boolean actualizar(Permiso obj) throws SQLException { return dao.actualizar(obj); }
    public boolean eliminar(Integer idPermiso) throws SQLException { return dao.eliminar(idPermiso); }
}
