package com.inmobiliaria.gestion.auth.service;

import com.inmobiliaria.gestion.auth.domain.UserAccount;
import com.inmobiliaria.gestion.auth.domain.UserRole;
import com.inmobiliaria.gestion.auth.dto.LoginRequest;
import com.inmobiliaria.gestion.auth.dto.LoginResponse;
import com.inmobiliaria.gestion.auth.dto.RegisterRequest;
import com.inmobiliaria.gestion.auth.dto.UserResponse;
import com.inmobiliaria.gestion.auth.security.JwtUtil;
import com.inmobiliaria.gestion.exception.EmailAlreadyUsedException;
import com.inmobiliaria.gestion.exception.InvalidCredentialsException;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

  private static final Logger log = LoggerFactory.getLogger(AuthService.class);
  private static final String TOKEN_TYPE = "Bearer";

  private final UserAccountService userAccountService;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;

  public AuthService(
      UserAccountService userAccountService, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
    this.userAccountService = userAccountService;
    this.passwordEncoder = passwordEncoder;
    this.jwtUtil = jwtUtil;
  }

  public LoginResponse login(LoginRequest request) {
    UserAccount user =
        userAccountService
            .findByEmail(request.getEmail())
            .orElseThrow(() -> new InvalidCredentialsException("Credenciales inválidas"));

    if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
      log.warn("Intento de acceso fallido para {}", request.getEmail());
      throw new InvalidCredentialsException("Credenciales inválidas");
    }

    String token = jwtUtil.generateToken(user);
    log.info("Usuario {} autenticado correctamente", user.getEmail());
    return new LoginResponse(token, TOKEN_TYPE, toUserResponse(user));
  }

  @Transactional
  public UserResponse register(RegisterRequest request) {
    if (userAccountService.existsByEmail(request.getEmail())) {
      throw new EmailAlreadyUsedException(
          "Ya existe un usuario registrado con el email " + request.getEmail());
    }

    UserRole role = request.getRole();
    UserAccount userAccount =
        new UserAccount(
            request.getEmail(),
            passwordEncoder.encode(request.getPassword()),
            request.getFullName(),
            role);
    UserAccount saved = userAccountService.create(userAccount);
    log.info("Usuario {} registrado con rol {}", saved.getEmail(), saved.getRole());
    return toUserResponse(saved);
  }

  public UserResponse toUserResponse(UserAccount userAccount) {
    Objects.requireNonNull(userAccount, "userAccount");
    return new UserResponse(
        userAccount.getId(),
        userAccount.getEmail(),
        userAccount.getFullName(),
        userAccount.getRole());
  }
}
