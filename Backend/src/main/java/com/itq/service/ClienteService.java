package com.itq.service;

import com.itq.dao.ClienteDAO;
import com.itq.model.Cliente;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ClienteService {
    private final ClienteDAO dao = new ClienteDAO();
    public List<Cliente> listar() throws SQLException { return dao.listar(); }
    public Optional<Cliente> buscarPorId(UUID idCliente) throws SQLException { return dao.buscarPorId(idCliente); }
    public Cliente crear(Cliente obj) throws SQLException { return dao.insertar(obj); }
    public boolean actualizar(Cliente obj) throws SQLException { return dao.actualizar(obj); }
    public boolean eliminar(UUID idCliente) throws SQLException { return dao.eliminar(idCliente); }
}
