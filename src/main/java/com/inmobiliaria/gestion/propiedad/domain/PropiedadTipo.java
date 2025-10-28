package com.inmobiliaria.gestion.propiedad.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Catálogo de tipos de propiedad admitidos por el sistema")
public enum PropiedadTipo {
  @Schema(description = "Vivienda unifamiliar independiente")
  CASA,

  @Schema(description = "Departamento dentro de un edificio residencial")
  DEPARTAMENTO,

  @Schema(description = "Espacio destinado a oficinas administrativas")
  OFICINA,

  @Schema(description = "Local comercial para venta al público")
  LOCAL,

  @Schema(description = "Espacio destinado a estacionamiento vehicular")
  ESTACIONAMIENTO,

  @Schema(description = "Inmueble vertical con múltiples unidades")
  EDIFICIO,

  @Schema(description = "Terreno sin construcción")
  TERRENO,

  @Schema(description = "Otro tipo de propiedad no contemplado en el catálogo principal")
  OTRO
}
