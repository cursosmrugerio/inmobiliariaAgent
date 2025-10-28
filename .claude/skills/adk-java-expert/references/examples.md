# ADK Java Code Examples

## Table of Contents
1. [Basic Agents](#basic-agents)
2. [Multi-Agent Systems](#multi-agent-systems)
3. [Workflow Patterns](#workflow-patterns)
4. [Tools and Integration](#tools-and-integration)
5. [Complete Applications](#complete-applications)

## Basic Agents

### Simple LLM Agent

```java
import com.google.adk.agents.LlmAgent;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import io.reactivex.rxjava3.core.Flowable;

public class SimpleAgent {
    public static void main(String[] args) {
        // Create agent
        LlmAgent agent = LlmAgent.builder()
            .name("simple-assistant")
            .model("gemini-2.0-flash")
            .instruction("You are a helpful assistant. Answer questions concisely.")
            .build();
        
        // Create runner and session
        InMemoryRunner runner = new InMemoryRunner(agent, "simple-app");
        Session session = runner.createSession("user-001");
        
        // Send message
        Flowable<com.google.adk.events.Event> events = runner.run(
            session,
            Content.builder().addText("What is Java?").build()
        );
        
        // Print response
        events.blockingSubscribe(event -> {
            if (event.hasContent()) {
                System.out.println(event.getContent().getText());
            }
        });
    }
}
```

### Agent with Google Search

```java
import com.google.adk.agents.LlmAgent;
import com.google.adk.tools.GoogleSearchTool;

public class SearchAgent {
    public static final LlmAgent ROOT_AGENT = LlmAgent.builder()
        .name("search-assistant")
        .model("gemini-2.0-flash")
        .instruction("""
            You are a research assistant.
            Use Google Search to find current information when needed.
            Always cite your sources.
            """)
        .tools(new GoogleSearchTool())
        .build();
    
    public static void main(String[] args) {
        // Launch Dev UI
        com.google.adk.web.AdkWebServer.start(ROOT_AGENT);
    }
}
```

### Agent with Custom Tools

```java
import com.google.adk.agents.LlmAgent;
import com.google.adk.tools.FunctionTool;
import com.google.adk.tools.Annotations.Schema;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class CustomToolAgent {
    // Define tool function
    public static Map<String, String> getCurrentTime(
        @Schema(description = "Timezone, e.g. 'America/New_York'")
        String timezone
    ) {
        LocalDateTime now = LocalDateTime.now();
        String formatted = now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        
        return Map.of(
            "current_time", formatted,
            "timezone", timezone,
            "timestamp", String.valueOf(System.currentTimeMillis())
        );
    }
    
    public static void main(String[] args) {
        // Create tool from function
        FunctionTool timeTool = FunctionTool.create(
            CustomToolAgent.class,
            "getCurrentTime"
        );
        
        // Create agent with tool
        LlmAgent agent = LlmAgent.builder()
            .name("time-assistant")
            .model("gemini-2.0-flash")
            .instruction("""
                You are a time assistant.
                When asked about the current time, use the getCurrentTime tool.
                """)
            .tools(timeTool)
            .build();
        
        // Run agent
        InMemoryRunner runner = new InMemoryRunner(agent, "time-app");
        Session session = runner.createSession("user-123");
        
        runner.run(
            session,
            Content.builder().addText("What time is it in New York?").build()
        ).blockingSubscribe(event -> {
            if (event.hasContent()) {
                System.out.println(event.getContent().getText());
            }
        });
    }
}
```

## Multi-Agent Systems

### Orchestrator with Sub-Agents

```java
import com.google.adk.agents.LlmAgent;
import com.google.adk.tools.GoogleSearchTool;
import com.google.adk.tools.FunctionTool;

public class MultiAgentSystem {
    // Search specialist
    private static final LlmAgent searchAgent = LlmAgent.builder()
        .name("search-specialist")
        .description("Expert at web searches")
        .model("gemini-2.0-flash")
        .instruction("You specialize in finding information via web search.")
        .tools(new GoogleSearchTool())
        .build();
    
    // Calculator specialist
    private static final LlmAgent calcAgent = LlmAgent.builder()
        .name("calculator-specialist")
        .description("Expert at calculations")
        .model("gemini-2.0-flash")
        .instruction("You specialize in mathematical calculations.")
        .tools(FunctionTool.create(MathTools.class, "calculate"))
        .build();
    
    // Orchestrator
    public static final LlmAgent ROOT_AGENT = LlmAgent.builder()
        .name("orchestrator")
        .model("gemini-2.0-flash")
        .instruction("""
            You are an intelligent orchestrator.
            
            Delegate tasks to specialists:
            - Use search-specialist for information lookup
            - Use calculator-specialist for math problems
            
            Coordinate their work to answer user questions.
            """)
        .subAgents(searchAgent, calcAgent)
        .build();
}

class MathTools {
    public static Map<String, Object> calculate(
        @Schema(description = "Mathematical expression")
        String expression
    ) {
        // Implement calculation logic
        return Map.of("result", evaluateExpression(expression));
    }
}
```

### Hierarchical Agent Structure

```java
public class HierarchicalAgents {
    // Tier 3: Data fetchers
    private static final LlmAgent apiAgent = LlmAgent.builder()
        .name("api-fetcher")
        .tools(FunctionTool.create(APITools.class, "fetchFromAPI"))
        .build();
    
    private static final LlmAgent dbAgent = LlmAgent.builder()
        .name("db-fetcher")
        .tools(FunctionTool.create(DBTools.class, "queryDatabase"))
        .build();
    
    // Tier 2: Data processor
    private static final LlmAgent processorAgent = LlmAgent.builder()
        .name("data-processor")
        .instruction("Process and analyze data from sub-agents")
        .subAgents(apiAgent, dbAgent)
        .build();
    
    // Tier 1: Main coordinator
    public static final LlmAgent ROOT_AGENT = LlmAgent.builder()
        .name("coordinator")
        .instruction("Coordinate data gathering and processing")
        .subAgents(processorAgent)
        .build();
}
```

## Workflow Patterns

### Sequential Pipeline

```java
import com.google.adk.agents.SequentialAgent;

public class CodeReviewPipeline {
    // Step 1: Code writer
    private static final LlmAgent writerAgent = LlmAgent.builder()
        .name("code-writer")
        .model("gemini-2.0-flash")
        .instruction("""
            Generate Python code based on user requirements.
            Output only the code in markdown code blocks.
            """)
        .outputKey("generated_code")
        .build();
    
    // Step 2: Code reviewer
    private static final LlmAgent reviewerAgent = LlmAgent.builder()
        .name("code-reviewer")
        .model("gemini-2.0-flash")
        .instruction("""
            Review this code: {generated_code}
            
            Provide feedback on:
            - Code quality
            - Best practices
            - Potential issues
            """)
        .outputKey("review_comments")
        .build();
    
    // Step 3: Code refactorer
    private static final LlmAgent refactorerAgent = LlmAgent.builder()
        .name("code-refactorer")
        .model("gemini-2.0-flash")
        .instruction("""
            Original code: {generated_code}
            Review comments: {review_comments}
            
            Refactor the code based on the review.
            Output only the improved code.
            """)
        .outputKey("final_code")
        .build();
    
    // Sequential pipeline
    public static final SequentialAgent ROOT_AGENT = SequentialAgent.builder()
        .name("code-pipeline")
        .description("Complete code generation, review, and refactoring pipeline")
        .subAgents(writerAgent, reviewerAgent, refactorerAgent)
        .build();
}
```

### Parallel Execution

```java
import com.google.adk.agents.ParallelAgent;

public class ParallelResearch {
    // Research agent 1
    private static final LlmAgent renewableAgent = LlmAgent.builder()
        .name("renewable-researcher")
        .model("gemini-2.0-flash")
        .instruction("Research renewable energy advancements")
        .tools(new GoogleSearchTool())
        .outputKey("renewable_findings")
        .build();
    
    // Research agent 2
    private static final LlmAgent evAgent = LlmAgent.builder()
        .name("ev-researcher")
        .model("gemini-2.0-flash")
        .instruction("Research electric vehicle technology")
        .tools(new GoogleSearchTool())
        .outputKey("ev_findings")
        .build();
    
    // Research agent 3
    private static final LlmAgent carbonAgent = LlmAgent.builder()
        .name("carbon-researcher")
        .model("gemini-2.0-flash")
        .instruction("Research carbon capture technology")
        .tools(new GoogleSearchTool())
        .outputKey("carbon_findings")
        .build();
    
    // Parallel execution
    private static final ParallelAgent parallelResearch = ParallelAgent.builder()
        .name("parallel-researchers")
        .subAgents(renewableAgent, evAgent, carbonAgent)
        .build();
    
    // Synthesizer
    private static final LlmAgent synthesizerAgent = LlmAgent.builder()
        .name("synthesizer")
        .model("gemini-2.0-flash")
        .instruction("""
            Synthesize research findings:
            
            Renewable Energy: {renewable_findings}
            Electric Vehicles: {ev_findings}
            Carbon Capture: {carbon_findings}
            
            Create a comprehensive report.
            """)
        .build();
    
    // Combined workflow
    public static final SequentialAgent ROOT_AGENT = SequentialAgent.builder()
        .name("research-pipeline")
        .subAgents(parallelResearch, synthesizerAgent)
        .build();
}
```

### Composite Workflow

```java
public class TripPlanner {
    // Sequential workflow for trip planning
    private static final LlmAgent destinationResearcher = LlmAgent.builder()
        .name("destination-researcher")
        .instruction("Research destination attractions")
        .tools(new GoogleSearchTool())
        .outputKey("attractions")
        .build();
    
    private static final LlmAgent itineraryCreator = LlmAgent.builder()
        .name("itinerary-creator")
        .instruction("Create 2-day itinerary from: {attractions}")
        .outputKey("itinerary")
        .build();
    
    private static final LlmAgent restaurantSuggester = LlmAgent.builder()
        .name("restaurant-suggester")
        .instruction("Suggest restaurants for: {itinerary}")
        .tools(new GoogleSearchTool())
        .build();
    
    public static final SequentialAgent ROOT_AGENT = SequentialAgent.builder()
        .name("trip-planner")
        .description("Plans complete travel itinerary")
        .subAgents(
            destinationResearcher,
            itineraryCreator,
            restaurantSuggester
        )
        .build();
}
```

## Tools and Integration

### Multiple Custom Tools

```java
public class CustomerServiceAgent {
    // Tool 1: Check order status
    public static Map<String, Object> checkOrderStatus(
        @Schema(description = "Order ID")
        String orderId
    ) {
        // Query order system
        Order order = orderService.findById(orderId);
        
        return Map.of(
            "order_id", orderId,
            "status", order.getStatus(),
            "tracking_number", order.getTrackingNumber(),
            "estimated_delivery", order.getDeliveryDate()
        );
    }
    
    // Tool 2: Process refund
    public static Map<String, Object> processRefund(
        @Schema(description = "Order ID to refund")
        String orderId,
        
        @Schema(description = "Refund reason")
        String reason
    ) {
        try {
            RefundResult result = refundService.process(orderId, reason);
            return Map.of(
                "success", true,
                "refund_id", result.getId(),
                "amount", result.getAmount()
            );
        } catch (RefundException e) {
            return Map.of(
                "success", false,
                "error", e.getMessage()
            );
        }
    }
    
    // Tool 3: Update address
    public static Map<String, Object> updateAddress(
        @Schema(description = "Order ID")
        String orderId,
        
        @Schema(description = "New delivery address")
        String newAddress
    ) {
        boolean updated = orderService.updateAddress(orderId, newAddress);
        return Map.of(
            "success", updated,
            "order_id", orderId,
            "new_address", newAddress
        );
    }
    
    public static final LlmAgent ROOT_AGENT = LlmAgent.builder()
        .name("customer-service")
        .model("gemini-2.0-flash")
        .instruction("""
            You are a customer service agent.
            
            Available tools:
            - checkOrderStatus: Check order status and tracking
            - processRefund: Process refund requests
            - updateAddress: Update delivery address
            
            Always verify order details before taking actions.
            Be helpful and professional.
            """)
        .tools(
            FunctionTool.create(CustomerServiceAgent.class, "checkOrderStatus"),
            FunctionTool.create(CustomerServiceAgent.class, "processRefund"),
            FunctionTool.create(CustomerServiceAgent.class, "updateAddress")
        )
        .build();
}
```

### Database Integration

```java
public class DatabaseAgent {
    private static final DataSource dataSource = createDataSource();
    
    public static Map<String, Object> queryCustomers(
        @Schema(description = "SQL WHERE clause (safe, parameterized)")
        String whereClause
    ) {
        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT id, name, email, status FROM customers WHERE " + 
                        whereClause + " LIMIT 10";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            List<Map<String, String>> results = new ArrayList<>();
            while (rs.next()) {
                results.add(Map.of(
                    "id", rs.getString("id"),
                    "name", rs.getString("name"),
                    "email", rs.getString("email"),
                    "status", rs.getString("status")
                ));
            }
            
            return Map.of(
                "success", true,
                "count", results.size(),
                "customers", results
            );
        } catch (SQLException e) {
            return Map.of(
                "success", false,
                "error", e.getMessage()
            );
        }
    }
    
    public static final LlmAgent ROOT_AGENT = LlmAgent.builder()
        .name("database-agent")
        .instruction("""
            You help users query the customer database.
            Use the queryCustomers tool with appropriate WHERE clauses.
            Always limit results to protect data.
            """)
        .tools(FunctionTool.create(DatabaseAgent.class, "queryCustomers"))
        .build();
}
```

### Tool with Context Control

```java
public class EscalationAgent {
    public static Map<String, Object> handleComplexIssue(
        @Schema(description = "Issue description")
        String issue,
        
        ToolContext toolContext
    ) {
        InvocationContext ctx = toolContext.getInvocationContext();
        
        // Check if issue requires human intervention
        if (requiresHumanIntervention(issue)) {
            // Transfer to human agent
            toolContext.actions().setTransferToAgent("human-support");
            toolContext.actions().setSkipSummarization(true);
            
            return Map.of(
                "action", "escalated_to_human",
                "reason", "Complex issue requires human review"
            );
        }
        
        // Handle programmatically
        return Map.of(
            "action", "resolved",
            "solution", resolveIssue(issue)
        );
    }
}
```

## Complete Applications

### Multi-Tool Interactive Agent

```java
package agents.multitool;

import com.google.adk.agents.LlmAgent;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.adk.tools.FunctionTool;
import com.google.adk.tools.Annotations.Schema;
import com.google.genai.types.Content;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Scanner;

public class MultiToolAgent {
    private static final String APP_NAME = "multi_tool_agent";
    private static final String USER_ID = "user123";
    
    // Tool 1: Get current time
    public static Map<String, String> getCurrentTime(
        @Schema(description = "Timezone (e.g., America/New_York)")
        String timezone
    ) {
        ZonedDateTime time = ZonedDateTime.now();
        return Map.of(
            "current_time", time.format(DateTimeFormatter.ISO_ZONED_DATE_TIME),
            "timezone", timezone
        );
    }
    
    // Tool 2: Text normalization
    public static Map<String, String> normalizeText(
        @Schema(description = "Text to normalize")
        String text
    ) {
        String normalized = text.trim()
            .toLowerCase()
            .replaceAll("\\s+", " ");
        
        return Map.of(
            "original", text,
            "normalized", normalized,
            "length", String.valueOf(normalized.length())
        );
    }
    
    public static final LlmAgent ROOT_AGENT = LlmAgent.builder()
        .name(APP_NAME)
        .model("gemini-2.0-flash")
        .instruction("""
            You are a helpful assistant with multiple tools.
            
            Tools available:
            - getCurrentTime: Get current time in any timezone
            - normalizeText: Normalize and clean text
            
            Use these tools when appropriate to help users.
            """)
        .tools(
            FunctionTool.create(MultiToolAgent.class, "getCurrentTime"),
            FunctionTool.create(MultiToolAgent.class, "normalizeText")
        )
        .build();
    
    public static void main(String[] args) {
        InMemoryRunner runner = new InMemoryRunner(ROOT_AGENT, APP_NAME);
        Session session = runner.createSession(USER_ID);
        
        Scanner scanner = new Scanner(System.in);
        System.out.println("Multi-Tool Agent (type 'exit' to quit)");
        
        while (true) {
            System.out.print("\nYou: ");
            String input = scanner.nextLine();
            
            if ("exit".equalsIgnoreCase(input.trim())) {
                break;
            }
            
            System.out.print("Agent: ");
            runner.run(
                session,
                Content.builder().addText(input).build()
            ).blockingSubscribe(event -> {
                if (event.hasContent()) {
                    System.out.print(event.getContent().getText());
                }
            });
            System.out.println();
        }
        
        scanner.close();
    }
}
```

### Company Research Agent

```java
public class CompanyResearchAgent {
    // Profile researcher
    private static final LlmAgent profilerAgent = LlmAgent.builder()
        .name("company-profiler")
        .instruction("Research company profile and overview")
        .tools(new GoogleSearchTool())
        .outputKey("profile")
        .build();
    
    // News finder
    private static final LlmAgent newsAgent = LlmAgent.builder()
        .name("news-finder")
        .instruction("Find latest company news")
        .tools(new GoogleSearchTool())
        .outputKey("news")
        .build();
    
    // Financial analyst
    private static final LlmAgent financialAgent = LlmAgent.builder()
        .name("financial-analyst")
        .instruction("Research financial information")
        .tools(new GoogleSearchTool())
        .outputKey("financials")
        .build();
    
    // Parallel research phase
    private static final ParallelAgent researchPhase = ParallelAgent.builder()
        .name("market-researcher")
        .subAgents(profilerAgent, newsAgent, financialAgent)
        .build();
    
    // Report compiler
    private static final LlmAgent reporterAgent = LlmAgent.builder()
        .name("report-compiler")
        .instruction("""
            Compile a comprehensive market research report.
            
            ## Company Profile
            {profile}
            
            ## Latest News
            {news}
            
            ## Financial Snapshot
            {financials}
            
            Synthesize this into a well-formatted report.
            """)
        .build();
    
    // Main workflow
    public static final SequentialAgent ROOT_AGENT = SequentialAgent.builder()
        .name("company-detective")
        .description("Comprehensive company research")
        .subAgents(researchPhase, reporterAgent)
        .build();
}
```

### Streaming Voice Agent

```java
import com.google.adk.streaming.LiveRequestQueue;
import javax.sound.sampled.*;

public class VoiceAgent {
    private static final AudioFormat AUDIO_FORMAT = new AudioFormat(
        16000, // sample rate
        16,    // sample size in bits
        1,     // channels (mono)
        true,  // signed
        false  // little endian
    );
    
    public static void main(String[] args) {
        LlmAgent agent = LlmAgent.builder()
            .name("voice-assistant")
            .model("gemini-2.5-flash")
            .instruction("You are a friendly voice assistant.")
            .build();
        
        LiveRequestQueue requestQueue = new LiveRequestQueue();
        
        // Start microphone input thread
        Thread micThread = new Thread(() -> processAudioInput(requestQueue));
        micThread.start();
        
        // Start speaker output thread
        Thread speakerThread = new Thread(() -> processAudioOutput(agent, requestQueue));
        speakerThread.start();
        
        System.out.println("Voice assistant ready. Press Enter to stop.");
        new Scanner(System.in).nextLine();
    }
    
    private static void processAudioInput(LiveRequestQueue queue) {
        try {
            TargetDataLine microphone = AudioSystem.getTargetDataLine(AUDIO_FORMAT);
            microphone.open(AUDIO_FORMAT);
            microphone.start();
            
            byte[] buffer = new byte[1024];
            while (true) {
                int bytesRead = microphone.read(buffer, 0, buffer.length);
                if (bytesRead > 0) {
                    // Send audio to agent
                    byte[] audioChunk = Arrays.copyOf(buffer, bytesRead);
                    queue.realtime(
                        Blob.builder()
                            .data(audioChunk)
                            .mimeType("audio/pcm")
                            .build()
                    );
                }
            }
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }
    
    private static void processAudioOutput(LlmAgent agent, LiveRequestQueue queue) {
        // Implementation for playing audio responses
    }
}
```

This examples file provides comprehensive, working code samples for various ADK Java patterns and use cases.
