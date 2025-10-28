# ADK Java Deployment Guide

## Deployment Options

ADK agents can be deployed anywhere Java applications run:
- Local development (laptop)
- Google Cloud (Cloud Run, Vertex AI Agent Engine)
- Kubernetes clusters
- Traditional servers
- Docker containers

## Project Setup

### Maven Dependencies

```xml
<dependencies>
    <!-- Core ADK -->
    <dependency>
        <groupId>com.google.adk</groupId>
        <artifactId>google-adk</artifactId>
        <version>0.2.0</version>
    </dependency>
    
    <!-- Dev UI (development only) -->
    <dependency>
        <groupId>com.google.adk</groupId>
        <artifactId>google-adk-dev</artifactId>
        <version>0.2.0</version>
        <scope>provided</scope>
    </dependency>
    
    <!-- LangChain4j integration (optional) -->
    <dependency>
        <groupId>com.google.adk</groupId>
        <artifactId>google-adk-contrib-langchain4j</artifactId>
        <version>0.2.0</version>
    </dependency>
</dependencies>
```

### Gradle Dependencies

```gradle
dependencies {
    implementation 'com.google.adk:google-adk:0.2.0'
    
    // Dev UI (development only)
    compileOnly 'com.google.adk:google-adk-dev:0.2.0'
    
    // LangChain4j integration (optional)
    implementation 'com.google.adk:google-adk-contrib-langchain4j:0.2.0'
}
```

### Using Unreleased Versions (JitPack)

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.google.adk-java</groupId>
    <artifactId>google-adk</artifactId>
    <version>main-SNAPSHOT</version>
</dependency>
```

### Project Structure

```
project-name/
├── pom.xml (or build.gradle)
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── agents/          # Agent definitions
│   │   │   │   ├── MainAgent.java
│   │   │   │   └── SubAgent.java
│   │   │   ├── tools/           # Custom tools
│   │   │   │   └── CustomTools.java
│   │   │   └── services/        # Business logic
│   │   │       └── DataService.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/
│           └── agents/
│               └── AgentTest.java
└── README.md
```

## Local Development

### Running with InMemoryRunner

```java
public class AgentApp {
    public static void main(String[] args) {
        // Build agent
        LlmAgent agent = LlmAgent.builder()
            .name("my-agent")
            .model("gemini-2.0-flash")
            .instruction("You are a helpful assistant...")
            .build();
        
        // Create runner
        InMemoryRunner runner = new InMemoryRunner(agent, "my-app");
        
        // Create session
        Session session = runner.createSession("user-123");
        
        // Interactive loop
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("You: ");
            String input = scanner.nextLine();
            
            if (input.equals("exit")) break;
            
            // Send message
            Flowable<Event> events = runner.run(
                session,
                Content.builder().addText(input).build()
            );
            
            // Process response
            System.out.print("Agent: ");
            events.blockingSubscribe(event -> {
                if (event.hasContent()) {
                    System.out.print(event.getContent().getText());
                }
            });
            System.out.println();
        }
    }
}
```

### Running Dev UI

**Maven:**
```bash
mvn exec:java \
  -Dexec.mainClass="com.google.adk.web.AdkWebServer" \
  -Dexec.args="--adk.agents.source-dir=src/main/java" \
  -Dexec.classpathScope="compile"
```

**Gradle:**
```bash
./gradlew run --args="--adk.agents.source-dir=src/main/java"
```

**From Java:**
```java
public class DevUIApp {
    public static void main(String[] args) {
        LlmAgent agent = LlmAgent.builder()
            .name("my-agent")
            .build();
        
        AdkWebServer.start(agent);
        // Navigate to http://localhost:8080
    }
}
```

## Environment Configuration

### API Keys and Credentials

**Using environment variables:**
```bash
export GOOGLE_API_KEY="your-gemini-api-key"
export ANTHROPIC_API_KEY="your-claude-api-key"
```

**In application.properties:**
```properties
google.api.key=${GOOGLE_API_KEY}
anthropic.api.key=${ANTHROPIC_API_KEY}
```

**In code:**
```java
String apiKey = System.getenv("GOOGLE_API_KEY");
if (apiKey == null) {
    throw new IllegalStateException("GOOGLE_API_KEY not set");
}
```

### Model Configuration

```java
// Using environment variable for model selection
String modelId = System.getenv("MODEL_ID");
if (modelId == null) {
    modelId = "gemini-2.0-flash";  // default
}

LlmAgent agent = LlmAgent.builder()
    .model(modelId)
    .build();
```

## Containerization

### Dockerfile

```dockerfile
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre

WORKDIR /app
COPY --from=build /app/target/agent-app.jar ./agent-app.jar

ENV GOOGLE_API_KEY=""
ENV PORT=8080

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "agent-app.jar"]
```

### Multi-stage Build for Optimization

```dockerfile
FROM maven:3.9-eclipse-temurin-17-alpine AS build

WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn package -DskipTests

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app
COPY --from=build /build/target/*.jar app.jar

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

EXPOSE 8080
ENTRYPOINT ["java", "-Xmx512m", "-jar", "app.jar"]
```

### Docker Compose for Development

```yaml
version: '3.8'

services:
  agent:
    build: .
    ports:
      - "8080:8080"
    environment:
      - GOOGLE_API_KEY=${GOOGLE_API_KEY}
      - MODEL_ID=gemini-2.0-flash
    volumes:
      - ./data:/app/data
    restart: unless-stopped

  postgres:
    image: postgres:15-alpine
    environment:
      - POSTGRES_DB=agentdb
      - POSTGRES_USER=agent
      - POSTGRES_PASSWORD=secret
    ports:
      - "5432:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data

volumes:
  postgres-data:
```

## Google Cloud Deployment

### Cloud Run

**Deploying to Cloud Run:**

```bash
# Set project
gcloud config set project YOUR_PROJECT_ID

# Build and deploy
gcloud run deploy agent-service \
  --source . \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated \
  --set-env-vars="MODEL_ID=gemini-2.0-flash" \
  --set-secrets="GOOGLE_API_KEY=gemini-api-key:latest"
```

**cloud-run.yaml:**
```yaml
apiVersion: serving.knative.dev/v1
kind: Service
metadata:
  name: agent-service
spec:
  template:
    spec:
      containers:
      - image: gcr.io/PROJECT_ID/agent-app:latest
        ports:
        - containerPort: 8080
        env:
        - name: MODEL_ID
          value: "gemini-2.0-flash"
        resources:
          limits:
            memory: 1Gi
            cpu: "1"
```

### Vertex AI Agent Engine

Deploy agents to Vertex AI for managed scaling:

```bash
# Deploy to Vertex AI Agent Engine Runtime
gcloud ai agents deploy \
  --region=us-central1 \
  --display-name="My Agent" \
  --agent-source=./agent-config.yaml
```

**Key benefits:**
- Managed infrastructure
- Auto-scaling
- Integrated monitoring
- Multi-region support

### Cloud Build for CI/CD

**cloudbuild.yaml:**
```yaml
steps:
  # Run tests
  - name: maven:3.9-eclipse-temurin-17
    entrypoint: mvn
    args: ['test']
  
  # Build jar
  - name: maven:3.9-eclipse-temurin-17
    entrypoint: mvn
    args: ['package', '-DskipTests']
  
  # Build Docker image
  - name: gcr.io/cloud-builders/docker
    args: 
      - build
      - -t
      - gcr.io/$PROJECT_ID/agent-app:$COMMIT_SHA
      - -t
      - gcr.io/$PROJECT_ID/agent-app:latest
      - .
  
  # Push to Container Registry
  - name: gcr.io/cloud-builders/docker
    args: 
      - push
      - gcr.io/$PROJECT_ID/agent-app:$COMMIT_SHA
  
  # Deploy to Cloud Run
  - name: gcr.io/google.com/cloudsdktool/cloud-sdk
    entrypoint: gcloud
    args:
      - run
      - deploy
      - agent-service
      - --image=gcr.io/$PROJECT_ID/agent-app:$COMMIT_SHA
      - --region=us-central1
      - --platform=managed

images:
  - gcr.io/$PROJECT_ID/agent-app:$COMMIT_SHA
  - gcr.io/$PROJECT_ID/agent-app:latest
```

## Production Considerations

### Logging

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AgentApp {
    private static final Logger logger = LoggerFactory.getLogger(AgentApp.class);
    
    public static void main(String[] args) {
        logger.info("Starting agent application");
        
        try {
            LlmAgent agent = buildAgent();
            InMemoryRunner runner = new InMemoryRunner(agent, "app");
            
            logger.info("Agent initialized successfully");
            
            // Run application
            runAgent(runner);
        } catch (Exception e) {
            logger.error("Fatal error in agent application", e);
            System.exit(1);
        }
    }
}
```

### Monitoring

Track key metrics:
- Request latency
- Token usage
- Error rates
- Tool call frequency
- Session duration

```java
public class MetricsCollector {
    private final MeterRegistry registry;
    
    public void recordAgentInteraction(
        String agentName, 
        long durationMs, 
        boolean success
    ) {
        registry.counter("agent.interactions",
            "agent", agentName,
            "status", success ? "success" : "error"
        ).increment();
        
        registry.timer("agent.duration",
            "agent", agentName
        ).record(Duration.ofMillis(durationMs));
    }
}
```

### Error Handling

```java
public class RobustAgentRunner {
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000;
    
    public Flowable<Event> runWithRetry(
        InMemoryRunner runner,
        Session session,
        Content message
    ) {
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                return runner.run(session, message);
            } catch (Exception e) {
                logger.warn("Attempt {} failed: {}", attempt, e.getMessage());
                
                if (attempt == MAX_RETRIES) {
                    logger.error("All retry attempts exhausted", e);
                    throw e;
                }
                
                try {
                    Thread.sleep(RETRY_DELAY_MS * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during retry", ie);
                }
            }
        }
        throw new IllegalStateException("Should not reach here");
    }
}
```

### Session Management

**In-memory (development):**
```java
InMemorySessionService sessionService = new InMemorySessionService();
```

**Persistent storage (production):**
```java
// Implement custom session service with database backing
public class DatabaseSessionService implements SessionService {
    private final DataSource dataSource;
    
    @Override
    public Session getSession(String sessionId) {
        // Load from database
    }
    
    @Override
    public void saveSession(Session session) {
        // Persist to database
    }
}
```

### Rate Limiting

```java
public class RateLimitedRunner {
    private final RateLimiter rateLimiter;
    
    public RateLimitedRunner(double permitsPerSecond) {
        this.rateLimiter = RateLimiter.create(permitsPerSecond);
    }
    
    public Flowable<Event> run(
        InMemoryRunner runner,
        Session session,
        Content message
    ) {
        if (!rateLimiter.tryAcquire(1, TimeUnit.SECONDS)) {
            throw new RateLimitException("Rate limit exceeded");
        }
        
        return runner.run(session, message);
    }
}
```

### Security Best Practices

1. **API Key Management:**
   - Never commit keys to version control
   - Use secret managers (Google Secret Manager, HashiCorp Vault)
   - Rotate keys regularly

2. **Input Validation:**
```java
public void validateUserInput(String input) {
    if (input == null || input.trim().isEmpty()) {
        throw new ValidationException("Input cannot be empty");
    }
    
    if (input.length() > MAX_INPUT_LENGTH) {
        throw new ValidationException("Input too long");
    }
    
    // Check for malicious patterns
    if (containsSuspiciousPatterns(input)) {
        logger.warn("Suspicious input detected: {}", sanitize(input));
        throw new SecurityException("Invalid input detected");
    }
}
```

3. **Authentication & Authorization:**
```java
public boolean authorize(String userId, String action) {
    // Implement permission checks
    return permissionService.hasPermission(userId, action);
}
```

## Health Checks

```java
@RestController
public class HealthController {
    private final InMemoryRunner runner;
    
    @GetMapping("/health")
    public HealthStatus health() {
        try {
            // Verify agent is responsive
            boolean agentHealthy = checkAgentHealth();
            
            return new HealthStatus(
                agentHealthy ? "UP" : "DOWN",
                System.currentTimeMillis()
            );
        } catch (Exception e) {
            return new HealthStatus("DOWN", System.currentTimeMillis());
        }
    }
}
```

## Performance Optimization

### Caching

```java
public class CachingToolWrapper {
    private final LoadingCache<String, Map<String, Object>> cache;
    
    public CachingToolWrapper() {
        this.cache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build(this::fetchData);
    }
    
    public Map<String, Object> getTool(String key) {
        return cache.get(key);
    }
}
```

### Connection Pooling

```java
// Database connection pool
HikariConfig config = new HikariConfig();
config.setJdbcUrl("jdbc:postgresql://localhost/agentdb");
config.setMaximumPoolSize(10);
config.setMinimumIdle(2);

HikariDataSource dataSource = new HikariDataSource(config);
```

### Async Processing

```java
public class AsyncAgentRunner {
    private final ExecutorService executorService;
    
    public AsyncAgentRunner(int threads) {
        this.executorService = Executors.newFixedThreadPool(threads);
    }
    
    public CompletableFuture<String> runAsync(
        InMemoryRunner runner,
        Session session,
        String message
    ) {
        return CompletableFuture.supplyAsync(() -> {
            // Run agent asynchronously
            StringBuilder response = new StringBuilder();
            runner.run(session, Content.builder().addText(message).build())
                .blockingSubscribe(event -> {
                    if (event.hasContent()) {
                        response.append(event.getContent().getText());
                    }
                });
            return response.toString();
        }, executorService);
    }
}
```

## Deployment Checklist

- [ ] Environment variables configured
- [ ] API keys securely stored
- [ ] Logging configured
- [ ] Monitoring enabled
- [ ] Health checks implemented
- [ ] Error handling robust
- [ ] Rate limiting in place
- [ ] Security review completed
- [ ] Load testing performed
- [ ] Rollback plan documented
- [ ] Documentation updated
- [ ] Team trained on operations
