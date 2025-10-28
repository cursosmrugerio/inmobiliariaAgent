package com.inmobiliaria.gestion.agent.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Response DTO containing the AI agent's response to a user message. */
@Schema(description = "Response from the Inmobiliaria AI agent")
public final class ChatResponse {

  private final String response;
  private final String sessionId;
  private final boolean success;
  private final String error;

  public ChatResponse(String response, String sessionId, boolean success, String error) {
    this.response = response;
    this.sessionId = sessionId;
    this.success = success;
    this.error = error;
  }

  /**
   * Create a successful response.
   *
   * @param response The agent's response text
   * @param sessionId The session ID
   * @return ChatResponse
   */
  public static ChatResponse success(String response, String sessionId) {
    return new ChatResponse(response, sessionId, true, null);
  }

  /**
   * Create an error response.
   *
   * @param error The error message
   * @param sessionId The session ID
   * @return ChatResponse
   */
  public static ChatResponse error(String error, String sessionId) {
    return new ChatResponse(null, sessionId, false, error);
  }

  @Schema(
      description = "The agent's response text in natural language",
      example = "I found 3 real estate agencies: ...")
  public String getResponse() {
    return response;
  }

  @Schema(description = "Session ID for maintaining conversation context", example = "user-123")
  public String getSessionId() {
    return sessionId;
  }

  @Schema(description = "Whether the request was successful", example = "true")
  public boolean isSuccess() {
    return success;
  }

  @Schema(description = "Error message if the request failed", example = "Agent execution failed")
  public String getError() {
    return error;
  }
}
