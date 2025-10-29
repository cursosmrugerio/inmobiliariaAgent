package com.inmobiliaria.gestion.auth.controller;

import com.inmobiliaria.gestion.auth.domain.UserAccount;
import com.inmobiliaria.gestion.auth.dto.LoginRequest;
import com.inmobiliaria.gestion.auth.dto.LoginResponse;
import com.inmobiliaria.gestion.auth.dto.RegisterRequest;
import com.inmobiliaria.gestion.auth.dto.UserResponse;
import com.inmobiliaria.gestion.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Gestión de autenticación de usuarios")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/login")
  @Operation(summary = "Autenticar usuario", description = "Devuelve un token JWT para sesiones")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Autenticación exitosa"),
        @ApiResponse(
            responseCode = "401",
            description = "Credenciales inválidas",
            content = @Content(schema = @Schema(ref = "ApiError")))
      })
  public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    return ResponseEntity.ok(authService.login(request));
  }

  @PostMapping("/register")
  @Operation(summary = "Registrar usuario", description = "Crea un nuevo usuario en el sistema")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "201", description = "Usuario registrado"),
        @ApiResponse(
            responseCode = "409",
            description = "El email ya está registrado",
            content = @Content(schema = @Schema(ref = "ApiError")))
      })
  public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
    UserResponse response = authService.register(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/me")
  @Operation(summary = "Obtener usuario actual", description = "Devuelve al usuario autenticado")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
        @ApiResponse(
            responseCode = "401",
            description = "Token inválido o ausente",
            content = @Content(schema = @Schema(ref = "ApiError")))
      })
  public ResponseEntity<UserResponse> getCurrentUser(
      @AuthenticationPrincipal UserAccount userAccount) {
    return ResponseEntity.ok(authService.toUserResponse(userAccount));
  }

  @PostMapping("/logout")
  @Operation(
      summary = "Cerrar sesión",
      description = "Invalida el token en el cliente (operación sin estado)")
  @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Sesión cerrada")})
  public ResponseEntity<Void> logout() {
    return ResponseEntity.noContent().build();
  }
}
