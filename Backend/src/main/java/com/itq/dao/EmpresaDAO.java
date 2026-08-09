package com.itq.dao;

import com.itq.config.ConexionBD;
import com.itq.model.Empresa;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class EmpresaDAO {

    // =========================================================
    // LISTAR TODAS LAS EMPRESAS
    // =========================================================

    public List<Empresa> listar()
            throws SQLException {

        String sql = """
                SELECT id_empresa,
                       ruc,
                       razon_social,
                       direccion,
                       activo
                FROM empresa
                ORDER BY razon_social
                """;

        List<Empresa> lista =
                new ArrayList<>();

        try (
                Connection cn =
                        ConexionBD.obtenerConexion();

                PreparedStatement ps =
                        cn.prepareStatement(sql);

                ResultSet rs =
                        ps.executeQuery()
        ) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }

        return lista;
    }

    // =========================================================
    // BUSCAR POR ID
    // =========================================================

    public Optional<Empresa> buscarPorId(
            UUID idEmpresa
    ) throws SQLException {

        String sql = """
                SELECT id_empresa,
                       ruc,
                       razon_social,
                       direccion,
                       activo
                FROM empresa
                WHERE id_empresa = ?
                """;

        try (
                Connection cn =
                        ConexionBD.obtenerConexion();

                PreparedStatement ps =
                        cn.prepareStatement(sql)
        ) {

            ps.setObject(
                    1,
                    idEmpresa
            );

            try (ResultSet rs =
                         ps.executeQuery()) {

                return rs.next()
                        ? Optional.of(mapear(rs))
                        : Optional.empty();
            }
        }
    }

    // =========================================================
    // CREAR
    // =========================================================

    public Empresa insertar(
            Empresa obj
    ) throws SQLException {

        String sql = """
                INSERT INTO empresa
                (
                    ruc,
                    razon_social,
                    direccion,
                    activo
                )
                VALUES (?, ?, ?, ?)
                RETURNING id_empresa
                """;

        try (
                Connection cn =
                        ConexionBD.obtenerConexion();

                PreparedStatement ps =
                        cn.prepareStatement(sql)
        ) {

            ps.setString(
                    1,
                    obj.getRuc()
            );

            ps.setString(
                    2,
                    obj.getRazonSocial()
            );

            ps.setString(
                    3,
                    obj.getDireccion()
            );

            ps.setBoolean(
                    4,
                    obj.isActivo()
            );

            try (ResultSet rs =
                         ps.executeQuery()) {

                if (!rs.next()) {

                    throw new SQLException(
                            "No se generó la clave primaria"
                    );
                }

                obj.setIdEmpresa(
                        rs.getObject(
                                "id_empresa",
                                UUID.class
                        )
                );
            }
        }

        return obj;
    }

    // =========================================================
    // ACTUALIZAR DATOS
    // =========================================================

    public boolean actualizar(
            Empresa obj
    ) throws SQLException {

        String sql = """
                UPDATE empresa
                SET ruc = ?,
                    razon_social = ?,
                    direccion = ?,
                    activo = ?
                WHERE id_empresa = ?
                """;

        try (
                Connection cn =
                        ConexionBD.obtenerConexion();

                PreparedStatement ps =
                        cn.prepareStatement(sql)
        ) {

            ps.setString(
                    1,
                    obj.getRuc()
            );

            ps.setString(
                    2,
                    obj.getRazonSocial()
            );

            ps.setString(
                    3,
                    obj.getDireccion()
            );

            ps.setBoolean(
                    4,
                    obj.isActivo()
            );

            ps.setObject(
                    5,
                    obj.getIdEmpresa()
            );

            return ps.executeUpdate() > 0;
        }
    }

    // =========================================================
    // ACTIVAR / DESACTIVAR
    // =========================================================

    public boolean cambiarEstado(
            UUID idEmpresa,
            boolean activo
    ) throws SQLException {

        String sql = """
                UPDATE empresa
                SET activo = ?
                WHERE id_empresa = ?
                """;

        try (
                Connection cn =
                        ConexionBD.obtenerConexion();

                PreparedStatement ps =
                        cn.prepareStatement(sql)
        ) {

            ps.setBoolean(
                    1,
                    activo
            );

            ps.setObject(
                    2,
                    idEmpresa
            );

            return ps.executeUpdate() > 0;
        }
    }

    // =========================================================
    // MAPEO
    // =========================================================

    private Empresa mapear(
            ResultSet rs
    ) throws SQLException {

        Empresa obj =
                new Empresa();

        obj.setIdEmpresa(
                rs.getObject(
                        "id_empresa",
                        UUID.class
                )
        );

        obj.setRuc(
                rs.getString("ruc")
        );

        obj.setRazonSocial(
                rs.getString(
                        "razon_social"
                )
        );

        obj.setDireccion(
                rs.getString(
                        "direccion"
                )
        );

        obj.setActivo(
                (Boolean)
                        rs.getObject("activo")
        );

        return obj;
    }
}