# Inmobiliaria AI Agent - Documentation

**Last Updated:** 2025-10-31

## Overview

This project integrates **Google's Agent Development Kit (ADK)** to provide a conversational AI agent for managing real estate agencies (inmobiliarias) through natural language interactions.

Instead of manually constructing REST API requests, users can simply chat with the AI agent using natural language to perform CRUD operations.

## Features

- **Natural Language Interface**: Chat with the AI agent in Spanish or English
- **Full CRUD Operations**: Create, Read, Update, and Delete inmobiliarias through conversation
- **Intelligent Tool Calling**: The agent automatically determines which operations to perform based on user intent
- **Session Management**: Maintains conversation context across multiple interactions
- **RESTful Integration**: Built on top of existing Spring Boot REST services

## Architecture

```
┌─────────────────┐
│   User Input    │ "List all agencies"
│  (Natural Lang) │
└────────┬────────┘
         │
         ▼
┌─────────────────────────┐
│   AgentController       │ POST /api/agent/chat
│   (REST Endpoint)       │
└────────┬────────────────┘
         │
         ▼
┌─────────────────────────┐
│   InMemoryRunner        │ Execute agent with user input
│   (ADK Runner)          │
└────────┬────────────────┘
         │
         ▼
┌─────────────────────────┐
│   InmobiliariaAgent     │ LlmAgent with instructions
│   (LlmAgent)            │
└────────┬────────────────┘
         │
         ▼
┌─────────────────────────┐
│   InmobiliariaTool      │ FunctionTools wrapping:
│   (FunctionTools)       │ - listAllInmobiliarias()
│                         │ - getInmobiliariaById()
│                         │ - createInmobiliaria()
│                         │ - updateInmobiliaria()
│                         │ - deleteInmobiliaria()
└────────┬────────────────┘
         │
         ▼
┌─────────────────────────┐
│   InmobiliariaService   │ Business logic & JPA
│   (Spring Service)      │
└─────────────────────────┘
```

## Setup

### Prerequisites

1. **Java 25** (configured in project)
2. **Maven** (for dependency management)
3. **Google Cloud Account** (for Gemini API access)

### Google Cloud Credentials

The agent uses Google's Gemini 2.0 Flash model via **Vertex AI**, which requires service account authentication.

#### Required Environment Variables

To use Vertex AI with service account credentials, you need:

```bash
# 1. Authentication
export GOOGLE_APPLICATION_CREDENTIALS="$HOME/inmobiliaria-service-account-key.json"

# 2. Vertex AI Configuration (CRITICAL - required for service account auth)
export GOOGLE_GENAI_USE_VERTEXAI=true
export GOOGLE_CLOUD_PROJECT=inmobiliaria-adk
export GOOGLE_CLOUD_LOCATION=us-central1
```

**Why all four variables?**
- Without `GOOGLE_GENAI_USE_VERTEXAI=true`, ADK tries to use the direct Gemini API (which needs an API key)
- With this flag, ADK uses Vertex AI (which uses your service account credentials)

#### Option 1: Service Account Key (Development & Production)

1. **Get your service account key file** (already provided: `~/inmobiliaria-service-account-key.json`)
2. **Set all required environment variables:**

```bash
# These are already added to your ~/.zshrc for persistence
export GOOGLE_APPLICATION_CREDENTIALS="$HOME/inmobiliaria-service-account-key.json"
export GOOGLE_GENAI_USE_VERTEXAI=true
export GOOGLE_CLOUD_PROJECT=inmobiliaria-adk
export GOOGLE_CLOUD_LOCATION=us-central1
```

3. **Verify setup:**

```bash
# Check that the file exists
ls -lh ~/inmobiliaria-service-account-key.json

# Verify all environment variables
echo "GOOGLE_APPLICATION_CREDENTIALS: $GOOGLE_APPLICATION_CREDENTIALS"
echo "GOOGLE_GENAI_USE_VERTEXAI: $GOOGLE_GENAI_USE_VERTEXAI"
echo "GOOGLE_CLOUD_PROJECT: $GOOGLE_CLOUD_PROJECT"
echo "GOOGLE_CLOUD_LOCATION: $GOOGLE_CLOUD_LOCATION"
```

#### Option 2: Application Default Credentials (Alternative)

If you prefer not to use a service account file:

```bash
# Authenticate with your Google account
gcloud auth application-default login

# Still need to set Vertex AI configuration
export GOOGLE_GENAI_USE_VERTEXAI=true
export GOOGLE_CLOUD_PROJECT=inmobiliaria-adk
export GOOGLE_CLOUD_LOCATION=us-central1
```

### Build and Run

```bash
# Build the project
mvn clean install

# Run the application
# Option 1: Use the convenience script (sets all environment variables)
./run-agent.sh

# Option 2: Manual start (ensure environment variables are set first)
export GOOGLE_APPLICATION_CREDENTIALS="$HOME/inmobiliaria-service-account-key.json"
export GOOGLE_GENAI_USE_VERTEXAI=true
export GOOGLE_CLOUD_PROJECT=inmobiliaria-adk
export GOOGLE_CLOUD_LOCATION=us-central1
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

**⚠️ Important:** If you see an error about API keys when testing, it means the Vertex AI environment variables weren't set before starting Maven. Stop the application and restart with the variables set.

## Usage

### API Endpoint

**POST** `/api/agent/chat`

#### Request Body

```json
{
  "message": "Your natural language query here",
  "sessionId": "optional-session-id"
}
```

- `message` (required): Your natural language query
- `sessionId` (optional): Session ID for maintaining conversation context. If not provided, a new session is created.

#### Response Body

```json
{
  "response": "Agent's response text",
  "sessionId": "session-123",
  "success": true,
  "error": null
}
```

### Example Interactions

#### 1. List All Agencies

**Request:**
```bash
curl -X POST http://localhost:8080/api/agent/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "List all real estate agencies"
  }'
```

**Response:**
```json
{
  "response": "I found 3 real estate agencies:\n1. Inmobiliaria Central (ID: 1) - RFC: ABC123456789\n   Contact: Laura Gómez (contacto@central.mx)\n2. Propiedades del Sur (ID: 2) - RFC: DEF987654321\n   Contact: Carlos Ruiz (info@sur.mx)\n3. Grupo Habitat (ID: 3) - RFC: GHI456789123\n   Contact: Ana Torres (contacto@habitat.mx)",
  "sessionId": "user-a1b2c3d4",
  "success": true,
  "error": null
}
```

#### 2. Get Specific Agency

**Request:**
```bash
curl -X POST http://localhost:8080/api/agent/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Show me the details of agency with ID 1"
  }'
```

**Response:**
```json
{
  "response": "Here are the details for Inmobiliaria Central:\n- ID: 1\n- Name: Inmobiliaria Central\n- RFC: ABC123456789\n- Contact Person: Laura Gómez\n- Email: contacto@central.mx\n- Phone: +52-55-1234-5678",
  "sessionId": "user-a1b2c3d4",
  "success": true,
  "error": null
}
```

#### 3. Create New Agency

**Request:**
```bash
curl -X POST http://localhost:8080/api/agent/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Create a new agency called \"Inmobiliaria del Norte\" with RFC XAXX010101000, contact person Juan Pérez, email juan@norte.mx and phone +52-55-9999-8888"
  }'
```

**Response:**
```json
{
  "response": "✅ I have successfully created the new real estate agency:\n- Name: Inmobiliaria del Norte\n- RFC: XAXX010101000\n- Contact: Juan Pérez\n- Email: juan@norte.mx\n- Phone: +52-55-9999-8888\n- Assigned ID: 4",
  "sessionId": "user-a1b2c3d4",
  "success": true,
  "error": null
}
```

#### 4. Update Agency

**Request:**
```bash
curl -X POST http://localhost:8080/api/agent/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Update agency 2 to change the contact person to María García"
  }'
```

**Response:**
```json
{
  "response": "✅ Agency updated successfully. The contact person for Propiedades del Sur (ID: 2) has been changed to María García.",
  "sessionId": "user-a1b2c3d4",
  "success": true,
  "error": null
}
```

#### 5. Delete Agency

**Request:**
```bash
curl -X POST http://localhost:8080/api/agent/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Delete the agency with ID 4"
  }'
```

**Response:**
```json
{
  "response": "⚠️ Are you sure you want to delete agency with ID 4? This action cannot be undone. Please confirm if you'd like me to proceed.",
  "sessionId": "user-a1b2c3d4",
  "success": true,
  "error": null
}
```

**Follow-up (in same session):**
```bash
curl -X POST http://localhost:8080/api/agent/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Yes, delete it",
    "sessionId": "user-a1b2c3d4"
  }'
```

**Response:**
```json
{
  "response": "✅ Agency with ID 4 has been deleted successfully.",
  "sessionId": "user-a1b2c3d4",
  "success": true,
  "error": null
}
```

## Supported Natural Language Queries

The agent understands various phrasings:

### List Operations
- "List all agencies"
- "Show me all inmobiliarias"
- "What agencies do we have?"
- "Get all real estate agencies"

### Get Specific
- "Show me agency 1"
- "Get details for inmobiliaria with ID 5"
- "What's the information for agency number 2?"

### Create
- "Create a new agency called [name]"
- "Register an inmobiliaria with name [name] and RFC [rfc]"
- "Add a new real estate agency"

### Update
- "Update agency 1 to change the contact to [name]"
- "Modify inmobiliaria 3's email to [email]"
- "Change the phone number for agency 2"

### Delete
- "Delete agency 4"
- "Remove inmobiliaria with ID 3"
- "Delete the agency called [name]"

## Session Management

Sessions allow the agent to maintain conversation context:

```bash
# First interaction - no session ID
curl -X POST http://localhost:8080/api/agent/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "List all agencies"}'
# Returns sessionId: "user-abc123"

# Follow-up interaction - use same session ID
curl -X POST http://localhost:8080/api/agent/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Show me more details about the first one",
    "sessionId": "user-abc123"
  }'
```

## Testing with Swagger UI

1. Start the application
2. Navigate to `http://localhost:8080/swagger-ui.html`
3. Find the **AI Agent** section
4. Click on **POST /api/agent/chat**
5. Click **Try it out**
6. Enter your natural language query in the request body
7. Click **Execute**

## Development

### Adding New Tools

To extend the agent with new capabilities:

1. **Add method to InmobiliariaTool**:
```java
@Schema(description = "Your tool description")
public Map<String, Object> yourNewTool(
    @Schema(description = "Parameter description") String param) {
    // Implementation
}
```

2. **Register tool in InmobiliariaAgent**:
```java
.tools(
    // ... existing tools
    FunctionTool.create(inmobiliariaTool, "yourNewTool")
)
```

3. **Update agent instructions** to mention the new tool

### Extending to Other Entities

When you add new entities (Propiedad, Contrato, Pago), you can follow the same pattern:

1. Create `PropertyTool` (or ContractTool, PaymentTool)
2. Create corresponding agent class
3. Create dedicated controller or extend AgentController
4. Update configuration

## Troubleshooting

### "API key must either be provided or set in the environment variable"

**Cause**: Vertex AI configuration not set. ADK is trying to use direct Gemini API instead of Vertex AI.

**Solution**:
```bash
# Set ALL required environment variables
export GOOGLE_APPLICATION_CREDENTIALS="$HOME/inmobiliaria-service-account-key.json"
export GOOGLE_GENAI_USE_VERTEXAI=true
export GOOGLE_CLOUD_PROJECT=inmobiliaria-adk
export GOOGLE_CLOUD_LOCATION=us-central1

# Then restart the application
mvn spring-boot:run
```

**Or use the convenience script:**
```bash
./run-agent.sh
```

### "Agent execution failed"

**Cause**: Missing, invalid, or incorrectly configured Google Cloud credentials

**Solution**:
```bash
# Verify service account file exists
ls -lh ~/inmobiliaria-service-account-key.json

# Verify all environment variables are set
echo $GOOGLE_APPLICATION_CREDENTIALS
echo $GOOGLE_GENAI_USE_VERTEXAI
echo $GOOGLE_CLOUD_PROJECT
echo $GOOGLE_CLOUD_LOCATION

# Reload environment if needed
source ~/.zshrc
```

### "No response from agent"

**Cause**: Network issues or API quota exceeded

**Solution**:
- Check Google Cloud Console for API quota
- Verify network connectivity
- Check application logs for detailed errors

### Tool not being called

**Cause**: LLM doesn't understand when to use the tool

**Solution**:
- Make tool description more explicit
- Mention tool name in agent instructions
- Provide usage examples in the instruction

### Compilation errors after adding dependencies

**Solution**:
```bash
mvn clean install -U
```

## Performance Considerations

- **First Request**: May take 2-3 seconds due to cold start
- **Subsequent Requests**: ~500ms-1s depending on complexity
- **Model**: Gemini 2.0 Flash is optimized for low latency
- **Virtual Threads**: Enabled for better concurrency

## Security Notes

- The agent endpoint is currently **not secured** (matches existing security config)
- For production, implement:
  - Authentication (JWT, OAuth2)
  - Rate limiting
  - Input validation and sanitization
  - Session timeout management
- Never expose Google Cloud credentials in code or version control

## Future Enhancements

- [ ] Multi-agent system (PropertyAgent, ContractAgent, PaymentAgent)
- [ ] Voice input/output integration
- [ ] RAG (Retrieval Augmented Generation) for policy queries
- [ ] Workflow automation (contract lifecycle, payment reminders)
- [ ] Multi-language support (explicit language switching)
- [ ] Agent memory persistence across sessions
- [ ] Integration with frontend chat UI

## Resources

- [ADK Java Documentation](https://google.github.io/adk-docs/)
- [ADK Samples](https://github.com/google/adk-samples)
- [Gemini API Documentation](https://ai.google.dev/docs)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)

## Support

For issues or questions:
1. Check application logs: `tail -f logs/application.log`
2. Review Google Cloud logs
3. Consult ADK documentation
4. Check project CLAUDE.md for architecture guidelines

---

## Next Steps

**Want to build your own agent?**
- 🚀 **Fast:** [reference/GENERATOR-QUICK-START.md](reference/GENERATOR-QUICK-START.md) - 2-minute agent generation
- 📖 **Manual:** [AGENT-DEVELOPMENT-GUIDE.md](AGENT-DEVELOPMENT-GUIDE.md) - Step-by-step guide

**Need to test the system?**
- 🧪 [reference/README-TESTING.md](reference/README-TESTING.md) - Complete testing workflow
- 📊 [reference/AGENT-STATUS.md](reference/AGENT-STATUS.md) - Current system status

**Deploying to production?**
- 🚀 [production/CRITICAL-PRODUCTION-CHECKLIST.md](production/CRITICAL-PRODUCTION-CHECKLIST.md) - Pre-deployment checklist
- 📦 [production/DEPLOYMENT-PRODUCTION.md](production/DEPLOYMENT-PRODUCTION.md) - Deployment guide

**All Documentation:**
- 📚 [README.md](README.md) - Complete documentation index
