package com.inmobiliaria.gestion.agent.exception;

/** Runtime exception representing an unexpected error when interacting with an ADK agent. */
public class AgentChatException extends RuntimeException {

  public AgentChatException(String message, Throwable cause) {
    super(message, cause);
  }
}
