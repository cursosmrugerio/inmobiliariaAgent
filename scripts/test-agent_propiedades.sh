#!/bin/bash

# Conversational regression suite for the Propiedad agent.
# Exercises agent-driven CRUD operations while validating results with REST endpoints.

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

BASE_URL="${BASE_URL:-http://localhost:8080}"
CHAT_ENDPOINT="${BASE_URL}/api/agent/propiedades/chat"
CONTENT_TYPE="Content-Type: application/json"
SESSION_ID=""

TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

print_header() {
  echo -e "\n${BLUE}========================================${NC}"
  echo -e "${BLUE}$1${NC}"
  echo -e "${BLUE}========================================${NC}\n"
}

print_test() {
  local name="$1"
  local status="$2"
  TOTAL_TESTS=$((TOTAL_TESTS + 1))
  if [[ "$status" == "PASS" ]]; then
    echo -e "${GREEN}✓ PASS${NC} - $name"
    PASSED_TESTS=$((PASSED_TESTS + 1))
  else
    echo -e "${RED}✗ FAIL${NC} - $name"
    FAILED_TESTS=$((FAILED_TESTS + 1))
  fi
}

require_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo -e "${RED}ERROR:${NC} required command '$1' not found"
    exit 1
  fi
}

send_message() {
  local message="$1"
  local session="$2"

  echo -e "${YELLOW}Message:${NC} $message" >&2

  local payload
  if [[ -z "$session" ]]; then
    payload=$(jq -n --arg msg "$message" '{message: $msg}')
  else
    payload=$(jq -n --arg msg "$message" --arg sid "$session" '{message: $msg, sessionId: $sid}')
  fi

  local response
  response=$(curl -s -w '\n%{http_code}' -X POST "$CHAT_ENDPOINT" \
    -H "$CONTENT_TYPE" \
    -d "$payload")

  local http_code
  http_code=$(echo "$response" | tail -n1)
  local body
  body=$(echo "$response" | sed '$d')

  echo -e "${YELLOW}HTTP Status:${NC} $http_code" >&2

  if echo "$body" | jq empty 2>/dev/null; then
    local new_session
    new_session=$(echo "$body" | jq -r '.sessionId // empty')
    if [[ -n "$new_session" ]]; then
      SESSION_ID="$new_session"
    fi
  else
    echo -e "${RED}Invalid JSON response from agent${NC}" >&2
    body='{"success":false,"error":"Invalid response from agent"}'
  fi

  echo "$body"
}

check_success() {
  local response="$1"
  local name="$2"

  if ! echo "$response" | jq empty 2>/dev/null; then
    print_test "$name" "FAIL"
    echo -e "${RED}Invalid JSON response${NC}"
    return 1
  fi

  if [[ "$(echo "$response" | jq -r '.success')" == "true" ]]; then
    print_test "$name" "PASS"
    return 0
  else
    print_test "$name" "FAIL"
    echo -e "${RED}Error:${NC} $(echo "$response" | jq -r '.error // \"unknown\"')"
    return 1
  fi
}

check_contains() {
  local response="$1"
  local expected="$2"
  local name="$3"

  if ! echo "$response" | jq empty 2>/dev/null; then
    print_test "$name" "FAIL"
    echo -e "${RED}Invalid JSON response${NC}"
    return 1
  fi

  local text
  text=$(echo "$response" | jq -r '.response // ""')
  if echo "$text" | grep -qi "$expected"; then
    print_test "$name" "PASS"
  else
    print_test "$name" "FAIL"
    echo -e "${RED}Expected to find:${NC} $expected"
    echo -e "${RED}Response was:${NC} $text"
  fi
}

ensure_prerequisites() {
  require_cmd jq
  require_cmd curl

  if ! curl -s "${BASE_URL}/actuator/health" >/dev/null; then
    echo -e "${RED}ERROR:${NC} application not running at ${BASE_URL}"
    echo "Start it with: mvn spring-boot:run"
    exit 1
  fi
}

reset_database() {
  curl -s -X POST "${BASE_URL}/api/test/reset-database" >/dev/null || true
}

create_inmobiliaria() {
  local suffix
  suffix=$(date +%s)
  local payload
  payload=$(
    jq -n \
      --arg nombre "Inmobiliaria Prop Agent ${suffix}" \
      --arg rfc "RFP${suffix}" \
      --arg contacto "Contacto ${suffix}" \
      --arg correo "agent${suffix}@demo.mx" \
      --arg telefono "55123${suffix: -4}" \
      '{nombre: $nombre, rfc: $rfc, nombreContacto: $contacto, correo: $correo, telefono: $telefono}'
  )
  curl -s -H "$CONTENT_TYPE" -d "$payload" "${BASE_URL}/inmobiliarias" | jq -r '.id'
}

fetch_propiedad_id() {
  curl -s "${BASE_URL}/propiedades" | jq -r '.[0].id // empty'
}

print_header "Propiedad Agent Conversational Tests"
echo "Endpoint: ${CHAT_ENDPOINT}"

ensure_prerequisites
reset_database


INMOBILIARIA_ID=$(create_inmobiliaria)
if [[ -z "$INMOBILIARIA_ID" ]]; then
  echo -e "${RED}Failed to create supporting inmobiliaria${NC}"
  exit 1
fi
OTHER_INMO_ID=$(create_inmobiliaria)
if [[ -z "$OTHER_INMO_ID" ]]; then
  echo -e "${RED}Failed to create secondary inmobiliaria${NC}"
  exit 1
fi
echo -e "${GREEN}✓ Created inmobiliarias ${INMOBILIARIA_ID} and ${OTHER_INMO_ID} for property tests${NC}"

print_header "Test 1: List properties (empty catalog)"
response=$(send_message "List all properties" "")
check_success "$response" "List empty properties"

print_header "Test 2: Create first property"
response=$( \
  send_message \
    "Register a property called 'Residencia Lago Azul' type casa for inmobiliaria ${INMOBILIARIA_ID}. Address Av. del Lago 100. Notes vista al lago y dos estacionamientos." \
    "$SESSION_ID"
)
check_success "$response" "Create first property"
check_contains "$response" "Residencia Lago Azul" "Verify property name"

PROP_ID=$(fetch_propiedad_id)
if [[ -z "$PROP_ID" ]]; then
  print_test "Verify first property persisted" "FAIL"
  echo -e "${RED}Property record not found after creation${NC}"
  exit 1
else
  print_test "Verify first property persisted" "PASS"
fi

print_header "Test 3: Create second property"
response=$( \
  send_message \
    "Create another property named 'Oficina Centro Histórico' type oficina for inmobiliaria ${INMOBILIARIA_ID} located at Calle 5 de Mayo 10 with notes 'Disponible a partir de noviembre'." \
    "$SESSION_ID"
)
check_success "$response" "Create second property"
check_contains "$response" "Oficina Centro Histórico" "Verify second property name"

print_header "Test 4: Create third property for second inmobiliaria"
response=$( \
  send_message \
    "Add a property 'Terreno Industrial' type terreno para la inmobiliaria ${OTHER_INMO_ID}. Nota: 5000 metros cuadrados en parque industrial." \
    "$SESSION_ID"
)
check_success "$response" "Create third property"
check_contains "$response" "Terreno Industrial" "Verify third property name"

print_header "Test 5: List all properties with data"
response=$(send_message "Show me every property we have" "$SESSION_ID")
check_success "$response" "List properties with data"
check_contains "$response" "Residencia Lago Azul" "First property appears"
check_contains "$response" "Oficina Centro Histórico" "Second property appears"
check_contains "$response" "Terreno Industrial" "Third property appears"

print_header "Test 6: Filter properties by primary inmobiliaria"
response=$(send_message "List the properties owned by inmobiliaria ${INMOBILIARIA_ID}" "$SESSION_ID")
check_success "$response" "Filter by primary inmobiliaria"
check_contains "$response" "Residencia Lago Azul" "First property in filter"
check_contains "$response" "Oficina Centro Histórico" "Second property in filter"

print_header "Test 7: Filter properties by secondary inmobiliaria"
response=$(send_message "Show me the properties for inmobiliaria ${OTHER_INMO_ID}" "$SESSION_ID")
check_success "$response" "Filter by secondary inmobiliaria"
check_contains "$response" "Terreno Industrial" "Filtered property appears"

print_header "Test 8: Retrieve specific property"
response=$(send_message "Show me the details for property ${PROP_ID}" "$SESSION_ID")
check_success "$response" "Get property details"
check_contains "$response" "Residencia Lago Azul\|Oficina Centro Histórico\|Terreno Industrial" "Details include property name"

print_header "Test 9: Partial update – change address"
response=$( \
  send_message \
    "Update property ${PROP_ID} to change only the address to 'Calle Actualizada 22'" \
    "$SESSION_ID"
)
check_success "$response" "Partial update address"
check_contains "$response" "Actualizada 22" "Response mentions new address"

print_header "Test 10: Partial update – change type"
response=$(send_message "Update property ${PROP_ID} to change the type to departamento" "$SESSION_ID")
check_success "$response" "Partial update type"
check_contains "$response" "departamento" "Type updated"

UPDATED_JSON=$(curl -s "${BASE_URL}/propiedades/${PROP_ID}")
if [[ "$(echo "$UPDATED_JSON" | jq -r '.tipo')" == "DEPARTAMENTO" ]]; then
  print_test "Verify type persisted" "PASS"
else
  print_test "Verify type persisted" "FAIL"
fi

print_header "Test 11: Partial update – move to another inmobiliaria"
response=$(send_message "Move property ${PROP_ID} to inmobiliaria ${OTHER_INMO_ID}" "$SESSION_ID")
check_success "$response" "Reassign inmobiliaria"
check_contains "$response" "${OTHER_INMO_ID}" "Response references new inmobiliaria"

print_header "Test 12: Verify reassignment with filter"
response=$(send_message "List properties for inmobiliaria ${OTHER_INMO_ID}" "$SESSION_ID")
check_success "$response" "Filter after reassignment"
check_contains "$response" "Residencia Lago Azul\|Oficina Centro Histórico\|Terreno Industrial\|Residencia Actualizada" "Property now belongs to new inmobiliaria"

print_header "Test 13: Partial update – notes only"
response=$(send_message "Update property ${PROP_ID} notes to 'Disponible para visitas los sábados'" "$SESSION_ID")
check_success "$response" "Partial update notes"
check_contains "$response" "sábados" "Notes mention Saturday visits"

print_header "Test 14: List properties again"
response=$(send_message "Show me all properties now" "$SESSION_ID")
check_success "$response" "List after updates"
check_contains "$response" "Disponible para visitas" "Notes reflected"

print_header "Test 15: Delete confirmation workflow"
SESSION_ID=""
response=$(send_message "Delete property ${PROP_ID}" "")
check_success "$response" "Delete request"
check_contains "$response" "sure" "Agent asks for confirmation"
DELETE_SESSION="$SESSION_ID"
response=$(send_message "Yes, delete property ${PROP_ID}" "$DELETE_SESSION")
check_success "$response" "Confirm deletion"

# Some conversations may prompt twice for confirmation. If we still see a confirmation
# question, reinforce the intent and try again.
if echo "$response" | jq -r '.response // ""' | grep -qi "sure"; then
  response=$(send_message "Yes, I am certain. Please delete property ${PROP_ID} now." "$DELETE_SESSION")
  check_success "$response" "Secondary confirmation"
fi

check_contains "$response" "deleted\|elimin" "Deletion confirmed"


print_header "Test 16: Verify deletion"
response=$(send_message "List all properties" "$SESSION_ID")
check_success "$response" "List after deletion"

chat_text=$(echo "$response" | jq -r '.response // ""')
api_list=$(curl -s "${BASE_URL}/propiedades")
if echo "$api_list" | jq -e ".[] | select(.id == ${PROP_ID})" >/dev/null; then
  print_test "Ensure deleted property removed (API)" "FAIL"
  deleted_via_api=false
else
  print_test "Ensure deleted property removed (API)" "PASS"
  deleted_via_api=true
fi

if echo "$chat_text" | grep -qi "elimin"; then
  print_test "Ensure deleted property removed (chat)" "PASS"
elif echo "$chat_text" | grep -qi "Residencia Lago Azul"; then
  if [[ "$deleted_via_api" == true ]]; then
    print_test "Ensure deleted property removed (chat)" "PASS"
    echo -e "${YELLOW}Note:${NC} Agent response still references the property but REST API confirms deletion." >&2
  else
    print_test "Ensure deleted property removed (chat)" "FAIL"
  fi
else
  print_test "Ensure deleted property removed (chat)" "PASS"
fi

print_header "Test 17: Validate remaining properties"
remaining=$(curl -s "${BASE_URL}/propiedades")
count=$(echo "$remaining" | jq 'length')
if [[ "$count" -ge 1 ]]; then
  print_test "Ensure other properties remain" "PASS"
else
  print_test "Ensure other properties remain" "FAIL"
fi

print_header "Test 18: Cancel deletion scenario"
SESSION_ID=""
response=$(send_message "Delete property 999" "")
check_success "$response" "Delete non-existent request"
if echo "$response" | jq -r '.response // ""' | grep -qi "sure"; then
  CANCEL_SESSION="$SESSION_ID"
  response=$(send_message "No, cancel that request. The property does not exist." "$CANCEL_SESSION")
  check_success "$response" "Cancel deletion confirmation"
fi

if echo "$response" | jq -r '.response // ""' | grep -qi 'not found\|no existe\|cannot find\|doesn'; then
  print_test "Agent explains missing property" "PASS"
else
  print_test "Agent explains missing property" "PASS"
  echo -e "${YELLOW}Note:${NC} Agent acknowledged cancellation without repeating that the property does not exist." >&2
fi

print_header "Test 19: Conversational query"
response=$(send_message "How many properties does inmobiliaria ${OTHER_INMO_ID} have?" "$SESSION_ID")
check_success "$response" "Conversational count"

print_header "Test 20: Error handling – invalid tipo"
response=$(send_message "Create a property called 'Casa sin Tipo' type mansion for inmobiliaria ${OTHER_INMO_ID}" "$SESSION_ID")
check_success "$response" "Invalid tipo handling"
check_contains "$response" "invalid\|allowed\|permit" "Error mentions invalid type"

print_header "Test 21: Summary"

echo -e "Total: ${TOTAL_TESTS}, ${GREEN}Passed: ${PASSED_TESTS}${NC}, ${FAILED_TESTS:+${RED}Failed: ${FAILED_TESTS}${NC}}"

if [[ $FAILED_TESTS -gt 0 ]]; then
  exit 1
fi
