package com.inmobiliaria.gestion.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.inmobiliaria.gestion.auth.domain.UserAccount;
import com.inmobiliaria.gestion.auth.domain.UserRole;
import com.inmobiliaria.gestion.auth.dto.LoginRequest;
import com.inmobiliaria.gestion.auth.dto.LoginResponse;
import com.inmobiliaria.gestion.auth.dto.RegisterRequest;
import com.inmobiliaria.gestion.auth.dto.UserResponse;
import com.inmobiliaria.gestion.auth.security.JwtUtil;
import com.inmobiliaria.gestion.exception.EmailAlreadyUsedException;
import com.inmobiliaria.gestion.exception.InvalidCredentialsException;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock private UserAccountService userAccountService;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private JwtUtil jwtUtil;
  @InjectMocks private AuthService authService;

  @Test
  @DisplayName("Debe autenticar un usuario y devolver el token JWT")
  void shouldAuthenticateUser() {
    LoginRequest request = new LoginRequest("user@example.com", "password");
    UserAccount user =
        new UserAccount("user@example.com", "hashed-password", "Usuario Ejemplo", UserRole.AGENT);
    ReflectionTestUtils.setField(user, "id", 99L);

    given(userAccountService.findByEmail("user@example.com")).willReturn(Optional.of(user));
    given(passwordEncoder.matches("password", "hashed-password")).willReturn(true);
    given(jwtUtil.generateToken(user)).willReturn("jwt-token");

    LoginResponse response = authService.login(request);

    assertThat(response.getToken()).isEqualTo("jwt-token");
    assertThat(response.getTokenType()).isEqualTo("Bearer");
    assertThat(response.getUser().getEmail()).isEqualTo("user@example.com");
    verify(jwtUtil).generateToken(user);
  }

  @Test
  @DisplayName("Debe lanzar excepción si la contraseña no coincide")
  void shouldFailAuthenticationWithInvalidPassword() {
    LoginRequest request = new LoginRequest("user@example.com", "password");
    UserAccount user =
        new UserAccount("user@example.com", "hashed-password", "Usuario Ejemplo", UserRole.AGENT);
    ReflectionTestUtils.setField(user, "id", 10L);

    given(userAccountService.findByEmail("user@example.com")).willReturn(Optional.of(user));
    given(passwordEncoder.matches("password", "hashed-password")).willReturn(false);

    assertThatThrownBy(() -> authService.login(request))
        .isInstanceOf(InvalidCredentialsException.class)
        .hasMessage("Credenciales inválidas");
  }

  @Test
  @DisplayName("Debe registrar un nuevo usuario")
  void shouldRegisterNewUser() {
    RegisterRequest request =
        new RegisterRequest("Laura Martínez", "laura@example.com", "password", UserRole.AGENT);

    given(userAccountService.existsByEmail("laura@example.com")).willReturn(false);
    given(passwordEncoder.encode("password")).willReturn("encoded");
    given(userAccountService.create(any(UserAccount.class)))
        .willAnswer(
            invocation -> {
              UserAccount created = invocation.getArgument(0);
              ReflectionTestUtils.setField(created, "id", 5L);
              return created;
            });

    UserResponse response = authService.register(request);

    assertThat(response.getId()).isEqualTo(5L);
    assertThat(response.getEmail()).isEqualTo("laura@example.com");
    assertThat(response.getFullName()).isEqualTo("Laura Martínez");

    ArgumentCaptor<UserAccount> captor = ArgumentCaptor.forClass(UserAccount.class);
    verify(userAccountService).create(captor.capture());
    UserAccount saved = captor.getValue();
    assertThat(saved.getPasswordHash()).isEqualTo("encoded");
  }

  @Test
  @DisplayName("Debe impedir registrar un correo duplicado")
  void shouldFailRegisteringExistingEmail() {
    RegisterRequest request =
        new RegisterRequest("Laura Martínez", "laura@example.com", "password", UserRole.AGENT);

    given(userAccountService.existsByEmail("laura@example.com")).willReturn(true);

    assertThatThrownBy(() -> authService.register(request))
        .isInstanceOf(EmailAlreadyUsedException.class)
        .hasMessage("Ya existe un usuario registrado con el email laura@example.com");
  }
}
