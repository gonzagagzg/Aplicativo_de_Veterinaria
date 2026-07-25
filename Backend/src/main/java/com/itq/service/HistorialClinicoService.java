package com.itq.service;

import com.itq.dao.HistorialClinicoDAO;
import com.itq.model.HistorialClinico;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class HistorialClinicoService {
    private final HistorialClinicoDAO dao = new HistorialClinicoDAO();
    public List<HistorialClinico> listar() throws SQLException { return dao.listar(); }
    public Optional<HistorialClinico> buscarPorId(UUID idHistorial) throws SQLException { return dao.buscarPorId(idHistorial); }
    public HistorialClinico crear(HistorialClinico obj) throws SQLException { return dao.insertar(obj); }
    public boolean actualizar(HistorialClinico obj) throws SQLException { return dao.actualizar(obj); }
    public boolean eliminar(UUID idHistorial) throws SQLException { return dao.eliminar(idHistorial); }
}
