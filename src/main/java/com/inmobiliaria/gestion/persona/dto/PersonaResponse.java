package com.inmobiliaria.gestion.persona.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.inmobiliaria.gestion.persona.domain.PersonaTipo;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Detalle de una persona registrada en el sistema")
public final class PersonaResponse {

  private final Long id;
  private final PersonaTipo tipoPersona;
  private final String nombre;
  private final String apellidos;
  private final String razonSocial;
  private final String rfc;
  private final String curp;
  private final String email;
  private final String telefono;
  private final LocalDateTime fechaAlta;
  private final boolean activo;

  @JsonCreator
  public PersonaResponse(
      @JsonProperty("id") Long id,
      @JsonProperty("tipoPersona") PersonaTipo tipoPersona,
      @JsonProperty("nombre") String nombre,
      @JsonProperty("apellidos") String apellidos,
      @JsonProperty("razonSocial") String razonSocial,
      @JsonProperty("rfc") String rfc,
      @JsonProperty("curp") String curp,
      @JsonProperty("email") String email,
      @JsonProperty("telefono") String telefono,
      @JsonProperty("fechaAlta") LocalDateTime fechaAlta,
      @JsonProperty("activo") boolean activo) {
    this.id = id;
    this.tipoPersona = tipoPersona;
    this.nombre = nombre;
    this.apellidos = apellidos;
    this.razonSocial = razonSocial;
    this.rfc = rfc;
    this.curp = curp;
    this.email = email;
    this.telefono = telefono;
    this.fechaAlta = fechaAlta;
    this.activo = activo;
  }

  @Schema(description = "Identificador interno", example = "25")
  public Long getId() {
    return id;
  }

  @Schema(
      description = "Tipo de persona según el catálogo del sistema",
      implementation = PersonaTipo.class)
  public PersonaTipo getTipoPersona() {
    return tipoPersona;
  }

  @Schema(description = "Nombre(s) de la persona física", example = "María Fernanda")
  public String getNombre() {
    return nombre;
  }

  @Schema(description = "Apellidos de la persona física", example = "Gómez Ramírez")
  public String getApellidos() {
    return apellidos;
  }

  @Schema(
      description = "Razón social de la persona moral",
      example = "Servicios Delta S.A. de C.V.")
  public String getRazonSocial() {
    return razonSocial;
  }

  @Schema(description = "RFC con homoclave", example = "GORA8501011H0")
  public String getRfc() {
    return rfc;
  }

  @Schema(description = "CURP de la persona física", example = "GORM850101HDFRNL01")
  public String getCurp() {
    return curp;
  }

  @Schema(description = "Correo electrónico de contacto", example = "contacto@delta.com")
  public String getEmail() {
    return email;
  }

  @Schema(description = "Número telefónico de contacto", example = "+52-55-5555-5555")
  public String getTelefono() {
    return telefono;
  }

  @Schema(description = "Fecha de alta en el sistema", example = "2024-02-10T09:30:00")
  public LocalDateTime getFechaAlta() {
    return fechaAlta;
  }

  @Schema(description = "Indica si la persona se encuentra activa", example = "true")
  public boolean isActivo() {
    return activo;
  }
}
