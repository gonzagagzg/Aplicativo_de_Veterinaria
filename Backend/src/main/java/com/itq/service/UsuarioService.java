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
    public Usuario crear(Usuario obj) throws SQLException { return dao.insertar(obj); }
    public boolean actualizar(Usuario obj) throws SQLException { return dao.actualizar(obj); }
    public boolean eliminar(UUID idUsuario) throws SQLException { return dao.eliminar(idUsuario); }
}
