package com.itq.service;

import com.itq.dao.FacturaDetalleDAO;
import com.itq.model.FacturaDetalle;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class FacturaDetalleService {
    private final FacturaDetalleDAO dao = new FacturaDetalleDAO();
    public List<FacturaDetalle> listar() throws SQLException { return dao.listar(); }
    public Optional<FacturaDetalle> buscarPorId(UUID idDetalle) throws SQLException { return dao.buscarPorId(idDetalle); }
    public FacturaDetalle crear(FacturaDetalle obj) throws SQLException { return dao.insertar(obj); }
    public boolean actualizar(FacturaDetalle obj) throws SQLException { return dao.actualizar(obj); }
    public boolean eliminar(UUID idDetalle) throws SQLException { return dao.eliminar(idDetalle); }
}
