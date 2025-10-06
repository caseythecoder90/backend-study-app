#!/usr/bin/env python3
"""
Test script for AI Image to Flashcards endpoint
Usage: python3 test_image_flashcards.py <image-path> <jwt-token>
"""

import sys
import base64
import json
import requests
from pathlib import Path

def test_image_to_flashcards(image_path: str, jwt_token: str, base_url: str = "http://localhost:8080"):
    # Read and encode image
    image_file = Path(image_path)
    if not image_file.exists():
        print(f"Error: Image file '{image_path}' not found")
        sys.exit(1)

    with open(image_file, 'rb') as f:
        image_base64 = base64.b64encode(f.read()).decode('utf-8')

    # Detect MIME type
    mime_types = {
        '.jpg': 'image/jpeg',
        '.jpeg': 'image/jpeg',
        '.png': 'image/png',
        '.gif': 'image/gif',
        '.webp': 'image/webp'
    }
    mime_type = mime_types.get(image_file.suffix.lower(), 'image/png')

    # Create request payload
    payload = {
        "deckId": "test-deck-123",
        "userId": "test-user-456",
        "imageBase64": image_base64,
        "imageMimeType": mime_type,
        "prompt": "Generate flashcards from this image focusing on key concepts and details",
        "count": 5,
        "difficulty": "MEDIUM",
        "category": "Test",
        "model": "GPT_4O"
    }

    # Make API request
    print(f"Testing Image to Flashcards endpoint...")
    print(f"Image: {image_path} ({mime_type})")
    print(f"Base64 length: {len(image_base64)} characters")
    print(f"Sending request to {base_url}/api/ai/flashcards/generate-image...")
    print()

    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {jwt_token}"
    }

    try:
        response = requests.post(
            f"{base_url}/api/ai/flashcards/generate-image",
            headers=headers,
            json=payload,
            timeout=60
        )

        print(f"Response Status: {response.status_code}")
        print()

        if response.status_code == 200:
            flashcards = response.json()
            print(f"✅ Success! Generated {len(flashcards)} flashcards")
            print()
            print(json.dumps(flashcards, indent=2))
        else:
            print(f"❌ Error: {response.status_code}")
            print(response.text)

    except requests.exceptions.RequestException as e:
        print(f"❌ Request failed: {e}")
        sys.exit(1)

if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("Usage: python3 test_image_flashcards.py <image-file> <jwt-token>")
        print("Example: python3 test_image_flashcards.py diagram.png your-jwt-token-here")
        sys.exit(1)

    image_path = sys.argv[1]
    jwt_token = sys.argv[2]

    test_image_to_flashcards(image_path, jwt_token)