package com.inmobiliaria.gestion.persona.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.inmobiliaria.gestion.persona.domain.PersonaTipo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Schema(description = "Payload para actualizar los datos de una persona existente")
public final class UpdatePersonaRequest {

  private final PersonaTipo tipoPersona;
  private final String nombre;
  private final String apellidos;
  private final String razonSocial;
  private final String rfc;
  private final String curp;
  private final String email;
  private final String telefono;
  private final LocalDateTime fechaAlta;
  private final Boolean activo;

  @JsonCreator
  public UpdatePersonaRequest(
      @JsonProperty("tipoPersona") PersonaTipo tipoPersona,
      @JsonProperty("nombre") String nombre,
      @JsonProperty("apellidos") String apellidos,
      @JsonProperty("razonSocial") String razonSocial,
      @JsonProperty("rfc") String rfc,
      @JsonProperty("curp") String curp,
      @JsonProperty("email") String email,
      @JsonProperty("telefono") String telefono,
      @JsonProperty("fechaAlta") LocalDateTime fechaAlta,
      @JsonProperty("activo") Boolean activo) {
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

  @Schema(
      description = "Tipo de persona según el catálogo del sistema",
      implementation = PersonaTipo.class,
      example = "MORAL")
  public PersonaTipo getTipoPersona() {
    return tipoPersona;
  }

  @Schema(description = "Nombre(s) de la persona física", example = "María Fernanda")
  @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
  public String getNombre() {
    return nombre;
  }

  @Schema(description = "Apellidos de la persona física", example = "Gómez Ramírez")
  @Size(max = 150, message = "Los apellidos no pueden exceder 150 caracteres")
  public String getApellidos() {
    return apellidos;
  }

  @Schema(
      description = "Razón social de la persona moral",
      example = "Servicios Delta S.A. de C.V.")
  @Size(max = 200, message = "La razón social no puede exceder 200 caracteres")
  public String getRazonSocial() {
    return razonSocial;
  }

  @Schema(description = "RFC con homoclave", example = "GORA8501011H0")
  @Size(max = 13, message = "El RFC no puede exceder 13 caracteres")
  public String getRfc() {
    return rfc;
  }

  @Schema(description = "CURP de la persona física", example = "GORM850101HDFRNL01")
  @Size(max = 18, message = "La CURP no puede exceder 18 caracteres")
  public String getCurp() {
    return curp;
  }

  @Schema(description = "Correo electrónico de contacto", example = "contacto@delta.com")
  @Email(message = "El correo debe tener un formato válido")
  public String getEmail() {
    return email;
  }

  @Schema(description = "Número telefónico de contacto", example = "+52-55-5555-5555")
  @Size(max = 30, message = "El teléfono no puede exceder 30 caracteres")
  public String getTelefono() {
    return telefono;
  }

  @Schema(description = "Fecha de alta en el sistema", example = "2024-02-10T09:30:00")
  public LocalDateTime getFechaAlta() {
    return fechaAlta;
  }

  @Schema(description = "Indica si la persona se encuentra activa", example = "true")
  public Boolean getActivo() {
    return activo;
  }
}
