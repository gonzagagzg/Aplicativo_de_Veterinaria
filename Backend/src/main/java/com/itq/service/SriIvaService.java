package com.itq.service;

import com.itq.dao.SriIvaDAO;
import com.itq.model.SriIva;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;


public class SriIvaService {
    private final SriIvaDAO dao = new SriIvaDAO();
    public List<SriIva> listar() throws SQLException { return dao.listar(); }
    public Optional<SriIva> buscarPorId(Integer idIva) throws SQLException { return dao.buscarPorId(idIva); }
    public SriIva crear(SriIva obj) throws SQLException { return dao.insertar(obj); }
    public boolean actualizar(SriIva obj) throws SQLException { return dao.actualizar(obj); }
    public boolean eliminar(Integer idIva) throws SQLException { return dao.eliminar(idIva); }
}
