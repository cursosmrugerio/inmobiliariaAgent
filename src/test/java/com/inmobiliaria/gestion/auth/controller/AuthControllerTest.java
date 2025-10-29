package com.inmobiliaria.gestion.auth.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inmobiliaria.gestion.auth.domain.UserAccount;
import com.inmobiliaria.gestion.auth.domain.UserRole;
import com.inmobiliaria.gestion.auth.dto.LoginRequest;
import com.inmobiliaria.gestion.auth.dto.LoginResponse;
import com.inmobiliaria.gestion.auth.dto.RegisterRequest;
import com.inmobiliaria.gestion.auth.repository.UserAccountRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private UserAccountRepository userAccountRepository;
  @Autowired private PasswordEncoder passwordEncoder;

  @AfterEach
  void cleanUp() {
    userAccountRepository.deleteAll();
  }

  @Test
  @DisplayName("Debe registrar un usuario y devolver la respuesta esperada")
  void shouldRegisterUser() throws Exception {
    RegisterRequest request =
        new RegisterRequest("Laura Martínez", "laura@example.com", "Secr3t0!", UserRole.AGENT);

    mockMvc
        .perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.email").value("laura@example.com"))
        .andExpect(jsonPath("$.role").value("AGENT"));
  }

  @Test
  @DisplayName("Debe autenticar a un usuario existente y acceder al endpoint /me")
  void shouldLoginAndAccessMeEndpoint() throws Exception {
    UserAccount user =
        new UserAccount(
            "usuario@example.com",
            passwordEncoder.encode("Secr3t0!"),
            "Usuario Ejemplo",
            UserRole.AGENT);
    userAccountRepository.save(user);

    LoginRequest loginRequest = new LoginRequest("usuario@example.com", "Secr3t0!");

    MvcResult result =
        mockMvc
            .perform(
                post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andReturn();

    LoginResponse loginResponse =
        objectMapper.readValue(result.getResponse().getContentAsString(), LoginResponse.class);

    mockMvc
        .perform(
            get("/api/auth/me")
                .header("Authorization", "Bearer " + loginResponse.getToken())
                .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("usuario@example.com"))
        .andExpect(jsonPath("$.fullName").value("Usuario Ejemplo"));
  }
}
