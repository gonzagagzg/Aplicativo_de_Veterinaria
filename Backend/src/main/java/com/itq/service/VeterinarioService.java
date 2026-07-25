package com.itq.service;

import com.itq.dao.VeterinarioDAO;
import com.itq.model.Veterinario;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class VeterinarioService {
    private final VeterinarioDAO dao = new VeterinarioDAO();
    public List<Veterinario> listar() throws SQLException { return dao.listar(); }
    public Optional<Veterinario> buscarPorId(UUID idVeterinario) throws SQLException { return dao.buscarPorId(idVeterinario); }
    public Veterinario crear(Veterinario obj) throws SQLException { return dao.insertar(obj); }
    public boolean actualizar(Veterinario obj) throws SQLException { return dao.actualizar(obj); }
    public boolean eliminar(UUID idVeterinario) throws SQLException { return dao.eliminar(idVeterinario); }
}
