#!/bin/bash

# Test script for AgentController and InmobiliariaTool
# This script tests all CRUD operations through the conversational AI agent

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
BASE_URL="http://localhost:8080"
API_ENDPOINT="${BASE_URL}/api/agent/chat"
CONTENT_TYPE="Content-Type: application/json"

# Session ID for maintaining conversation context
SESSION_ID=""

# Test counter
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Function to print section headers
print_header() {
    echo -e "\n${BLUE}========================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}========================================${NC}\n"
}

# Function to print test results
print_test() {
    local test_name="$1"
    local status="$2"
    TOTAL_TESTS=$((TOTAL_TESTS + 1))

    if [ "$status" = "PASS" ]; then
        echo -e "${GREEN}✓ PASS${NC} - $test_name"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        echo -e "${RED}✗ FAIL${NC} - $test_name"
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
}

# Function to send a chat message
send_message() {
    local message="$1"
    local session="$2"

    echo -e "${YELLOW}Message:${NC} $message" >&2

    local payload
    if [ -z "$session" ]; then
        payload=$(jq -n --arg msg "$message" '{message: $msg}')
    else
        payload=$(jq -n --arg msg "$message" --arg sid "$session" '{message: $msg, sessionId: $sid}')
    fi

    local response=$(curl -s -w '\n%{http_code}' -X POST "$API_ENDPOINT" \
        -H "$CONTENT_TYPE" \
        -d "$payload")

    # Split response body and HTTP code
    local http_code=$(echo "$response" | tail -n1)
    local body=$(echo "$response" | sed '$d')

    echo -e "${YELLOW}HTTP Status:${NC} $http_code" >&2
    echo -e "${YELLOW}Response:${NC}" >&2

    # Check if response is valid JSON
    if echo "$body" | jq empty 2>/dev/null; then
        echo "$body" | jq -C '.' >&2

        # Extract session ID if present
        local new_session=$(echo "$body" | jq -r '.sessionId // empty' 2>/dev/null)
        if [ -n "$new_session" ]; then
            SESSION_ID="$new_session"
        fi
    else
        echo -e "${RED}Invalid JSON response:${NC}" >&2
        echo "$body" >&2
        # Return error JSON
        body='{"success":false,"error":"Invalid response from server","response":null,"sessionId":null}'
    fi
    echo "" >&2

    # Return ONLY the response body to stdout
    echo "$body"
}

# Function to check if response is successful
check_success() {
    local response="$1"
    local test_name="$2"

    # Check if response is valid JSON first
    if ! echo "$response" | jq empty 2>/dev/null; then
        print_test "$test_name" "FAIL"
        echo -e "${RED}Error:${NC} Invalid JSON response"
        return 1
    fi

    local success=$(echo "$response" | jq -r '.success' 2>/dev/null)

    if [ "$success" = "true" ]; then
        print_test "$test_name" "PASS"
        return 0
    else
        print_test "$test_name" "FAIL"
        local error=$(echo "$response" | jq -r '.error // "Unknown error"' 2>/dev/null)
        echo -e "${RED}Error:${NC} $error"
        return 1
    fi
}

# Function to check if response contains expected text
check_contains() {
    local response="$1"
    local expected="$2"
    local test_name="$3"

    # Check if response is valid JSON first
    if ! echo "$response" | jq empty 2>/dev/null; then
        print_test "$test_name" "FAIL"
        echo -e "${RED}Error:${NC} Invalid JSON response, cannot check content"
        return 1
    fi

    local response_text=$(echo "$response" | jq -r '.response // ""' 2>/dev/null)

    if echo "$response_text" | grep -qi "$expected"; then
        print_test "$test_name" "PASS"
        return 0
    else
        print_test "$test_name" "FAIL"
        echo -e "${RED}Expected to contain:${NC} $expected"
        echo -e "${RED}Got:${NC} $response_text"
        return 1
    fi
}

# Check prerequisites
check_prerequisites() {
    local all_good=true

    # Check if jq is installed
    if ! command -v jq &> /dev/null; then
        echo -e "${RED}ERROR: jq is not installed${NC}"
        echo "Install with: brew install jq (macOS) or sudo apt-get install jq (Linux)"
        all_good=false
    fi

    # Check if server is running
    if ! curl -s "$BASE_URL/actuator/health" &> /dev/null; then
        echo -e "${RED}ERROR: Application is not running at $BASE_URL${NC}"
        echo "Start with: mvn spring-boot:run"
        all_good=false
    fi

    if [ "$all_good" = false ]; then
        exit 1
    fi

    echo -e "${GREEN}✓ Prerequisites check passed${NC}\n"
}

# Start tests
print_header "AGENT CONTROLLER & INMOBILIARIA TOOL TESTS"

echo "Testing endpoint: $API_ENDPOINT"
echo ""

check_prerequisites

# ====================================
# RESET DATABASE
# ====================================
echo -e "${BLUE}Resetting database...${NC}"
reset_response=$(curl -s -X POST "$BASE_URL/api/test/reset-database" 2>/dev/null)
if echo "$reset_response" | grep -q "successfully"; then
    echo -e "${GREEN}✓ Database reset successfully${NC}\n"
else
    echo -e "${YELLOW}⚠ Could not reset database (endpoint may not be available)${NC}"
    echo -e "${YELLOW}  Tests will run but IDs may not start at 1${NC}\n"
fi

# ====================================
# TEST 1: LIST ALL AGENCIES (Empty)
# ====================================
print_header "TEST 1: List All Agencies (Empty State)"

response=$(send_message "List all real estate agencies" "")
check_success "$response" "List all agencies command"

# ====================================
# TEST 2: CREATE FIRST AGENCY
# ====================================
print_header "TEST 2: Create First Agency"

response=$(send_message "Create a new agency called 'Inmobiliaria Central' with RFC ABC123456789, contact person Juan Pérez, email juan@central.com, and phone +52-55-1234-5678" "$SESSION_ID")
check_success "$response" "Create first agency"
check_contains "$response" "Inmobiliaria Central" "Verify agency name in response"

# ====================================
# TEST 3: CREATE SECOND AGENCY
# ====================================
print_header "TEST 3: Create Second Agency"

response=$(send_message "Create another agency named 'Propiedades del Sur' with RFC DEF987654321, contact Pedro Sánchez, email pedro@sur.com, and phone +52-33-5555-9999" "$SESSION_ID")
check_success "$response" "Create second agency"
check_contains "$response" "Propiedades del Sur" "Verify second agency name"

# ====================================
# TEST 4: CREATE THIRD AGENCY
# ====================================
print_header "TEST 4: Create Third Agency"

response=$(send_message "Register a new agency: Inmobiliaria Norte, RFC GHI111222333, contact María López, email maria@norte.com, phone +52-33-9876-5432" "$SESSION_ID")
check_success "$response" "Create third agency"
check_contains "$response" "Inmobiliaria Norte" "Verify third agency name"

# ====================================
# TEST 5: LIST ALL AGENCIES (With Data)
# ====================================
print_header "TEST 5: List All Agencies (With Data)"

response=$(send_message "Show me all the agencies" "$SESSION_ID")
check_success "$response" "List all agencies with data"
check_contains "$response" "Inmobiliaria Central" "Verify first agency in list"
check_contains "$response" "Propiedades del Sur" "Verify second agency in list"
check_contains "$response" "Inmobiliaria Norte" "Verify third agency in list"

# ====================================
# TEST 6: GET SPECIFIC AGENCY BY ID
# ====================================
print_header "TEST 6: Get Specific Agency by ID"

response=$(send_message "Show me the details of agency with ID 1" "$SESSION_ID")
check_success "$response" "Get agency by ID"
check_contains "$response" "Inmobiliaria Central" "Verify correct agency retrieved"

# ====================================
# TEST 7: GET ANOTHER AGENCY BY ID
# ====================================
print_header "TEST 7: Get Another Agency by ID"

response=$(send_message "What are the details of agency 2?" "$SESSION_ID")
check_success "$response" "Get second agency by ID"
check_contains "$response" "Propiedades del Sur" "Verify correct second agency"

# ====================================
# TEST 8: PARTIAL UPDATE - Contact Person Only
# ====================================
print_header "TEST 8: Partial Update - Contact Person Only"

response=$(send_message "Update agency 1 to change the contact person to Carlos Rodríguez" "$SESSION_ID")
check_success "$response" "Partial update - contact person"
check_contains "$response" "Carlos Rodríguez" "Verify contact person updated"

# ====================================
# TEST 9: PARTIAL UPDATE - Email Only
# ====================================
print_header "TEST 9: Partial Update - Email Only"

response=$(send_message "Change the email of agency 2 to nuevoemail@sur.com" "$SESSION_ID")
check_success "$response" "Partial update - email"
check_contains "$response" "nuevoemail@sur.com" "Verify email updated"

# ====================================
# TEST 10: PARTIAL UPDATE - Phone Only
# ====================================
print_header "TEST 10: Partial Update - Phone Only"

response=$(send_message "Update the phone number of agency 3 to +52-81-5555-1234" "$SESSION_ID")
check_success "$response" "Partial update - phone"
check_contains "$response" "5555-1234" "Verify phone updated"

# ====================================
# TEST 11: FULL UPDATE - Multiple Fields
# ====================================
print_header "TEST 11: Full Update - Multiple Fields"

response=$(send_message "Update agency 1: change name to 'Inmobiliaria Central Premium', contact to Ana García, and email to ana@centralpremium.com" "$SESSION_ID")
check_success "$response" "Full update - multiple fields"
check_contains "$response" "Premium" "Verify name updated"
check_contains "$response" "Ana García" "Verify contact updated"

# ====================================
# TEST 12: VERIFY UPDATE (Get Updated Agency)
# ====================================
print_header "TEST 12: Verify Update - Get Updated Agency"

response=$(send_message "Show me agency 1 details" "$SESSION_ID")
check_success "$response" "Get updated agency details"
check_contains "$response" "Premium" "Verify name persisted"
check_contains "$response" "Ana García" "Verify contact persisted"

# ====================================
# TEST 13: DELETE WITH CONFIRMATION - Part 1
# ====================================
print_header "TEST 13: Delete Agency - Request Deletion"

# Start a new session for delete confirmation test
SESSION_ID=""
response=$(send_message "Delete agency with ID 3" "")
check_success "$response" "Delete agency request"
check_contains "$response" "sure" "Verify confirmation request"

# Save the session ID for the confirmation in TEST 14
DELETE_SESSION_ID="$SESSION_ID"

# ====================================
# TEST 14: DELETE WITH CONFIRMATION - Part 2
# ====================================
print_header "TEST 14: Delete Agency - Confirm Deletion"

# Include the agency ID in the confirmation to ensure context
# This works around the ADK session behavior where context may be lost
response=$(send_message "Yes, I'm sure. Delete agency 3." "$DELETE_SESSION_ID")
check_success "$response" "Confirm deletion"
check_contains "$response" "deleted" "Verify deletion confirmed"

# ====================================
# TEST 15: VERIFY DELETION
# ====================================
print_header "TEST 15: Verify Deletion - List Remaining Agencies"

response=$(send_message "List all agencies" "$SESSION_ID")
check_success "$response" "List agencies after deletion"

# Check that agency 3 is NOT in the list
if echo "$response" | jq -r '.response' | grep -qi "Inmobiliaria Norte"; then
    print_test "Verify agency 3 was deleted" "FAIL"
    echo -e "${RED}Agency 3 still exists!${NC}"
else
    print_test "Verify agency 3 was deleted" "PASS"
fi

# ====================================
# TEST 16: DELETE CANCELLED - Part 1
# ====================================
print_header "TEST 16: Delete Agency - Cancel Request"

SESSION_ID=""
response=$(send_message "Delete agency 2" "")
check_success "$response" "Delete request for agency 2"
check_contains "$response" "sure" "Verify confirmation asked"

# ====================================
# TEST 17: DELETE CANCELLED - Part 2
# ====================================
print_header "TEST 17: Delete Agency - Cancel Deletion"

response=$(send_message "No, cancel that" "$SESSION_ID")
check_success "$response" "Cancel deletion"

# Verify agency 2 still exists
response=$(send_message "Show me agency 2" "$SESSION_ID")
check_success "$response" "Verify agency 2 still exists"
check_contains "$response" "Propiedades del Sur" "Verify agency 2 not deleted"

# ====================================
# TEST 18: CONVERSATIONAL QUERY
# ====================================
print_header "TEST 18: Conversational Query"

response=$(send_message "How many agencies do we have?" "$SESSION_ID")
check_success "$response" "Conversational query"

# ====================================
# TEST 19: ERROR HANDLING - Non-existent Agency
# ====================================
print_header "TEST 19: Error Handling - Get Non-existent Agency"

response=$(send_message "Show me agency 999" "$SESSION_ID")
check_success "$response" "Get non-existent agency"
# Should contain error message about not found
check_contains "$response" "not found\|no encontrada\|not exist" "Verify error message"

# ====================================
# TEST 20: ERROR HANDLING - Update Non-existent Agency
# ====================================
print_header "TEST 20: Error Handling - Update Non-existent Agency"

response=$(send_message "Update agency 999 to change name to Test" "$SESSION_ID")
check_success "$response" "Update non-existent agency"
check_contains "$response" "not found\|no encontrada\|error" "Verify error message for update"

# ====================================
# FINAL REPORT
# ====================================
print_header "TEST SUMMARY"

echo -e "Total Tests:  ${BLUE}$TOTAL_TESTS${NC}"
echo -e "Passed:       ${GREEN}$PASSED_TESTS${NC}"
echo -e "Failed:       ${RED}$FAILED_TESTS${NC}"
echo ""

if [ $FAILED_TESTS -eq 0 ]; then
    echo -e "${GREEN}✓ ALL TESTS PASSED!${NC}"
    exit 0
else
    echo -e "${RED}✗ SOME TESTS FAILED${NC}"
    exit 1
fi
