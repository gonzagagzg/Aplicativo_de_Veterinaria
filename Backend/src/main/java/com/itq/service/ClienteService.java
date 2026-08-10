package com.itq.service;

import com.itq.dao.ClienteDAO;
import com.itq.dto.PaginaResponse;
import com.itq.model.Cliente;
import com.itq.validation.EcuadorValidator;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ClienteService {

    private final ClienteDAO dao =
            new ClienteDAO();

    // =========================================================
    // LISTADO ANTIGUO
    // Se conserva por compatibilidad
    // =========================================================

    public List<Cliente> listar(
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (superUsuario) {

            return dao.listar();
        }

        validarEmpresaSesion(
                idEmpresa
        );

        return dao.listarPorEmpresa(
                idEmpresa
        );
    }

    // =========================================================
    // LISTADO PAGINADO
    // =========================================================

    public PaginaResponse<Cliente> listarPaginado(
            UUID idEmpresa,
            boolean superUsuario,
            int pagina,
            int tamanio
    ) throws SQLException {

        validarPaginacion(
                pagina,
                tamanio
        );

        int offset =
                pagina * tamanio;

        List<Cliente> contenido;

        long totalRegistros;

        if (superUsuario) {

            contenido =
                    dao.listarPaginado(
                            tamanio,
                            offset
                    );

            totalRegistros =
                    dao.contar();

        } else {

            validarEmpresaSesion(
                    idEmpresa
            );

            contenido =
                    dao.listarPorEmpresaPaginado(
                            idEmpresa,
                            tamanio,
                            offset
                    );

            totalRegistros =
                    dao.contarPorEmpresa(
                            idEmpresa
                    );
        }

        return new PaginaResponse<>(
                contenido,
                totalRegistros,
                pagina,
                tamanio
        );
    }

    // =========================================================
    // BUSCAR POR ID
    // =========================================================

    public Optional<Cliente> buscarPorId(
            UUID idCliente,
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (idCliente == null) {

            throw new IllegalArgumentException(
                    "El cliente es obligatorio"
            );
        }

        if (superUsuario) {

            return dao.buscarPorId(
                    idCliente
            );
        }

        validarEmpresaSesion(
                idEmpresa
        );

        return dao.buscarPorIdYEmpresa(
                idCliente,
                idEmpresa
        );
    }

    // =========================================================
    // CREAR
    // =========================================================

    public Cliente crear(
            Cliente obj,
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (obj == null) {

            throw new IllegalArgumentException(
                    "Los datos del cliente son obligatorios"
            );
        }

        if (!superUsuario) {

            validarEmpresaSesion(
                    idEmpresa
            );

            obj.setIdEmpresa(
                    idEmpresa
            );
        }

        validar(obj);

        return dao.insertar(
                obj
        );
    }

    // =========================================================
    // ACTUALIZAR
    // =========================================================

    public boolean actualizar(
            Cliente obj,
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (obj == null) {

            throw new IllegalArgumentException(
                    "Los datos del cliente son obligatorios"
            );
        }

        if (obj.getIdCliente() == null) {

            throw new IllegalArgumentException(
                    "El cliente es obligatorio"
            );
        }

        if (superUsuario) {

            validar(obj);

            return dao.actualizar(
                    obj
            );
        }

        validarEmpresaSesion(
                idEmpresa
        );

        obj.setIdEmpresa(
                idEmpresa
        );

        validar(obj);

        return dao.actualizarPorEmpresa(
                obj,
                idEmpresa
        );
    }

    // =========================================================
    // ELIMINAR
    // =========================================================

    public boolean eliminar(
            UUID idCliente,
            UUID idEmpresa,
            boolean superUsuario
    ) throws SQLException {

        if (idCliente == null) {

            throw new IllegalArgumentException(
                    "El cliente es obligatorio"
            );
        }

        if (superUsuario) {

            return dao.eliminar(
                    idCliente
            );
        }

        validarEmpresaSesion(
                idEmpresa
        );

        return dao.eliminarPorEmpresa(
                idCliente,
                idEmpresa
        );
    }

    // =========================================================
    // VALIDACIÓN CLIENTE
    // =========================================================

    private void validar(
            Cliente obj
    ) {

        if (obj == null) {

            throw new IllegalArgumentException(
                    "Los datos del cliente son obligatorios"
            );
        }

        if (obj.getIdEmpresa() == null) {

            throw new IllegalArgumentException(
                    "La empresa es obligatoria"
            );
        }

        if (vacio(
                obj.getIdentificacion()
        )) {

            throw new IllegalArgumentException(
                    "La identificación es obligatoria"
            );
        }

        String identificacion =
                EcuadorValidator.limpiarNumero(
                        obj.getIdentificacion()
                );

        if (identificacion == null ||
                identificacion.length() != 10) {

            throw new IllegalArgumentException(
                    "La cédula debe contener 10 dígitos"
            );
        }

        if (!EcuadorValidator.cedulaValida(
                identificacion
        )) {

            throw new IllegalArgumentException(
                    "La cédula ecuatoriana no es válida"
            );
        }

        obj.setIdentificacion(
                identificacion
        );

        if (vacio(
                obj.getNombres()
        )) {

            throw new IllegalArgumentException(
                    "Los nombres son obligatorios"
            );
        }

        obj.setNombres(
                obj.getNombres()
                        .trim()
        );
    }

    // =========================================================
    // VALIDACIÓN PAGINACIÓN
    // =========================================================

    private void validarPaginacion(
            int pagina,
            int tamanio
    ) {

        if (pagina < 0) {

            throw new IllegalArgumentException(
                    "La página no puede ser negativa"
            );
        }

        if (tamanio <= 0) {

            throw new IllegalArgumentException(
                    "El tamaño de página debe ser mayor que cero"
            );
        }

        /*
         * Evitamos que un cliente pida por ejemplo
         * 500000 registros en una sola petición.
         */
        if (tamanio > 100) {

            throw new IllegalArgumentException(
                    "El tamaño máximo de página es 100"
            );
        }
    }

    // =========================================================
    // EMPRESA DEL JWT
    // =========================================================

    private void validarEmpresaSesion(
            UUID idEmpresa
    ) {

        if (idEmpresa == null) {

            throw new SecurityException(
                    "El usuario no tiene una empresa asignada"
            );
        }
    }

    // =========================================================
    // UTILIDAD
    // =========================================================

    private boolean vacio(
            String valor
    ) {

        return valor == null ||
                valor.trim().isEmpty();
    }
}