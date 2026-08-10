package com.itq.service;

import com.itq.dao.EmpresaDAO;
import com.itq.model.Empresa;
import com.itq.validation.EcuadorValidator;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class EmpresaService {

    private final EmpresaDAO dao =
            new EmpresaDAO();

    // =========================================================
    // LISTAR
    // =========================================================

    public List<Empresa> listar()
            throws SQLException {

        return dao.listar();
    }

    // =========================================================
    // BUSCAR POR ID
    // =========================================================

    public Optional<Empresa> buscarPorId(
            UUID idEmpresa
    ) throws SQLException {

        if (idEmpresa == null) {

            throw new IllegalArgumentException(
                    "La empresa es obligatoria"
            );
        }

        return dao.buscarPorId(
                idEmpresa
        );
    }

    // =========================================================
    // CREAR
    // =========================================================

    public Empresa crear(
            Empresa obj
    ) throws SQLException {

        if (obj == null) {

            throw new IllegalArgumentException(
                    "Los datos de la empresa son obligatorios"
            );
        }

        /*
         * Toda veterinaria nueva queda activa
         * por defecto.
         */
        if (obj.isActivo() == null) {

            obj.setActivo(true);
        }

        validar(obj);

        return dao.insertar(obj);
    }

    // =========================================================
    // ACTUALIZAR
    // =========================================================

    public boolean actualizar(
            Empresa obj
    ) throws SQLException {

        if (obj == null ||
                obj.getIdEmpresa() == null) {

            throw new IllegalArgumentException(
                    "Los datos de la empresa son obligatorios"
            );
        }

        validar(obj);

        return dao.actualizar(obj);
    }

    // =========================================================
    // ACTIVAR
    // =========================================================

    public boolean activar(
            UUID idEmpresa
    ) throws SQLException {

        if (idEmpresa == null) {

            throw new IllegalArgumentException(
                    "La empresa es obligatoria"
            );
        }

        return dao.cambiarEstado(
                idEmpresa,
                true
        );
    }

    // =========================================================
    // DESACTIVAR
    // =========================================================

    public boolean desactivar(
            UUID idEmpresa
    ) throws SQLException {

        if (idEmpresa == null) {

            throw new IllegalArgumentException(
                    "La empresa es obligatoria"
            );
        }

        return dao.cambiarEstado(
                idEmpresa,
                false
        );
    }

    // =========================================================
    // VALIDACIONES
    // =========================================================

    private void validar(
            Empresa obj
    ) {

        // -----------------------------------------------------
        // RUC obligatorio
        // -----------------------------------------------------

        if (vacio(obj.getRuc())) {

            throw new IllegalArgumentException(
                    "El RUC es obligatorio"
            );
        }

        /*
         * Limpiamos espacios, guiones u otros caracteres
         * antes de validar.
         *
         * Ejemplo:
         * 1792457812-001
         * se convierte a:
         * 1792457812001
         */
        String rucLimpio =
                EcuadorValidator.limpiarNumero(
                        obj.getRuc()
                );

        if (rucLimpio == null ||
                rucLimpio.length() != 13) {

            throw new IllegalArgumentException(
                    "El RUC debe contener 13 dígitos"
            );
        }

        /*
         * Validación ecuatoriana:
         * - provincia
         * - tercer dígito
         * - dígito verificador
         * - establecimiento
         */
        if (!EcuadorValidator.rucValido(
                rucLimpio
        )) {

            throw new IllegalArgumentException(
                    "El RUC ecuatoriano no es válido"
            );
        }

        // Guardamos siempre el RUC normalizado.
        obj.setRuc(
                rucLimpio
        );

        // -----------------------------------------------------
        // RAZÓN SOCIAL
        // -----------------------------------------------------

        if (vacio(
                obj.getRazonSocial()
        )) {

            throw new IllegalArgumentException(
                    "La razón social es obligatoria"
            );
        }

        // -----------------------------------------------------
        // DIRECCIÓN
        // -----------------------------------------------------

        if (vacio(
                obj.getDireccion()
        )) {

            throw new IllegalArgumentException(
                    "La dirección es obligatoria"
            );
        }

        // -----------------------------------------------------
        // ESTADO
        // -----------------------------------------------------

        if (obj.isActivo() == null) {

            throw new IllegalArgumentException(
                    "El estado activo es obligatorio"
            );
        }

        // -----------------------------------------------------
        // NORMALIZACIÓN
        // -----------------------------------------------------

        obj.setRazonSocial(
                obj.getRazonSocial()
                        .trim()
        );

        obj.setDireccion(
                obj.getDireccion()
                        .trim()
        );
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