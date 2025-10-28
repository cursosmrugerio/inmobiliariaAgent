#!/usr/bin/env bash

# Runs a full CRUD cycle against the Inmobiliarias API.
# By default it starts the Spring Boot app (mvn spring-boot:run),
# waits until /health is ready, runs the tests, then shuts it down.
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

BASE_URL="${BASE_URL:-http://127.0.0.1:8080}"
START_SERVER="${START_SERVER:-true}"
APP_PID=""
APP_LOG="${APP_LOG:-$(mktemp -t inmobiliaria-app.log.XXXX)}"
TMP_DIR="$(mktemp -d)"

cleanup() {
  if [[ -n "${APP_PID}" ]] && kill -0 "${APP_PID}" 2>/dev/null; then
    INFO "Stopping Spring Boot app (PID ${APP_PID})"
    kill "${APP_PID}" >/dev/null 2>&1 || true
    wait "${APP_PID}" 2>/dev/null || true
  fi
  rm -rf "${TMP_DIR}"
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
create_payload=$(
  jq -n \
    --arg nombre "Inmo Demo ${payload_suffix}" \
    --arg rfc "DEM${payload_suffix}" \
    --arg contacto "Contacto ${payload_suffix}" \
    --arg correo "demo${payload_suffix}@inmo.mx" \
    --arg telefono "551234${payload_suffix: -4}" \
    '{nombre: $nombre, rfc: $rfc, nombreContacto: $contacto, correo: $correo, telefono: $telefono}'
)

INFO "Creating inmobiliaria"
create_body="${TMP_DIR}/create.json"
create_status="$(
  curl -sS -o "${create_body}" -w '%{http_code}' \
    -H 'Content-Type: application/json' \
    -d "${create_payload}" \
    "${BASE_URL}/inmobiliarias"
)"
assert_status 201 "${create_status}" "create"
inmo_id="$(jq -r '.id' "${create_body}")"
INFO "Created inmobiliaria with id ${inmo_id}"

INFO "Listing inmobiliarias"
list_body="${TMP_DIR}/list.json"
list_status="$(
  curl -sS -o "${list_body}" -w '%{http_code}' \
    "${BASE_URL}/inmobiliarias"
)"
assert_status 200 "${list_status}" "list"
if ! jq -e ".[] | select(.id == ${inmo_id})" "${list_body}" >/dev/null; then
  ERROR "created inmobiliaria not found in list response"
  exit 1
fi

INFO "Fetching inmobiliaria by id"
get_body="${TMP_DIR}/get.json"
get_status="$(
  curl -sS -o "${get_body}" -w '%{http_code}' \
    "${BASE_URL}/inmobiliarias/${inmo_id}"
)"
assert_status 200 "${get_status}" "get by id"

update_payload=$(
  jq -n \
    --arg nombre "Inmo Demo ${payload_suffix} Updated" \
    --arg rfc "DEM${payload_suffix}" \
    --arg contacto "Contacto ${payload_suffix} Update" \
    --arg correo "demo${payload_suffix}@inmo.mx" \
    --arg telefono "551234${payload_suffix: -4}" \
    '{nombre: $nombre, rfc: $rfc, nombreContacto: $contacto, correo: $correo, telefono: $telefono}'
)

INFO "Updating inmobiliaria"
update_body="${TMP_DIR}/update.json"
update_status="$(
  curl -sS -o "${update_body}" -w '%{http_code}' \
    -X PUT \
    -H 'Content-Type: application/json' \
    -d "${update_payload}" \
    "${BASE_URL}/inmobiliarias/${inmo_id}"
)"
assert_status 200 "${update_status}" "update"

INFO "Deleting inmobiliaria"
delete_status="$(
  curl -sS -o /dev/null -w '%{http_code}' \
    -X DELETE \
    "${BASE_URL}/inmobiliarias/${inmo_id}"
)"
assert_status 204 "${delete_status}" "delete"

INFO "Verifying inmobiliaria no longer appears in list"
post_delete_status="$(
  curl -sS -o "${list_body}" -w '%{http_code}' \
    "${BASE_URL}/inmobiliarias"
)"
assert_status 200 "${post_delete_status}" "post-delete list"
if jq -e ".[] | select(.id == ${inmo_id})" "${list_body}" >/dev/null; then
  ERROR "inmobiliaria ${inmo_id} still present after deletion"
  exit 1
fi

INFO "CRUD flow completed successfully."
