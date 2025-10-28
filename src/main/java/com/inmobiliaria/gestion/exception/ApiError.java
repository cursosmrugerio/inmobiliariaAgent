package com.inmobiliaria.gestion.exception;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Error payload")
public final class ApiError {

  private final String message;

  public ApiError(String message) {
    this.message = message;
  }

  @Schema(description = "Mensaje descriptivo del error", example = "Recurso no encontrado")
  public String getMessage() {
    return message;
  }
}
