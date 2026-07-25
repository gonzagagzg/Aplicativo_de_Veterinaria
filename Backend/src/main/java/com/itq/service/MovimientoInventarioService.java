package com.itq.service;

import com.itq.dao.MovimientoInventarioDAO;
import com.itq.model.MovimientoInventario;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MovimientoInventarioService {
    private final MovimientoInventarioDAO dao = new MovimientoInventarioDAO();
    public List<MovimientoInventario> listar() throws SQLException { return dao.listar(); }
    public Optional<MovimientoInventario> buscarPorId(UUID idMovimiento) throws SQLException { return dao.buscarPorId(idMovimiento); }
    public MovimientoInventario crear(MovimientoInventario obj) throws SQLException { return dao.insertar(obj); }
    public boolean actualizar(MovimientoInventario obj) throws SQLException { return dao.actualizar(obj); }
    public boolean eliminar(UUID idMovimiento) throws SQLException { return dao.eliminar(idMovimiento); }
}
