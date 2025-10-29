package com.inmobiliaria.gestion.auth.security;

import java.time.Instant;

public final class JwtPayload {

  private final Long userId;
  private final String email;
  private final String fullName;
  private final String role;
  private final Instant issuedAt;
  private final Instant expiresAt;

  public JwtPayload(
      Long userId,
      String email,
      String fullName,
      String role,
      Instant issuedAt,
      Instant expiresAt) {
    this.userId = userId;
    this.email = email;
    this.fullName = fullName;
    this.role = role;
    this.issuedAt = issuedAt;
    this.expiresAt = expiresAt;
  }

  public Long getUserId() {
    return userId;
  }

  public String getEmail() {
    return email;
  }

  public String getFullName() {
    return fullName;
  }

  public String getRole() {
    return role;
  }

  public Instant getIssuedAt() {
    return issuedAt;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }
}
