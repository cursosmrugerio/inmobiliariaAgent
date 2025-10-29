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

/** REST controller exposing conversational access to the Persona agent. */
@RestController
@RequestMapping("/api/agent/personas")
@Tag(
    name = "AI Persona Agent",
    description = "Conversational agent para gestionar personas (clientes/contactos)")
public class PersonaAgentController {

  private static final Logger log = LoggerFactory.getLogger(PersonaAgentController.class);

  private final InMemoryRunner personaAgentRunner;
  private final AgentChatService chatService;

  public PersonaAgentController(
      @Qualifier("personaAgentRunner") InMemoryRunner personaAgentRunner,
      AgentChatService chatService) {
    this.personaAgentRunner = personaAgentRunner;
    this.chatService = chatService;
  }

  @PostMapping("/chat")
  @Operation(
      summary = "Conversar con el agente de personas",
      description =
          "Envia un mensaje en lenguaje natural para listar, crear, actualizar o eliminar personas.")
  @ApiResponse(responseCode = "200", description = "Respuesta generada correctamente por el agente")
  @ApiResponse(responseCode = "400", description = "Solicitud inválida")
  @ApiResponse(responseCode = "500", description = "Error al ejecutar la conversación")
  public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
    log.info(
        "Received persona chat request: message='{}', sessionId='{}'",
        request.getMessage(),
        request.getSessionId());
    try {
      ChatResponse response = chatService.executeChat(personaAgentRunner, request);
      return ResponseEntity.ok(response);
    } catch (AgentChatException ex) {
      log.error("Error executing persona chat", ex);
      return ResponseEntity.status(500)
          .body(ChatResponse.error("Unexpected error: " + ex.getMessage(), null));
    }
  }
}
