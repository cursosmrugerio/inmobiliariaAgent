package com.inmobiliaria.gestion.auth.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta de autenticación con token JWT")
public final class LoginResponse {

  @Schema(description = "Token JWT firmado", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
  private final String token;

  @Schema(description = "Tipo de token", example = "Bearer")
  private final String tokenType;

  @Schema(description = "Información del usuario autenticado")
  private final UserResponse user;

  @JsonCreator
  public LoginResponse(
      @JsonProperty("token") String token,
      @JsonProperty("tokenType") String tokenType,
      @JsonProperty("user") UserResponse user) {
    this.token = token;
    this.tokenType = tokenType;
    this.user = user;
  }

  public String getToken() {
    return token;
  }

  public String getTokenType() {
    return tokenType;
  }

  public UserResponse getUser() {
    return user;
  }
}
