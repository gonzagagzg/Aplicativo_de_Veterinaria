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
    public Empresa crear(Empresa obj) throws SQLException { validar(obj); return dao.insertar(obj); }
    public boolean actualizar(Empresa obj) throws SQLException { validar(obj); return dao.actualizar(obj); }
    public boolean eliminar(UUID idEmpresa) throws SQLException { return dao.eliminar(idEmpresa); }
    private void validar(Empresa obj) {
        if (obj == null) throw new IllegalArgumentException("Los datos de la empresa son obligatorios");
        if (vacio(obj.getRuc())) throw new IllegalArgumentException("El RUC es obligatorio");
        if (vacio(obj.getRazonSocial())) throw new IllegalArgumentException("La razón social es obligatoria");
        if (vacio(obj.getDireccion())) throw new IllegalArgumentException("La dirección es obligatoria");
        if (obj.isActivo() == null) throw new IllegalArgumentException("El estado activo es obligatorio");
        obj.setRuc(obj.getRuc().trim());
        obj.setRazonSocial(obj.getRazonSocial().trim());
        obj.setDireccion(obj.getDireccion().trim());
    }
    private boolean vacio(String valor) { return valor == null || valor.trim().isEmpty(); }
}
