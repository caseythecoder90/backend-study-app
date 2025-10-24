# Environment Setup Guide

This guide explains how to configure environment variables for the Flashcards application in both local development and production environments.

## Table of Contents
- [How Google Credentials Work](#how-google-credentials-work)
- [Local Development Setup](#local-development-setup)
- [Production Setup](#production-setup)
- [IntelliJ Run Configuration](#intellij-run-configuration)
- [Troubleshooting](#troubleshooting)

---

## How Google Credentials Work

### The Problem
- Google Vertex AI requires a JSON credentials file for authentication
- This file contains sensitive secrets and **cannot be committed to Git**
- GitHub automatically flags credential files as security risks
- Production environments (like Railway) work best with environment variables, not files

### Our Solution: GoogleCredentialsInitializer

The application uses a custom `GoogleCredentialsInitializer` that runs **before** the Spring context loads:

1. **You provide**: Base64-encoded Google credentials as the `GOOGLE_CREDENTIALS_JSON` environment variable
2. **At startup**, the initializer:
   - Decodes the base64 string back to JSON
   - Writes it to a temporary file (e.g., `/tmp/gcp-credentials-xxxxx/gcp-credentials-uuid.json`)
   - Dynamically sets the Spring property `spring.ai.vertex.ai.gemini.credentials-uri` to point to this temp file
   - Marks the temp file for deletion when the JVM exits
3. **Spring AI** uses the file URI to authenticate with Google Cloud

### Benefits
✅ No credentials in source code or Git
✅ Same approach works locally and in production
✅ Credentials automatically cleaned up on shutdown
✅ GitHub won't flag your repository for exposed secrets

---

## Local Development Setup

### Required Environment Variables

For local development with the `local` profile, you need to set:

```bash
# Admin User Configuration
ADMIN_EMAIL=admin@example.com
ADMIN_USERNAME=admin
ADMIN_PASSWORD=YourSecurePassword123!
ADMIN_FIRST_NAME=Admin
ADMIN_LAST_NAME=User

# Google Vertex AI Credentials (base64-encoded)
GOOGLE_CREDENTIALS_JSON=<your_base64_encoded_credentials_here>
```

### Already Configured in `application-local.yml`

The following are already encrypted and stored in `application-local.yml`:
- MongoDB connection (hardcoded to `localhost:27017`)
- OpenAI API key (encrypted with Jasypt)
- Anthropic API key (encrypted with Jasypt)
- Google OAuth client ID/secret (encrypted with Jasypt)
- GitHub OAuth client ID/secret (encrypted with Jasypt)
- JWT secret (encrypted with Jasypt)
- Jasypt encryption password (plaintext in local profile only)

### How to Generate Base64-Encoded Google Credentials

1. **Download your GCP service account JSON file**:
   - Go to Google Cloud Console
   - Navigate to IAM & Admin → Service Accounts
   - Create or select a service account with Vertex AI permissions
   - Click "Keys" → "Add Key" → "Create new key" → JSON
   - Download the JSON file

2. **Encode the JSON file to base64**:

   **On Mac/Linux:**
   ```bash
   base64 -i path/to/your-service-account.json | tr -d '\n'
   ```

   **On Windows (PowerShell):**
   ```powershell
   [Convert]::ToBase64String([IO.File]::ReadAllBytes("path\to\your-service-account.json"))
   ```

3. **Copy the entire output** (it will be a very long string)

4. **Store it safely** (e.g., in a password manager)

5. **Delete the original JSON file** from your computer (you won't need it anymore)

---

## Production Setup (Railway)

### Required Environment Variables

Set these in your Railway environment:

```bash
# Database
MONGO_URL=mongodb://username:password@host:port/database?authSource=admin
MONGODB_DATABASE=flashcards

# AI Service API Keys
OPENAI_API_KEY=sk-...
ANTHROPIC_API_KEY=sk-ant-...

# Google Vertex AI
VERTEX_AI_PROJECT_ID=your-gcp-project-id
VERTEX_AI_LOCATION=us-central1
GOOGLE_CREDENTIALS_JSON=<your_base64_encoded_credentials>

# Optional: Override default models
OPENAI_MODEL=gpt-4o-mini
ANTHROPIC_MODEL=claude-3-sonnet-20240229
VERTEX_AI_MODEL=gemini-2.0-flash

# OAuth Credentials
GOOGLE_CLIENT_ID=your-google-oauth-client-id
GOOGLE_CLIENT_SECRET=your-google-oauth-client-secret
GITHUB_CLIENT_ID=your-github-oauth-app-id
GITHUB_CLIENT_SECRET=your-github-oauth-app-secret

# OAuth Redirect URIs (adjust for your domain)
GOOGLE_REDIRECT_URI=https://your-domain.com/login/oauth2/code/google
GITHUB_REDIRECT_URI=https://your-domain.com/login/oauth2/code/github

# JWT Configuration
JWT_SECRET=your-secure-random-jwt-secret-at-least-32-chars
JWT_EXPIRATION=86400000
JWT_ISSUER=flashcards-app

# Admin User (first-time setup)
ADMIN_EMAIL=admin@yourdomain.com
ADMIN_USERNAME=admin
ADMIN_PASSWORD=SecureProductionPassword123!
ADMIN_FIRST_NAME=Admin
ADMIN_LAST_NAME=User

# OAuth Frontend Redirects (adjust for your frontend domain)
OAUTH_SUCCESS_REDIRECT_URL=https://your-frontend.com/auth/success
OAUTH_FAILURE_REDIRECT_URL=https://your-frontend.com/auth/error

# Jasypt Encryption (for encrypted values in properties)
JASYPT_ENCRYPTOR_PASSWORD=your-jasypt-encryption-password

# Application Name
APP_NAME=Flashcards
```

---

## IntelliJ Run Configuration

### Step-by-Step Setup

1. **Open Run Configurations**:
   - Click Run → Edit Configurations...
   - Or click the configurations dropdown in the toolbar

2. **Select your Spring Boot Application**:
   - If none exists, click "+" → Spring Boot
   - Set Main class: `com.flashcards.backend.flashcards.FlashcardsApplication`

3. **Set Active Profile to `local`**:
   - In the "Active profiles" field, enter: `local`
   - Or add VM option: `-Dspring.profiles.active=local`

4. **Add Environment Variables**:
   - Click the folder icon next to "Environment variables"
   - Add each variable:
     ```
     ADMIN_EMAIL=admin@example.com
     ADMIN_USERNAME=admin
     ADMIN_PASSWORD=SecurePass123!
     ADMIN_FIRST_NAME=Admin
     ADMIN_LAST_NAME=User
     GOOGLE_CREDENTIALS_JSON=<paste your base64 string here>
     ```

   **Or** paste them as a single line (semicolon-separated on Windows, colon-separated on Mac/Linux):
   ```
   ADMIN_EMAIL=admin@example.com;ADMIN_USERNAME=admin;ADMIN_PASSWORD=SecurePass123!;ADMIN_FIRST_NAME=Admin;ADMIN_LAST_NAME=User;GOOGLE_CREDENTIALS_JSON=<base64_string>
   ```

5. **Save** the configuration

6. **Run** the application using this configuration

### Alternative: Use `.env` File (Not Recommended)

You can create a `.env` file in the project root (it's gitignored), but IntelliJ doesn't load these automatically. You'd need a plugin like EnvFile.

---

## Troubleshooting

### Error: "Could not open ServletContext resource [/${VERTEX_AI_CREDENTIALS_URI}]"

**Cause**: The `GOOGLE_CREDENTIALS_JSON` environment variable is not set.

**Solution**:
1. Verify the environment variable is set in your run configuration
2. Make sure you're using the correct run configuration when starting the app
3. Restart IntelliJ if you just added the variable

### Error: "Invalid base64 encoding for Google credentials"

**Cause**: The base64 string is malformed or contains invalid characters.

**Solution**:
1. Re-generate the base64 string using the commands above
2. Make sure there are **no line breaks** in the base64 string
3. Don't add quotes around the base64 string

### Error: "User already exists" on first run

**Cause**: Admin user was already created in a previous run.

**Solution**: This is normal. The admin user is created once and persists in MongoDB.

### MongoDB Connection Failed

**Cause**: MongoDB is not running or connection settings are wrong.

**Solution**:
1. **Start MongoDB locally**:
   ```bash
   docker-compose up -d
   ```
2. **Verify connection** string in `application-local.yml`:
   ```yaml
   mongodb://flashcards_user:flashcards_pass@localhost:27017/flashcards
   ```

### Tests Failing with MongoDB Errors

**Cause**: Integration tests trying to load full Spring context with MongoDB.

**Solution**:
- Unit tests (services, DAOs, controllers) are configured to use mocks and don't require MongoDB
- If `FlashcardsApplicationTests` is failing, you can delete it (it's a basic context load test)
- Only integration tests require MongoDB running

### Error: "Failed to authenticate with Google Vertex AI"

**Cause**: The service account doesn't have proper permissions.

**Solution**:
1. Go to Google Cloud Console
2. Ensure your service account has the **Vertex AI User** role
3. Re-download the credentials and re-encode to base64

### Jasypt Encryption Errors

**Cause**: Encrypted values in `application-local.yml` can't be decrypted.

**Solution**: The `jasypt.encryptor.password` in `application-local.yml` must match the password used to encrypt the values. If you need to re-encrypt:
```bash
./mvnw jasypt:encrypt-value -Djasypt.encryptor.password="your-password" -Djasypt.plugin.value="value-to-encrypt"
```

---

## Security Best Practices

1. **Never commit** `.env` files, credential files, or unencrypted secrets
2. **Use different credentials** for local development vs production
3. **Rotate secrets regularly**, especially if they may have been exposed
4. **Use strong passwords** for admin accounts (at least 12 characters)
5. **Keep your `GOOGLE_CREDENTIALS_JSON`** in a secure password manager
6. **Enable MFA** on your Google Cloud account

---

## Quick Reference

### Minimum Required for Local Development
```bash
ADMIN_EMAIL=admin@example.com
ADMIN_USERNAME=admin
ADMIN_PASSWORD=SecurePass123!
ADMIN_FIRST_NAME=Admin
ADMIN_LAST_NAME=User
GOOGLE_CREDENTIALS_JSON=<base64_credentials>
```

### Start MongoDB Locally
```bash
docker-compose up -d
```

### Run Application with Profile
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### Check Logs for Credentials Initialization
Look for:
```
Successfully initialized Google Cloud credentials from environment variable
```

---

## Additional Resources

- [Google Cloud Service Accounts](https://cloud.google.com/iam/docs/service-accounts)
- [Vertex AI Authentication](https://cloud.google.com/vertex-ai/docs/authentication)
- [Spring Boot Profiles](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.profiles)
- [Jasypt Spring Boot](https://github.com/ulisesbocchio/jasypt-spring-boot)
