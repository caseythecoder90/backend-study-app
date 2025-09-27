#!/bin/bash

# Script to encode Google Cloud credentials JSON file to base64
# Usage: ./encode-credentials.sh path/to/your/credentials.json

if [ $# -eq 0 ]; then
    echo "Usage: $0 <path-to-credentials-json-file>"
    echo "Example: $0 study-app-472920-1182c7a4b7dd.json"
    exit 1
fi

CREDENTIALS_FILE="$1"

if [ ! -f "$CREDENTIALS_FILE" ]; then
    echo "Error: File '$CREDENTIALS_FILE' not found!"
    exit 1
fi

echo "Encoding credentials file: $CREDENTIALS_FILE"
echo ""
echo "Base64 encoded credentials (copy this entire string):"
echo "=================================================="
base64 -i "$CREDENTIALS_FILE" | tr -d '\n'
echo ""
echo "=================================================="
echo ""
echo "Set this value as the GOOGLE_CREDENTIALS_JSON environment variable"