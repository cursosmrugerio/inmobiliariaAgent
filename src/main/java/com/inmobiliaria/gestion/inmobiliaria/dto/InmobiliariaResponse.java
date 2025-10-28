package com.inmobiliaria.gestion.inmobiliaria.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Detalle de una inmobiliaria")
public final class InmobiliariaResponse {

  private final Long id;
  private final String nombre;
  private final String rfc;
  private final String nombreContacto;
  private final String correo;
  private final String telefono;

  public InmobiliariaResponse(
      Long id, String nombre, String rfc, String nombreContacto, String correo, String telefono) {
    this.id = id;
    this.nombre = nombre;
    this.rfc = rfc;
    this.nombreContacto = nombreContacto;
    this.correo = correo;
    this.telefono = telefono;
  }

  @Schema(description = "Identificador interno", example = "1")
  public Long getId() {
    return id;
  }

  @Schema(description = "Nombre comercial", example = "Grupo Habitat")
  public String getNombre() {
    return nombre;
  }

  @Schema(description = "RFC de la inmobiliaria", example = "HBI920101AA1")
  public String getRfc() {
    return rfc;
  }

  @Schema(description = "Nombre de contacto principal", example = "Laura Gómez")
  public String getNombreContacto() {
    return nombreContacto;
  }

  @Schema(description = "Correo electrónico de contacto", example = "contacto@habitat.mx")
  public String getCorreo() {
    return correo;
  }

  @Schema(description = "Teléfono de contacto", example = "+52-55-1234-5678")
  public String getTelefono() {
    return telefono;
  }
}
