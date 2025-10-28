# Test Failures Explanation

## Summary

**Total Tests:** 43
**Passed:** 34 (79%)
**Failed:** 9 (21%)

## Root Causes

### 1. Database Not Empty (5 failures)

**Problem:** The test assumes a clean database starting with ID 1, but the database already has agencies with IDs 2-10.

**Failed Tests:**
- TEST 6: Get Specific Agency by ID (ID 1 doesn't exist)
- TEST 8: Partial Update - Contact Person Only (ID 1 doesn't exist)
- TEST 11: Full Update - Multiple Fields (ID 1 doesn't exist)
- TEST 12: Verify Update - Get Updated Agency (ID 1 doesn't exist, so updates in TEST 11 failed)

**Evidence:**
```
Message: Show me the details of agency with ID 1
Response: "There was an error retrieving the agency with ID 1.
          It seems that the agency was not found."
```

**Solution:** Run `./scripts/clean-database.sh` before running tests, or use H2 console to delete all records.

---

### 2. Incomplete Agency Creation (4 failures)

**Problem:** TEST 3 tries to create "Propiedades del Sur" but only provides name and RFC. The agent asks for additional required fields (contact, email, phone), but the test doesn't provide them in a follow-up message.

**Failed Tests:**
- TEST 5: Verify second agency in list (Propiedades del Sur was never created)
- TEST 7: Verify correct second agency (expects Propiedades del Sur but finds Inmobiliaria Central)
- TEST 17: Verify agency 2 not deleted (expects Propiedades del Sur but it's Inmobiliaria Central)

**Evidence:**
```
Message: Create another agency named 'Propiedades del Sur' with RFC DEF987654321
Response: "Could you please provide the contact person's name, email, and
          phone number for 'Propiedades del Sur'?"
✓ PASS - Create second agency  [FALSE POSITIVE - agent is asking for more info!]
```

**Why It Passes:** The test only checks if `success: true` (which it is) and if response contains "Propiedades del Sur" (which it does, in the question). It doesn't verify the agency was actually created.

**Solution:** Update TEST 3 to either:
1. Provide all required fields in one message
2. Add a follow-up message with the missing details
3. Make the required fields truly optional in the API

---

### 3. Session Continuity Lost in Delete Confirmation

**Problem:** TEST 13 and TEST 14 use different sessions, so the agent doesn't remember the deletion request.

**Failed Tests:**
- TEST 14: Delete Agency - Confirm Deletion
- TEST 15: Verify agency 3 was deleted

**Evidence:**
```
TEST 13:
  Message: Delete agency with ID 3
  SessionId: user-7fceebda-0d38-4008-8e97-87dd01c1049d
  Response: "Are you sure you want to delete agency with ID 3?"

TEST 14:
  Message: Yes, I'm sure. Delete it.
  SessionId: user-75b7c369-7136-41a4-9e00-993442fd309b  [DIFFERENT!]
  Response: "Could you please provide the ID of the agency you want to delete?"
```

**Why:** Line 241 of the test script resets the session: `SESSION_ID=""`

**Solution:** Comment out line 241, or save and restore the session ID.

---

## How to Get All Tests Passing

### Option 1: Clean Database Before Testing
```bash
# Delete all existing agencies first
curl -X DELETE http://localhost:8080/inmobiliarias/2
curl -X DELETE http://localhost:8080/inmobiliarias/3
# ... etc for all IDs

# Then run tests
./scripts/test-agent_inmobiliarias.sh
```

### Option 2: Fix the Test Script

Update `scripts/test-agent_inmobiliarias.sh`:

**Fix 1 - Don't Reset Session in TEST 13** (line 241)
```bash
# BEFORE:
SESSION_ID=""

# AFTER:
# SESSION_ID=""  # Keep session for confirmation flow
```

**Fix 2 - Provide All Fields for TEST 3** (line 146)
```bash
# BEFORE:
response=$(send_message "Create another agency named 'Propiedades del Sur' with RFC DEF987654321" "$SESSION_ID")

# AFTER:
response=$(send_message "Create another agency named 'Propiedades del Sur' with RFC DEF987654321, contact Pedro Sánchez, email pedro@sur.com, phone +52-33-5555-9999" "$SESSION_ID")
```

**Fix 3 - Use Dynamic IDs**

Instead of hardcoding ID 1, 2, 3, extract the IDs from creation responses:
```bash
# After creating first agency
AGENCY_1_ID=$(echo "$response" | jq -r '.response' | grep -oP 'ID \K\d+')

# Then use $AGENCY_1_ID instead of hardcoded 1
```

### Option 3: Make Tests Database-Independent

Modify tests to:
1. Query for existing IDs before testing
2. Use those IDs instead of assuming 1, 2, 3
3. Clean up created test data at the end

## Current Test Status Breakdown

### ✓ Working Great (34 tests)
- Basic CRUD operations
- Partial updates (when ID exists)
- Conversational queries
- Error handling for non-existent resources
- Session management (when not reset)

### ✗ Needs Fixes (9 tests)
- Tests assuming specific IDs (5)
- Tests assuming specific data exists (4)

## Recommendations

1. **For CI/CD:** Use a fresh H2 database for each test run (restart app between tests)
2. **For development:** Create a `reset-database.sh` script that deletes all data
3. **For production:** Never run these tests against production data!

## Quick Fix

The easiest immediate fix:

```bash
# 1. Stop the application
lsof -ti:8080 | xargs kill

# 2. Restart it (H2 in-memory database will be empty)
mvn spring-boot:run

# 3. Run tests immediately
./scripts/test-agent_inmobiliarias.sh
```

This gives you a clean slate since H2 is in-memory and loses all data on restart.
