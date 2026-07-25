package com.itq.service;

import com.itq.dao.RecetaDAO;
import com.itq.model.Receta;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class RecetaService {
    private final RecetaDAO dao = new RecetaDAO();
    public List<Receta> listar() throws SQLException { return dao.listar(); }
    public Optional<Receta> buscarPorId(UUID idReceta) throws SQLException { return dao.buscarPorId(idReceta); }
    public Receta crear(Receta obj) throws SQLException { return dao.insertar(obj); }
    public boolean actualizar(Receta obj) throws SQLException { return dao.actualizar(obj); }
    public boolean eliminar(UUID idReceta) throws SQLException { return dao.eliminar(idReceta); }
}
