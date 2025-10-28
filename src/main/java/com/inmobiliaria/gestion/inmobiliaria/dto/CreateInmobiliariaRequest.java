package com.inmobiliaria.gestion.inmobiliaria.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload para registrar una nueva inmobiliaria")
public final class CreateInmobiliariaRequest {

  private final String nombre;
  private final String rfc;
  private final String nombreContacto;
  private final String correo;
  private final String telefono;

  @JsonCreator
  public CreateInmobiliariaRequest(
      @JsonProperty("nombre") String nombre,
      @JsonProperty("rfc") String rfc,
      @JsonProperty("nombreContacto") String nombreContacto,
      @JsonProperty("correo") String correo,
      @JsonProperty("telefono") String telefono) {
    this.nombre = nombre;
    this.rfc = rfc;
    this.nombreContacto = nombreContacto;
    this.correo = correo;
    this.telefono = telefono;
  }

  @Schema(description = "Nombre comercial de la inmobiliaria", example = "Grupo Habitat")
  @NotBlank(message = "El nombre es obligatorio")
  public String getNombre() {
    return nombre;
  }

  @Schema(description = "RFC de la inmobiliaria", example = "HBI920101AA1")
  @Size(max = 13, message = "El RFC no puede exceder 13 caracteres")
  public String getRfc() {
    return rfc;
  }

  @Schema(description = "Nombre de la persona de contacto", example = "Laura Gómez")
  public String getNombreContacto() {
    return nombreContacto;
  }

  @Schema(description = "Correo electrónico de contacto", example = "contacto@habitat.mx")
  @Email(message = "El correo debe tener un formato válido")
  public String getCorreo() {
    return correo;
  }

  @Schema(description = "Teléfono de contacto", example = "+52-55-1234-5678")
  public String getTelefono() {
    return telefono;
  }
}
