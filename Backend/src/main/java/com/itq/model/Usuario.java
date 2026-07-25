package com.itq.model;

import java.util.UUID;

public class Usuario {
    private UUID idUsuario;
    private UUID idEmpresa;
    private Integer idRol;
    private String usuario;
    private String claveHash;
    private String nombres;
    private Boolean activo;

    public Usuario() {}

    public UUID getIdUsuario() { return idUsuario; }
    public void setIdUsuario(UUID idUsuario) { this.idUsuario = idUsuario; }
    public UUID getIdEmpresa() { return idEmpresa; }
    public void setIdEmpresa(UUID idEmpresa) { this.idEmpresa = idEmpresa; }
    public Integer getIdRol() { return idRol; }
    public void setIdRol(Integer idRol) { this.idRol = idRol; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public String getClaveHash() { return claveHash; }
    public void setClaveHash(String claveHash) { this.claveHash = claveHash; }
    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }
    public Boolean isActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}
