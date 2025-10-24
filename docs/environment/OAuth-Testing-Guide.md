# OAuth2 Testing Guide

This guide will help you test OAuth2 authentication with Google and GitHub locally.

## 📁 Files Created

Three HTML test pages have been added to `src/main/resources/static/`:

1. **`oauth-test.html`** - Main login page with OAuth buttons
2. **`auth-success.html`** - Success callback page displaying JWT token
3. **`auth-error.html`** - Error page for failed authentication

## 🔧 Setup Instructions

### 1. Set Environment Variables

For local testing, add these to your environment (or `.env` file):

```bash
# OAuth Redirect URLs for local development
export OAUTH_SUCCESS_REDIRECT_URL=http://localhost:8080/auth-success.html
export OAUTH_FAILURE_REDIRECT_URL=http://localhost:8080/auth-error.html

# Google OAuth redirect
export GOOGLE_REDIRECT_URI=http://localhost:8080/login/oauth2/code/google

# GitHub OAuth redirect
export GITHUB_REDIRECT_URI=http://localhost:8080/login/oauth2/code/github
```

### 2. Configure OAuth Apps

#### **Google OAuth Setup**

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing
3. Go to **APIs & Services** → **Credentials**
4. Click **Create Credentials** → **OAuth 2.0 Client ID**
5. Application type: **Web application**
6. Add authorized redirect URI:
   ```
   http://localhost:8080/login/oauth2/code/google
   ```
7. Copy **Client ID** and **Client Secret**
8. Set environment variables:
   ```bash
   export GOOGLE_CLIENT_ID=your-client-id
   export GOOGLE_CLIENT_SECRET=your-client-secret
   ```

#### **GitHub OAuth Setup**

1. Go to [GitHub Developer Settings](https://github.com/settings/developers)
2. Click **New OAuth App**
3. Fill in details:
   - **Application name**: Flashcards Local Dev
   - **Homepage URL**: `http://localhost:8080`
   - **Authorization callback URL**: `http://localhost:8080/login/oauth2/code/github`
4. Copy **Client ID** and generate **Client Secret**
5. Set environment variables:
   ```bash
   export GITHUB_CLIENT_ID=your-client-id
   export GITHUB_CLIENT_SECRET=your-client-secret
   ```

### 3. Start Your Application

```bash
./mvnw spring-boot:run
```

## 🧪 Testing OAuth Flow

### **Step 1: Open the Test Page**

Navigate to: **http://localhost:8080/oauth-test.html**

You'll see two OAuth login buttons:
- **Continue with Google**
- **Continue with GitHub**

### **Step 2: Test Google OAuth**

1. Click **Continue with Google**
2. Browser redirects to Google login
3. Select your Google account
4. Grant permissions (email, profile)
5. **Success**: Redirected to `auth-success.html` with JWT token
6. **Failure**: Redirected to `auth-error.html` with error details

### **Step 3: Test GitHub OAuth**

1. Click **Continue with GitHub**
2. Browser redirects to GitHub login
3. Authorize the application
4. **Success**: Redirected to `auth-success.html` with JWT token
5. **Failure**: Redirected to `auth-error.html` with error details

## ✅ Success Page Features

After successful OAuth login, `auth-success.html` displays:

- ✅ **JWT Access Token** (with copy button)
- ✅ **Token Payload** (decoded JWT claims)
- ✅ **User Information** (fetched from `/api/users/me`)
- ✅ **Test API Call** button to verify token works
- ✅ Token automatically saved to localStorage

## 🔍 What to Check

### **Database Verification**

After OAuth login, check MongoDB for the new user:

```javascript
db.users.findOne({ email: "your-oauth-email@gmail.com" })
```

**Expected fields:**
```json
{
  "username": "johndoe",
  "email": "john@gmail.com",
  "oauthProvider": "google",  // or "github"
  "oauthId": "1234567890",
  "profileImageUrl": "https://...",
  "firstName": "John",
  "lastName": "Doe",
  "password": null,  // No password for OAuth users
  "totpEnabled": false,
  "roles": ["USER"],
  "enabled": true,
  "lastLoginAt": "2025-10-23T14:30:00.000Z"
}
```

### **Application Logs**

Check console for OAuth flow logs:

```
INFO  - OAuth2 authentication successful
DEBUG - Processing OAuth2 authentication for provider: google
DEBUG - Creating new OAuth user: johndoe
DEBUG - OAuth2 authentication successful for user: johndoe
```

## 🧩 Testing Scenarios

### **Scenario 1: New User (First-time OAuth)**
1. Use an email that doesn't exist in database
2. OAuth creates new user automatically
3. Username generated from OAuth name/email
4. Verify user in database with `oauthProvider` set

### **Scenario 2: Existing User (Email Match)**
1. Manually register user with email `test@example.com`
2. Log in via Google OAuth with same email
3. OAuth links to existing user
4. Verify `oauthProvider` and `oauthId` added to existing user

### **Scenario 3: Returning OAuth User**
1. Log in via OAuth (creates user)
2. Log out (clear localStorage)
3. Log in again via same OAuth provider
4. Verify it finds existing user by `oauthProvider` + `oauthId`
5. Verify `lastLoginAt` is updated

### **Scenario 4: Username Collision**
1. Manually create user with username `johndoe`
2. OAuth login with name "John Doe"
3. Verify new user created with username `johndoe1`

## 🐛 Troubleshooting

### **Issue: "Redirect URI mismatch"**
- **Cause**: OAuth app redirect URI doesn't match configured URI
- **Fix**: Ensure Google/GitHub app has exact redirect URI: `http://localhost:8080/login/oauth2/code/{provider}`

### **Issue: "Email not found" error**
- **Cause**: OAuth provider doesn't provide email
- **Fix**: Ensure email scope is requested and email is public on provider

### **Issue: "Authentication failed"**
- **Cause**: Invalid client ID/secret or missing environment variables
- **Fix**: Verify `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, etc. are set correctly

### **Issue: Token not working for API calls**
- **Cause**: Token expired or invalid
- **Fix**: Check token expiration in decoded payload, regenerate token by logging in again

## 📊 API Endpoints Reference

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/oauth2/authorization/google` | GET | Initiate Google OAuth flow |
| `/oauth2/authorization/github` | GET | Initiate GitHub OAuth flow |
| `/login/oauth2/code/google` | GET | Google OAuth callback (handled by Spring) |
| `/login/oauth2/code/github` | GET | GitHub OAuth callback (handled by Spring) |
| `/api/auth/oauth2/providers` | GET | Get available OAuth providers and URLs |
| `/api/users/me` | GET | Get current user info (requires JWT) |

## 🎯 Expected Flow Diagram

```
User                  Browser                Spring Security           Your Handler              Database
  |                      |                          |                          |                      |
  |--[Click OAuth]------>|                          |                          |                      |
  |                      |---[Redirect to Provider]->|                          |                      |
  |                      |                          |                          |                      |
  |<-[OAuth Login Page]--|                          |                          |                      |
  |                      |                          |                          |                      |
  |--[Login & Approve]-->|                          |                          |                      |
  |                      |                          |                          |                      |
  |                      |<--[Auth Code]------------|                          |                      |
  |                      |                          |                          |                      |
  |                      |---[Exchange Code]------->|                          |                      |
  |                      |                          |                          |                      |
  |                      |<--[Access Token]---------|                          |                      |
  |                      |                          |                          |                      |
  |                      |                          |----[onAuthSuccess]------>|                      |
  |                      |                          |                          |                      |
  |                      |                          |                          |---[Find/Create]----->|
  |                      |                          |                          |                      |
  |                      |                          |                          |<--[User]-------------|
  |                      |                          |                          |                      |
  |                      |                          |                          |--[Generate JWT]      |
  |                      |                          |                          |                      |
  |                      |<-[Redirect to success.html?token=...]----------------|                      |
  |                      |                          |                          |                      |
  |<-[Success Page]------|                          |                          |                      |
```

## 🎉 Success Criteria

OAuth is working correctly when:

- ✅ Google OAuth redirects to success page with valid JWT
- ✅ GitHub OAuth redirects to success page with valid JWT
- ✅ JWT token can be used to call protected API endpoints
- ✅ User data is saved correctly in MongoDB
- ✅ Existing users are linked by email when using OAuth
- ✅ Unique usernames are generated for new OAuth users
- ✅ Error page shows proper error messages on failure
- ✅ `lastLoginAt` is updated on each OAuth login

## 📝 Notes

- Tokens are saved to `localStorage` for easy testing
- Use browser DevTools to inspect network requests
- Check MongoDB Compass to verify user data
- Review application logs for detailed OAuth flow
- For production, update redirect URLs to production domain