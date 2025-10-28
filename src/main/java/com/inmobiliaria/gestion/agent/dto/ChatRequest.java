package com.inmobiliaria.gestion.agent.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/** Request DTO for sending messages to the conversational AI agent. */
@Schema(description = "Request for chatting with the Inmobiliaria AI agent")
public final class ChatRequest {

  private final String message;
  private final String sessionId;

  @JsonCreator
  public ChatRequest(
      @JsonProperty("message") String message, @JsonProperty("sessionId") String sessionId) {
    this.message = message;
    this.sessionId = sessionId;
  }

  @Schema(
      description = "The user's message or query in natural language",
      example = "List all real estate agencies",
      required = true)
  @NotBlank(message = "Message is required")
  public String getMessage() {
    return message;
  }

  @Schema(
      description =
          "Optional session ID for maintaining conversation context. If not provided, a new"
              + " session will be created.",
      example = "user-123")
  public String getSessionId() {
    return sessionId;
  }
}
