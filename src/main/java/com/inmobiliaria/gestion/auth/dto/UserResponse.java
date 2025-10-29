package com.inmobiliaria.gestion.auth.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.inmobiliaria.gestion.auth.domain.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Información pública sobre un usuario autenticado")
public final class UserResponse {

  @Schema(description = "Identificador único del usuario", example = "1")
  private final Long id;

  @Schema(description = "Correo electrónico", example = "usuario@inmobiliaria.com")
  private final String email;

  @Schema(description = "Nombre completo", example = "Laura Martínez")
  private final String fullName;

  @Schema(description = "Rol del usuario", example = "AGENT")
  private final UserRole role;

  @JsonCreator
  public UserResponse(
      @JsonProperty("id") Long id,
      @JsonProperty("email") String email,
      @JsonProperty("fullName") String fullName,
      @JsonProperty("role") UserRole role) {
    this.id = id;
    this.email = email;
    this.fullName = fullName;
    this.role = role;
  }

  public Long getId() {
    return id;
  }

  public String getEmail() {
    return email;
  }

  public String getFullName() {
    return fullName;
  }

  public UserRole getRole() {
    return role;
  }
}
