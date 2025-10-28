# ADK Java Tools and Integration

## Tool Overview

Tools extend agent capabilities beyond text generation, enabling agents to:
- Access real-time information
- Interact with external systems
- Perform calculations and data processing
- Execute code
- Call APIs

## Tool Types

### 1. Function Tools

Custom tools created from Java methods for application-specific needs.

#### Basic Function Tool

```java
import com.google.adk.tools.FunctionTool;
import com.google.adk.tools.Annotations.Schema;

public class WeatherTool {
    // Method becomes a tool
    public static Map<String, Object> getWeather(
        @Schema(description = "The location to get weather for") 
        String location
    ) {
        Map<String, Object> result = new HashMap<>();
        result.put("location", location);
        result.put("temperature", "72");
        result.put("unit", "F");
        return result;
    }
}

// Create tool from static method
FunctionTool weatherTool = FunctionTool.create(
    WeatherTool.class, 
    "getWeather"
);

// Use in agent
LlmAgent agent = LlmAgent.builder()
    .tools(weatherTool)
    .build();
```

#### Instance-Based Function Tools

Create tools from object instances (not just static methods):

```java
public class DatabaseService {
    private Connection connection;
    
    public DatabaseService(Connection conn) {
        this.connection = conn;
    }
    
    public Map<String, Object> queryUser(
        @Schema(description = "User ID to query") 
        String userId
    ) {
        // Query database using instance connection
        return userData;
    }
}

// Create tool from instance
DatabaseService dbService = new DatabaseService(connection);
FunctionTool dbTool = FunctionTool.create(dbService, "queryUser");
```

#### @Schema Annotations

Use `@Schema` to help the LLM understand parameters:

```java
public static String searchDocuments(
    @Schema(description = "Search query keywords")
    String query,
    
    @Schema(description = "Maximum number of results to return")
    int maxResults,
    
    @Schema(description = "Filter by document type: PDF, DOCX, TXT")
    String documentType
) {
    // Implementation
}
```

**Best practices:**
- Provide clear, descriptive parameter descriptions
- Mention valid values or formats when relevant
- Explain what each parameter does

#### Return Values

**Preferred: Dictionary/Map**
```java
public static Map<String, Object> analyzeText(String text) {
    Map<String, Object> result = new HashMap<>();
    result.put("sentiment", "positive");
    result.put("confidence", 0.87);
    result.put("keywords", Arrays.asList("AI", "agents", "tools"));
    return result;
}
```

**Other types**: Automatically wrapped in `{"result": value}`

### 2. Long-Running Function Tools

For operations that take significant time or require human-in-the-loop:

```java
import com.google.adk.tools.LongRunningFunctionTool;

public class ApprovalService {
    public Map<String, Object> requestApproval(
        @Schema(description = "Expense amount")
        double amount,
        
        @Schema(description = "Expense reason")
        String reason
    ) {
        // Acknowledge request
        String requestId = createApprovalRequest(amount, reason);
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "pending");
        response.put("request_id", requestId);
        response.put("message", "Approval request submitted");
        return response;
    }
}

// Wrap as long-running tool
LongRunningFunctionTool approvalTool = new LongRunningFunctionTool(
    FunctionTool.create(ApprovalService.class, "requestApproval")
);
```

**Use cases:**
- Human approval workflows
- Long-running computations
- External system integrations with delays
- Asynchronous operations

### 3. Async Function Tools

Support for methods returning `Single` (RxJava):

```java
import io.reactivex.rxjava3.core.Single;

public class AsyncService {
    public Single<Map<String, Object>> fetchDataAsync(String query) {
        return Single.fromCallable(() -> {
            // Asynchronous operation
            return performQuery(query);
        }).subscribeOn(Schedulers.io());
    }
}

FunctionTool asyncTool = FunctionTool.create(
    AsyncService.class, 
    "fetchDataAsync"
);
```

### 4. Tool Context

Access runtime context and control agent behavior:

```java
import com.google.adk.tools.ToolContext;
import com.google.adk.context.InvocationContext;

public static Map<String, Object> escalateTool(
    @Schema(description = "Issue to escalate")
    String issue,
    
    ToolContext toolContext
) {
    // Access session state
    InvocationContext ctx = toolContext.getInvocationContext();
    String userId = ctx.session().userId();
    Map<String, Object> state = ctx.session().state();
    
    // Control agent behavior
    if (requiresHumanIntervention(issue)) {
        toolContext.actions().setTransferToAgent("human-agent");
        toolContext.actions().setSkipSummarization(true);
    }
    
    return Map.of("status", "escalated");
}
```

**ToolContext capabilities:**
- `getInvocationContext()`: Access session, user info, state
- `actions().setSkipSummarization(true)`: Skip LLM summarization
- `actions().setTransferToAgent("name")`: Transfer to another agent
- `actions().setEscalate(true)`: Escalate to parent agent
- `actions().setEndInvocation(true)`: Stop agent loop

## Built-in Tools

### GoogleSearchTool

```java
import com.google.adk.tools.GoogleSearchTool;

LlmAgent agent = LlmAgent.builder()
    .name("search-assistant")
    .instruction("Use Google Search for recent information...")
    .tools(new GoogleSearchTool())
    .build();
```

**Use cases:**
- Recent events and news
- Real-time information
- Fact verification
- Research and information gathering

### Code Execution Tool

Execute code directly within the model (model-native feature):

```java
// Enable code execution via model configuration
// Specific to Gemini models with code execution capability
```

**Use cases:**
- Data analysis and computation
- Running calculations
- Processing structured data
- Quick prototyping

## Agents as Tools

Use specialized agents as tools for other agents:

```java
// Specialist agent
LlmAgent searchSpecialist = LlmAgent.builder()
    .name("search-specialist")
    .description("Expert at web searches")
    .tools(new GoogleSearchTool())
    .build();

// Use as tool in parent agent
LlmAgent coordinator = LlmAgent.builder()
    .name("coordinator")
    .instruction("Use search-specialist for web queries...")
    .subAgents(searchSpecialist)
    .build();
```

**Benefits:**
- Modular specialization
- Reusable components
- Clear separation of concerns
- Hierarchical orchestration

## Third-Party Tool Integration

### LangChain Tools

Integrate LangChain ecosystem tools:

```java
// Example: Using LangChain StackExchange tool
// (Requires appropriate LangChain4j dependencies)
```

### MCP (Model Context Protocol) Tools

Connect to MCP servers for standardized tool access:

#### Remote MCP Servers

```java
import com.google.adk.tools.mcp.MCPToolset;

// Connect to GitHub MCP server
MCPToolset githubTools = MCPToolset.builder()
    .serverUrl("https://github-mcp-server.example.com")
    .build();

LlmAgent agent = LlmAgent.builder()
    .tools(githubTools)
    .build();
```

**Available MCP servers:**
- GitHub (issues, PRs, notifications, security)
- StackExchange
- Database connectors
- Custom MCP servers

#### Local MCP Servers

```java
// Start local MCP server
MCPServer mcpServer = new MCPServer();
mcpServer.registerTool("custom-tool", customToolImpl);
mcpServer.start(8081);

// Connect agent to local server
MCPToolset localTools = MCPToolset.builder()
    .serverUrl("http://localhost:8081")
    .build();
```

**Benefits of MCP:**
- Standardized protocol
- Tool discovery
- Reduced duplication
- Easy sharing across frameworks

## Tool Best Practices

### Tool Design

1. **Single Responsibility**: Each tool should do one thing well
2. **Clear Naming**: Use descriptive, action-oriented names
3. **Good Descriptions**: Help the LLM understand when to use the tool
4. **Structured Returns**: Return dictionaries with meaningful keys
5. **Error Handling**: Return error information in structured format

```java
public static Map<String, Object> processPayment(
    @Schema(description = "Payment amount in USD")
    double amount,
    
    @Schema(description = "Payment method: card, paypal, or bank")
    String method
) {
    try {
        PaymentResult result = paymentService.process(amount, method);
        return Map.of(
            "success", true,
            "transaction_id", result.getId(),
            "confirmation", result.getConfirmation()
        );
    } catch (PaymentException e) {
        return Map.of(
            "success", false,
            "error", e.getMessage(),
            "error_code", e.getCode()
        );
    }
}
```

### Instruction Integration

Reference tools explicitly in agent instructions:

```java
LlmAgent agent = LlmAgent.builder()
    .instruction("""
        You are a customer service agent.
        
        When users ask about order status, use the `checkOrderStatus` tool.
        When users request refunds, use the `processRefund` tool.
        For payment issues, use the `investigatePayment` tool.
        
        Always verify information before taking actions.
        """)
    .tools(orderTool, refundTool, paymentTool)
    .build();
```

### Tool Selection Tips

- **Start simple**: Begin with Function Tools for custom needs
- **Use built-ins**: Leverage GoogleSearchTool when appropriate
- **Consider MCP**: For standard integrations (GitHub, databases)
- **Agent-as-tool**: For complex, specialized sub-tasks
- **LangChain**: When you need existing third-party integrations

### Testing Tools

Test tools independently before integrating:

```java
@Test
public void testWeatherTool() {
    Map<String, Object> result = WeatherTool.getWeather("Seattle");
    
    assertNotNull(result);
    assertEquals("Seattle", result.get("location"));
    assertTrue(result.containsKey("temperature"));
}
```

## Advanced Patterns

### Conditional Tool Access

```java
public static Map<String, Object> sensitiveOperation(
    String param,
    ToolContext toolContext
) {
    InvocationContext ctx = toolContext.getInvocationContext();
    String userId = ctx.session().userId();
    
    if (!hasPermission(userId, "sensitive_access")) {
        toolContext.actions().setTransferToAgent("approval-agent");
        return Map.of("status", "requires_approval");
    }
    
    // Proceed with operation
    return performOperation(param);
}
```

### Tool Chaining via State

```java
// Tool 1: Fetch data
public static Map<String, Object> fetchData(String query) {
    return Map.of("data", queryResult);
}

LlmAgent fetcher = LlmAgent.builder()
    .tools(fetchTool)
    .outputKey("fetched_data")  // Store in state
    .build();

// Tool 2: Process data from state
LlmAgent processor = LlmAgent.builder()
    .instruction("Process this data: {fetched_data}")
    .tools(processingTool)
    .build();

// Chain with Sequential
SequentialAgent pipeline = SequentialAgent.builder()
    .subAgents(fetcher, processor)
    .build();
```

### Dynamic Tool Loading

```java
public class DynamicToolLoader {
    public static List<FunctionTool> loadTools(String category) {
        List<FunctionTool> tools = new ArrayList<>();
        
        switch (category) {
            case "finance":
                tools.add(FunctionTool.create(FinanceTools.class, "getStockPrice"));
                tools.add(FunctionTool.create(FinanceTools.class, "analyzePortfolio"));
                break;
            case "weather":
                tools.add(FunctionTool.create(WeatherTools.class, "getCurrentWeather"));
                tools.add(FunctionTool.create(WeatherTools.class, "getForecast"));
                break;
        }
        
        return tools;
    }
}

// Use dynamic tools
List<FunctionTool> tools = DynamicToolLoader.loadTools("finance");
LlmAgent agent = LlmAgent.builder()
    .tools(tools.toArray(new FunctionTool[0]))
    .build();
```

## Integration Examples

### Database Integration

```java
public class DatabaseTools {
    private final DataSource dataSource;
    
    public DatabaseTools(DataSource ds) {
        this.dataSource = ds;
    }
    
    public Map<String, Object> queryCustomer(
        @Schema(description = "Customer email address")
        String email
    ) {
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM customers WHERE email = ?"
            );
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return Map.of(
                    "found", true,
                    "name", rs.getString("name"),
                    "id", rs.getString("id"),
                    "status", rs.getString("status")
                );
            } else {
                return Map.of("found", false);
            }
        } catch (SQLException e) {
            return Map.of(
                "error", true,
                "message", e.getMessage()
            );
        }
    }
}
```

### REST API Integration

```java
public class APITools {
    private final HttpClient httpClient;
    private final String apiKey;
    
    public Map<String, Object> callExternalAPI(
        @Schema(description = "API endpoint path")
        String endpoint
    ) {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("https://api.example.com/" + endpoint))
            .header("Authorization", "Bearer " + apiKey)
            .GET()
            .build();
        
        try {
            HttpResponse<String> response = httpClient.send(
                request, 
                HttpResponse.BodyHandlers.ofString()
            );
            
            return Map.of(
                "status", response.statusCode(),
                "body", response.body()
            );
        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }
}
```

### File System Integration

```java
public class FileTools {
    public static Map<String, Object> readFile(
        @Schema(description = "Path to file to read")
        String filePath
    ) {
        try {
            String content = Files.readString(Path.of(filePath));
            return Map.of(
                "success", true,
                "content", content,
                "size", content.length()
            );
        } catch (IOException e) {
            return Map.of(
                "success", false,
                "error", e.getMessage()
            );
        }
    }
}
```
