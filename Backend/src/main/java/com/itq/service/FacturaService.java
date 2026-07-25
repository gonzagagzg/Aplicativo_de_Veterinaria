package com.itq.service;

import com.itq.dao.FacturaDAO;
import com.itq.model.Factura;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class FacturaService {
    private final FacturaDAO dao = new FacturaDAO();
    public List<Factura> listar() throws SQLException { return dao.listar(); }
    public Optional<Factura> buscarPorId(UUID idFactura) throws SQLException { return dao.buscarPorId(idFactura); }
    public Factura crear(Factura obj) throws SQLException { return dao.insertar(obj); }
    public boolean actualizar(Factura obj) throws SQLException { return dao.actualizar(obj); }
    public boolean eliminar(UUID idFactura) throws SQLException { return dao.eliminar(idFactura); }
}
