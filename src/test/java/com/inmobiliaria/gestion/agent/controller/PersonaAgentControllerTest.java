package com.inmobiliaria.gestion.agent.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inmobiliaria.gestion.agent.dto.ChatRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PersonaAgentControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @Test
  @DisplayName("Debe aceptar solicitudes válidas al agente de personas")
  void chat_validRequest_returnsResponse() throws Exception {
    ChatRequest request = new ChatRequest("Lista las personas registradas", null);

    mockMvc
        .perform(
            post("/api/agent/personas/chat")
                .contentType(MediaType.APPLICATION_JSON)
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
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.sessionId").value("persona-session-001"));
  }
}
