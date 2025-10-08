# Authentication Flow Sequence Diagrams

This document contains sequence diagrams for all authentication and authorization flows in the Flashcards application.

## 1. OAuth2 Login Flow (Google/GitHub)

```mermaid
sequenceDiagram
    participant User
    participant Frontend
    participant AuthController
    participant OAuth2Provider as OAuth2 Provider<br/>(Google/GitHub)
    participant UserService
    participant UserDao
    participant MongoDB
    participant JwtUtil
    participant TotpService

    User->>Frontend: Click "Login with Google/GitHub"
    Frontend->>AuthController: GET /oauth2/authorization/{provider}
    AuthController->>OAuth2Provider: Redirect to OAuth2 consent page
    OAuth2Provider->>User: Show consent screen
    User->>OAuth2Provider: Authorize application
    OAuth2Provider->>AuthController: Callback with authorization code
    AuthController->>OAuth2Provider: Exchange code for access token
    OAuth2Provider->>AuthController: Return user profile data

    AuthController->>UserService: findOrCreateOAuth2User(profile)
    UserService->>UserDao: findByEmail(email)
    UserDao->>MongoDB: Query users collection

    alt User exists
        MongoDB-->>UserDao: Return existing user
        UserDao-->>UserService: Return UserDto
    else User does not exist
        UserService->>UserDao: save(newUser)
        UserDao->>MongoDB: Insert new user document
        MongoDB-->>UserDao: Return saved user
        UserDao-->>UserService: Return UserDto
    end

    UserService->>TotpService: generateTotpSecret()
    TotpService-->>UserService: Return TOTP secret
    UserService->>UserDao: update(user with TOTP secret)
    UserDao->>MongoDB: Update user document

    UserService-->>AuthController: Return UserDto
    AuthController->>JwtUtil: generateToken(userId, email, roles)
    JwtUtil-->>AuthController: Return JWT token

    AuthController->>Frontend: Redirect to success URL with JWT
    Frontend->>User: Show dashboard
```

## 2. TOTP Two-Factor Authentication Verification

```mermaid
sequenceDiagram
    participant User
    participant Frontend
    participant AuthController
    participant TotpService
    participant UserService
    participant UserDao
    participant MongoDB
    participant JwtUtil

    User->>Frontend: Enter TOTP code from authenticator app
    Frontend->>AuthController: POST /api/auth/verify-totp<br/>{userId, totpCode}

    AuthController->>UserService: getUserById(userId)
    UserService->>UserDao: findById(userId)
    UserDao->>MongoDB: Query users collection
    MongoDB-->>UserDao: Return user document
    UserDao-->>UserService: Return UserDto
    UserService-->>AuthController: Return UserDto

    AuthController->>TotpService: verifyTotp(totpCode, secret)
    TotpService->>TotpService: Generate expected TOTP<br/>using time-based algorithm

    alt TOTP is valid
        TotpService-->>AuthController: Return true
        AuthController->>UserService: markTotpAsVerified(userId)
        UserService->>UserDao: update(user, totpVerified=true)
        UserDao->>MongoDB: Update user document
        MongoDB-->>UserDao: Confirmation

        AuthController->>JwtUtil: generateToken(userId, email, roles)
        JwtUtil-->>AuthController: Return JWT token
        AuthController-->>Frontend: 200 OK {token, user}
        Frontend->>User: Navigate to dashboard
    else TOTP is invalid
        TotpService-->>AuthController: Return false
        AuthController-->>Frontend: 401 Unauthorized<br/>"Invalid TOTP code"
        Frontend->>User: Show error message
    end
```

## 3. TOTP Setup Flow (First Time)

```mermaid
sequenceDiagram
    participant User
    participant Frontend
    participant AuthController
    participant TotpService
    participant UserService
    participant UserDao
    participant MongoDB

    User->>Frontend: Navigate to security settings
    Frontend->>AuthController: POST /api/auth/setup-totp<br/>{userId}

    AuthController->>UserService: getUserById(userId)
    UserService->>UserDao: findById(userId)
    UserDao->>MongoDB: Query users collection
    MongoDB-->>UserDao: Return user document
    UserDao-->>UserService: Return UserDto

    alt TOTP already enabled
        UserService-->>AuthController: User already has TOTP
        AuthController-->>Frontend: 400 Bad Request<br/>"TOTP already enabled"
        Frontend->>User: Show error message
    else TOTP not enabled
        AuthController->>TotpService: generateTotpSecret()
        TotpService-->>AuthController: Return secret key

        AuthController->>TotpService: generateQrCodeUrl(secret, email)
        TotpService-->>AuthController: Return QR code data URL

        AuthController->>UserService: saveTotpSecret(userId, secret)
        UserService->>UserDao: update(user, totpSecret=secret)
        UserDao->>MongoDB: Update user document

        AuthController-->>Frontend: 200 OK<br/>{qrCodeUrl, secret}
        Frontend->>User: Display QR code for scanning
        User->>User: Scan QR code with<br/>authenticator app
        User->>Frontend: Enter verification code
        Frontend->>AuthController: POST /api/auth/verify-totp<br/>(continues to TOTP verification flow)
    end
```

## 4. JWT Token Validation (Protected Endpoints)

```mermaid
sequenceDiagram
    participant Frontend
    participant JwtAuthFilter as JWT Auth Filter
    participant JwtUtil
    participant UserService
    participant Controller as Protected Controller
    participant Service

    Frontend->>JwtAuthFilter: Request with<br/>Authorization: Bearer {token}

    JwtAuthFilter->>JwtUtil: validateToken(token)

    alt Token is valid
        JwtUtil->>JwtUtil: Verify signature<br/>Check expiration
        JwtUtil-->>JwtAuthFilter: Token valid

        JwtAuthFilter->>JwtUtil: extractUserId(token)
        JwtUtil-->>JwtAuthFilter: Return userId

        JwtAuthFilter->>UserService: getUserById(userId)
        UserService-->>JwtAuthFilter: Return UserDto

        JwtAuthFilter->>JwtAuthFilter: Set authentication<br/>in SecurityContext

        JwtAuthFilter->>Controller: Forward request
        Controller->>Service: Business logic
        Service-->>Controller: Response
        Controller-->>Frontend: 200 OK {data}

    else Token is invalid/expired
        JwtUtil-->>JwtAuthFilter: Token invalid
        JwtAuthFilter-->>Frontend: 401 Unauthorized<br/>"Invalid or expired token"
    end
```

## 5. Password Reset Flow

```mermaid
sequenceDiagram
    participant User
    participant Frontend
    participant AuthController
    participant UserService
    participant UserDao
    participant MongoDB
    participant EmailService
    participant PasswordUtil

    User->>Frontend: Click "Forgot Password"
    Frontend->>User: Request email address
    User->>Frontend: Enter email
    Frontend->>AuthController: POST /api/auth/forgot-password<br/>{email}

    AuthController->>UserService: findByEmail(email)
    UserService->>UserDao: findByEmail(email)
    UserDao->>MongoDB: Query users collection

    alt User exists
        MongoDB-->>UserDao: Return user
        UserDao-->>UserService: Return UserDto

        UserService->>UserService: Generate reset token<br/>(UUID + expiration)
        UserService->>UserDao: saveResetToken(userId, token, expiry)
        UserDao->>MongoDB: Update user with reset token

        UserService->>EmailService: sendPasswordResetEmail(email, token)
        EmailService-->>UserService: Email sent

        UserService-->>AuthController: Success
        AuthController-->>Frontend: 200 OK<br/>"Reset link sent"
        Frontend->>User: Show success message

        User->>User: Check email
        User->>Frontend: Click reset link
        Frontend->>User: Show reset password form
        User->>Frontend: Enter new password

        Frontend->>AuthController: POST /api/auth/reset-password<br/>{token, newPassword}

        AuthController->>UserService: validateResetToken(token)
        UserService->>UserDao: findByResetToken(token)
        UserDao->>MongoDB: Query users collection

        alt Token valid and not expired
            MongoDB-->>UserDao: Return user
            UserDao-->>UserService: Return UserDto

            UserService->>PasswordUtil: hashPassword(newPassword)
            PasswordUtil-->>UserService: Return hashed password

            UserService->>UserDao: updatePassword(userId, hashedPassword)
            UserDao->>MongoDB: Update user password<br/>Clear reset token

            UserService-->>AuthController: Password updated
            AuthController-->>Frontend: 200 OK<br/>"Password reset successful"
            Frontend->>User: Navigate to login
        else Token invalid or expired
            MongoDB-->>UserDao: No user found
            UserDao-->>UserService: Token invalid
            UserService-->>AuthController: Invalid token
            AuthController-->>Frontend: 400 Bad Request<br/>"Invalid or expired token"
            Frontend->>User: Show error message
        end
    else User does not exist
        MongoDB-->>UserDao: No user found
        UserDao-->>UserService: User not found
        UserService-->>AuthController: User not found
        Note over AuthController: Security: Return same message<br/>to prevent email enumeration
        AuthController-->>Frontend: 200 OK<br/>"Reset link sent"
        Frontend->>User: Show success message
    end
```

## Implementation Status

| Flow | Status | Notes |
|------|--------|-------|
| OAuth2 Login | ✅ Implemented | Google and GitHub providers configured |
| TOTP Verification | ✅ Implemented | Using time-based OTP algorithm |
| TOTP Setup | ✅ Implemented | QR code generation working |
| JWT Validation | ✅ Implemented | Custom filter in security chain |
| Password Reset | ⚠️ Partial | Token generation works, email service needs configuration |

## Security Considerations

1. **JWT Tokens**
   - Currently stored in localStorage (consider HttpOnly cookies for XSS protection)
   - Token expiration: 24 hours (configurable via JWT_EXPIRATION)
   - Token refresh mechanism not implemented (needed for MVP)

2. **TOTP**
   - Secret keys stored encrypted in MongoDB
   - Time window: 30 seconds
   - Consider backup codes for account recovery

3. **OAuth2**
   - Redirect URIs validated
   - State parameter used to prevent CSRF
   - Token exchange happens server-side

4. **Rate Limiting**
   - ⚠️ NOT IMPLEMENTED - Critical security gap
   - Should limit login attempts, TOTP attempts, password reset requests
   - Redis-based rate limiting planned (see Redis Rate Limiting design doc)

## Related Documentation

- [MVP Readiness Assessment](./MVP-Readiness-Assessment.md)
- [Security Measures Review](./Security-Measures-Review.md) (to be created)
- [Rate Limiting with Redis](./Rate-Limiting-Redis-Design.md) (to be created)