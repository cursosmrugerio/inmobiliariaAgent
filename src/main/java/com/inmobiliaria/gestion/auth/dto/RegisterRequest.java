package com.inmobiliaria.gestion.auth.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.inmobiliaria.gestion.auth.domain.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload para registrar un nuevo usuario")
public final class RegisterRequest {

  @NotBlank
  @Schema(description = "Nombre completo del usuario", example = "Laura Martínez")
  private final String fullName;

  @NotBlank
  @Email
  @Schema(description = "Correo electrónico único", example = "laura.martinez@inmobiliaria.com")
  private final String email;

  @NotBlank
  @Size(min = 6, max = 120)
  @Schema(description = "Contraseña en texto plano", example = "Secr3t0!")
  private final String password;

  @NotNull
  @Schema(description = "Rol asignado al usuario", example = "AGENT")
  private final UserRole role;

  @JsonCreator
  public RegisterRequest(
      @JsonProperty("fullName") String fullName,
      @JsonProperty("email") String email,
      @JsonProperty("password") String password,
      @JsonProperty("role") UserRole role) {
    this.fullName = fullName;
    this.email = email;
    this.password = password;
    this.role = role;
  }

  public String getFullName() {
    return fullName;
  }

  public String getEmail() {
    return email;
  }

  public String getPassword() {
    return password;
  }

  public UserRole getRole() {
    return role;
  }
}
