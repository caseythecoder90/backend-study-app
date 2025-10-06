#!/bin/bash

# Test script for AI Image to Flashcards endpoint
# Usage: ./test-image-to-flashcards.sh <path-to-image> <jwt-token>

if [ "$#" -ne 2 ]; then
    echo "Usage: $0 <image-file> <jwt-token>"
    echo "Example: $0 diagram.png your-jwt-token-here"
    exit 1
fi

IMAGE_FILE="$1"
JWT_TOKEN="$2"

# Check if image file exists
if [ ! -f "$IMAGE_FILE" ]; then
    echo "Error: Image file '$IMAGE_FILE' not found"
    exit 1
fi

# Encode image to base64
IMAGE_BASE64=$(base64 -i "$IMAGE_FILE" | tr -d '\n')

# Detect MIME type based on file extension
EXTENSION="${IMAGE_FILE##*.}"
case "$EXTENSION" in
    jpg|jpeg)
        MIME_TYPE="image/jpeg"
        ;;
    png)
        MIME_TYPE="image/png"
        ;;
    gif)
        MIME_TYPE="image/gif"
        ;;
    webp)
        MIME_TYPE="image/webp"
        ;;
    *)
        echo "Unsupported image format: $EXTENSION"
        exit 1
        ;;
esac

# Create JSON payload
JSON_PAYLOAD=$(cat <<EOF
{
  "deckId": "test-deck-123",
  "userId": "test-user-456",
  "imageBase64": "$IMAGE_BASE64",
  "imageMimeType": "$MIME_TYPE",
  "prompt": "Generate flashcards from this image focusing on key concepts",
  "count": 5,
  "difficulty": "MEDIUM",
  "category": "Test",
  "model": "GPT_4O"
}
EOF
)

# Make API request
echo "Testing Image to Flashcards endpoint..."
echo "Image: $IMAGE_FILE ($MIME_TYPE)"
echo "Sending request..."
echo ""

curl -X POST "http://localhost:8080/api/ai/flashcards/generate-image" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -d "$JSON_PAYLOAD" \
  | jq '.'
