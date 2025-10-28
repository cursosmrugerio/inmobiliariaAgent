package com.inmobiliaria.gestion.agent.controller;

import com.google.adk.agents.RunConfig;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import com.inmobiliaria.gestion.agent.dto.ChatRequest;
import com.inmobiliaria.gestion.agent.dto.ChatResponse;
import io.reactivex.rxjava3.core.Flowable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for conversational AI agent interactions. Provides endpoints for chatting with
 * the Inmobiliaria AI agent using natural language.
 */
@RestController
@RequestMapping("/api/agent")
@Tag(
    name = "AI Agent",
    description = "Conversational AI agent for managing inmobiliarias via natural language")
public class AgentController {

  private static final Logger log = LoggerFactory.getLogger(AgentController.class);

  private final InMemoryRunner agentRunner;

  public AgentController(InMemoryRunner agentRunner) {
    this.agentRunner = agentRunner;
  }

  /**
   * Chat with the Inmobiliaria AI agent. Send natural language queries to perform CRUD operations
   * on real estate agencies.
   *
   * @param request The chat request containing the user's message
   * @return ChatResponse with the agent's response
   */
  @PostMapping("/chat")
  @Operation(
      summary = "Chat with the AI agent",
      description =
          "Send a natural language message to the AI agent to manage inmobiliarias. "
              + "Examples: 'List all agencies', 'Create agency named X', 'Update agency 1', etc.")
  @ApiResponse(responseCode = "200", description = "Agent response received successfully")
  @ApiResponse(responseCode = "400", description = "Invalid request")
  @ApiResponse(responseCode = "500", description = "Agent execution error")
  public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
    log.info(
        "Received chat request: message='{}', sessionId='{}'",
        request.getMessage(),
        request.getSessionId());

    try {
      // Get or create session ID
      String userId =
          request.getSessionId() != null && !request.getSessionId().isBlank()
              ? request.getSessionId()
              : "user-" + UUID.randomUUID();

      // Create or get session
      Session session =
          agentRunner.sessionService().createSession(agentRunner.appName(), userId).blockingGet();

      // Build user content
      Content userContent = Content.fromParts(Part.fromText(request.getMessage()));

      // Create run configuration
      RunConfig runConfig = RunConfig.builder().build();

      // Execute agent and collect response
      Flowable<Event> events =
          agentRunner.runAsync(session.userId(), session.id(), userContent, runConfig);

      // Collect all response text
      StringBuilder responseBuilder = new StringBuilder();
      events.blockingForEach(
          event -> {
            if (event.finalResponse()) {
              String content = event.stringifyContent();
              if (content != null && !content.isBlank()) {
                responseBuilder.append(content);
              }
            }
          });

      // Return successful response
      String finalResponse = responseBuilder.toString();
      if (finalResponse.isBlank()) {
        finalResponse = "I processed your request but have no specific response.";
      }

      log.info("Agent response generated successfully for session '{}'", userId);
      return ResponseEntity.ok(ChatResponse.success(finalResponse, userId));

    } catch (Exception e) {
      log.error("Unexpected error in chat endpoint", e);
      return ResponseEntity.status(500)
          .body(ChatResponse.error("Unexpected error: " + e.getMessage(), null));
    }
  }
}
