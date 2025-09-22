# Enterprise TOTP Authentication Flow

This document outlines the complete TOTP (Time-based One-Time Password) authentication flow as implemented in professional enterprise applications.

## 1. User Registration and Initial Login

```mermaid
sequenceDiagram
    participant U as User
    participant UI as Frontend
    participant API as Backend API
    participant DB as Database
    participant Auth as Auth Service

    Note over U,Auth: Initial User Registration
    U->>UI: Fill registration form
    UI->>API: POST /api/auth/register
    API->>Auth: validateUser()
    Auth->>DB: checkUserExists()
    DB-->>Auth: user not found
    Auth->>DB: createUser(totpEnabled: false)
    DB-->>Auth: user created
    Auth->>Auth: generateJWT(totpEnabled: false)
    Auth-->>API: {token, user, requiresTotpSetup: false}
    API-->>UI: Registration successful
    UI-->>U: Welcome! Account created

    Note over U,Auth: First Login (No TOTP yet)
    U->>UI: Enter credentials
    UI->>API: POST /api/auth/login
    API->>Auth: validateCredentials()
    Auth->>DB: findUser()
    Auth->>Auth: verifyPassword()
    Auth->>Auth: checkTotpEnabled() → false
    Auth->>Auth: generateJWT(totpEnabled: false)
    Auth-->>API: {token, user, requiresTotpSetup: false}
    API-->>UI: Login successful
    UI-->>U: Dashboard access granted
```

## 2. TOTP Setup Process

```mermaid
sequenceDiagram
    participant U as User
    participant UI as Frontend
    participant API as Backend API
    participant Auth as Auth Service
    participant TOTP as TOTP Service
    participant DB as Database
    participant App as Authenticator App

    Note over U,App: TOTP Setup Initiation
    U->>UI: Click "Enable 2FA"
    UI->>API: POST /api/auth/totp/setup
    Note right of UI: Authorization: Bearer <token>

    API->>Auth: setupTotp(userId)
    Auth->>DB: findUser(userId)
    Auth->>Auth: checkTotpNotEnabled()
    Auth->>TOTP: generateSecret()
    TOTP-->>Auth: secretKey
    Auth->>TOTP: generateQrCode(secret, username)
    TOTP-->>Auth: qrCodeDataUri
    Auth->>DB: updateUser(totpSecret: secret)
    Auth-->>API: {secret, qrCodeDataUri, instructions}
    API-->>UI: QR code and secret

    UI-->>U: Display QR code
    U->>App: Scan QR code
    App-->>U: Shows 6-digit codes

    Note over U,App: TOTP Verification and Enabling
    U->>UI: Enter 6-digit code
    UI->>API: POST /api/auth/totp/enable
    Note right of UI: {totpCode: "123456"}

    API->>Auth: enableTotp(userId, code)
    Auth->>DB: findUser(userId)
    Auth->>TOTP: validateCode(secret, code)
    TOTP-->>Auth: validation result
    Auth->>DB: updateUser(totpEnabled: true)
    Auth->>Auth: generateJWT(totpEnabled: true)
    Auth-->>API: {newToken, user, requiresTotpSetup: false}
    API-->>UI: TOTP enabled successfully
    UI-->>U: 2FA is now active
```

## 3. Login with TOTP Enabled

```mermaid
sequenceDiagram
    participant U as User
    participant UI as Frontend
    participant API as Backend API
    participant Auth as Auth Service
    participant TOTP as TOTP Service
    participant DB as Database
    participant App as Authenticator App

    Note over U,App: Standard Login Attempt
    U->>UI: Enter username/password
    UI->>API: POST /api/auth/login
    Note right of UI: {username, password} - NO totpCode

    API->>Auth: login(credentials)
    Auth->>DB: findUser()
    Auth->>Auth: verifyPassword() ✓
    Auth->>Auth: checkTotpEnabled() → true
    Auth->>Auth: checkTotpCodeProvided() → false
    Auth-->>API: ERROR: TOTP code required
    API-->>UI: {error: "TOTP code is required"}
    UI-->>U: Please enter 2FA code

    Note over U,App: Login with TOTP Code
    U->>App: Get current code
    App-->>U: "789123"
    U->>UI: Enter code in login form
    UI->>API: POST /api/auth/login
    Note right of UI: {username, password, totpCode: "789123"}

    API->>Auth: login(credentials)
    Auth->>DB: findUser()
    Auth->>Auth: verifyPassword() ✓
    Auth->>Auth: checkTotpEnabled() → true
    Auth->>Auth: checkTotpCodeProvided() → true
    Auth->>TOTP: validateCode(secret, code)
    TOTP-->>Auth: validation successful
    Auth->>Auth: generateJWT(totpEnabled: true)
    Auth-->>API: {token, user, requiresTotpSetup: false}
    API-->>UI: Login successful
    UI-->>U: Dashboard access granted
```

## 4. TOTP Disable Process

```mermaid
sequenceDiagram
    participant U as User
    participant UI as Frontend
    participant API as Backend API
    participant Auth as Auth Service
    participant DB as Database

    Note over U,DB: Disable TOTP
    U->>UI: Click "Disable 2FA"
    UI->>API: POST /api/auth/totp/disable
    Note right of UI: Authorization: Bearer <token>

    API->>Auth: disableTotp(userId)
    Auth->>DB: findUser(userId)
    Auth->>Auth: checkTotpEnabled() → true
    Auth->>DB: updateUser(totpEnabled: false, totpSecret: null)
    Auth->>Auth: generateJWT(totpEnabled: false)
    Auth-->>API: {newToken, user, requiresTotpSetup: false}
    API-->>UI: 2FA disabled successfully
    UI-->>U: 2FA is now disabled

    Note over U,DB: Future logins no longer require TOTP
```

## 5. Error Scenarios

```mermaid
sequenceDiagram
    participant U as User
    participant UI as Frontend
    participant API as Backend API
    participant Auth as Auth Service
    participant TOTP as TOTP Service

    Note over U,TOTP: Invalid TOTP Code
    U->>UI: Enter wrong 6-digit code
    UI->>API: POST /api/auth/login
    Note right of UI: {username, password, totpCode: "000000"}

    API->>Auth: login(credentials)
    Auth->>Auth: verifyPassword() ✓
    Auth->>Auth: checkTotpEnabled() → true
    Auth->>TOTP: validateCode(secret, "000000")
    TOTP-->>Auth: INVALID CODE
    Auth-->>API: ERROR: Invalid TOTP code
    API-->>UI: {error: "Invalid TOTP code"}
    UI-->>U: Invalid 2FA code, try again

    Note over U,TOTP: Expired TOTP Code
    U->>UI: Enter expired code
    UI->>API: POST /api/auth/login
    API->>Auth: login(credentials)
    Auth->>TOTP: validateCode(secret, expiredCode)
    TOTP-->>Auth: CODE EXPIRED
    Auth-->>API: ERROR: Invalid TOTP code
    API-->>UI: {error: "Invalid TOTP code"}
    UI-->>U: Code expired, get new one
```

## Key Enterprise Security Principles

### 1. **Token Lifecycle Management**
- New JWT tokens are issued after TOTP state changes
- Old tokens become invalid when TOTP is enabled/disabled
- Tokens contain current TOTP status for validation

### 2. **State Consistency**
- Database `totpEnabled` field is source of truth
- JWT token `totpEnabled` claim reflects database state
- No partial states - either fully enabled or disabled

### 3. **Security Boundaries**
- TOTP setup requires valid authentication
- TOTP enable requires valid TOTP code verification
- Login with TOTP enabled MUST provide valid code

### 4. **Error Handling**
- Specific error messages for different failure modes
- No information leakage about account state
- Consistent error responses

### 5. **User Experience**
- Clear feedback at each step
- Progressive enhancement (optional → required)
- Graceful degradation for edge cases

## Common Issues and Debugging

### Issue: Login succeeds without TOTP when it should be required

**Root Causes:**
1. Using old JWT token (before TOTP was enabled)
2. Database `totpEnabled` not properly updated
3. Login validation logic not checking TOTP state
4. JWT generation not reflecting current database state

**Debug Steps:**
1. Check JWT token payload for `totpEnabled` field
2. Verify database user record has `totpEnabled: true`
3. Ensure using latest token from TOTP enable response
4. Validate login logic checks `user.isTotpEnabled()`