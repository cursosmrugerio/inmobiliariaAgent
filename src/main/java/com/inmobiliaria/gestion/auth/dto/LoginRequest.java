package com.inmobiliaria.gestion.auth.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload para solicitar autenticación de usuario")
public final class LoginRequest {

  @NotBlank
  @Email
  @Schema(description = "Correo electrónico del usuario", example = "usuario@inmobiliaria.com")
  private final String email;

  @NotBlank
  @Size(min = 6, max = 120)
  @Schema(description = "Contraseña en texto plano", example = "Secr3t0!")
  private final String password;

  @JsonCreator
  public LoginRequest(
      @JsonProperty("email") String email, @JsonProperty("password") String password) {
    this.email = email;
    this.password = password;
  }

  public String getEmail() {
    return email;
  }

  public String getPassword() {
    return password;
  }
}
