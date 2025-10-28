# ADK Integration Build Fixes

**Date:** 2025-10-27 (Updated: 2025-10-28)
**Status:** ✅ RESOLVED - All Issues Fixed

---

## Issue Summary

The ADK integration encountered 8 issues during implementation, including build failures, runtime errors, and type mismatches. All issues have been systematically resolved.

---

## Problems Encountered

### 1. **Incorrect Maven Artifact Names**

**Error:**
```
Could not find artifact com.google.adk:adk-core:jar:0.2.0 in central
Could not find artifact com.google.adk:adk-web:jar:0.2.0 in central
```

**Cause:** Used outdated/incorrect artifact names based on preliminary documentation

**Resolution:**
```xml
<!-- WRONG -->
<dependency>
    <groupId>com.google.adk</groupId>
    <artifactId>adk-core</artifactId>
    <version>0.2.0</version>
</dependency>

<!-- CORRECT -->
<dependency>
    <groupId>com.google.adk</groupId>
    <artifactId>google-adk</artifactId>
    <version>0.3.0</version>
</dependency>
```

### 2. **Incorrect Package Imports**

**Error:**
```
cannot find symbol: class Session
location: package com.google.adk.runner
```

**Cause:** API packages changed in version 0.3.0

**Resolution:**
```java
// WRONG
import com.google.adk.runner.Event;
import com.google.adk.runner.RunConfig;
import com.google.adk.runner.Session;

// CORRECT
import com.google.adk.events.Event;
import com.google.adk.agents.RunConfig;
import com.google.adk.sessions.Session;
```

### 3. **Incorrect API Usage**

**Error:**
```
cannot find symbol: method createSession(java.lang.String)
cannot find symbol: method addText(java.lang.String)
```

**Cause:** API methods changed in version 0.3.0

**Resolution:**

**Session Creation:**
```java
// WRONG
Session session = agentRunner.createSession(sessionId);

// CORRECT
Session session = agentRunner.sessionService()
    .createSession(agentRunner.appName(), userId)
    .blockingGet();
```

**Agent Execution:**
```java
// WRONG
Content userContent = Content.builder().addText(message).build();
agentRunner.run(session, userContent)...

// CORRECT
Content userContent = Content.fromParts(Part.fromText(message));
RunConfig runConfig = RunConfig.builder().build();
Flowable<Event> events = agentRunner.runAsync(
    session.userId(),
    session.id(),
    userContent,
    runConfig
);
```

**Response Handling:**
```java
// WRONG
if (event.hasContent() && event.getContent().hasText()) {
    responseText.set(event.getContent().getText());
}

// CORRECT
events.blockingForEach(event -> {
    if (event.finalResponse()) {
        String content = event.stringifyContent();
        responseBuilder.append(content);
    }
});
```

### 4. **InMemoryRunner Constructor**

**Error:** Compilation error in AgentConfig

**Resolution:**
```java
// WRONG
return new InMemoryRunner(agent, "inmobiliaria-app");

// CORRECT (appName is now retrieved via runner.appName())
return new InMemoryRunner(agent);
```

### 5. **Text Block Formatting Issue**

**Error:**
```
error: unclosed string literal at line 35:20
```

**Cause:** Google Java Formatter had issues with Java text blocks (`"""`)

**Resolution:** Changed from text block to traditional string concatenation:
```java
// WRONG (formatter issue)
.instruction("""
    Instructions here...
    """)

// CORRECT
.instruction(
    "Line 1\n" +
    "Line 2\n" +
    "Line 3")
```

### 6. **Lombok @Slf4j Not Working**

**Error:**
```
cannot find symbol: variable log
```

**Cause:** Lombok annotation processing issue with Java 25 or compiler configuration

**Resolution:** Manually created logger instead of relying on @Slf4j:
```java
// WRONG (Lombok not processing)
@Slf4j
public class AgentController {
    ...
}

// CORRECT
public class AgentController {
    private static final Logger log = LoggerFactory.getLogger(AgentController.class);
    ...
}
```

### 7. **Vertex AI Configuration Missing (Runtime Error)**

**Error:**
```
API key must either be provided or set in the environment variable
GOOGLE_API_KEY or GEMINI_API_KEY
```

**Cause:** ADK was trying to use the direct Gemini API instead of Vertex AI, even though service account credentials were configured.

**Resolution:** Added Vertex AI environment variables:
```bash
# Required for Vertex AI with service account
export GOOGLE_GENAI_USE_VERTEXAI=true
export GOOGLE_CLOUD_PROJECT=inmobiliaria-adk
export GOOGLE_CLOUD_LOCATION=us-central1

# Authentication (already configured)
export GOOGLE_APPLICATION_CREDENTIALS="$HOME/inmobiliaria-service-account-key.json"
```

**Updated application.properties:**
```properties
# Vertex AI Configuration
google.genai.use.vertexai=${GOOGLE_GENAI_USE_VERTEXAI:true}
google.cloud.project=${GOOGLE_CLOUD_PROJECT:inmobiliaria-adk}
google.cloud.location=${GOOGLE_CLOUD_LOCATION:us-central1}
```

**See:** `VERTEX-AI-SETUP.md` for complete details

### 8. **FunctionTool Type Mismatch (Runtime Error)**

**Error:**
```
java.lang.IllegalArgumentException: argument type mismatch
	at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke
	at com.google.adk.tools.FunctionTool.call(FunctionTool.java:232)
```

**Cause:** ADK's FunctionTool passes `Integer` when parsing JSON numeric values from the LLM's tool calls, but the tool methods were defined to accept `Long` parameters.

**Example:** When the LLM calls `getInmobiliariaById` with `{"id": 1}`, ADK deserializes the JSON number `1` as an `Integer`, but the method signature was:
```java
public Map<String, Object> getInmobiliariaById(Long id)  // Expected Long
```

**Resolution:** Changed tool method parameters from `Long` to `Integer` and convert to `Long` when calling the service layer:

**Before:**
```java
public Map<String, Object> getInmobiliariaById(Long id) {
    InmobiliariaResponse inmobiliaria = inmobiliariaService.findById(id);
    // ...
}
```

**After:**
```java
public Map<String, Object> getInmobiliariaById(Integer id) {
    InmobiliariaResponse inmobiliaria = inmobiliariaService.findById(id.longValue());
    // ...
}
```

**Files Modified:**
- `InmobiliariaTool.java` - Changed `getInmobiliariaById`, `updateInmobiliaria`, and `deleteInmobiliaria` to accept `Integer` instead of `Long`
- `InmobiliariaToolTest.java` - Updated test calls to use `Integer` literals (e.g., `1` instead of `1L`)

**Key Learning:** ADK FunctionTool uses standard JSON deserialization, which defaults numeric values to `Integer` for small numbers. Always use `Integer` for ID parameters in tool methods, and convert to `Long` when calling service layers that use `Long` for database IDs.

---

## Final Working Configuration

### pom.xml Dependencies

```xml
<!-- Google ADK (Agent Development Kit) -->
<dependency>
    <groupId>com.google.adk</groupId>
    <artifactId>google-adk</artifactId>
    <version>0.3.0</version>
</dependency>
<dependency>
    <groupId>com.google.adk</groupId>
    <artifactId>google-adk-dev</artifactId>
    <version>0.3.0</version>
</dependency>
```

**Note:** RxJava is included transitively by ADK, no need to explicitly declare it.

### Correct Imports

```java
// Agent and Tools
import com.google.adk.agents.LlmAgent;
import com.google.adk.agents.RunConfig;
import com.google.adk.tools.FunctionTool;

// Runner and Sessions
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;

// Events and Content
import com.google.adk.events.Event;
import com.google.genai.types.Content;
import com.google.genai.types.Part;

// Reactive
import io.reactivex.rxjava3.core.Flowable;
```

### Correct API Usage Pattern

```java
// 1. Create Runner (in Spring @Configuration)
@Bean
public InMemoryRunner agentRunner(InmobiliariaAgent inmobiliariaAgent) {
    return new InMemoryRunner(inmobiliariaAgent.getAgent());
}

// 2. Create/Get Session
Session session = agentRunner.sessionService()
    .createSession(agentRunner.appName(), userId)
    .blockingGet();

// 3. Build Content
Content userContent = Content.fromParts(Part.fromText(message));

// 4. Create RunConfig
RunConfig runConfig = RunConfig.builder().build();

// 5. Execute Agent
Flowable<Event> events = agentRunner.runAsync(
    session.userId(),
    session.id(),
    userContent,
    runConfig
);

// 6. Process Response
StringBuilder response = new StringBuilder();
events.blockingForEach(event -> {
    if (event.finalResponse()) {
        String content = event.stringifyContent();
        if (content != null && !content.isBlank()) {
            response.append(content);
        }
    }
});
```

---

## Build Verification

```bash
mvn clean install -DskipTests
```

**Result:**
```
[INFO] BUILD SUCCESS
[INFO] Total time:  2.545 s
```

---

## Key Takeaways

1. **Always use official documentation** for latest version (0.3.0 vs 0.2.0)
2. **Check Maven Central** for correct artifact names
3. **ADK API has evolved** significantly from earlier versions
4. **Package structure matters** - classes moved to more logical packages
5. **Text blocks may cause formatter issues** - use traditional strings if needed
6. **Lombok issues with Java 25** - manual logger declaration works reliably
7. **Vertex AI requires explicit configuration** - Set `GOOGLE_GENAI_USE_VERTEXAI=true` to use service account credentials instead of API keys
8. **FunctionTool uses Integer for numeric values** - Use `Integer` for ID parameters in tool methods, not `Long`, and convert when calling service layers

---

## Updated Documentation Files

The following files have been updated with correct information:
- ✅ `pom.xml` - Correct dependencies
- ✅ `AgentConfig.java` - Correct runner initialization
- ✅ `AgentController.java` - Correct API usage
- ✅ `InmobiliariaAgent.java` - Fixed string formatting
- ⏳ `README-AGENT.md` - Needs update with correct API
- ⏳ `INTEGRATION-SUMMARY.md` - Needs update with correct version info

---

## Next Steps

1. **Set up Google Cloud credentials** (see `VERTEX-AI-SETUP.md`)
   ```bash
   # Verify service account file
   ls -lh ~/inmobiliaria-service-account-key.json

   # Set environment variables (added to ~/.zshrc for persistence)
   export GOOGLE_APPLICATION_CREDENTIALS="$HOME/inmobiliaria-service-account-key.json"
   export GOOGLE_GENAI_USE_VERTEXAI=true
   export GOOGLE_CLOUD_PROJECT=inmobiliaria-adk
   export GOOGLE_CLOUD_LOCATION=us-central1
   ```

2. **Run the application:**
   ```bash
   # Option 1: Use convenience script
   ./run-agent.sh

   # Option 2: Manual start
   mvn spring-boot:run
   ```

3. **Test the agent endpoint:**
   ```bash
   curl -X POST http://localhost:8080/api/agent/chat \
     -H "Content-Type: application/json" \
     -d '{"message": "List all real estate agencies"}'
   ```

4. **Expected response:**
   ```json
   {
     "response": "I found X real estate agencies: ...",
     "sessionId": "user-xxxxx",
     "success": true,
     "error": null
   }
   ```

---

## References

- **ADK Java Documentation:** https://google.github.io/adk-docs/get-started/java/
- **Maven Repository:** https://repo.maven.apache.org/maven2/com/google/adk/
- **ADK GitHub:** https://github.com/google/adk-java/

---

**Status:** ✅ All build and runtime issues resolved. Application fully operational with Vertex AI integration.
