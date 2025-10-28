package com.inmobiliaria.gestion.agent.service;

import com.google.adk.agents.RunConfig;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import com.inmobiliaria.gestion.agent.dto.ChatRequest;
import com.inmobiliaria.gestion.agent.dto.ChatResponse;
import com.inmobiliaria.gestion.agent.exception.AgentChatException;
import io.reactivex.rxjava3.core.Flowable;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Shared service encapsulating the interaction flow with ADK agents. Controllers delegate to this
 * service to avoid duplicating session and event-handling logic.
 */
@Service
public class AgentChatService {

  private static final Logger log = LoggerFactory.getLogger(AgentChatService.class);

  /**
   * Execute a chat request against the provided {@link InMemoryRunner}.
   *
   * @param agentRunner Runner bound to the desired agent.
   * @param request Incoming chat request with the user message and optional session id.
   * @return Successful chat response.
   * @throws AgentChatException if something fails during execution.
   */
  public ChatResponse executeChat(InMemoryRunner agentRunner, ChatRequest request) {
    try {
      String userId = resolveUserId(request.getSessionId());
      Session session = resolveSession(agentRunner, userId);

      Content userContent = Content.fromParts(Part.fromText(request.getMessage()));
      RunConfig runConfig = RunConfig.builder().build();

      Flowable<Event> events =
          agentRunner.runAsync(session.userId(), session.id(), userContent, runConfig);

      String responseText = collectResponse(events);
      log.debug("Agent produced response for session '{}': {}", userId, responseText);

      return ChatResponse.success(responseText, userId);
    } catch (Exception ex) {
      throw new AgentChatException("Failed to execute agent conversation", ex);
    }
  }

  private String resolveUserId(String rawSessionId) {
    if (rawSessionId != null && !rawSessionId.isBlank()) {
      return rawSessionId;
    }
    return "user-" + UUID.randomUUID();
  }

  private Session resolveSession(InMemoryRunner agentRunner, String userId) {
    try {
      var listResponse =
          agentRunner.sessionService().listSessions(agentRunner.appName(), userId).blockingGet();
      var sessions = listResponse.sessions();
      if (sessions != null && !sessions.isEmpty()) {
        return sessions.get(0);
      }
    } catch (Exception ignored) {
      log.debug("Unable to find existing session for '{}', creating new one", userId);
    }
    return agentRunner.sessionService().createSession(agentRunner.appName(), userId).blockingGet();
  }

  private String collectResponse(Flowable<Event> events) {
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
    if (responseBuilder.isEmpty()) {
      return "I processed your request but have no specific response.";
    }
    return responseBuilder.toString();
  }
}
