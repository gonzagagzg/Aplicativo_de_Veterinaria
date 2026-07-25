package com.itq.service;

import com.itq.dao.CitaDAO;
import com.itq.model.Cita;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CitaService {
    private final CitaDAO dao = new CitaDAO();
    public List<Cita> listar() throws SQLException { return dao.listar(); }
    public Optional<Cita> buscarPorId(UUID idCita) throws SQLException { return dao.buscarPorId(idCita); }
    public Cita crear(Cita obj) throws SQLException { return dao.insertar(obj); }
    public boolean actualizar(Cita obj) throws SQLException { return dao.actualizar(obj); }
    public boolean eliminar(UUID idCita) throws SQLException { return dao.eliminar(idCita); }
}
