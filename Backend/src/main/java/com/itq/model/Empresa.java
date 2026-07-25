package com.itq.model;

import java.util.UUID;

public class Empresa {
    private UUID idEmpresa;
    private String ruc;
    private String razonSocial;
    private String direccion;
    private Boolean activo;

    public Empresa() {}

    public UUID getIdEmpresa() { return idEmpresa; }
    public void setIdEmpresa(UUID idEmpresa) { this.idEmpresa = idEmpresa; }
    public String getRuc() { return ruc; }
    public void setRuc(String ruc) { this.ruc = ruc; }
    public String getRazonSocial() { return razonSocial; }
    public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public Boolean isActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}
