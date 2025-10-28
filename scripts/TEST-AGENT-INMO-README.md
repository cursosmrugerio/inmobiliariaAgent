# Agent Controller Test Script

## Overview

`test-agent.sh` is a comprehensive test script that validates all functionality of the `AgentController` and `InmobiliariaTool` through conversational AI interactions.

## Prerequisites

1. **Application Running**: Ensure the Spring Boot application is running on `http://localhost:8080`
2. **jq installed**: The script uses `jq` for JSON parsing
   ```bash
   # macOS
   brew install jq

   # Ubuntu/Debian
   sudo apt-get install jq
   ```

## Running the Tests

```bash
./test-agent.sh
```

## What the Script Tests

### 20 Comprehensive Test Cases

#### **CRUD Operations**

1. **List All Agencies (Empty State)** - Verify listing works when no agencies exist
2. **Create First Agency** - Create agency with all fields
3. **Create Second Agency** - Create agency with minimal fields
4. **Create Third Agency** - Create another complete agency
5. **List All Agencies (With Data)** - Verify all created agencies appear
6. **Get Specific Agency by ID** - Retrieve individual agency details
7. **Get Another Agency by ID** - Test retrieval with different ID

#### **Partial Update Tests**

8. **Partial Update - Contact Person Only** - Update only the contact name, verify other fields preserved
9. **Partial Update - Email Only** - Update only email field
10. **Partial Update - Phone Only** - Update only phone field
11. **Full Update - Multiple Fields** - Update multiple fields at once
12. **Verify Update** - Confirm updates persisted correctly

#### **Delete with Conversation Flow**

13. **Delete Request** - Ask to delete an agency, expect confirmation
14. **Delete Confirmation** - Confirm deletion in follow-up message
15. **Verify Deletion** - Confirm agency no longer exists
16. **Delete Request (Cancel)** - Ask to delete an agency
17. **Cancel Deletion** - Decline the deletion, verify agency preserved

#### **Conversational Queries**

18. **Conversational Query** - Test natural language questions like "How many agencies?"

#### **Error Handling**

19. **Non-existent Agency Retrieval** - Attempt to get agency with ID 999
20. **Non-existent Agency Update** - Attempt to update agency with ID 999

## Test Output

The script provides:

- **Color-coded results**: Green ✓ for PASS, Red ✗ for FAIL
- **Request/Response logging**: See exactly what's sent and received
- **Session tracking**: Maintains conversation context across requests
- **Final summary**: Total, passed, and failed test counts

### Sample Output

```
========================================
TEST 8: Partial Update - Contact Person Only
========================================

Message: Update agency 1 to change the contact person to Carlos Rodríguez
Response:
{
  "response": "Agency 1 has been updated. Contact person is now Carlos Rodríguez.",
  "sessionId": "user-abc123...",
  "success": true,
  "error": null
}

✓ PASS - Partial update - contact person
✓ PASS - Verify contact person updated
```

## Key Features Tested

### 1. **Conversational AI**
- Natural language understanding
- Multi-turn conversations
- Context preservation across messages

### 2. **Session Management**
- Session creation on first message
- Session retrieval for follow-up messages
- Conversation history maintained

### 3. **Partial Updates**
- Update single field without affecting others
- No need to provide all data
- Verify unchanged fields preserved

### 4. **Confirmation Workflows**
- Delete operations require confirmation
- Follow-up messages maintain context
- Can cancel operations

### 5. **Error Handling**
- Graceful handling of non-existent resources
- Clear error messages
- No crashes on invalid input

## Exit Codes

- `0` - All tests passed
- `1` - One or more tests failed

## Troubleshooting

### Application Not Running

```
curl: (7) Failed to connect to localhost port 8080
```

**Solution**: Start the application
```bash
mvn spring-boot:run
```

### jq Not Found

```
bash: jq: command not found
```

**Solution**: Install jq
```bash
brew install jq  # macOS
```

### Google API Key Not Configured

```json
{
  "response": null,
  "sessionId": null,
  "success": false,
  "error": "Unexpected error: API key must either be provided..."
}
```

**Solution**: Set up Google Cloud credentials (see main README)

## Extending the Tests

To add new tests:

1. Add a new test section following the existing pattern:
```bash
print_header "TEST XX: Your Test Name"

response=$(send_message "Your test message" "$SESSION_ID")
check_success "$response" "Test description"
check_contains "$response" "expected text" "Verification description"
```

2. Update the test numbering
3. Add to the test summary section

## Integration with CI/CD

The script can be integrated into CI/CD pipelines:

```yaml
# Example GitHub Actions
- name: Run Agent Tests
  run: |
    mvn spring-boot:run &
    sleep 10  # Wait for app to start
    ./test-agent.sh
```

## Related Documentation

- [AgentController.java](src/main/java/com/inmobiliaria/gestion/agent/controller/AgentController.java)
- [InmobiliariaTool.java](src/main/java/com/inmobiliaria/gestion/agent/tools/InmobiliariaTool.java)
- [InmobiliariaAgent.java](src/main/java/com/inmobiliaria/gestion/agent/InmobiliariaAgent.java)
- [Main README](README.md)

## License

Same as the main project.
