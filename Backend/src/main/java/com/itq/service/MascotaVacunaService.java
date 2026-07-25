package com.itq.service;

import com.itq.dao.MascotaVacunaDAO;
import com.itq.model.MascotaVacuna;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MascotaVacunaService {
    private final MascotaVacunaDAO dao = new MascotaVacunaDAO();
    public List<MascotaVacuna> listar() throws SQLException { return dao.listar(); }
    public Optional<MascotaVacuna> buscarPorId(UUID idMascotaVacuna) throws SQLException { return dao.buscarPorId(idMascotaVacuna); }
    public MascotaVacuna crear(MascotaVacuna obj) throws SQLException { return dao.insertar(obj); }
    public boolean actualizar(MascotaVacuna obj) throws SQLException { return dao.actualizar(obj); }
    public boolean eliminar(UUID idMascotaVacuna) throws SQLException { return dao.eliminar(idMascotaVacuna); }
}
