package com.inmobiliaria.gestion.agent.controller;

import com.google.adk.runner.InMemoryRunner;
import com.inmobiliaria.gestion.agent.dto.ChatRequest;
import com.inmobiliaria.gestion.agent.dto.ChatResponse;
import com.inmobiliaria.gestion.agent.exception.AgentChatException;
import com.inmobiliaria.gestion.agent.service.AgentChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST controller exposing the Propiedad conversational agent. */
@RestController
@RequestMapping("/api/agent/propiedades")
@Tag(name = "Propiedad AI Agent", description = "Conversational agent for managing propiedades")
public class PropiedadAgentController {

  private static final Logger log = LoggerFactory.getLogger(PropiedadAgentController.class);

  private final InMemoryRunner agentRunner;
  private final AgentChatService chatService;

  public PropiedadAgentController(
      @Qualifier("propiedadAgentRunner") InMemoryRunner agentRunner, AgentChatService chatService) {
    this.agentRunner = agentRunner;
    this.chatService = chatService;
  }

  @PostMapping("/chat")
  @Operation(
      summary = "Chat with the Propiedad AI agent",
      description =
          "Send a natural language message to perform CRUD operations on properties "
              + "(propiedades). Examples: 'Crear una propiedad ...', 'Actualiza la propiedad 5', "
              + "'Muestra las propiedades de la inmobiliaria 2'.")
  @ApiResponse(responseCode = "200", description = "Agent response generated successfully")
  @ApiResponse(responseCode = "400", description = "Invalid request payload")
  @ApiResponse(responseCode = "500", description = "Agent execution error")
  public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
    log.info(
        "Received propiedad chat request: message='{}', sessionId='{}'",
        request.getMessage(),
        request.getSessionId());
    try {
      ChatResponse response = chatService.executeChat(agentRunner, request);
      log.info(
          "Propiedad agent response generated successfully for session '{}'",
          response.getSessionId());
      return ResponseEntity.ok(response);
    } catch (AgentChatException ex) {
      log.error("Unexpected error in propiedad chat endpoint", ex);
      return ResponseEntity.status(500)
          .body(ChatResponse.error("Unexpected error: " + ex.getMessage(), null));
    }
  }
}
