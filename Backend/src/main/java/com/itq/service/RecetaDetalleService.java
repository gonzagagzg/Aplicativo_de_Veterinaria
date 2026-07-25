package com.itq.service;

import com.itq.dao.RecetaDetalleDAO;
import com.itq.model.RecetaDetalle;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class RecetaDetalleService {
    private final RecetaDetalleDAO dao = new RecetaDetalleDAO();
    public List<RecetaDetalle> listar() throws SQLException { return dao.listar(); }
    public Optional<RecetaDetalle> buscarPorId(UUID idDetalleReceta) throws SQLException { return dao.buscarPorId(idDetalleReceta); }
    public RecetaDetalle crear(RecetaDetalle obj) throws SQLException { return dao.insertar(obj); }
    public boolean actualizar(RecetaDetalle obj) throws SQLException { return dao.actualizar(obj); }
    public boolean eliminar(UUID idDetalleReceta) throws SQLException { return dao.eliminar(idDetalleReceta); }
}
