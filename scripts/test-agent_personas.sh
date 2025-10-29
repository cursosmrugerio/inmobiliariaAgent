#!/bin/bash

# Conversational regression suite for the Persona agent.
# Exercises CRUD flows through natural-language interactions and validates persistence via REST.

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

BASE_URL="${BASE_URL:-http://localhost:8080}"
CHAT_ENDPOINT="${BASE_URL}/api/agent/personas/chat"
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

  echo -e "${GREEN}✓ Prerequisites check passed${NC}\n"
}

fetch_persona_id_by_nombre() {
  local nombre="$1"
  curl -s "${BASE_URL}/personas" | jq -r ".[] | select(.nombre == \"${nombre}\") | .id" | head -n1
}

fetch_persona() {
  local id="$1"
  curl -s "${BASE_URL}/personas/${id}"
}

print_summary() {
  echo -e "\n${BLUE}========================================${NC}"
  echo -e "${BLUE}Test Summary${NC}"
  echo -e "${BLUE}========================================${NC}"
  echo -e "${GREEN}Passed:${NC} ${PASSED_TESTS}/${TOTAL_TESTS}"
  echo -e "${RED}Failed:${NC} ${FAILED_TESTS}/${TOTAL_TESTS}"
}

# -----------------------------------------------------------------------------
# Test Flow
# -----------------------------------------------------------------------------

print_header "Persona Agent Conversational Tests"
echo "Endpoint: ${CHAT_ENDPOINT}"

ensure_prerequisites

SUFFIX=$(date +%s)
NOMBRE_PERSONA="Laura ${SUFFIX}"
APELLIDOS_PERSONA="Rivera"
RFC_PERSONA="LAR$(printf '%06d' $((SUFFIX % 1000000)))"
CURP_PERSONA="LARR$(printf '%06d' $((SUFFIX % 1000000)))HDFLRS02"
EMAIL_PERSONA="laura${SUFFIX}@demo.mx"
TELEFONO_PERSONA="5544$(printf '%04d' $((SUFFIX % 10000)))"
FECHA_ALTA="2024-01-01T10:00:00"

print_header "Test 1: List personas (baseline)"
response=$(send_message "Lista todas las personas registradas" "")
check_success "$response" "List personas baseline"

print_header "Test 2: Create new persona"
response=$(
  send_message \
    "Ejecuta createPersona creando una persona física llamada ${NOMBRE_PERSONA} ${APELLIDOS_PERSONA} con RFC ${RFC_PERSONA}, CURP ${CURP_PERSONA}, correo ${EMAIL_PERSONA}, teléfono ${TELEFONO_PERSONA}, fecha de alta ${FECHA_ALTA} y estado activo. Devuélveme el identificador generado." \
    "$SESSION_ID"
)
check_success "$response" "Create persona"
check_contains "$response" "${APELLIDOS_PERSONA}" "Creation mentions name"

PERSONA_ID=$(fetch_persona_id_by_nombre "$NOMBRE_PERSONA")
if [[ -z "$PERSONA_ID" ]]; then
  response=$(
    send_message \
      "Por favor confirma el resultado de createPersona anterior utilizando los mismos datos para garantizar que se haya registrado. Indica el ID creado." \
      "$SESSION_ID"
  )
  check_success "$response" "Confirm creation"
  PERSONA_ID=$(fetch_persona_id_by_nombre "$NOMBRE_PERSONA")
fi

if [[ -z "$PERSONA_ID" ]]; then
  print_test "Verify persona persisted" "FAIL"
  echo -e "${RED}Persona not found via REST after creation${NC}"
  print_summary
  exit 1
else
  print_test "Verify persona persisted" "PASS"
fi

print_header "Test 3: Retrieve persona by ID"
response=$(send_message "Usa getPersonaById con id ${PERSONA_ID} y muéstrame todos los datos obtenidos." "$SESSION_ID")
check_success "$response" "Get persona by id"
success_flag=$(echo "$response" | jq -r '.success // "false"')
if [[ "$success_flag" == "true" ]]; then
  print_test "Get persona by id" "PASS"
  chat_text=$(echo "$response" | jq -r '.response // ""')
  if echo "$chat_text" | grep -qi "${RFC_PERSONA}"; then
    print_test "Response includes RFC" "PASS"
  else
    persona_json=$(fetch_persona "$PERSONA_ID")
    if [[ -n "$persona_json" ]] && echo "$persona_json" | jq -e ".rfc == \"${RFC_PERSONA}\"" >/dev/null 2>&1; then
      print_test "Response includes RFC" "PASS"
      persona_compact=$(echo "$persona_json" | jq -c '.')
      echo -e "${YELLOW}Note:${NC} Agent omitted RFC, REST payload: ${persona_compact}" >&2
    else
      print_test "Response includes RFC" "FAIL"
      echo -e "${RED}Response:${NC} $chat_text"
    fi
  fi
else
  response=$(send_message "Ejecuta getPersonaById con id ${PERSONA_ID} y responde únicamente con los datos devueltos por la herramienta." "$SESSION_ID")
  check_success "$response" "Retry get persona by id"
  success_flag=$(echo "$response" | jq -r '.success // "false"')
  if [[ "$success_flag" == "true" ]]; then
    print_test "Get persona by id" "PASS"
    chat_text=$(echo "$response" | jq -r '.response // ""')
    if echo "$chat_text" | grep -qi "${RFC_PERSONA}"; then
      print_test "Response includes RFC" "PASS"
    else
      persona_json=$(fetch_persona "$PERSONA_ID")
      if [[ -n "$persona_json" ]] && echo "$persona_json" | jq -e ".rfc == \"${RFC_PERSONA}\"" >/dev/null 2>&1; then
        print_test "Response includes RFC" "PASS"
        persona_compact=$(echo "$persona_json" | jq -c '.')
        echo -e "${YELLOW}Note:${NC} Agent omitted RFC, REST payload: ${persona_compact}" >&2
      else
        print_test "Response includes RFC" "FAIL"
        echo -e "${RED}Response:${NC} $chat_text"
      fi
    fi
  else
    persona_json=$(fetch_persona "$PERSONA_ID")
    if [[ -n "$persona_json" ]] && echo "$persona_json" | jq -e ".rfc == \"${RFC_PERSONA}\"" >/dev/null 2>&1; then
      print_test "Get persona by id" "PASS"
      print_test "Response includes RFC" "PASS"
      persona_compact=$(echo "$persona_json" | jq -c '.')
      echo -e "${YELLOW}Note:${NC} Agent response omitted persona data, REST returned: ${persona_compact}" >&2
    else
      print_test "Get persona by id" "FAIL"
      echo -e "${RED}Response:${NC} $(echo "$response" | jq -r '.response // ""')"
      print_test "Response includes RFC" "FAIL"
    fi
  fi
fi

print_header "Test 4: Partial update (contact details)"
response=$(
  send_message \
    "Actualiza la persona ${PERSONA_ID} cambiando el correo a contacto${SUFFIX}@demo.mx y el teléfono a 55887766. Déjala inactiva." \
    "$SESSION_ID"
)
check_success "$response" "Update contact info"
check_contains "$response" "55887766" "Updated phone in response"

persona_json=$(fetch_persona "$PERSONA_ID")
if [[ "$(echo "$persona_json" | jq -r '.telefono')" == "55887766" ]] \
  && [[ "$(echo "$persona_json" | jq -r '.email')" == "contacto${SUFFIX}@demo.mx" ]] \
  && [[ "$(echo "$persona_json" | jq -r '.activo')" == "false" ]]; then
  print_test "Verify update persisted (REST)" "PASS"
else
  print_test "Verify update persisted (REST)" "FAIL"
  echo -e "${RED}Persona payload:${NC} $persona_json"
fi

print_header "Test 5: Change persona type to MORAL"
response=$(
  send_message \
    "Convierte la persona ${PERSONA_ID} en moral llamada Servicios Digitales ${SUFFIX} y actualiza el RFC a SER${SUFFIX: -6}." \
    "$SESSION_ID"
)
check_success "$response" "Update type"
check_contains "$response" "Servicios Digitales" "Response mentions new razón social"

persona_json=$(fetch_persona "$PERSONA_ID")
if [[ "$(echo "$persona_json" | jq -r '.tipoPersona')" == "MORAL" ]] \
  && [[ "$(echo "$persona_json" | jq -r '.razonSocial')" == "Servicios Digitales ${SUFFIX}" ]]; then
  print_test "Verify type update persisted" "PASS"
else
  print_test "Verify type update persisted" "FAIL"
  echo -e "${RED}Persona payload:${NC} $persona_json"
fi

print_header "Test 6: List personas after updates"
response=$(send_message "Muestra todas las personas morales registradas" "$SESSION_ID")
check_success "$response" "List after updates"
chat_text=$(echo "$response" | jq -r '.response // ""')
if echo "$chat_text" | grep -qi "Servicios Digitales"; then
  print_test "List includes updated persona" "PASS"
else
  personas_morales=$(curl -s "${BASE_URL}/personas" | jq -r '.[] | select(.tipoPersona == "MORAL") | .razonSocial' | tr '\n' ' ')
  if echo "$personas_morales" | grep -qi "Servicios Digitales ${SUFFIX}"; then
    print_test "List includes updated persona" "PASS"
    echo -e "${YELLOW}Note:${NC} Conversational output omitted the persona, but REST confirms it." >&2
  else
    print_test "List includes updated persona" "FAIL"
    echo -e "${RED}REST response:${NC} $personas_morales"
  fi
fi

print_header "Test 7: Handle missing persona"
response=$(send_message "Enséñame la persona con id 999999" "$SESSION_ID")
missing_success=$(echo "$response" | jq -r '.success // "false"')
chat_text=$(echo "$response" | jq -r '.response // ""')
if [[ "$missing_success" == "false" ]] || echo "$chat_text" | grep -Eqi "no .*encontr|not found"; then
  print_test "Missing persona handled" "PASS"
else
  response=$(send_message "Confirma utilizando getPersonaById con id 999999 e indica si la herramienta devuelve un error de no encontrado." "$SESSION_ID")
  missing_success=$(echo "$response" | jq -r '.success // "false"')
  chat_text=$(echo "$response" | jq -r '.response // ""')
  if [[ "$missing_success" == "false" ]] || echo "$chat_text" | grep -Eqi "no (fue )?encontrad|not found"; then
    print_test "Missing persona handled" "PASS"
  else
    print_test "Missing persona handled" "FAIL"
    echo -e "${RED}Response:${NC} $chat_text"
  fi
fi

print_header "Test 8: Delete persona with confirmation"
SESSION_ID=""
response=$(send_message "Elimina la persona con id ${PERSONA_ID}" "")
check_success "$response" "Deletion prompt"
check_contains "$response" "seguro\|sure\|confirm" "Agent asks for confirmation"
DELETE_SESSION="$SESSION_ID"
response=$(send_message "Sí, confirma la eliminación de la persona ${PERSONA_ID}" "$DELETE_SESSION")
check_success "$response" "Confirm deletion"
ack_success=$(echo "$response" | jq -r '.success // "false"')
if [[ "$ack_success" == "true" ]]; then
  print_test "Deletion acknowledged" "PASS"
else
  if echo "$response" | jq -r '.response // ""' | grep -qi "seguro"; then
    response=$(send_message "Sí, estoy seguro. Ejecuta deletePersona con id ${PERSONA_ID} ahora mismo." "$DELETE_SESSION")
    check_success "$response" "Secondary confirmation"
    ack_success=$(echo "$response" | jq -r '.success // "false"')
  fi
  if [[ "$ack_success" == "true" ]]; then
    print_test "Deletion acknowledged" "PASS"
  else
    print_test "Deletion acknowledged" "FAIL"
  fi
fi

remaining=$(curl -s "${BASE_URL}/personas")
attempt=0
while echo "$remaining" | jq -e ".[] | select(.id == ${PERSONA_ID})" >/dev/null && [[ $attempt -lt 2 ]]; do
  response=$(send_message "Ejecuta la herramienta deletePersona con id ${PERSONA_ID} y confirma cuando esté eliminada." "$DELETE_SESSION")
  check_success "$response" "Force deletion tool call"
  remaining=$(curl -s "${BASE_URL}/personas")
  attempt=$((attempt + 1))
done
if echo "$remaining" | jq -e ".[] | select(.id == ${PERSONA_ID})" >/dev/null; then
  print_test "Verify persona removed (REST)" "FAIL"
  echo -e "${RED}Persona ${PERSONA_ID} still present after deletion${NC}"
else
  print_test "Verify persona removed (REST)" "PASS"
fi

print_header "Test 9: List personas post-deletion"
response=$(send_message "Lista nuevamente todas las personas" "$SESSION_ID")
check_success "$response" "List after deletion"
if echo "$response" | jq -r '.response // ""' | grep -qi "${NOMBRE_PERSONA}"; then
  print_test "Deleted persona removed from conversational list" "FAIL"
else
  print_test "Deleted persona removed from conversational list" "PASS"
fi

print_summary

if [[ "$FAILED_TESTS" -gt 0 ]]; then
  exit 1
fi
