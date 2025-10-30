# Testing Guide for Inmobiliaria ADK Agent

This guide provides complete instructions for testing the AI agent functionality of the Inmobiliaria system.

## Overview

The Inmobiliaria system includes an AI-powered agent built with Google's Agent Development Kit (ADK) that can:
- Create, read, update, and delete real estate agencies
- Handle partial updates (update only specific fields)
- Maintain conversation context across multiple requests
- Ask for confirmation before destructive operations
- Provide helpful error messages

## Quick Start

### 1. Start the Application

```bash
cd /Users/mike/Desarrollo/compyser/inmobiliaria/backend
mvn spring-boot:run
```

The application will start on port 8080.

### 2. Set Up Google Cloud Credentials (Required for Agent)

Follow the detailed guide in [GOOGLE-CREDENTIALS-SETUP.md](./GOOGLE-CREDENTIALS-SETUP.md).

Quick summary:
```bash
# Set environment variables
export GOOGLE_GENAI_USE_VERTEXAI=true
export GOOGLE_CLOUD_PROJECT=inmobiliaria-adk
export GOOGLE_CLOUD_LOCATION=us-central1
export GOOGLE_APPLICATION_CREDENTIALS=/Users/mike/Desarrollo/compyser/inmobiliaria/backend/credentials.json
```

### 3. Run the Test Suite

```bash
./scripts/test-agent_inmobiliarias.sh
```

## Test Suite Details

### What the Test Suite Does

The test script (`scripts/test-agent_inmobiliarias.sh`) performs **43 automated tests** covering:

1. **Database Reset** - Ensures clean test environment
2. **Create Operations** (3 tests) - Creating multiple agencies
3. **Read Operations** (5 tests) - Listing and retrieving agencies
4. **Partial Updates** (4 tests) - Updating individual fields
5. **Full Updates** (2 tests) - Updating multiple fields at once
6. **Delete Operations** (4 tests) - Deletion with confirmation flow
7. **Conversational Queries** (1 test) - Natural language queries
8. **Error Handling** (2 tests) - Non-existent resources

### Test Results

#### With Google Cloud Credentials Configured
- **Expected:** 43/43 tests passing (100%)
- **Duration:** ~2-3 minutes (depends on API latency)

#### Without Google Cloud Credentials
- **Expected:** 42/43 failures (only database reset succeeds)
- **Reason:** Agent requires Vertex AI API access

### Key Test Scenarios

#### Partial Update Example
```
TEST 8: User says "Update agency 1 to change the contact person to Carlos Rodríguez"
        Agent updates only the contact field, leaves all others unchanged
```

#### Delete Confirmation Flow
```
TEST 13: User says "Delete agency with ID 3"
         Agent responds "Are you sure you want to delete agency with ID 3?"

TEST 14: User confirms "Yes, I'm sure. Delete agency 3."
         Agent deletes the agency
```

#### Error Handling
```
TEST 19: User asks for "agency 999" (doesn't exist)
         Agent responds with helpful error message
```

## Manual Testing

You can also test the agent manually using curl:

### Example 1: List All Agencies
```bash
curl -X POST http://localhost:8080/api/agent/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "List all real estate agencies"
  }'
```

### Example 2: Create an Agency
```bash
curl -X POST http://localhost:8080/api/agent/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Create a new agency called Inmobiliaria Central with RFC ABC123456789, contact Juan Pérez, email juan@central.com, and phone +52-55-1234-5678"
  }'
```

### Example 3: Partial Update (with session)
```bash
# First message
response=$(curl -s -X POST http://localhost:8080/api/agent/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "Show me agency 1"}')

# Extract session ID
session_id=$(echo $response | jq -r '.sessionId')

# Update using the session
curl -X POST http://localhost:8080/api/agent/chat \
  -H "Content-Type: application/json" \
  -d "{
    \"message\": \"Update the email to newemail@example.com\",
    \"sessionId\": \"$session_id\"
  }"
```

## Database Management

### Reset Database (for clean tests)
```bash
curl -X POST http://localhost:8080/api/test/reset-database
```

This endpoint:
- Deletes all agencies
- Resets the H2 auto-increment sequence to 1
- Only available in non-production environments

### Direct API Access (without agent)

If you need to bypass the agent and access the REST API directly:

```bash
# List all agencies
curl http://localhost:8080/api/inmobiliarias

# Get specific agency
curl http://localhost:8080/api/inmobiliarias/1

# Create agency
curl -X POST http://localhost:8080/api/inmobiliarias \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Inmobiliaria Test",
    "rfc": "TEST123456789",
    "nombreContacto": "Test User",
    "correo": "test@example.com",
    "telefono": "+52-55-0000-0000"
  }'
```

## Troubleshooting

### Issue: Tests Fail with "Connection Refused"
**Solution:** Ensure the application is running:
```bash
lsof -ti:8080  # Should return a process ID
```

If not running, start it:
```bash
mvn spring-boot:run
```

### Issue: Tests Fail with "Invalid Credentials"
**Solution:** Verify Google Cloud credentials are set up correctly:
```bash
# Check environment variables
echo $GOOGLE_APPLICATION_CREDENTIALS
ls -la $GOOGLE_APPLICATION_CREDENTIALS

# Verify credentials file exists and is valid JSON
cat $GOOGLE_APPLICATION_CREDENTIALS | jq .
```

See [GOOGLE-CREDENTIALS-SETUP.md](./GOOGLE-CREDENTIALS-SETUP.md) for detailed setup.

### Issue: Database IDs Don't Start at 1
**Solution:** Reset the database before running tests:
```bash
curl -X POST http://localhost:8080/api/test/reset-database
```

The test script does this automatically, but you can do it manually if needed.

### Issue: Port 8080 Already in Use
**Solution:** Kill the existing process and restart:
```bash
lsof -ti:8080 | xargs kill
mvn spring-boot:run
```

### Issue: Tests Pass but Agent Responses are Incorrect
**Possible causes:**
1. Old session data - reset database and try again
2. API rate limits - wait a few minutes
3. Model behavior changes - check Google Cloud Console for service health

## File Structure

```
backend/
├── scripts/
│   ├── test-agent_inmobiliarias.sh    # Main test suite
│   └── clean-database.sh              # Manual database cleanup
├── src/
│   ├── main/java/com/inmobiliaria/gestion/
│   │   ├── agent/
│   │   │   ├── InmobiliariaAgent.java       # Agent configuration
│   │   │   ├── controller/
│   │   │   │   └── AgentController.java     # REST endpoint
│   │   │   └── tools/
│   │   │       └── InmobiliariaTool.java    # CRUD operations tool
│   │   └── inmobiliaria/
│   │       ├── controller/
│   │       │   └── TestDataController.java  # Test utilities
│   │       ├── service/
│   │       │   └── InmobiliariaService.java # Business logic
│   │       └── dto/
│   │           └── UpdateInmobiliariaRequest.java
├── FIXES-SUMMARY.md                   # Summary of all fixes
├── GOOGLE-CREDENTIALS-SETUP.md        # Credentials setup guide
├── AGENT-STATUS.md                    # Current status and summary
├── README-TESTING.md                  # This file
└── archive/                           # Archived documentation
    ├── TEST-AGENT-INMO-README.md      # Legacy test docs
    ├── TEST-FAILURES-EXPLAINED.md     # Historical failures
    └── [other archived summaries]
```

## Key Features Implemented

### 1. Partial Updates ✅
You can update only the fields you want to change without providing all fields.

**Example:**
```
User: "Update agency 1 to change the phone number to +52-55-9999-8888"
Agent: Updates only the phone field
```

### 2. Session Management ✅
The agent remembers context across multiple messages within a session.

**Example:**
```
Message 1: "Show me agency 1"
Message 2: "Update the email"  # Agent knows we're talking about agency 1
```

### 3. Database Reset for Testing ✅
Clean slate for every test run with predictable IDs starting at 1.

### 4. Comprehensive Error Handling ✅
- Validates input before making API calls
- Provides clear error messages
- Handles non-existent resources gracefully

### 5. Delete Confirmation ✅
Agent asks for confirmation before deleting and can handle confirmations properly.

## Next Steps

1. **Configure Credentials** - Follow [GOOGLE-CREDENTIALS-SETUP.md](./GOOGLE-CREDENTIALS-SETUP.md)
2. **Run Tests** - Execute `./scripts/test-agent_inmobiliarias.sh`
3. **Verify Results** - Should see 43/43 tests passing
4. **Explore** - Try manual testing with different queries

## Additional Documentation

- **AGENT-STATUS.md** - Current system status and summary
- **FIXES-SUMMARY.md** - Complete history of fixes and improvements
- **GOOGLE-CREDENTIALS-SETUP.md** - Detailed credentials setup guide
- **../archive/** - Archived/historical documentation

## Support

If you encounter issues:
1. Check the troubleshooting section above
2. Review the application logs
3. Verify Google Cloud project configuration
4. Check [ADK Java documentation](https://github.com/google-cloud/genai-adk-java)
