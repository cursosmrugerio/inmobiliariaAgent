package com.inmobiliaria.gestion.auth.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Enumeración de roles de usuario soportados por el sistema")
public enum UserRole {
  ADMIN,
  AGENT,
  USER
}
