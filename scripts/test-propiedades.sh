#!/usr/bin/env bash

# Runs an end-to-end CRUD validation against the Propiedades API.
# Optionally starts the Spring Boot application (mvn spring-boot:run),
# waits for readiness, exercises the endpoints, and tears everything down.
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
APP_LOG="${APP_LOG:-$(mktemp -t propiedades-app.log.XXXX)}"
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

create_inmobiliaria() {
  local name="$1"
  local suffix="$2"
  local payload
  payload=$(
    jq -n \
      --arg nombre "${name}" \
      --arg rfc "RFC${suffix}" \
      --arg contacto "Contacto ${suffix}" \
      --arg correo "prop${suffix}@demo.mx" \
      --arg telefono "55123${suffix: -4}" \
      '{nombre: $nombre, rfc: $rfc, nombreContacto: $contacto, correo: $correo, telefono: $telefono}'
  )

  local body="${TMP_DIR}/inmobiliaria-${suffix}.json"
  local status
  status="$(
    curl -sS -o "${body}" -w '%{http_code}' \
      -H 'Content-Type: application/json' \
      -d "${payload}" \
      "${BASE_URL}/inmobiliarias"
  )"
  assert_status 201 "${status}" "create inmobiliaria (${name})"
  jq -r '.id' "${body}"
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
INFO "Creating supporting inmobiliarias"
primary_inmo_id="$(create_inmobiliaria "Inmo Prop ${payload_suffix}" "${payload_suffix}")"
secondary_inmo_id="$(create_inmobiliaria "Inmo Prop ${payload_suffix} B" "$((payload_suffix + 1))")"
INFO "Primary inmobiliaria id ${primary_inmo_id}, secondary id ${secondary_inmo_id}"

create_prop_payload=$(
  jq -n \
    --arg nombre "Prop Demo ${payload_suffix}" \
    --arg tipo "CASA" \
    --arg direccion "Av. Propiedad ${payload_suffix} #100" \
    --arg observaciones "Observaciones iniciales ${payload_suffix}" \
    --argjson inmobiliariaId "${primary_inmo_id}" \
    '{nombre: $nombre, tipo: $tipo, direccion: $direccion, observaciones: $observaciones, inmobiliariaId: $inmobiliariaId}'
)

INFO "Creating propiedad"
create_body="${TMP_DIR}/prop-create.json"
create_status="$(
  curl -sS -o "${create_body}" -w '%{http_code}' \
    -H 'Content-Type: application/json' \
    -d "${create_prop_payload}" \
    "${BASE_URL}/propiedades"
)"
assert_status 201 "${create_status}" "create propiedad"
prop_id="$(jq -r '.id' "${create_body}")"
INFO "Created propiedad with id ${prop_id}"

INFO "Listing propiedades"
list_body="${TMP_DIR}/prop-list.json"
list_status="$(
  curl -sS -o "${list_body}" -w '%{http_code}' \
    "${BASE_URL}/propiedades"
)"
assert_status 200 "${list_status}" "list propiedades"
if ! jq -e ".[] | select(.id == ${prop_id})" "${list_body}" >/dev/null; then
  ERROR "created propiedad not found in listado general"
  exit 1
fi

INFO "Filtering propiedades por inmobiliaria ${primary_inmo_id}"
filter_body="${TMP_DIR}/prop-filter.json"
filter_status="$(
  curl -sS -o "${filter_body}" -w '%{http_code}' \
    "${BASE_URL}/propiedades?inmobiliariaId=${primary_inmo_id}"
)"
assert_status 200 "${filter_status}" "filter propiedades"
if ! jq -e ".[] | select(.id == ${prop_id})" "${filter_body}" >/dev/null; then
  ERROR "propiedad ${prop_id} not present in filtered listado for inmobiliaria ${primary_inmo_id}"
  exit 1
fi

INFO "Fetching propiedad by id"
get_body="${TMP_DIR}/prop-get.json"
get_status="$(
  curl -sS -o "${get_body}" -w '%{http_code}' \
    "${BASE_URL}/propiedades/${prop_id}"
)"
assert_status 200 "${get_status}" "get propiedad"

update_prop_payload=$(
  jq -n \
    --arg nombre "Prop Demo ${payload_suffix} Actualizada" \
    --arg tipo "DEPARTAMENTO" \
    --arg direccion "Calle Actualizada ${payload_suffix} #200" \
    --arg observaciones "Observaciones actualizadas ${payload_suffix}" \
    --argjson inmobiliariaId "${secondary_inmo_id}" \
    '{nombre: $nombre, tipo: $tipo, direccion: $direccion, observaciones: $observaciones, inmobiliariaId: $inmobiliariaId}'
)

INFO "Updating propiedad"
update_body="${TMP_DIR}/prop-update.json"
update_status="$(
  curl -sS -o "${update_body}" -w '%{http_code}' \
    -X PUT \
    -H 'Content-Type: application/json' \
    -d "${update_prop_payload}" \
    "${BASE_URL}/propiedades/${prop_id}"
)"
assert_status 200 "${update_status}" "update propiedad"

updated_tipo="$(jq -r '.tipo' "${update_body}")"
if [[ "${updated_tipo}" != "DEPARTAMENTO" ]]; then
  ERROR "expected tipo DEPARTAMENTO after update but got ${updated_tipo}"
  exit 1
fi
if [[ "$(jq -r '.inmobiliariaId' "${update_body}")" != "${secondary_inmo_id}" ]]; then
  ERROR "expected propiedad to belong to inmobiliaria ${secondary_inmo_id} after update"
  exit 1
fi

INFO "Verifying filtered listado points to nueva inmobiliaria ${secondary_inmo_id}"
filter_new_body="${TMP_DIR}/prop-filter-new.json"
filter_new_status="$(
  curl -sS -o "${filter_new_body}" -w '%{http_code}' \
    "${BASE_URL}/propiedades?inmobiliariaId=${secondary_inmo_id}"
)"
assert_status 200 "${filter_new_status}" "filter propiedades updated inmobiliaria"
if ! jq -e ".[] | select(.id == ${prop_id})" "${filter_new_body}" >/dev/null; then
  ERROR "propiedad ${prop_id} not found after reasignación to inmobiliaria ${secondary_inmo_id}"
  exit 1
fi

INFO "Deleting propiedad"
delete_status="$(
  curl -sS -o /dev/null -w '%{http_code}' \
    -X DELETE \
    "${BASE_URL}/propiedades/${prop_id}"
)"
assert_status 204 "${delete_status}" "delete propiedad"

INFO "Confirming propiedad removal from catalog"
post_delete_status="$(
  curl -sS -o "${list_body}" -w '%{http_code}' \
    "${BASE_URL}/propiedades"
)"
assert_status 200 "${post_delete_status}" "post-delete propiedad list"
if jq -e ".[] | select(.id == ${prop_id})" "${list_body}" >/dev/null; then
  ERROR "propiedad ${prop_id} still present after deletion"
  exit 1
fi

INFO "Confirming filtered listado for nueva inmobiliaria is vacío"
filter_empty_status="$(
  curl -sS -o "${filter_new_body}" -w '%{http_code}' \
    "${BASE_URL}/propiedades?inmobiliariaId=${secondary_inmo_id}"
)"
assert_status 200 "${filter_empty_status}" "filter post-delete"
if jq -e ".[] | select(.id == ${prop_id})" "${filter_new_body}" >/dev/null; then
  ERROR "propiedad ${prop_id} still present in filtered listado after deletion"
  exit 1
fi

INFO "Cleaning up inmobiliarias de prueba"
for inmo_id in "${primary_inmo_id}" "${secondary_inmo_id}"; do
  delete_inmo_status="$(
    curl -sS -o /dev/null -w '%{http_code}' \
      -X DELETE \
      "${BASE_URL}/inmobiliarias/${inmo_id}"
  )"
  assert_status 204 "${delete_inmo_status}" "delete inmobiliaria ${inmo_id}"
done

INFO "Propiedad CRUD flow completed successfully."
