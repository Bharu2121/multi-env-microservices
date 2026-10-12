#!/usr/bin/env bash
# ==============================================================================
# Smoke Test Script for Multi-Environment Microservices
# Calls /health on every service and exits non-zero on any failure.
#
# Usage:
#   ./scripts/smoke-test.sh <BASE_URL_OR_HOST> [AUTH_PORT] [ORDER_PORT] [NOTIFY_PORT]
# Example:
#   ./scripts/smoke-test.sh localhost
#   ./scripts/smoke-test.sh staging.mycompany.internal 8081 8082 8083
# ==============================================================================

set -euo pipefail

if [ "$#" -lt 1 ]; then
    echo "Usage: $0 <BASE_HOST_OR_URL> [AUTH_PORT] [ORDER_PORT] [NOTIFY_PORT]"
    echo "Example: $0 localhost"
    exit 1
fi

HOST="$1"
# Strip protocol prefix if present
HOST="${HOST#http://}"
HOST="${HOST#https://}"

AUTH_PORT="${2:-8081}"
ORDER_PORT="${3:-8082}"
NOTIFY_PORT="${4:-8083}"

SERVICES=(
    "auth-service:http://${HOST}:${AUTH_PORT}/health"
    "order-service:http://${HOST}:${ORDER_PORT}/health"
    "notification-service:http://${HOST}:${NOTIFY_PORT}/health"
)

echo "=========================================================="
echo " Starting Smoke Tests against host: ${HOST}"
echo "=========================================================="

FAILED=0

for ENTRY in "${SERVICES[@]}"; do
    NAME="${ENTRY%%:*}"
    URL="${ENTRY#*:}"
    
    echo -n "Checking ${NAME} at ${URL}... "
    
    # Run curl with timeout
    HTTP_CODE=$(curl -s -o /tmp/smoke-response.json -w "%{http_code}" --max-time 10 "${URL}" || echo "000")
    
    if [ "$HTTP_CODE" -eq 200 ]; then
        if grep -q '"status"[[:space:]]*:[[:space:]]*"UP"' /tmp/smoke-response.json 2>/dev/null; then
            echo " [PASS] (HTTP 200 - UP)"
        else
            echo " [FAIL] (HTTP 200 but status != UP)"
            cat /tmp/smoke-response.json 2>/dev/null || true
            FAILED=1
        fi
    else
        echo " [FAIL] (HTTP ${HTTP_CODE})"
        FAILED=1
    fi
done

rm -f /tmp/smoke-response.json

echo "=========================================================="
if [ "$FAILED" -ne 0 ]; then
    echo "❌ Smoke test FAILED for one or more services!"
    exit 1
else
    echo "✅ All service health checks PASSED!"
    exit 0
fi
