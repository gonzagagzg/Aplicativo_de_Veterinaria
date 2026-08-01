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
    public Cliente crear(Cliente obj) throws SQLException { validar(obj); return dao.insertar(obj); }
    public boolean actualizar(Cliente obj) throws SQLException { validar(obj); return dao.actualizar(obj); }
    public boolean eliminar(UUID idCliente) throws SQLException { return dao.eliminar(idCliente); }
    private void validar(Cliente obj) {
        if (obj == null) throw new IllegalArgumentException("Los datos del cliente son obligatorios");
        if (obj.getIdEmpresa() == null) throw new IllegalArgumentException("La empresa es obligatoria");
        if (vacio(obj.getIdentificacion())) throw new IllegalArgumentException("La identificación es obligatoria");
        if (vacio(obj.getNombres())) throw new IllegalArgumentException("Los nombres son obligatorios");
        obj.setIdentificacion(obj.getIdentificacion().trim());
        obj.setNombres(obj.getNombres().trim());
    }
    private boolean vacio(String valor) { return valor == null || valor.trim().isEmpty(); }
}
