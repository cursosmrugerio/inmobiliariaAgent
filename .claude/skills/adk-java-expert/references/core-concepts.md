# ADK Java Core Concepts

## Overview

The Agent Development Kit (ADK) for Java is an open-source, code-first toolkit for building, evaluating, and deploying sophisticated AI agents with fine-grained control and flexibility. It's designed for developers who want robust debugging, versioning, and deployment anywhere—from laptop to cloud.

## Agent Types

### LlmAgent

The primary agent type powered by a Large Language Model. Use LlmAgent for:
- Dynamic decision-making and flexible routing
- Natural language understanding and generation
- Tool selection and orchestration
- Sub-agent delegation

**Key characteristics:**
- LLM-powered: Uses models like Gemini, Claude, or LangChain4j-supported models
- Flexible routing: Can dynamically decide which sub-agents or tools to use
- Instruction-based: Behavior defined through natural language instructions
- Tool-capable: Can use any number of tools to extend capabilities

**Builder pattern:**
```java
LlmAgent agent = LlmAgent.builder()
    .name("agent-name")
    .description("Agent description")
    .model("gemini-2.0-flash")  // or new LangChain4j(chatModel)
    .instruction("System prompt and instructions...")
    .tools(tool1, tool2)
    .subAgents(subAgent1, subAgent2)
    .outputKey("result_key")  // Store output in session state
    .build();
```

### SequentialAgent

A workflow agent that executes sub-agents in a fixed, predetermined order. Use SequentialAgent when:
- Tasks must occur in a specific sequence
- Output from one step is required input for the next
- You need predictable, deterministic execution flow
- Automating linear pipelines (e.g., CI/CD: build → test → deploy)

**Key characteristics:**
- NOT LLM-powered: Deterministic execution
- Sequential execution: Sub-agents run in order specified
- State sharing: All sub-agents share the same session state
- Pipeline pattern: Perfect for multi-stage workflows

**Example use cases:**
- Code writer → code reviewer → code refactorer
- Destination researcher → itinerary creator → restaurant suggester
- Data fetcher → data processor → report generator

```java
SequentialAgent pipeline = SequentialAgent.builder()
    .name("pipeline-agent")
    .description("Executes steps in sequence")
    .subAgents(step1Agent, step2Agent, step3Agent)
    .build();
```

### ParallelAgent

A workflow agent that executes sub-agents concurrently for efficiency. Use ParallelAgent when:
- Tasks are independent and don't depend on each other
- You want to minimize total execution time
- Performing multiple I/O-bound operations (API calls, searches, database queries)
- Gathering data from multiple sources simultaneously

**Key characteristics:**
- NOT LLM-powered: Deterministic execution
- Concurrent execution: All sub-agents start simultaneously
- Independent tasks: No data dependencies between sub-agents
- Performance optimization: Reduces total wall-clock time

**Example use cases:**
- Fetching data from multiple APIs concurrently
- Running multiple research queries in parallel
- Gathering information from different sources

```java
ParallelAgent researcher = ParallelAgent.builder()
    .name("parallel-researcher")
    .description("Researches multiple topics concurrently")
    .subAgents(researcher1, researcher2, researcher3)
    .build();
```

**Common pattern - Fan-out/Gather:**
```java
// Parallel research phase
ParallelAgent parallelResearch = ParallelAgent.builder()
    .subAgents(energyResearcher, evResearcher, carbonResearcher)
    .build();

// Synthesis phase
LlmAgent synthesizer = LlmAgent.builder()
    .instruction("Combine results from {energy_result}, {ev_result}, {carbon_result}")
    .build();

// Combined workflow
SequentialAgent workflow = SequentialAgent.builder()
    .subAgents(parallelResearch, synthesizer)
    .build();
```

### LoopAgent

A workflow agent for iterative refinement and self-correction. Use LoopAgent when:
- Tasks require iteration until quality criteria are met
- Implementing feedback loops and refinement processes
- Self-correction workflows
- Generating and improving content iteratively

**Key characteristics:**
- Iterative execution: Runs sub-agents repeatedly
- Condition-based termination: Stops when criteria are met
- Refinement pattern: Perfect for improve-until-good-enough workflows

**Example use cases:**
- Code generation with iterative improvement
- Content creation with quality checking
- Problem-solving with refinement

## Multi-Agent Architecture

### Agent Hierarchy

ADK supports building modular systems through parent-child relationships:

```java
// Specialist agents
LlmAgent searchAgent = LlmAgent.builder()
    .name("search-specialist")
    .tools(new GoogleSearchTool())
    .build();

LlmAgent calculatorAgent = LlmAgent.builder()
    .name("calculator-specialist")
    .tools(calculatorTool)
    .build();

// Orchestrator with sub-agents
LlmAgent orchestrator = LlmAgent.builder()
    .name("orchestrator")
    .instruction("You coordinate between specialists...")
    .subAgents(searchAgent, calculatorAgent)
    .build();
```

### Sub-Agent Delegation Patterns

**LLM-Driven Delegation:**
- The orchestrator LLM decides which sub-agent to call
- Flexible, dynamic routing based on user input
- Use when conversation flow is unpredictable

**Workflow-Driven Orchestration:**
- Sequential/Parallel/Loop agents control execution
- Deterministic, predictable flow
- Use when process is well-defined

### Combining Workflow Patterns

The true power comes from composing different agent types:

```java
// Parallel research
ParallelAgent parallelResearch = ParallelAgent.builder()
    .subAgents(apiResearcher1, apiResearcher2, apiResearcher3)
    .build();

// Sequential workflow with embedded parallel step
SequentialAgent mainWorkflow = SequentialAgent.builder()
    .subAgents(
        preparationAgent,
        parallelResearch,      // Parallel step embedded
        synthesisAgent,
        reportGenerator
    )
    .build();
```

## Session State and Data Flow

### Shared Session State

All agents in a hierarchy share the same session state, enabling data passing:

```java
// Agent 1 writes to state
LlmAgent writerAgent = LlmAgent.builder()
    .outputKey("research_data")  // Stores output in state["research_data"]
    .build();

// Agent 2 reads from state using template variables
LlmAgent readerAgent = LlmAgent.builder()
    .instruction("Analyze this data: {research_data}")
    .build();
```

### State Namespaces

- **Persistent state**: `state["key"]` - Available across turns
- **Temporary state**: `temp:key` - Available within a single turn
- **Output keys**: Automatically store agent results in state

## Model Selection

### Built-in Models

```java
// Gemini models
.model("gemini-2.0-flash")
.model("gemini-2.5-flash")
.model("gemini-1.5-pro")

// Claude models (via ADK)
.model("claude-3-5-sonnet-20250514")
```

### LangChain4j Integration

Access third-party and local models:

```java
// OpenAI via LangChain4j
OpenAiChatModel chatModel = OpenAiChatModel.builder()
    .apiKey("...")
    .modelName("gpt-4")
    .build();

LlmAgent agent = LlmAgent.builder()
    .model(new LangChain4j(chatModel))
    .build();

// Ollama local models
OllamaChatModel ollamaModel = OllamaChatModel.builder()
    .modelName("qwen3:1.7b")
    .baseUrl("http://127.0.0.1:11434")
    .build();

LlmAgent agent = LlmAgent.builder()
    .model(new LangChain4j(ollamaModel))
    .build();
```

**Dependency required:**
```xml
<dependency>
    <groupId>com.google.adk</groupId>
    <artifactId>google-adk-contrib-langchain4j</artifactId>
    <version>0.2.0</version>
</dependency>
```

## Running Agents

### InMemoryRunner

For local development and testing:

```java
InMemoryRunner runner = new InMemoryRunner(rootAgent, "app-name");
Session session = runner.createSession("user-id");

// Send message and get response
Flowable<Event> eventStream = runner.run(
    session,
    Content.builder()
        .addText("User message")
        .build()
);

// Process events
eventStream.blockingSubscribe(event -> {
    if (event.hasContent()) {
        System.out.println(event.getContent().getText());
    }
});
```

### Dev UI

Interactive browser-based development interface:

```java
// Launch Dev UI
AdkWebServer.start(rootAgent);
```

Then navigate to http://localhost:8080 to interact with your agent.

**Maven command:**
```bash
mvn exec:java \
  -Dexec.mainClass="com.google.adk.web.AdkWebServer" \
  -Dexec.args="--adk.agents.source-dir=src/main/java" \
  -Dexec.classpathScope="compile"
```

## Agent Discovery

For Dev UI to discover agents, structure your code:

```
project_folder/
├── src/main/java/
│   └── agents/          # Dev UI scans this directory
│       ├── agent1/
│       │   └── Agent1.java
│       └── agent2/
│           └── Agent2.java
```

Agents must be:
- Public static final variables named `ROOT_AGENT` or `root_agent`
- Located in the `agents` directory (configurable)

## Event System

ADK uses reactive streams (RxJava Flowable) for event handling:

```java
Flowable<Event> events = runner.run(session, userMessage);

events.subscribe(event -> {
    switch (event.getEventType()) {
        case AGENT_START:
            // Agent started
            break;
        case TOOL_CALL:
            // Tool was called
            break;
        case CONTENT:
            // Content response
            System.out.println(event.getContent().getText());
            break;
        case AGENT_END:
            // Agent finished
            break;
    }
});
```

## Best Practices

### When to Use Each Agent Type

| Scenario | Agent Type | Reason |
|----------|------------|--------|
| Flexible conversation | LlmAgent | Dynamic routing needed |
| Fixed pipeline | SequentialAgent | Predictable order required |
| Independent data gathering | ParallelAgent | Maximize efficiency |
| Iterative refinement | LoopAgent | Quality improvement loop |
| Complex orchestration | Combination | Compose patterns together |

### Instruction Writing

- **Be specific**: Clear, detailed instructions improve performance
- **Use examples**: Show expected behavior when possible
- **Reference tools**: Mention tool names and when to use them
- **Template variables**: Use `{state_key}` to reference session state
- **Output format**: Specify desired response format

### State Management

- Use `outputKey()` to automatically store results
- Reference state with `{key}` in instructions
- Keep state keys descriptive
- Clean up temporary state when no longer needed

### Error Handling

```java
events.subscribe(
    event -> { /* process event */ },
    error -> { 
        System.err.println("Error: " + error.getMessage());
        error.printStackTrace();
    },
    () -> { /* completion handler */ }
);
```
