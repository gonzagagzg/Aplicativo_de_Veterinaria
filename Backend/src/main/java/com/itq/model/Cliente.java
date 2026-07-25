package com.itq.model;

import java.util.UUID;

public class Cliente {
    private UUID idCliente;
    private UUID idEmpresa;
    private String identificacion;
    private String nombres;

    public Cliente() {}

    public UUID getIdCliente() { return idCliente; }
    public void setIdCliente(UUID idCliente) { this.idCliente = idCliente; }
    public UUID getIdEmpresa() { return idEmpresa; }
    public void setIdEmpresa(UUID idEmpresa) { this.idEmpresa = idEmpresa; }
    public String getIdentificacion() { return identificacion; }
    public void setIdentificacion(String identificacion) { this.identificacion = identificacion; }
    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }
}
