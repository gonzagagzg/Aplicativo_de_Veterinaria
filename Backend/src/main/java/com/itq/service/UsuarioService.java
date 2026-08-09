package com.itq.service;

import com.itq.dao.UsuarioDAO;
import com.itq.model.Usuario;
import com.itq.util.PasswordUtil;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UsuarioService {

    private final UsuarioDAO dao = new UsuarioDAO();

    public List<Usuario> listar(
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (superUsuario) {
            return dao.listar();
        }

        validarEmpresaSesion(idEmpresa);

        return dao.listarPorEmpresa(idEmpresa);
    }

    public Optional<Usuario> buscarPorId(
            UUID idUsuario,
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (superUsuario) {
            return dao.buscarPorId(idUsuario);
        }

        validarEmpresaSesion(idEmpresa);

        return dao.buscarPorIdYEmpresa(
                idUsuario,
                idEmpresa
        );
    }

    public Usuario crear(
            Usuario obj,
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (obj == null) {
            throw new IllegalArgumentException(
                    "Los datos del usuario son obligatorios"
            );
        }

        /*
         * Un usuario normal NO decide a qué empresa
         * pertenece el nuevo empleado.
         */
        if (!superUsuario) {
            validarEmpresaSesion(idEmpresa);
            obj.setIdEmpresa(idEmpresa);
        }

        validarBase(obj);

        if (vacio(obj.getClaveHash())) {
            throw new IllegalArgumentException(
                    "La contraseña es obligatoria"
            );
        }

        /*
         * Lo recibido como claveHash desde el JSON
         * se considera contraseña nueva en texto plano.
         * Antes de guardar SIEMPRE se convierte a BCrypt.
         */
        obj.setClaveHash(
                PasswordUtil.hash(
                        obj.getClaveHash().trim()
                )
        );

        Usuario creado = dao.insertar(obj);

        /*
         * Nunca devolvemos el hash al cliente.
         */
        creado.setClaveHash(null);

        return creado;
    }

    public boolean actualizar(
            Usuario obj,
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (obj == null || obj.getIdUsuario() == null) {
            throw new IllegalArgumentException(
                    "Los datos del usuario son obligatorios"
            );
        }

        Optional<Usuario> existenteOpt =
                dao.buscarPorIdConHash(obj.getIdUsuario());

        if (existenteOpt.isEmpty()) {
            return false;
        }

        Usuario existente = existenteOpt.get();

        /*
         * Un usuario normal no puede modificar
         * usuarios de otra empresa.
         */
        if (!superUsuario) {

            validarEmpresaSesion(idEmpresa);

            if (!idEmpresa.equals(
                    existente.getIdEmpresa()
            )) {
                return false;
            }

            obj.setIdEmpresa(idEmpresa);
        }

        validarBase(obj);

        /*
         * Si el PUT no trae contraseña nueva,
         * conservamos el hash existente.
         *
         * Si trae una contraseña,
         * generamos un BCrypt nuevo.
         */
        if (vacio(obj.getClaveHash())) {

            obj.setClaveHash(
                    existente.getClaveHash()
            );

        } else {

            obj.setClaveHash(
                    PasswordUtil.hash(
                            obj.getClaveHash().trim()
                    )
            );
        }

        if (superUsuario) {
            return dao.actualizar(obj);
        }

        return dao.actualizarPorEmpresa(
                obj,
                idEmpresa
        );
    }

    public boolean eliminar(
            UUID idUsuario,
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (superUsuario) {
            return dao.eliminar(idUsuario);
        }

        validarEmpresaSesion(idEmpresa);

        return dao.eliminarPorEmpresa(
                idUsuario,
                idEmpresa
        );
    }

    private void validarBase(Usuario obj) {

        if (obj.getIdEmpresa() == null) {
            throw new IllegalArgumentException(
                    "La empresa es obligatoria"
            );
        }

        if (obj.getIdRol() == null ||
                obj.getIdRol() <= 0) {

            throw new IllegalArgumentException(
                    "El rol es obligatorio"
            );
        }

        if (vacio(obj.getUsuario())) {
            throw new IllegalArgumentException(
                    "El nombre de usuario es obligatorio"
            );
        }

        if (vacio(obj.getNombres())) {
            throw new IllegalArgumentException(
                    "Los nombres son obligatorios"
            );
        }

        if (obj.isActivo() == null) {
            throw new IllegalArgumentException(
                    "El estado activo es obligatorio"
            );
        }

        obj.setUsuario(
                obj.getUsuario().trim()
        );

        obj.setNombres(
                obj.getNombres().trim()
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