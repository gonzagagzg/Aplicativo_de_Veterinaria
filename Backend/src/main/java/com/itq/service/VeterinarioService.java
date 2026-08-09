package com.itq.service;

import com.itq.dao.UsuarioDAO;
import com.itq.dao.VeterinarioDAO;
import com.itq.model.Usuario;
import com.itq.model.Veterinario;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class VeterinarioService {

    private final VeterinarioDAO dao =
            new VeterinarioDAO();

    private final UsuarioDAO usuarioDAO =
            new UsuarioDAO();

    public List<Veterinario> listar(
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (superUsuario) {
            return dao.listar();
        }

        validarEmpresaSesion(idEmpresa);

        return dao.listarPorEmpresa(idEmpresa);
    }

    public Optional<Veterinario> buscarPorId(
            UUID idVeterinario,
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (superUsuario) {
            return dao.buscarPorId(idVeterinario);
        }

        validarEmpresaSesion(idEmpresa);

        return dao.buscarPorIdYEmpresa(
                idVeterinario,
                idEmpresa
        );
    }

    public Veterinario crear(
            Veterinario obj,
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (obj == null) {
            throw new IllegalArgumentException(
                    "Los datos del veterinario son obligatorios"
            );
        }

        if (!superUsuario) {

            validarEmpresaSesion(idEmpresa);

            /*
             * Un usuario normal no puede decidir
             * la empresa del veterinario.
             */
            obj.setIdEmpresa(idEmpresa);
        }

        validar(obj);

        /*
         * El usuario asociado al veterinario
         * debe pertenecer a la misma empresa.
         */
        validarUsuarioDeEmpresa(
                obj.getIdUsuario(),
                obj.getIdEmpresa()
        );

        return dao.insertar(obj);
    }

    public boolean actualizar(
            Veterinario obj,
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (obj == null ||
                obj.getIdVeterinario() == null) {

            throw new IllegalArgumentException(
                    "Los datos del veterinario son obligatorios"
            );
        }

        if (superUsuario) {

            validar(obj);

            validarUsuarioDeEmpresa(
                    obj.getIdUsuario(),
                    obj.getIdEmpresa()
            );

            return dao.actualizar(obj);
        }

        validarEmpresaSesion(idEmpresa);

        /*
         * Comprobamos primero que el veterinario
         * pertenezca realmente a la empresa del JWT.
         */
        if (dao.buscarPorIdYEmpresa(
                obj.getIdVeterinario(),
                idEmpresa
        ).isEmpty()) {

            return false;
        }

        /*
         * También comprobamos que el usuario
         * seleccionado pertenezca a esa empresa.
         */
        validarUsuarioDeEmpresa(
                obj.getIdUsuario(),
                idEmpresa
        );

        obj.setIdEmpresa(idEmpresa);

        validar(obj);

        return dao.actualizarPorEmpresa(
                obj,
                idEmpresa
        );
    }

    public boolean eliminar(
            UUID idVeterinario,
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (superUsuario) {
            return dao.eliminar(idVeterinario);
        }

        validarEmpresaSesion(idEmpresa);

        return dao.eliminarPorEmpresa(
                idVeterinario,
                idEmpresa
        );
    }

    private void validarUsuarioDeEmpresa(
            UUID idUsuario,
            UUID idEmpresa
    ) throws SQLException {

        if (idUsuario == null) {
            throw new IllegalArgumentException(
                    "El usuario es obligatorio"
            );
        }

        if (idEmpresa == null) {
            throw new IllegalArgumentException(
                    "La empresa es obligatoria"
            );
        }

        Optional<Usuario> usuario =
                usuarioDAO.buscarPorIdYEmpresa(
                        idUsuario,
                        idEmpresa
                );

        if (usuario.isEmpty()) {
            throw new IllegalArgumentException(
                    "El usuario no pertenece a la empresa indicada"
            );
        }
    }

    private void validar(Veterinario obj) {

        if (obj == null) {
            throw new IllegalArgumentException(
                    "Los datos del veterinario son obligatorios"
            );
        }

        if (obj.getIdUsuario() == null) {
            throw new IllegalArgumentException(
                    "El usuario es obligatorio"
            );
        }

        if (obj.getIdEmpresa() == null) {
            throw new IllegalArgumentException(
                    "La empresa es obligatoria"
            );
        }

        if (vacio(obj.getEspecialidad())) {
            throw new IllegalArgumentException(
                    "La especialidad es obligatoria"
            );
        }

        obj.setEspecialidad(
                obj.getEspecialidad().trim()
        );
    }

    private void validarEmpresaSesion(UUID idEmpresa) {

        if (idEmpresa == null) {
            throw new SecurityException(
                    "El usuario no tiene una empresa asignada"
            );
        }
    }

    private boolean vacio(String valor) {

        return valor == null ||
                valor.trim().isEmpty();
    }
}