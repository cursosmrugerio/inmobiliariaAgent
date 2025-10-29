package com.inmobiliaria.gestion.agent.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inmobiliaria.gestion.agent.dto.ChatRequest;
import com.inmobiliaria.gestion.agent.dto.ChatResponse;
import com.inmobiliaria.gestion.agent.service.AgentChatService;
import com.inmobiliaria.gestion.auth.domain.UserAccount;
import com.inmobiliaria.gestion.auth.domain.UserRole;
import com.inmobiliaria.gestion.auth.repository.UserAccountRepository;
import com.inmobiliaria.gestion.auth.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
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

/**
 * Integration tests for AgentController.
 *
 * <p>Note: These tests require valid Google Cloud credentials. If credentials are not available,
 * tests may fail. For CI/CD, consider mocking the InMemoryRunner.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AgentControllerTest {

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
            "test.agent@example.com",
            passwordEncoder.encode("Secr3t0!"),
            "Tester Agent",
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
  void chat_ValidRequest_ReturnsResponse() throws Exception {
    // Given
    ChatRequest request = new ChatRequest("Hello", null);

    // When & Then
    mockMvc
        .perform(
            post("/api/agent/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").exists())
        .andExpect(jsonPath("$.sessionId").exists());
  }

  @Test
  void chat_EmptyMessage_ReturnsBadRequest() throws Exception {
    // Given
    ChatRequest request = new ChatRequest("", null);

    // When & Then
    mockMvc
        .perform(
            post("/api/agent/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void chat_WithSessionId_MaintainsSession() throws Exception {
    // Given
    ChatRequest request = new ChatRequest("List all agencies", "test-session-123");

    // When & Then
    mockMvc
        .perform(
            post("/api/agent/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.sessionId").value("test-session-123"));
  }

  @Test
  void chat_InvalidJson_ReturnsBadRequest() throws Exception {
    // When & Then
    mockMvc
        .perform(
            post("/api/agent/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .content("{invalid json}"))
        .andExpect(status().isBadRequest());
  }
}
