---
name: adk-java-expert
description: Expert guidance for building AI agents with Google's Agent Development Kit (ADK) for Java. Use when creating agents, implementing tools, designing multi-agent systems, working with workflow patterns (Sequential, Parallel, Loop), integrating with Google Cloud services, or deploying agentic applications.
---

# ADK Java Expert

This skill provides comprehensive guidance for developing AI agents using Google's Agent Development Kit (ADK) for Java.

## When to Use This Skill

Use this skill when:
- Creating new AI agents with LlmAgent
- Building multi-agent systems with orchestration
- Implementing custom tools or integrating APIs
- Designing workflows (Sequential, Parallel, Loop patterns)
- Working with session state and data flow
- Deploying agents to Google Cloud or other platforms
- Integrating with Gemini, Claude, or other LLMs via LangChain4j
- Setting up the Dev UI for testing
- Troubleshooting agent behavior or performance

## Quick Start

### Basic Agent Creation

```java
import com.google.adk.agents.LlmAgent;

LlmAgent agent = LlmAgent.builder()
    .name("my-agent")
    .model("gemini-2.0-flash")
    .instruction("You are a helpful assistant...")
    .build();
```

### Agent with Tools

```java
import com.google.adk.tools.GoogleSearchTool;
import com.google.adk.tools.FunctionTool;

LlmAgent agent = LlmAgent.builder()
    .name("assistant")
    .tools(
        new GoogleSearchTool(),
        FunctionTool.create(MyTools.class, "myFunction")
    )
    .build();
```

### Running an Agent

```java
import com.google.adk.runner.InMemoryRunner;

InMemoryRunner runner = new InMemoryRunner(agent, "app-name");
Session session = runner.createSession("user-id");

runner.run(session, Content.builder().addText("Hello!").build())
    .blockingSubscribe(event -> {
        if (event.hasContent()) {
            System.out.println(event.getContent().getText());
        }
    });
```

## Core Workflows

### 1. Creating a New Agent

**When:** Building a new AI agent from scratch

**Steps:**
1. Define the agent's purpose and capabilities
2. Choose appropriate agent type (LlmAgent, Sequential, Parallel, Loop)
3. Write clear instructions
4. Add necessary tools
5. Configure model and parameters
6. Test with InMemoryRunner or Dev UI

**See:** [core-concepts.md](references/core-concepts.md) for agent types and patterns

### 2. Adding Custom Tools

**When:** Extending agent capabilities with custom functions or APIs

**Steps:**
1. Create Java method with `@Schema` annotations
2. Wrap method with `FunctionTool.create()`
3. Add tool to agent's `.tools()` list
4. Reference tool in agent instructions
5. Test tool execution

**See:** [tools-integration.md](references/tools-integration.md) for tool patterns

### 3. Building Multi-Agent Systems

**When:** Creating complex systems with specialized agents

**Patterns:**
- **LLM-Driven Delegation:** Orchestrator decides which sub-agent to use
- **Sequential Workflow:** Fixed order execution (A → B → C)
- **Parallel Execution:** Concurrent independent tasks
- **Loop Pattern:** Iterative refinement

**See:** [core-concepts.md](references/core-concepts.md#multi-agent-architecture)

### 4. Managing State

**When:** Passing data between agents or storing results

**Key concepts:**
- Use `.outputKey("key")` to store agent results
- Reference state with `{key}` in instructions
- All agents in hierarchy share session state
- Use descriptive state keys

**Example:**
```java
LlmAgent researcher = LlmAgent.builder()
    .outputKey("research_data")  // Store output
    .build();

LlmAgent analyzer = LlmAgent.builder()
    .instruction("Analyze: {research_data}")  // Use stored data
    .build();
```

## Decision Framework

### Choosing Agent Types

| Scenario | Use | Why |
|----------|-----|-----|
| Flexible conversation | `LlmAgent` | LLM decides flow |
| Fixed pipeline | `SequentialAgent` | Predictable order |
| Independent tasks | `ParallelAgent` | Maximize speed |
| Iterative refinement | `LoopAgent` | Quality improvement |
| Complex orchestration | Combination | Compose patterns |

### Choosing Tools

| Need | Tool Type | When |
|------|-----------|------|
| Custom logic | `FunctionTool` | Application-specific |
| Web search | `GoogleSearchTool` | Current information |
| API integration | `FunctionTool` + HTTP | External services |
| Database access | `FunctionTool` + JDBC | Data queries |
| Specialized task | Agent-as-tool | Complex sub-task |
| Standard APIs | MCP Tools | GitHub, etc. |

## Reference Documentation

### Core Concepts
**File:** [core-concepts.md](references/core-concepts.md)

**Contents:**
- Agent types (LlmAgent, Sequential, Parallel, Loop)
- Multi-agent architecture patterns
- Session state and data flow
- Model selection and configuration
- Event system and reactive streams
- Best practices for each pattern

**When to read:** Understanding agent fundamentals, designing system architecture

### Tools and Integration
**File:** [tools-integration.md](references/tools-integration.md)

**Contents:**
- Function tools creation and usage
- Built-in tools (GoogleSearch, Code Execution)
- Tool context and control flow
- Agents as tools
- Third-party integrations (LangChain, MCP)
- Database, REST API, file system integration

**When to read:** Implementing custom tools, integrating external systems

### Deployment Guide
**File:** [deployment.md](references/deployment.md)

**Contents:**
- Project setup (Maven, Gradle)
- Local development with InMemoryRunner and Dev UI
- Containerization with Docker
- Google Cloud deployment (Cloud Run, Vertex AI)
- Production considerations (logging, monitoring, security)
- Performance optimization

**When to read:** Deploying to production, setting up infrastructure

### Code Examples
**File:** [examples.md](references/examples.md)

**Contents:**
- Complete working examples
- Basic agents and tool usage
- Multi-agent systems
- Workflow patterns (Sequential, Parallel)
- Real-world applications
- Integration patterns

**When to read:** Looking for implementation patterns, getting started quickly

## Common Patterns

### Pattern: Research and Synthesize

**Use case:** Gather information from multiple sources, then synthesize

```java
// Parallel research
ParallelAgent research = ParallelAgent.builder()
    .subAgents(researcher1, researcher2, researcher3)
    .build();

// Synthesis
LlmAgent synthesizer = LlmAgent.builder()
    .instruction("Combine: {result1}, {result2}, {result3}")
    .build();

// Pipeline
SequentialAgent pipeline = SequentialAgent.builder()
    .subAgents(research, synthesizer)
    .build();
```

### Pattern: Generate-Review-Refine

**Use case:** Iterative content improvement

```java
SequentialAgent pipeline = SequentialAgent.builder()
    .subAgents(
        generatorAgent,    // outputKey("draft")
        reviewerAgent,     // outputKey("feedback")
        refinerAgent       // uses {draft} and {feedback}
    )
    .build();
```

### Pattern: Conditional Tool Execution

**Use case:** Tool decides next action

```java
public static Map<String, Object> checkAndEscalate(
    String issue,
    ToolContext ctx
) {
    if (isComplex(issue)) {
        ctx.actions().setTransferToAgent("expert-agent");
        return Map.of("status", "escalated");
    }
    return resolveIssue(issue);
}
```

## Development Workflow

### 1. Prototype Locally

```bash
# Create project structure
mkdir -p src/main/java/agents

# Add ADK dependencies to pom.xml
# Create agent class with ROOT_AGENT

# Run with Dev UI
mvn exec:java \
  -Dexec.mainClass="com.google.adk.web.AdkWebServer" \
  -Dexec.args="--adk.agents.source-dir=src/main/java"
```

### 2. Test Iteratively

- Use Dev UI to interact with agent
- Inspect Events tab for tool calls and responses
- Review Trace for latency and execution flow
- Refine instructions and tools based on behavior

### 3. Deploy to Production

- Containerize with Docker
- Deploy to Cloud Run or Kubernetes
- Configure environment variables
- Set up monitoring and logging
- Implement error handling and retries

**See:** [deployment.md](references/deployment.md) for complete deployment guide

## Troubleshooting

### Agent not responding as expected

**Check:**
1. Instructions clarity - Are they specific enough?
2. Tool descriptions - Does LLM understand when to use them?
3. State management - Is data properly stored and referenced?
4. Model selection - Is the model appropriate for the task?

### Tool not being called

**Check:**
1. Tool function signature has `@Schema` annotations
2. Tool is added to agent's `.tools()` list
3. Tool name is mentioned in agent instructions
4. Tool description clearly indicates when to use it

### State not persisting

**Check:**
1. Using `.outputKey()` to store results
2. Referencing with correct `{key}` syntax in instructions
3. Agents share same session
4. Session service is properly configured

### Performance issues

**Solutions:**
- Use ParallelAgent for independent tasks
- Cache frequently accessed data
- Optimize tool execution time
- Use connection pooling for databases
- Consider async operations

## Best Practices

### Instructions

- **Be specific:** Clear, detailed instructions improve LLM performance
- **Include examples:** Show expected behavior when possible
- **Reference tools:** Explicitly mention tool names and usage
- **Format output:** Specify desired response structure

### Tool Design

- **Single responsibility:** Each tool does one thing well
- **Clear naming:** Use action-oriented, descriptive names
- **Structured returns:** Return dictionaries with meaningful keys
- **Error handling:** Return errors in structured format

### Architecture

- **Modular design:** Break complex tasks into specialized agents
- **Clear hierarchy:** Define parent-child relationships intentionally
- **Appropriate patterns:** Match workflow type to use case
- **State management:** Keep state keys descriptive and organized

### Testing

- **Test tools independently:** Verify before integrating
- **Use Dev UI:** Interactive testing during development
- **Test edge cases:** Handle errors and unexpected inputs
- **Monitor in production:** Track performance and errors

## Additional Resources

- **Official Documentation:** https://google.github.io/adk-docs/
- **GitHub Repository:** https://github.com/google/adk-java
- **Samples Repository:** https://github.com/google/adk-samples
- **Community:** https://www.reddit.com/r/agentdevelopmentkit/

## Version Information

This skill is based on ADK Java version 0.2.0 (latest as of October 2025).

**Key features in 0.2.0:**
- LangChain4j integration for third-party models
- Instance-based FunctionTools
- Improved async support with Single return types
- Enhanced loop control with endInvocation
- InMemoryMemoryService for simple memory management
- VertexAiRagRetrieval for advanced RAG patterns
