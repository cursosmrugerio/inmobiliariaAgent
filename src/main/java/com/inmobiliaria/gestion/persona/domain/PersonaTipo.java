package com.inmobiliaria.gestion.persona.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Catálogo de tipos de persona admitidos por el sistema")
public enum PersonaTipo {
  @Schema(description = "Persona física")
  FISICA,

  @Schema(description = "Persona moral")
  MORAL
}
