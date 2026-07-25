package com.itq.service;

import com.itq.dao.MascotaDAO;
import com.itq.model.Mascota;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MascotaService {
    private final MascotaDAO dao = new MascotaDAO();
    public List<Mascota> listar() throws SQLException { return dao.listar(); }
    public Optional<Mascota> buscarPorId(UUID idMascota) throws SQLException { return dao.buscarPorId(idMascota); }
    public Mascota crear(Mascota obj) throws SQLException { return dao.insertar(obj); }
    public boolean actualizar(Mascota obj) throws SQLException { return dao.actualizar(obj); }
    public boolean eliminar(UUID idMascota) throws SQLException { return dao.eliminar(idMascota); }
}
