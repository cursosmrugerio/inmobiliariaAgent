package com.inmobiliaria.gestion.agent.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inmobiliaria.gestion.agent.dto.ChatRequest;
import com.inmobiliaria.gestion.agent.dto.ChatResponse;
import com.inmobiliaria.gestion.agent.service.AgentChatService;
import com.inmobiliaria.gestion.auth.domain.UserAccount;
import com.inmobiliaria.gestion.auth.domain.UserRole;
import com.inmobiliaria.gestion.auth.repository.UserAccountRepository;
import com.inmobiliaria.gestion.auth.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PersonaAgentControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private JwtUtil jwtUtil;
  @Autowired private UserAccountRepository userAccountRepository;
  @Autowired private PasswordEncoder passwordEncoder;

  private String bearerToken;
  @MockBean private AgentChatService agentChatService;

  @BeforeEach
  void setUp() {
    userAccountRepository.deleteAll();
    UserAccount user =
        new UserAccount(
            "persona.agent@example.com",
            passwordEncoder.encode("Secr3t0!"),
            "Persona Agent Tester",
            UserRole.ADMIN);
    UserAccount saved = userAccountRepository.save(user);
    bearerToken = "Bearer " + jwtUtil.generateToken(saved);

    given(agentChatService.executeChat(any(), any()))
        .willAnswer(
            invocation -> {
              ChatRequest chatRequest = invocation.getArgument(1);
              String sessionId =
                  chatRequest.getSessionId() == null
                      ? "generated-session"
                      : chatRequest.getSessionId();
              return ChatResponse.success("Respuesta simulada", sessionId);
            });
  }

  @Test
  @DisplayName("Debe aceptar solicitudes válidas al agente de personas")
  void chat_validRequest_returnsResponse() throws Exception {
    ChatRequest request = new ChatRequest("Lista las personas registradas", null);

    mockMvc
        .perform(
            post("/api/agent/personas/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").exists())
        .andExpect(jsonPath("$.sessionId").exists());
  }

  @Test
  @DisplayName("Debe rechazar mensajes vacíos")
  void chat_emptyMessage_returnsBadRequest() throws Exception {
    ChatRequest request = new ChatRequest("", null);

    mockMvc
        .perform(
            post("/api/agent/personas/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("Debe respetar el sessionId provisto")
  void chat_withSessionId_preservesSession() throws Exception {
    ChatRequest request = new ChatRequest("Muestra la persona 1", "persona-session-001");

    mockMvc
        .perform(
            post("/api/agent/personas/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.sessionId").value("persona-session-001"));
  }
}
