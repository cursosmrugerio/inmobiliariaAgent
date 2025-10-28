package com.inmobiliaria.gestion.propiedad.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.inmobiliaria.gestion.propiedad.domain.PropiedadTipo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload para registrar una nueva propiedad")
public final class CreatePropiedadRequest {

  private final String nombre;
  private final PropiedadTipo tipo;
  private final String direccion;
  private final String observaciones;
  private final Long inmobiliariaId;

  @JsonCreator
  public CreatePropiedadRequest(
      @JsonProperty("nombre") String nombre,
      @JsonProperty("tipo") PropiedadTipo tipo,
      @JsonProperty("direccion") String direccion,
      @JsonProperty("observaciones") String observaciones,
      @JsonProperty("inmobiliariaId") Long inmobiliariaId) {
    this.nombre = nombre;
    this.tipo = tipo;
    this.direccion = direccion;
    this.observaciones = observaciones;
    this.inmobiliariaId = inmobiliariaId;
  }

  @Schema(description = "Nombre o alias de la propiedad", example = "Residencia Las Palmas")
  @NotBlank(message = "El nombre de la propiedad es obligatorio")
  public String getNombre() {
    return nombre;
  }

  @Schema(
      description = "Tipo de propiedad según el catálogo del sistema",
      implementation = PropiedadTipo.class,
      example = "CASA")
  @NotNull(message = "El tipo de propiedad es obligatorio")
  public PropiedadTipo getTipo() {
    return tipo;
  }

  @Schema(description = "Dirección física de la propiedad", example = "Av. Central 123, CDMX")
  @Size(max = 255, message = "La dirección no puede exceder 255 caracteres")
  public String getDireccion() {
    return direccion;
  }

  @Schema(
      description = "Notas internas u observaciones relevantes",
      example = "Cuenta con estacionamiento privado")
  @Size(max = 2000, message = "Las observaciones no pueden exceder 2000 caracteres")
  public String getObservaciones() {
    return observaciones;
  }

  @Schema(
      description = "Identificador de la inmobiliaria a la que pertenece la propiedad",
      example = "7")
  @NotNull(message = "La inmobiliaria propietaria es obligatoria")
  public Long getInmobiliariaId() {
    return inmobiliariaId;
  }
}
