package com.itq.model;

import java.util.UUID;

public class Veterinario {
    private UUID idVeterinario;
    private UUID idUsuario;
    private UUID idEmpresa;
    private String especialidad;

    public Veterinario() {}

    public UUID getIdVeterinario() { return idVeterinario; }
    public void setIdVeterinario(UUID idVeterinario) { this.idVeterinario = idVeterinario; }
    public UUID getIdUsuario() { return idUsuario; }
    public void setIdUsuario(UUID idUsuario) { this.idUsuario = idUsuario; }
    public UUID getIdEmpresa() { return idEmpresa; }
    public void setIdEmpresa(UUID idEmpresa) { this.idEmpresa = idEmpresa; }
    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }
}
