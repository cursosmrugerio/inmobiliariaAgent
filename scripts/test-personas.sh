#!/usr/bin/env bash

# Runs an end-to-end CRUD validation against the Personas API.
# Starts the Spring Boot application by default, waits for readiness,
# exercises the endpoints, and tears everything down.
# Set START_SERVER=false to skip starting/stopping the app.

set -euo pipefail

INFO() { printf '==> %s\n' "$1"; }
ERROR() { printf 'error: %s\n' "$1" >&2; }

require_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    ERROR "required command '$1' not found in PATH"
    exit 1
  fi
}

require_cmd curl
require_cmd jq
require_cmd date

BASE_URL="${BASE_URL:-http://127.0.0.1:8080}"
START_SERVER="${START_SERVER:-true}"
APP_PID=""
APP_LOG="${APP_LOG:-$(mktemp -t personas-app.log.XXXX)}"
TMP_DIR="$(mktemp -d)"

cleanup() {
  rm -rf "${TMP_DIR}"
  if [[ -n "${APP_PID}" ]] && kill -0 "${APP_PID}" 2>/dev/null; then
    INFO "Stopping Spring Boot app (PID ${APP_PID})"
    kill "${APP_PID}" >/dev/null 2>&1 || true
    wait "${APP_PID}" 2>/dev/null || true
  fi
  rm -f "${APP_LOG}"
}
trap cleanup EXIT

assert_status() {
  local expected="$1"
  local actual="$2"
  local context="$3"
  if [[ "${expected}" != "${actual}" ]]; then
    ERROR "expected HTTP ${expected} but got ${actual} during ${context}"
    [[ -f "${APP_LOG}" ]] && ERROR "See application log at ${APP_LOG}"
    exit 1
  fi
}

wait_for_app() {
  local retries=45
  for ((attempt = 1; attempt <= retries; attempt++)); do
    if curl -sS "${BASE_URL}/health" >/dev/null; then
      return 0
    fi
    sleep 1
  done
  ERROR "Application did not become ready after ${retries} seconds"
  [[ -f "${APP_LOG}" ]] && ERROR "Check ${APP_LOG}"
  exit 1
}

if [[ "${START_SERVER}" == "true" ]]; then
  require_cmd mvn
  INFO "Starting Spring Boot app with mvn spring-boot:run"
  mvn spring-boot:run >"${APP_LOG}" 2>&1 &
  APP_PID=$!
  INFO "Spring Boot app PID ${APP_PID} (log: ${APP_LOG})"
  wait_for_app
else
  INFO "Skipping server startup; using existing app at ${BASE_URL}"
fi

payload_suffix="$(date +%s)"
suffix_short="$(printf '%06d' "$((payload_suffix % 1000000))")"
fecha_alta="$(date -u +"%Y-%m-%dT%H:%M:%S")"

create_payload=$(
  jq -n \
    --arg tipo "FISICA" \
    --arg nombre "María ${payload_suffix}" \
    --arg apellidos "Gómez ${payload_suffix}" \
    --arg rfc "GOM${suffix_short}" \
    --arg curp "GOMR${suffix_short}HDFLGT08" \
    --arg email "maria${payload_suffix}@demo.mx" \
    --arg telefono "555${suffix_short}" \
    --arg fechaAlta "${fecha_alta}" \
    '{tipoPersona: $tipo, nombre: $nombre, apellidos: $apellidos, rfc: $rfc, curp: $curp, email: $email, telefono: $telefono, fechaAlta: $fechaAlta, activo: true}'
)

INFO "Creating persona"
create_body="${TMP_DIR}/persona-create.json"
create_status="$(
  curl -sS -o "${create_body}" -w '%{http_code}' \
    -H 'Content-Type: application/json' \
    -d "${create_payload}" \
    "${BASE_URL}/personas"
)"
assert_status 201 "${create_status}" "create persona"
persona_id="$(jq -r '.id' "${create_body}")"
INFO "Created persona with id ${persona_id}"

INFO "Listing personas"
list_body="${TMP_DIR}/persona-list.json"
list_status="$(
  curl -sS -o "${list_body}" -w '%{http_code}' \
    "${BASE_URL}/personas"
)"
assert_status 200 "${list_status}" "list personas"
if ! jq -e ".[] | select(.id == ${persona_id})" "${list_body}" >/dev/null; then
  ERROR "created persona not found in list response"
  exit 1
fi

INFO "Fetching persona by id"
get_body="${TMP_DIR}/persona-get.json"
get_status="$(
  curl -sS -o "${get_body}" -w '%{http_code}' \
    "${BASE_URL}/personas/${persona_id}"
)"
assert_status 200 "${get_status}" "get persona"

update_payload=$(
  jq -n \
    --arg tipo "MORAL" \
    --arg razon "Servicios Demo ${payload_suffix}" \
    --arg rfc "DEM${suffix_short}" \
    --arg email "servicios${payload_suffix}@demo.mx" \
    --arg telefono "556${suffix_short}" \
    --arg fechaAlta "${fecha_alta}" \
    '{tipoPersona: $tipo, razonSocial: $razon, rfc: $rfc, email: $email, telefono: $telefono, fechaAlta: $fechaAlta, activo: false}'
)

INFO "Updating persona"
update_body="${TMP_DIR}/persona-update.json"
update_status="$(
  curl -sS -o "${update_body}" -w '%{http_code}' \
    -X PUT \
    -H 'Content-Type: application/json' \
    -d "${update_payload}" \
    "${BASE_URL}/personas/${persona_id}"
)"
assert_status 200 "${update_status}" "update persona"
if [[ "$(jq -r '.tipoPersona' "${update_body}")" != "MORAL" ]]; then
  ERROR "expected tipoPersona MORAL after update"
  exit 1
fi
if [[ "$(jq -r '.activo' "${update_body}")" != "false" ]]; then
  ERROR "expected persona to be inactivo after update"
  exit 1
fi

INFO "Deleting persona"
delete_status="$(
  curl -sS -o /dev/null -w '%{http_code}' \
    -X DELETE \
    "${BASE_URL}/personas/${persona_id}"
)"
assert_status 204 "${delete_status}" "delete persona"

INFO "Verifying persona no longer appears in list"
post_delete_status="$(
  curl -sS -o "${list_body}" -w '%{http_code}' \
    "${BASE_URL}/personas"
)"
assert_status 200 "${post_delete_status}" "post-delete list personas"
if jq -e ".[] | select(.id == ${persona_id})" "${list_body}" >/dev/null; then
  ERROR "persona ${persona_id} still present after deletion"
  exit 1
fi

INFO "CRUD flow completed successfully."
