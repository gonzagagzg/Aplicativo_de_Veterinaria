package com.itq.service;

import com.itq.dao.UsuarioDAO;
import com.itq.model.Usuario;
import com.itq.util.PasswordUtil;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class UsuarioService {

    private final UsuarioDAO dao = new UsuarioDAO();

    /*
     * Roles que un usuario local puede asignar
     * a empleados de su propia veterinaria.
     *
     * 3 = Veterinario
     * 4 = Recepcionista
     * 5 = Asistente Clínico
     * 7 = Farmacéutico
     */
    private static final Set<Integer> ROLES_EMPLEADOS =
            Set.of(3, 4, 5, 7);

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

        if (idUsuario == null) {
            throw new IllegalArgumentException(
                    "El usuario es obligatorio"
            );
        }

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
         * Un usuario normal no puede decidir
         * a qué empresa pertenece el empleado.
         *
         * Siempre se utiliza la empresa obtenida
         * desde la sesión/JWT.
         */
        if (!superUsuario) {

            validarEmpresaSesion(idEmpresa);

            obj.setIdEmpresa(idEmpresa);

            /*
             * Un usuario local solamente puede
             * asignar roles de empleados.
             */
            validarRolEmpleado(obj.getIdRol());
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
         *
         * Antes de guardar siempre se convierte
         * a BCrypt.
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

        if (obj == null ||
                obj.getIdUsuario() == null) {

            throw new IllegalArgumentException(
                    "Los datos del usuario son obligatorios"
            );
        }

        Optional<Usuario> existenteOpt =
                dao.buscarPorIdConHash(
                        obj.getIdUsuario()
                );

        if (existenteOpt.isEmpty()) {
            return false;
        }

        Usuario existente =
                existenteOpt.get();

        /*
         * Un usuario normal no puede modificar
         * usuarios pertenecientes a otra empresa.
         */
        if (!superUsuario) {

            validarEmpresaSesion(idEmpresa);

            if (!idEmpresa.equals(
                    existente.getIdEmpresa()
            )) {
                return false;
            }

            /*
             * La empresa siempre se toma
             * desde la sesión/JWT.
             */
            obj.setIdEmpresa(idEmpresa);

            /*
             * Tampoco puede convertir un empleado
             * en Administrador Global,
             * Administrador Local o SuperUsuario.
             */
            validarRolEmpleado(obj.getIdRol());
        }

        validarBase(obj);

        /*
         * Si el PUT no trae una contraseña nueva,
         * conservamos el hash existente.
         *
         * Si trae una contraseña nueva,
         * generamos un nuevo BCrypt.
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

        if (idUsuario == null) {
            throw new IllegalArgumentException(
                    "El usuario es obligatorio"
            );
        }

        if (superUsuario) {
            return dao.eliminar(idUsuario);
        }

        validarEmpresaSesion(idEmpresa);

        /*
         * Primero comprobamos que el usuario
         * realmente pertenece a la veterinaria
         * de la sesión.
         */
        Optional<Usuario> existente =
                dao.buscarPorIdYEmpresa(
                        idUsuario,
                        idEmpresa
                );

        if (existente.isEmpty()) {
            return false;
        }

        /*
         * Un usuario local tampoco puede eliminar
         * una cuenta administrativa reservada.
         */
        validarRolEmpleado(
                existente.get().getIdRol()
        );

        return dao.eliminarPorEmpresa(
                idUsuario,
                idEmpresa
        );
    }

    private void validarBase(
            Usuario obj
    ) {

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

    /*
     * Impide que un usuario local pueda
     * escalar privilegios asignando roles
     * administrativos reservados.
     */
    private void validarRolEmpleado(
            Integer idRol
    ) {

        if (idRol == null) {
            throw new IllegalArgumentException(
                    "El rol es obligatorio"
            );
        }

        if (!ROLES_EMPLEADOS.contains(idRol)) {

            throw new SecurityException(
                    "No tiene autorización para asignar este rol"
            );
        }
    }

    private void validarEmpresaSesion(
            UUID idEmpresa
    ) {

        if (idEmpresa == null) {
            throw new SecurityException(
                    "El usuario no tiene una empresa asignada"
            );
        }
    }

    private boolean vacio(
            String valor
    ) {

        return valor == null ||
                valor.trim().isEmpty();
    }
}