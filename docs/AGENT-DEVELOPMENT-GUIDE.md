# ADK CRUD Agent Development Guide

**A Comprehensive Step-by-Step Guide for Junior Developers**

---

## Table of Contents

1. [Introduction](#introduction)
2. [Prerequisites](#prerequisites)
3. [Architecture Overview](#architecture-overview)
4. [Step-by-Step Implementation](#step-by-step-implementation)
5. [Integration Testing Guide](#integration-testing-guide)
6. [Troubleshooting](#troubleshooting)
7. [Development Checklist](#development-checklist)
8. [Additional Resources](#additional-resources)

---

## Introduction

This guide will teach you how to create conversational AI agents for CRUD operations using **Google's Agent Development Kit (ADK)**. These agents allow users to manage business entities through natural language instead of manually constructing REST API calls.

### ⚡ Quick Start: Automated Generator

**NEW!** Use the automated generator to create 90% of your agent code in 2 minutes:

```bash
./scripts/generate-crud-agent.sh
```

See **[GENERATOR-QUICK-START.md](reference/GENERATOR-QUICK-START.md)** for instant setup, or continue reading for detailed manual implementation.

### What You'll Build

A complete conversational agent that can:
- Understand natural language queries in Spanish and English
- Perform full CRUD operations (Create, Read, Update, Delete)
- Handle partial updates intelligently
- Maintain conversation context across multiple interactions
- Provide user-friendly, conversational responses

### Reference Implementation

The **Inmobiliaria (Real Estate Agency) agent** is the reference implementation. Review these files before starting:
- `src/main/java/com/inmobiliaria/gestion/agent/tools/InmobiliariaTool.java`
- `src/main/java/com/inmobiliaria/gestion/agent/InmobiliariaAgent.java`
- `src/main/java/com/inmobiliaria/gestion/agent/config/AgentConfig.java`
- `src/main/java/com/inmobiliaria/gestion/agent/controller/AgentController.java`
- `scripts/test-agent_inmobiliarias.sh`

---

## Prerequisites

### Required Knowledge

- **Java 25** basics (classes, methods, annotations)
- **Spring Boot** fundamentals (dependency injection, REST controllers)
- **JPA/Hibernate** basics (entities, repositories, services)
- Basic understanding of **REST APIs**
- Basic **bash scripting** for testing

### Required Tools

- Java 25 JDK installed
- Maven 3.8+
- Your IDE configured (IntelliJ IDEA, Eclipse, VS Code)
- `curl` and `jq` installed (for testing)
- Google Cloud credentials configured

### Required Reading

Before starting, read:
1. `CLAUDE.md` - Project constitution and coding standards
2. `docs/README-AGENT.md` - Agent architecture documentation
3. `docs/vertex-ai.md` - Vertex AI configuration

---

## Architecture Overview

### Layer Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    User Input                               │
│               (Natural Language)                            │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│              AgentController                                │
│         POST /api/agent/chat                                │
│  - Session management                                       │
│  - Request/response handling                                │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│           InMemoryRunner (ADK)                              │
│  - Manages agent sessions                                   │
│  - Executes agent with user input                           │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│            [Entity]Agent                                    │
│  - LlmAgent configuration                                   │
│  - Model: Gemini 2.0 Flash                                  │
│  - Agent instructions (prompt)                              │
│  - Registered tools                                         │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│            [Entity]Tool                                     │
│  - FunctionTools wrapper                                    │
│  - Exposes CRUD methods to LLM                              │
│  - Returns Map<String, Object>                              │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│         [Entity]Service                                     │
│  - Business logic                                           │
│  - Transaction management                                   │
│  - Entity ↔ DTO conversion                                  │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│      [Entity]Repository (JPA)                               │
│  - Database operations                                      │
└─────────────────────────────────────────────────────────────┘
```

### Package Structure

```
com.inmobiliaria.gestion/
├── agent/                          # Agent layer (new for each agent)
│   ├── controller/
│   │   └── AgentController.java    # REST endpoint (shared or per-agent)
│   ├── tools/
│   │   └── [Entity]Tool.java       # FunctionTools wrapper
│   ├── dto/
│   │   ├── ChatRequest.java        # Shared DTOs
│   │   └── ChatResponse.java
│   ├── config/
│   │   └── AgentConfig.java        # Spring bean configuration
│   └── [Entity]Agent.java          # LlmAgent definition
│
└── [entity]/                       # Domain layer (existing)
    ├── controller/
    │   └── [Entity]Controller.java # Traditional REST API
    ├── service/
    │   └── [Entity]Service.java    # Business logic
    ├── repository/
    │   └── [Entity]Repository.java # JPA repository
    ├── domain/
    │   └── [Entity].java           # JPA entity
    └── dto/
        ├── [Entity]Response.java
        ├── Create[Entity]Request.java
        └── Update[Entity]Request.java
```

### Key Principle: Thin Adapter Pattern

**The agent layer should be THIN wrappers around existing service methods.**

- **DON'T**: Put business logic in Tool classes
- **DO**: Delegate all operations to the service layer
- **DON'T**: Duplicate service methods
- **DO**: Reuse existing CRUD methods

---

## Step-by-Step Implementation

Let's build a complete CRUD agent for a generic entity. Replace `[Entity]` with your actual entity name (e.g., Cliente, Propiedad, Contrato).

### Step 1: Verify Domain Layer Exists

**Before creating the agent**, ensure you have:

1. **JPA Entity** (`[entity]/domain/[Entity].java`)
2. **Repository** (`[entity]/repository/[Entity]Repository.java`)
3. **Service** with CRUD methods (`[entity]/service/[Entity]Service.java`)
4. **DTOs** (`[Entity]Response`, `Create[Entity]Request`, `Update[Entity]Request`)

**Critical: Your service MUST support partial updates!**

Example service method for partial updates:

```java
@Service
public class [Entity]Service {

  @Transactional
  public [Entity]Response update(Long id, Update[Entity]Request request) {
    [Entity] entity = repository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("..."));

    // Apply partial updates (only non-null fields)
    applyRequest(entity, request);

    return toResponse(repository.save(entity));
  }

  private void applyRequest([Entity] entity, Update[Entity]Request request) {
    if (request.getField1() != null) {
      entity.setField1(request.getField1());
    }
    if (request.getField2() != null) {
      entity.setField2(request.getField2());
    }
    // ... for all fields
  }
}
```

If your service doesn't support partial updates, implement it first!

---

### Step 2: Create the Tool Class

**Location**: `src/main/java/com/inmobiliaria/gestion/agent/tools/[Entity]Tool.java`

**Purpose**: Expose service methods as ADK FunctionTools

```java
package com.inmobiliaria.gestion.agent.tools;

import com.inmobiliaria.gestion.[entity].dto.Create[Entity]Request;
import com.inmobiliaria.gestion.[entity].dto.[Entity]Response;
import com.inmobiliaria.gestion.[entity].dto.Update[Entity]Request;
import com.inmobiliaria.gestion.[entity].service.[Entity]Service;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * ADK FunctionTool for managing [Entity] entities.
 * Provides conversational CRUD operations callable by AI agents.
 */
@Component
public class [Entity]Tool {

  private final [Entity]Service [entity]Service;

  public [Entity]Tool([Entity]Service [entity]Service) {
    this.[entity]Service = [entity]Service;
  }

  // ========================================
  // LIST ALL
  // ========================================

  /**
   * List all [entities] in the system.
   *
   * @return Map containing list of all [entities]
   */
  @Schema(description = "List all [entities] in the system")
  public Map<String, Object> listAll[Entity]s() {
    try {
      List<[Entity]Response> entities = [entity]Service.findAll();
      Map<String, Object> result = new HashMap<>();
      result.put("success", true);
      result.put("count", entities.size());
      result.put("[entities]", entities);
      return result;
    } catch (Exception e) {
      return createErrorResponse("Error listing [entities]: " + e.getMessage());
    }
  }

  // ========================================
  // GET BY ID
  // ========================================

  /**
   * Get a specific [entity] by its ID.
   *
   * @param id The unique identifier of the [entity]
   * @return Map containing the [entity] details or error
   */
  @Schema(
      description = "Get details of a specific [entity] by its ID. "
          + "Use this when the user asks about a specific [entity].")
  public Map<String, Object> get[Entity]ById(
      @Schema(description = "The ID of the [entity] to retrieve", example = "1", required = true)
          Integer id) {
    try {
      [Entity]Response entity = [entity]Service.findById(id.longValue());
      Map<String, Object> result = new HashMap<>();
      result.put("success", true);
      result.put("[entity]", entity);
      return result;
    } catch (Exception e) {
      return createErrorResponse("Error retrieving [entity]: " + e.getMessage());
    }
  }

  // ========================================
  // CREATE
  // ========================================

  /**
   * Create a new [entity].
   *
   * @param field1 Description of field1 (required)
   * @param field2 Description of field2 (optional)
   * @return Map containing the created [entity] or error
   */
  @Schema(
      description = "Create a new [entity]. Use this when the user wants to register a new [entity].")
  public Map<String, Object> create[Entity](
      @Schema(description = "Description of field1", example = "Example value", required = true)
          String field1,
      @Schema(description = "Description of field2", example = "Example value")
          String field2
      // Add all fields from Create[Entity]Request
  ) {
    try {
      Create[Entity]Request request = new Create[Entity]Request(field1, field2);
      [Entity]Response created = [entity]Service.create(request);
      Map<String, Object> result = new HashMap<>();
      result.put("success", true);
      result.put("message", "[Entity] created successfully");
      result.put("[entity]", created);
      return result;
    } catch (Exception e) {
      return createErrorResponse("Error creating [entity]: " + e.getMessage());
    }
  }

  // ========================================
  // UPDATE (PARTIAL)
  // ========================================

  /**
   * Update an existing [entity]. Supports PARTIAL updates - only provide fields you want to change.
   *
   * @param id The ID of the [entity] to update (required)
   * @param field1 Updated field1 (optional - if null, current value is preserved)
   * @param field2 Updated field2 (optional - if null, current value is preserved)
   * @return Map containing the updated [entity] or error
   */
  @Schema(
      description = "Update an existing [entity]. Supports PARTIAL updates - only provide the "
          + "fields you want to change. Fields not provided will keep their current values. "
          + "Use this when the user wants to modify specific [entity] information without "
          + "requiring all fields.")
  public Map<String, Object> update[Entity](
      @Schema(description = "The ID of the [entity] to update", example = "1", required = true)
          Integer id,
      @Schema(description = "Updated field1 (optional - omit to keep current value)", example = "New value")
          String field1,
      @Schema(description = "Updated field2 (optional - omit to keep current value)", example = "New value")
          String field2
      // Add all fields from Update[Entity]Request - ALL OPTIONAL except id
  ) {
    try {
      Update[Entity]Request request = new Update[Entity]Request(field1, field2);
      [Entity]Response updated = [entity]Service.update(id.longValue(), request);
      Map<String, Object> result = new HashMap<>();
      result.put("success", true);
      result.put("message", "[Entity] updated successfully");
      result.put("[entity]", updated);
      return result;
    } catch (Exception e) {
      return createErrorResponse("Error updating [entity]: " + e.getMessage());
    }
  }

  // ========================================
  // DELETE
  // ========================================

  /**
   * Delete a [entity] by its ID.
   *
   * @param id The ID of the [entity] to delete
   * @return Map containing success status or error
   */
  @Schema(
      description = "Delete a [entity]. Use this when the user wants to remove a [entity] from the system.")
  public Map<String, Object> delete[Entity](
      @Schema(description = "The ID of the [entity] to delete", example = "1", required = true)
          Integer id) {
    try {
      [entity]Service.delete(id.longValue());
      Map<String, Object> result = new HashMap<>();
      result.put("success", true);
      result.put("message", "[Entity] with ID " + id + " deleted successfully");
      return result;
    } catch (Exception e) {
      return createErrorResponse("Error deleting [entity]: " + e.getMessage());
    }
  }

  // ========================================
  // HELPER METHOD
  // ========================================

  private Map<String, Object> createErrorResponse(String message) {
    Map<String, Object> error = new HashMap<>();
    error.put("success", false);
    error.put("error", message);
    return error;
  }
}
```

**Key Points**:
- ✅ All methods return `Map<String, Object>` (required by ADK)
- ✅ Use `Integer` for ID parameters (ADK convention), convert to `Long` for service calls
- ✅ Comprehensive `@Schema` annotations on methods and parameters
- ✅ All update parameters are optional (except ID) for partial updates
- ✅ Wrap all calls in try-catch and return error maps
- ✅ Delegate everything to the service layer

---

### Step 3: Create the Agent Class

**Location**: `src/main/java/com/inmobiliaria/gestion/agent/[Entity]Agent.java`

**Purpose**: Configure the LlmAgent with instructions and register tools

```java
package com.inmobiliaria.gestion.agent;

import com.google.adk.agents.LlmAgent;
import com.google.adk.tools.FunctionTool;
import com.inmobiliaria.gestion.agent.tools.[Entity]Tool;
import org.springframework.stereotype.Component;

/**
 * Conversational AI Agent for managing [Entity] entities.
 * Understands natural language queries and performs CRUD operations through function tools.
 *
 * Example interactions:
 * - "List all [entities]"
 * - "Show me [entity] with ID 1"
 * - "Create a new [entity] called '[name]' with [field]..."
 * - "Update [entity] 2 to change [field] to '[value]'"
 * - "Delete [entity] with ID 3"
 */
@Component
public class [Entity]Agent {

  public static final String ROOT_AGENT = "[entity]-assistant";

  private final [Entity]Tool [entity]Tool;
  private LlmAgent agent;

  public [Entity]Agent([Entity]Tool [entity]Tool) {
    this.[entity]Tool = [entity]Tool;
    initializeAgent();
  }

  private void initializeAgent() {
    this.agent =
        LlmAgent.builder()
            .name(ROOT_AGENT)
            .model("gemini-2.0-flash")
            .instruction(buildInstruction())
            .tools(
                FunctionTool.create([entity]Tool, "listAll[Entity]s"),
                FunctionTool.create([entity]Tool, "get[Entity]ById"),
                FunctionTool.create([entity]Tool, "create[Entity]"),
                FunctionTool.create([entity]Tool, "update[Entity]"),
                FunctionTool.create([entity]Tool, "delete[Entity]")
            )
            .build();
  }

  private String buildInstruction() {
    return """
        You are a helpful assistant for managing [entities] in a property management system.

        Your role is to help users perform CRUD operations on [entity] entities through natural language.

        **Available Operations:**
        1. **List all [entities]**: Use listAll[Entity]s() when the user wants to see all [entities]
        2. **Get specific [entity]**: Use get[Entity]ById() when the user asks about a specific [entity] by ID
        3. **Create new [entity]**: Use create[Entity]() when the user wants to register a new [entity]
        4. **Update [entity]**: Use update[Entity]() when the user wants to modify [entity] information
        5. **Delete [entity]**: Use delete[Entity]() when the user wants to remove a [entity]

        **Important Guidelines:**
        - Always confirm before deleting a [entity]
        - When creating, the [required_fields] field(s) are required
        - **PARTIAL UPDATES**: When updating, you only need to provide the fields that are changing. DO NOT ask for fields that the user didn't mention changing. Only pass the fields the user wants to update.
        - If the user says 'update [entity] X to change Y', only provide the Y field, leave all other fields as null
        - Provide clear, conversational responses in Spanish or English based on user preference
        - Format data in a user-friendly way, not just raw JSON
        - If an operation fails, explain the error clearly to the user
        - When listing [entities], present them in a numbered, readable format

        **Response Format:**
        - For lists: Present [entities] in a numbered format with key details
        - For single [entity]: Show all details clearly
        - For create/update/delete: Confirm the action and show the result
        - Always be polite and helpful

        **Example Interactions:**
        User: "List all [entities]"
        → Call listAll[Entity]s() and format results like:
          "I found 3 [entities]:
           1. [Entity] Name (ID: 1) - [Key Field]: [Value]
           2. [Entity] Name (ID: 2) - [Key Field]: [Value]
           ..."

        User: "Create [entity] '[name]' with [field] [value]"
        → Call create[Entity]() with appropriate parameters

        User: "Update [entity] 2 to change the [field] to [value]"
        → Call update[Entity](id=2, [field]=[value], other_fields=null)
        → DO NOT ask for other fields, only provide the field that is being changed

        User: "Delete [entity] 5"
        → Ask for confirmation: "Are you sure you want to delete [entity] with ID 5?"
        → If confirmed, call delete[Entity](5)
        """;
  }

  public LlmAgent getAgent() {
    return agent;
  }

  public String getAgentName() {
    return ROOT_AGENT;
  }
}
```

**Key Points**:
- ✅ Use `gemini-2.0-flash` model (fast and cost-effective)
- ✅ Clear, detailed instruction prompt
- ✅ Emphasize partial update behavior
- ✅ Provide example interactions
- ✅ Register all 5 CRUD tools
- ✅ Initialize in constructor for Spring lifecycle

**Prompt Engineering Tips**:
- Be explicit about when to use each tool
- Include examples of user input → tool call
- Emphasize partial updates (very important!)
- Specify response formatting preferences
- Request confirmation for destructive operations

---

### Step 4: Update Spring Configuration

**Location**: `src/main/java/com/inmobiliaria/gestion/agent/config/AgentConfig.java`

Add a bean for your new agent's runner:

```java
package com.inmobiliaria.gestion.agent.config;

import com.google.adk.runner.InMemoryRunner;
import com.inmobiliaria.gestion.agent.InmobiliariaAgent;
import com.inmobiliaria.gestion.agent.[Entity]Agent;  // Add this import
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentConfig {

  // Existing Inmobiliaria agent runner
  @Bean
  public InMemoryRunner inmobiliariaAgentRunner(InmobiliariaAgent inmobiliariaAgent) {
    return new InMemoryRunner(inmobiliariaAgent.getAgent());
  }

  // Add your new agent runner
  @Bean
  public InMemoryRunner [entity]AgentRunner([Entity]Agent [entity]Agent) {
    return new InMemoryRunner([entity]Agent.getAgent());
  }
}
```

**Alternative: Single Multi-Agent Runner**

If you want one unified agent handling multiple entities:

```java
@Bean
public InMemoryRunner multiAgentRunner(
    InmobiliariaAgent inmobiliariaAgent,
    [Entity]Agent [entity]Agent) {

  // Create a coordinator agent that delegates to specific agents
  LlmAgent coordinator = LlmAgent.builder()
      .name("coordinator")
      .model("gemini-2.0-flash")
      .agents(
          inmobiliariaAgent.getAgent(),
          [entity]Agent.getAgent()
      )
      .instruction("You are a coordinator. Route [entity] requests to the [entity] agent...")
      .build();

  return new InMemoryRunner(coordinator);
}
```

---

### Step 5: Create or Update Controller

**Option A: Separate Controller per Agent**

**Location**: `src/main/java/com/inmobiliaria/gestion/agent/controller/[Entity]AgentController.java`

```java
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/agent/[entity]")
@Tag(name = "[Entity] AI Agent", description = "Conversational AI agent for managing [entities]")
public class [Entity]AgentController {

  private static final Logger log = LoggerFactory.getLogger([Entity]AgentController.class);

  private final InMemoryRunner agentRunner;

  public [Entity]AgentController(@Qualifier("[entity]AgentRunner") InMemoryRunner agentRunner) {
    this.agentRunner = agentRunner;
  }

  @PostMapping("/chat")
  @Operation(
      summary = "Chat with the [Entity] AI agent",
      description = "Send natural language messages to manage [entities]. "
          + "Examples: 'List all [entities]', 'Create [entity] named X', etc.")
  @ApiResponse(responseCode = "200", description = "Agent response received successfully")
  @ApiResponse(responseCode = "400", description = "Invalid request")
  @ApiResponse(responseCode = "500", description = "Agent execution error")
  public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
    log.info("Received [entity] chat request: message='{}', sessionId='{}'",
        request.getMessage(), request.getSessionId());

    try {
      // Get or create session ID
      String userId = request.getSessionId() != null && !request.getSessionId().isBlank()
          ? request.getSessionId()
          : "user-" + UUID.randomUUID();

      // Get existing session or create new one
      Session session;
      try {
        var listResponse = agentRunner.sessionService()
            .listSessions(agentRunner.appName(), userId)
            .blockingGet();
        var sessions = listResponse.sessions();

        if (sessions != null && !sessions.isEmpty()) {
          session = sessions.get(0);
          log.debug("Retrieved existing session for user '{}'", userId);
        } else {
          log.debug("Creating new session for user '{}'", userId);
          session = agentRunner.sessionService()
              .createSession(agentRunner.appName(), userId)
              .blockingGet();
        }
      } catch (Exception e) {
        log.debug("Error checking session, creating new one for user '{}'", userId);
        session = agentRunner.sessionService()
            .createSession(agentRunner.appName(), userId)
            .blockingGet();
      }

      // Build user content
      Content userContent = Content.fromParts(Part.fromText(request.getMessage()));

      // Create run configuration
      RunConfig runConfig = RunConfig.builder().build();

      // Execute agent and collect response
      Flowable<Event> events = agentRunner.runAsync(
          session.userId(), session.id(), userContent, runConfig);

      // Collect all response text
      StringBuilder responseBuilder = new StringBuilder();
      events.blockingForEach(event -> {
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
      log.error("Unexpected error in [entity] chat endpoint", e);
      return ResponseEntity.status(500)
          .body(ChatResponse.error("Unexpected error: " + e.getMessage(), null));
    }
  }
}
```

**Option B: Unified Controller with Entity Parameter**

Extend the existing `AgentController` to accept an entity type parameter.

---

### Step 6: Add Unit Tests (Optional but Recommended)

**Location**: `src/test/java/com/inmobiliaria/gestion/agent/tools/[Entity]ToolTest.java`

```java
package com.inmobiliaria.gestion.agent.tools;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.inmobiliaria.gestion.[entity].dto.Create[Entity]Request;
import com.inmobiliaria.gestion.[entity].dto.[Entity]Response;
import com.inmobiliaria.gestion.[entity].service.[Entity]Service;
import com.inmobiliaria.gestion.exception.ResourceNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class [Entity]ToolTest {

  @Mock
  private [Entity]Service [entity]Service;

  @InjectMocks
  private [Entity]Tool [entity]Tool;

  private [Entity]Response sampleResponse;

  @BeforeEach
  void setUp() {
    sampleResponse = new [Entity]Response(1L, "Test [Entity]", /* other fields */);
  }

  @Test
  void listAll[Entity]s_Success() {
    // Given
    List<[Entity]Response> responses = Arrays.asList(sampleResponse);
    when([entity]Service.findAll()).thenReturn(responses);

    // When
    Map<String, Object> result = [entity]Tool.listAll[Entity]s();

    // Then
    assertTrue((Boolean) result.get("success"));
    assertEquals(1, result.get("count"));
    assertNotNull(result.get("[entities]"));
    verify([entity]Service, times(1)).findAll();
  }

  @Test
  void get[Entity]ById_Success() {
    // Given
    when([entity]Service.findById(1L)).thenReturn(sampleResponse);

    // When
    Map<String, Object> result = [entity]Tool.get[Entity]ById(1);

    // Then
    assertTrue((Boolean) result.get("success"));
    assertNotNull(result.get("[entity]"));
    verify([entity]Service, times(1)).findById(1L);
  }

  @Test
  void create[Entity]_Success() {
    // Given
    when([entity]Service.create(any(Create[Entity]Request.class)))
        .thenReturn(sampleResponse);

    // When
    Map<String, Object> result = [entity]Tool.create[Entity]("Test", /* other params */);

    // Then
    assertTrue((Boolean) result.get("success"));
    assertNotNull(result.get("[entity]"));
    verify([entity]Service, times(1)).create(any(Create[Entity]Request.class));
  }

  // Add tests for update and delete...
}
```

---

## Integration Testing Guide

Integration tests are **critical** for verifying that the agent correctly interprets natural language and performs the expected operations.

### Creating a Comprehensive Test Script

**Location**: `scripts/test-agent_[entity].sh`

**Structure**:

```bash
#!/bin/bash

# Test script for [Entity]Agent and [Entity]Tool
# Tests all CRUD operations through conversational AI

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
BASE_URL="http://localhost:8080"
API_ENDPOINT="${BASE_URL}/api/agent/[entity]/chat"
CONTENT_TYPE="Content-Type: application/json"

# Session ID for maintaining conversation context
SESSION_ID=""

# Test counters
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# ========================================
# HELPER FUNCTIONS
# ========================================

print_header() {
    echo -e "\n${BLUE}========================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}========================================${NC}\n"
}

print_test() {
    local test_name="$1"
    local status="$2"
    TOTAL_TESTS=$((TOTAL_TESTS + 1))

    if [ "$status" = "PASS" ]; then
        echo -e "${GREEN}✓ PASS${NC} - $test_name"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        echo -e "${RED}✗ FAIL${NC} - $test_name"
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
}

send_message() {
    local message="$1"
    local session="$2"

    echo -e "${YELLOW}Message:${NC} $message" >&2

    local payload
    if [ -z "$session" ]; then
        payload=$(jq -n --arg msg "$message" '{message: $msg}')
    else
        payload=$(jq -n --arg msg "$message" --arg sid "$session" \
            '{message: $msg, sessionId: $sid}')
    fi

    local response=$(curl -s -w '\n%{http_code}' -X POST "$API_ENDPOINT" \
        -H "$CONTENT_TYPE" \
        -d "$payload")

    # Split response body and HTTP code
    local http_code=$(echo "$response" | tail -n1)
    local body=$(echo "$response" | sed '$d')

    echo -e "${YELLOW}HTTP Status:${NC} $http_code" >&2
    echo -e "${YELLOW}Response:${NC}" >&2

    # Check if response is valid JSON
    if echo "$body" | jq empty 2>/dev/null; then
        echo "$body" | jq -C '.' >&2

        # Extract session ID
        local new_session=$(echo "$body" | jq -r '.sessionId // empty' 2>/dev/null)
        if [ -n "$new_session" ]; then
            SESSION_ID="$new_session"
        fi
    else
        echo -e "${RED}Invalid JSON response:${NC}" >&2
        echo "$body" >&2
        body='{"success":false,"error":"Invalid response","response":null,"sessionId":null}'
    fi
    echo "" >&2

    echo "$body"
}

check_success() {
    local response="$1"
    local test_name="$2"

    if ! echo "$response" | jq empty 2>/dev/null; then
        print_test "$test_name" "FAIL"
        echo -e "${RED}Error:${NC} Invalid JSON response"
        return 1
    fi

    local success=$(echo "$response" | jq -r '.success' 2>/dev/null)

    if [ "$success" = "true" ]; then
        print_test "$test_name" "PASS"
        return 0
    else
        print_test "$test_name" "FAIL"
        local error=$(echo "$response" | jq -r '.error // "Unknown error"' 2>/dev/null)
        echo -e "${RED}Error:${NC} $error"
        return 1
    fi
}

check_contains() {
    local response="$1"
    local expected="$2"
    local test_name="$3"

    if ! echo "$response" | jq empty 2>/dev/null; then
        print_test "$test_name" "FAIL"
        echo -e "${RED}Error:${NC} Invalid JSON response"
        return 1
    fi

    local response_text=$(echo "$response" | jq -r '.response // ""' 2>/dev/null)

    if echo "$response_text" | grep -qi "$expected"; then
        print_test "$test_name" "PASS"
        return 0
    else
        print_test "$test_name" "FAIL"
        echo -e "${RED}Expected to contain:${NC} $expected"
        echo -e "${RED}Got:${NC} $response_text"
        return 1
    fi
}

check_prerequisites() {
    local all_good=true

    if ! command -v jq &> /dev/null; then
        echo -e "${RED}ERROR: jq is not installed${NC}"
        all_good=false
    fi

    if ! curl -s "$BASE_URL/actuator/health" &> /dev/null; then
        echo -e "${RED}ERROR: Application not running at $BASE_URL${NC}"
        all_good=false
    fi

    if [ "$all_good" = false ]; then
        exit 1
    fi

    echo -e "${GREEN}✓ Prerequisites check passed${NC}\n"
}

# ========================================
# START TESTS
# ========================================

print_header "[ENTITY] AGENT CRUD TESTS"
echo "Testing endpoint: $API_ENDPOINT"
echo ""

check_prerequisites

# Reset database
echo -e "${BLUE}Resetting database...${NC}"
reset_response=$(curl -s -X POST "$BASE_URL/api/test/reset-database" 2>/dev/null)
if echo "$reset_response" | grep -q "successfully"; then
    echo -e "${GREEN}✓ Database reset successfully${NC}\n"
else
    echo -e "${YELLOW}⚠ Could not reset database${NC}\n"
fi

# ========================================
# TEST 1: LIST ALL (Empty)
# ========================================
print_header "TEST 1: List All [Entities] (Empty State)"

response=$(send_message "List all [entities]" "")
check_success "$response" "List all [entities] command"

# ========================================
# TEST 2: CREATE FIRST [ENTITY]
# ========================================
print_header "TEST 2: Create First [Entity]"

response=$(send_message "Create a new [entity] called '[Name]' with [field] [value]..." "$SESSION_ID")
check_success "$response" "Create first [entity]"
check_contains "$response" "[Expected Name]" "Verify [entity] name in response"

# ========================================
# TEST 3: CREATE SECOND [ENTITY]
# ========================================
print_header "TEST 3: Create Second [Entity]"

response=$(send_message "Create another [entity]..." "$SESSION_ID")
check_success "$response" "Create second [entity]"

# ========================================
# TEST 4: LIST ALL (With Data)
# ========================================
print_header "TEST 4: List All [Entities] (With Data)"

response=$(send_message "Show me all [entities]" "$SESSION_ID")
check_success "$response" "List all [entities] with data"
check_contains "$response" "[First Entity Name]" "Verify first [entity] in list"
check_contains "$response" "[Second Entity Name]" "Verify second [entity] in list"

# ========================================
# TEST 5: GET BY ID
# ========================================
print_header "TEST 5: Get Specific [Entity] by ID"

response=$(send_message "Show me [entity] with ID 1" "$SESSION_ID")
check_success "$response" "Get [entity] by ID"
check_contains "$response" "[Expected Name]" "Verify correct [entity] retrieved"

# ========================================
# TEST 6: PARTIAL UPDATE - Single Field
# ========================================
print_header "TEST 6: Partial Update - [Field] Only"

response=$(send_message "Update [entity] 1 to change [field] to [new value]" "$SESSION_ID")
check_success "$response" "Partial update - [field]"
check_contains "$response" "[New Value]" "Verify [field] updated"

# ========================================
# TEST 7: FULL UPDATE - Multiple Fields
# ========================================
print_header "TEST 7: Full Update - Multiple Fields"

response=$(send_message "Update [entity] 1: change [field1] to [value1] and [field2] to [value2]" "$SESSION_ID")
check_success "$response" "Full update"
check_contains "$response" "[Value1]" "Verify [field1] updated"

# ========================================
# TEST 8: DELETE WITH CONFIRMATION
# ========================================
print_header "TEST 8: Delete [Entity] - Request"

SESSION_ID=""
response=$(send_message "Delete [entity] 2" "")
check_success "$response" "Delete request"
check_contains "$response" "sure" "Verify confirmation request"

DELETE_SESSION_ID="$SESSION_ID"

print_header "TEST 9: Delete [Entity] - Confirm"

response=$(send_message "Yes, delete [entity] 2" "$DELETE_SESSION_ID")
check_success "$response" "Confirm deletion"
check_contains "$response" "deleted" "Verify deletion confirmed"

# ========================================
# TEST 10: VERIFY DELETION
# ========================================
print_header "TEST 10: Verify Deletion"

response=$(send_message "List all [entities]" "$SESSION_ID")
check_success "$response" "List after deletion"

if echo "$response" | jq -r '.response' | grep -qi "[Deleted Entity Name]"; then
    print_test "Verify [entity] was deleted" "FAIL"
else
    print_test "Verify [entity] was deleted" "PASS"
fi

# ========================================
# TEST 11: ERROR HANDLING - Non-existent
# ========================================
print_header "TEST 11: Error Handling - Non-existent [Entity]"

response=$(send_message "Show me [entity] 999" "$SESSION_ID")
check_success "$response" "Get non-existent [entity]"
check_contains "$response" "not found\|no encontrada\|error" "Verify error message"

# ========================================
# FINAL REPORT
# ========================================
print_header "TEST SUMMARY"

echo -e "Total Tests:  ${BLUE}$TOTAL_TESTS${NC}"
echo -e "Passed:       ${GREEN}$PASSED_TESTS${NC}"
echo -e "Failed:       ${RED}$FAILED_TESTS${NC}"
echo ""

if [ $FAILED_TESTS -eq 0 ]; then
    echo -e "${GREEN}✓ ALL TESTS PASSED!${NC}"
    exit 0
else
    echo -e "${RED}✗ SOME TESTS FAILED${NC}"
    exit 1
fi
```

### Make Script Executable

```bash
chmod +x scripts/test-agent_[entity].sh
```

### Running Tests

```bash
# Ensure application is running
mvn spring-boot:run

# In another terminal
./scripts/test-agent_[entity].sh
```

### Test Coverage Checklist

Your test script should cover:
- ✅ List empty state
- ✅ Create multiple entities
- ✅ List with data
- ✅ Get by ID
- ✅ Partial updates (single field)
- ✅ Full updates (multiple fields)
- ✅ Delete with confirmation
- ✅ Delete cancellation
- ✅ Verify deletion
- ✅ Error handling (non-existent entities)
- ✅ Conversational queries
- ✅ Session management

---

## Troubleshooting

### Common Issues and Solutions

#### 1. "API key must either be provided or set in the environment variable"

**Cause**: Vertex AI configuration not set properly.

**Solution**:
```bash
export GOOGLE_APPLICATION_CREDENTIALS="$HOME/inmobiliaria-service-account-key.json"
export GOOGLE_GENAI_USE_VERTEXAI=true
export GOOGLE_CLOUD_PROJECT=inmobiliaria-adk
export GOOGLE_CLOUD_LOCATION=us-central1

# Then restart the application
mvn spring-boot:run
```

#### 2. Agent not calling the correct tool

**Cause**: Unclear or ambiguous instructions in the agent prompt.

**Solution**:
- Make tool descriptions more explicit
- Add usage examples in the instruction
- Mention specific keywords that should trigger each tool
- Test with various phrasings

#### 3. Partial updates asking for all fields

**Cause**: Service doesn't support partial updates, or instruction isn't clear.

**Solution**:
- Verify service's `applyRequest()` method only updates non-null fields
- Emphasize in instruction: "DO NOT ask for fields not mentioned by user"
- Add explicit examples of partial updates

#### 4. Session context not maintained

**Cause**: Session ID not being passed correctly.

**Solution**:
- Verify session ID extraction in controller
- Check that test script saves and reuses `SESSION_ID`
- Log session IDs to debug

#### 5. Tool method not found

**Cause**: Method name in `FunctionTool.create()` doesn't match actual method name.

**Solution**:
- Ensure exact method name match (case-sensitive)
- Check for typos
- Verify method is public

#### 6. Invalid JSON response from tool

**Cause**: Tool method not returning proper `Map<String, Object>` format.

**Solution**:
- Always return a Map with "success" key
- Wrap all operations in try-catch
- Use `createErrorResponse()` helper for errors

#### 7. Database reset fails in tests

**Cause**: `TestDataController` endpoint not available or H2 issues.

**Solution**:
- Verify `TestDataController` is in your codebase
- Check H2 configuration in `application.properties`
- Manually clear database if needed

---

## Development Checklist

Use this checklist when implementing a new CRUD agent:

### Planning Phase
- [ ] Domain entity exists with JPA annotations
- [ ] Repository interface extends `JpaRepository`
- [ ] Service layer has all CRUD methods
- [ ] Service supports **partial updates**
- [ ] DTOs created (Response, CreateRequest, UpdateRequest)
- [ ] Identified required vs optional fields

### Tool Development
- [ ] Created `[Entity]Tool.java` in `agent/tools/`
- [ ] Implemented all 5 CRUD methods (list, get, create, update, delete)
- [ ] All methods return `Map<String, Object>`
- [ ] Comprehensive `@Schema` annotations on methods
- [ ] Comprehensive `@Schema` annotations on parameters
- [ ] All update parameters are optional (except ID)
- [ ] Error handling with try-catch
- [ ] Created `createErrorResponse()` helper
- [ ] Component annotation added (`@Component`)

### Agent Configuration
- [ ] Created `[Entity]Agent.java` in `agent/`
- [ ] Used `gemini-2.0-flash` model
- [ ] Wrote comprehensive instruction prompt
- [ ] Included partial update guidelines in prompt
- [ ] Added example interactions to prompt
- [ ] Registered all 5 tools with `FunctionTool.create()`
- [ ] Agent initialized in constructor
- [ ] Component annotation added (`@Component`)

### Spring Configuration
- [ ] Added `InMemoryRunner` bean in `AgentConfig`
- [ ] Bean properly qualified if multiple runners exist
- [ ] Dependency injection configured

### Controller
- [ ] Created or updated controller for agent endpoint
- [ ] Session management implemented
- [ ] Error handling with try-catch
- [ ] OpenAPI documentation added (`@Operation`, `@Tag`)
- [ ] Logging added (info and debug levels)
- [ ] Request validation with `@Valid`

### Testing
- [ ] Created integration test script `scripts/test-agent_[entity].sh`
- [ ] Script tests all CRUD operations
- [ ] Partial update tests included
- [ ] Delete confirmation workflow tested
- [ ] Error handling tested
- [ ] Session management tested
- [ ] Script made executable (`chmod +x`)
- [ ] All tests pass successfully

### Documentation
- [ ] Updated `README-AGENT.md` if needed
- [ ] Documented endpoint in Swagger UI
- [ ] Added inline Javadoc comments
- [ ] Created example interactions documentation

### Code Quality
- [ ] Code formatted with Google Java Style
- [ ] No compiler warnings
- [ ] Follows patterns from Inmobiliaria agent
- [ ] No business logic in Tool class
- [ ] Proper exception handling
- [ ] SLF4J logging used (not System.out)

---

## Additional Resources

### Essential Reading
- **CLAUDE.md** - Project constitution and coding standards
- **docs/README-AGENT.md** - Agent architecture and usage
- **docs/vertex-ai.md** - Vertex AI setup and configuration
- **Inmobiliaria Agent Implementation** - Reference implementation

### External Documentation
- [ADK Java Documentation](https://google.github.io/adk-docs/) - Official ADK docs
- [Gemini API Documentation](https://ai.google.dev/docs) - Gemini model capabilities
- [Spring Boot Documentation](https://spring.io/projects/spring-boot) - Spring framework
- [Vertex AI Documentation](https://cloud.google.com/vertex-ai/docs) - Google Cloud AI platform

### Google Java Style Guide
- [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)

### Tools
- **Swagger UI**: `http://localhost:8080/swagger-ui.html` - API testing interface
- **H2 Console**: `http://localhost:8080/h2-console` - Database inspection (dev mode)
- **Actuator Health**: `http://localhost:8080/actuator/health` - App health check

---

## Quick Reference: File Checklist

When implementing a new [Entity] agent, you'll create/modify these files:

### New Files
```
src/main/java/com/inmobiliaria/gestion/agent/
├── tools/[Entity]Tool.java                    # NEW
└── [Entity]Agent.java                         # NEW

src/test/java/com/inmobiliaria/gestion/agent/
└── tools/[Entity]ToolTest.java               # NEW (optional)

scripts/
└── test-agent_[entity].sh                     # NEW
```

### Modified Files
```
src/main/java/com/inmobiliaria/gestion/agent/
├── config/AgentConfig.java                    # ADD BEAN
└── controller/
    └── [Entity]AgentController.java          # NEW or MODIFY existing
```

---

## Summary

You now have everything you need to build production-quality CRUD agents:

1. **Understand the architecture** - Thin adapter pattern over existing services
2. **Follow the step-by-step guide** - Create Tool, Agent, Config, Controller
3. **Write comprehensive tests** - Integration tests with bash scripts
4. **Validate with the checklist** - Ensure nothing is missed
5. **Refer to Inmobiliaria** - Use as a reference implementation

**Key Success Factors**:
- Service layer **must** support partial updates
- Tool layer is **thin** (no business logic)
- Agent instructions are **explicit** and **detailed**
- Tests are **comprehensive** and cover edge cases
- Code follows **Google Java Style** and project standards

Good luck with your agent development!
