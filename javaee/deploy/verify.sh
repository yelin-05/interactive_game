#!/usr/bin/env bash
set -euo pipefail

BASE="${1:-http://127.0.0.1:18080/moshi}"
URL="${BASE%/}/health"
BODY="$(mktemp)"
trap 'rm -f "$BODY"' EXIT

CODE="$(curl --silent --show-error --max-time 10 --output "$BODY" --write-out '%{http_code}' "$URL" || true)"
printf 'HTTP %s  %s\n' "$CODE" "$URL"
cat "$BODY"
printf '\n'

if [[ "$CODE" != "200" ]]; then
    printf 'FAIL: expected HTTP 200, got %s\n' "$CODE" >&2
    exit 1
fi
if ! grep -q '"status":"UP"' "$BODY" || ! grep -q '"db":"UP"' "$BODY"; then
    printf 'FAIL: health payload is not UP\n' >&2
    exit 1
fi
printf 'PASS: application and database are healthy\n'
