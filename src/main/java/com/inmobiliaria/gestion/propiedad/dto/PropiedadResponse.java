package com.inmobiliaria.gestion.propiedad.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.inmobiliaria.gestion.propiedad.domain.PropiedadTipo;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta con los datos de una propiedad registrada")
public final class PropiedadResponse {

  private final Long id;
  private final String nombre;
  private final PropiedadTipo tipo;
  private final String direccion;
  private final String observaciones;
  private final Long inmobiliariaId;
  private final String inmobiliariaNombre;

  @JsonCreator
  public PropiedadResponse(
      @JsonProperty("id") Long id,
      @JsonProperty("nombre") String nombre,
      @JsonProperty("tipo") PropiedadTipo tipo,
      @JsonProperty("direccion") String direccion,
      @JsonProperty("observaciones") String observaciones,
      @JsonProperty("inmobiliariaId") Long inmobiliariaId,
      @JsonProperty("inmobiliariaNombre") String inmobiliariaNombre) {
    this.id = id;
    this.nombre = nombre;
    this.tipo = tipo;
    this.direccion = direccion;
    this.observaciones = observaciones;
    this.inmobiliariaId = inmobiliariaId;
    this.inmobiliariaNombre = inmobiliariaNombre;
  }

  @Schema(description = "Identificador único de la propiedad", example = "42")
  public Long getId() {
    return id;
  }

  @Schema(description = "Nombre o alias de la propiedad", example = "Residencia Las Palmas")
  public String getNombre() {
    return nombre;
  }

  @Schema(
      description = "Tipo de propiedad de acuerdo con el catálogo del sistema",
      implementation = PropiedadTipo.class)
  public PropiedadTipo getTipo() {
    return tipo;
  }

  @Schema(description = "Dirección física de la propiedad", example = "Av. Central 123, CDMX")
  public String getDireccion() {
    return direccion;
  }

  @Schema(
      description = "Notas internas u observaciones relevantes de la propiedad",
      example = "Contrato vigente hasta diciembre")
  public String getObservaciones() {
    return observaciones;
  }

  @Schema(description = "Identificador de la inmobiliaria propietaria", example = "7")
  public Long getInmobiliariaId() {
    return inmobiliariaId;
  }

  @Schema(description = "Nombre de la inmobiliaria propietaria", example = "Inmo Norte")
  public String getInmobiliariaNombre() {
    return inmobiliariaNombre;
  }
}
