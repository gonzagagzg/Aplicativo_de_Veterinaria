package com.itq.service;

import com.itq.dao.EmpresaDAO;
import com.itq.model.Empresa;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class EmpresaService {
    private final EmpresaDAO dao = new EmpresaDAO();
    public List<Empresa> listar() throws SQLException { return dao.listar(); }
    public Optional<Empresa> buscarPorId(UUID idEmpresa) throws SQLException { return dao.buscarPorId(idEmpresa); }
    public Empresa crear(Empresa obj) throws SQLException { return dao.insertar(obj); }
    public boolean actualizar(Empresa obj) throws SQLException { return dao.actualizar(obj); }
    public boolean eliminar(UUID idEmpresa) throws SQLException { return dao.eliminar(idEmpresa); }
}
