package com.itq.service;

import com.itq.dao.UsuarioDAO;
import com.itq.model.Usuario;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UsuarioService {
    private final UsuarioDAO dao = new UsuarioDAO();
    public List<Usuario> listar() throws SQLException { return dao.listar(); }
    public Optional<Usuario> buscarPorId(UUID idUsuario) throws SQLException { return dao.buscarPorId(idUsuario); }
    public Usuario crear(Usuario obj) throws SQLException { validar(obj); return dao.insertar(obj); }
    public boolean actualizar(Usuario obj) throws SQLException { validar(obj); return dao.actualizar(obj); }
    public boolean eliminar(UUID idUsuario) throws SQLException { return dao.eliminar(idUsuario); }
    private void validar(Usuario obj) {
        if (obj == null) throw new IllegalArgumentException("Los datos del usuario son obligatorios");
        if (obj.getIdEmpresa() == null) throw new IllegalArgumentException("La empresa es obligatoria");
        if (obj.getIdRol() == null || obj.getIdRol() <= 0) throw new IllegalArgumentException("El rol es obligatorio");
        if (vacio(obj.getUsuario())) throw new IllegalArgumentException("El nombre de usuario es obligatorio");
        if (vacio(obj.getClaveHash())) throw new IllegalArgumentException("La clave es obligatoria");
        if (vacio(obj.getNombres())) throw new IllegalArgumentException("Los nombres son obligatorios");
        if (obj.isActivo() == null) throw new IllegalArgumentException("El estado activo es obligatorio");
        obj.setUsuario(obj.getUsuario().trim());
        obj.setClaveHash(obj.getClaveHash().trim());
        obj.setNombres(obj.getNombres().trim());
    }
    private boolean vacio(String valor) { return valor == null || valor.trim().isEmpty(); }
}
