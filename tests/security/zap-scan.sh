#!/bin/bash

# Configuration
TARGET_URL=${1:-"http://localhost:8900/app"}
REPORT_NAME=${2:-"zap-report.html"}
REPORT_DIR="$(pwd)/tests/security/reports"

echo "Starting OWASP ZAP Baseline Scan against $TARGET_URL..."

# Create reports directory if it doesn't exist
mkdir -p "$REPORT_DIR"

# Run ZAP Docker container
# We use --network host to allow ZAP to access localhost if running on Linux/CI
docker run --rm \
  --network host \
  -v "$REPORT_DIR:/zap/wrk/:rw" \
  -t zaproxy/zap-stable \
  zap-baseline.py \
  -t "$TARGET_URL" \
  -r "$REPORT_NAME" \
  -I # Fail on warnings/errors

echo "Scan complete. Report saved to $REPORT_DIR/$REPORT_NAME"
