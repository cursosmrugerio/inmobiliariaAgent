package com.inmobiliaria.gestion.agent.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inmobiliaria.gestion.agent.dto.ChatRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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

  @Test
  void chat_ValidRequest_ReturnsResponse() throws Exception {
    // Given
    ChatRequest request = new ChatRequest("Hello", null);

    // When & Then
    mockMvc
        .perform(
            post("/api/agent/chat")
                .contentType(MediaType.APPLICATION_JSON)
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
                .content("{invalid json}"))
        .andExpect(status().isBadRequest());
  }
}
