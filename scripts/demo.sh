#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${1:-http://localhost:8080}"

curl -fsS "${BASE_URL}/actuator/health"
echo

AGENDA_JSON="$(curl -fsS -X POST "${BASE_URL}/api/v1/agendas" \
  -H 'Content-Type: application/json' \
  -d '{"title":"Demonstração","description":"Fluxo criado por scripts/demo.sh"}')"
AGENDA_ID="$(printf '%s' "${AGENDA_JSON}" | sed -n 's/.*"id":"\([^"]*\)".*/\1/p')"

curl -fsS -X POST "${BASE_URL}/api/v1/agendas/${AGENDA_ID}/sessions" \
  -H 'Content-Type: application/json' -d '{"durationMinutes":5}'
echo

curl -fsS "${BASE_URL}/api/v1/mobile/agendas"
echo

curl -fsS -X POST "${BASE_URL}/api/v1/agendas/${AGENDA_ID}/votes" \
  -H 'Content-Type: application/json' \
  -d '{"associateId":"member-demo","choice":"SIM"}'
echo

HTTP_CODE="$(curl -sS -o /tmp/cooperative-voting-duplicate.json -w '%{http_code}' \
  -X POST "${BASE_URL}/api/v1/agendas/${AGENDA_ID}/votes" \
  -H 'Content-Type: application/json' \
  -d '{"associateId":"member-demo","choice":"NAO"}')"
test "${HTTP_CODE}" = "409"

curl -fsS "${BASE_URL}/api/v1/agendas/${AGENDA_ID}/results"
echo
