# JWT Authentication Flow

This document explains how JWT (JSON Web Token) authentication works in the application, from token generation to validation.

## 1. JWT Token Structure

```mermaid
graph TB
    JWT[JWT Token] --> Header[Header]
    JWT --> Payload[Payload]
    JWT --> Signature[Signature]

    Header --> H1[Algorithm: HS256]
    Header --> H2[Type: JWT]

    Payload --> P1[sub: userId]
    Payload --> P2[username: casquinn]
    Payload --> P3[email: user@example.com]
    Payload --> P4[authorities: ROLE_USER]
    Payload --> P5[totpEnabled: true/false]
    Payload --> P6[iss: flashcards-app]
    Payload --> P7[iat: issued at timestamp]
    Payload --> P8[exp: expiration timestamp]

    Signature --> S1[HMACSHA256 of Header.Payload]
    Signature --> S2[Signed with Secret Key]
```

## 2. JWT Token Generation Flow

```mermaid
sequenceDiagram
    participant U as User
    participant API as API Controller
    participant Auth as AuthService
    participant JWT as JwtService
    participant DB as Database
    participant Crypto as BCrypt

    Note over U,Crypto: Registration/Login Token Generation

    U->>API: POST /api/auth/register or /login
    API->>Auth: register() or login()
    Auth->>DB: findUser() or createUser()
    DB-->>Auth: User object

    Note over Auth,JWT: Generate JWT Token
    Auth->>JWT: generateToken(user)
    JWT->>JWT: createClaims(user)
    Note right of JWT: Claims include:<br/>- userId (sub)<br/>- username<br/>- email<br/>- authorities<br/>- totpEnabled<br/>- issuer<br/>- timestamps

    JWT->>JWT: setIssuedAt(now)
    JWT->>JWT: setExpiration(now + 1 hour)
    JWT->>JWT: signWithSecretKey(HS256)
    JWT-->>Auth: JWT token string

    Auth->>Auth: buildAuthResponse()
    Auth-->>API: AuthResponseDto with token
    API-->>U: {accessToken, tokenType: "Bearer", expiresIn: 3600, user, totpEnabled}
```

## 3. JWT Token Validation Flow (Every Request)

```mermaid
sequenceDiagram
    participant U as User/Client
    participant Filter as JwtAuthenticationFilter
    participant JWT as JwtService
    participant Security as SecurityContext
    participant API as Protected Endpoint

    Note over U,API: Client Makes Protected Request

    U->>Filter: Request with Authorization: Bearer <token>

    Filter->>Filter: extractTokenFromHeader()
    Note right of Filter: Remove "Bearer " prefix

    Filter->>JWT: isTokenValid(token)
    JWT->>JWT: extractAllClaims(token)

    alt Token Validation
        JWT->>JWT: validateSignature()
        JWT->>JWT: checkExpiration()
        JWT->>JWT: validateIssuer()
    end

    alt Token is Valid
        JWT-->>Filter: true + claims
        Filter->>JWT: extractUserId(token)
        JWT-->>Filter: userId
        Filter->>JWT: extractUsername(token)
        JWT-->>Filter: username

        Filter->>Filter: createAuthentication()
        Note right of Filter: UsernamePasswordAuthenticationToken<br/>with userId as principal

        Filter->>Security: setAuthentication(authToken)
        Filter->>API: Continue to endpoint
        API-->>U: Protected resource

    else Token is Invalid or Expired
        JWT-->>Filter: false / exception
        Filter->>Security: clearContext()
        Filter->>U: 401 Unauthorized
    end
```

## 4. JWT Token Refresh Pattern (Not Implemented Yet)

```mermaid
sequenceDiagram
    participant U as User
    participant API as API
    participant JWT as JwtService
    participant DB as Database

    Note over U,DB: Token Near Expiration

    U->>API: POST /api/auth/refresh
    Note right of U: Authorization: Bearer <current-token>

    API->>JWT: validateToken(currentToken)

    alt Token Valid but Expiring Soon
        JWT->>JWT: extractUserId(currentToken)
        JWT->>DB: findUser(userId)
        DB-->>JWT: User (with latest state)
        JWT->>JWT: generateToken(user)
        Note right of JWT: New token with:<br/>- Updated totpEnabled<br/>- Fresh expiration<br/>- Current authorities
        JWT-->>API: New JWT token
        API-->>U: {accessToken: newToken, expiresIn: 3600}

    else Token Expired or Invalid
        JWT-->>API: Invalid token error
        API-->>U: 401 - Please login again
    end
```

## 5. JWT Security Filter Chain

```mermaid
graph TB
    Request[HTTP Request] --> SecurityFilter[Spring Security Filter Chain]

    SecurityFilter --> CORS[CORS Filter]
    CORS --> JWTFilter[JwtAuthenticationFilter]

    JWTFilter --> HasToken{Has Bearer Token?}

    HasToken -->|Yes| ValidateToken[Validate JWT Token]
    HasToken -->|No| CheckPublic{Public Endpoint?}

    ValidateToken --> TokenValid{Token Valid?}

    TokenValid -->|Yes| SetAuth[Set Authentication]
    TokenValid -->|No| Clear[Clear SecurityContext]

    SetAuth --> Controller[Controller]
    Clear --> EntryPoint[JwtAuthenticationEntryPoint]

    CheckPublic -->|Yes| Controller
    CheckPublic -->|No| EntryPoint

    EntryPoint --> Error401[401 Unauthorized Response]
    Controller --> Response[Success Response]
```

## 6. Token State Changes with TOTP

```mermaid
sequenceDiagram
    participant U as User
    participant API as API
    participant Auth as AuthService
    participant JWT as JwtService
    participant DB as Database

    Note over U,DB: TOTP State Change Requires New Token

    rect rgb(240, 240, 240)
        Note over U,DB: Enable TOTP Flow
        U->>API: POST /api/auth/totp/enable
        API->>Auth: enableTotp(userId, code)
        Auth->>DB: updateUser(totpEnabled: true)
        Auth->>JWT: generateToken(updatedUser)
        JWT-->>Auth: New token with totpEnabled: true
        Auth-->>U: {accessToken: NEW_TOKEN, totpEnabled: true}
    end

    rect rgb(240, 240, 240)
        Note over U,DB: Disable TOTP Flow
        U->>API: POST /api/auth/totp/disable
        API->>Auth: disableTotp(userId)
        Auth->>DB: updateUser(totpEnabled: false)
        Auth->>JWT: generateToken(updatedUser)
        JWT-->>Auth: New token with totpEnabled: false
        Auth-->>U: {accessToken: NEW_TOKEN, totpEnabled: false}
    end

    Note over U: MUST use new token for future requests
```

## 7. JWT Claims Extraction

```mermaid
graph LR
    Token[JWT Token String] --> Parser[JWT Parser]

    Parser --> Claims[Claims Object]

    Claims --> Sub[userId from 'sub' claim]
    Claims --> Username[username claim]
    Claims --> Email[email claim]
    Claims --> Auth[authorities claim]
    Claims --> TOTP[totpEnabled claim]
    Claims --> Exp[expiration claim]
    Claims --> Iat[issuedAt claim]

    Sub --> SecurityContext[Security Context Principal]
    Username --> Logging[Audit Logging]
    TOTP --> Features[Feature Flags]
    Exp --> Validation[Token Validation]
```

## JWT Implementation Details

### Token Generation (JwtService.java)
```java
public String generateToken(User user) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("username", user.getUsername());
    claims.put("email", user.getEmail());
    claims.put("authorities", user.getAuthorities());
    claims.put("totpEnabled", user.isTotpEnabled());

    return Jwts.builder()
        .setClaims(claims)
        .setSubject(user.getId())
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
        .setIssuer("flashcards-app")
        .signWith(getSigningKey(), SignatureAlgorithm.HS256)
        .compact();
}
```

### Token Validation (JwtService.java)
```java
public boolean isTokenValid(String token) {
    try {
        Claims claims = extractAllClaims(token);
        return !isTokenExpired(claims) &&
               "flashcards-app".equals(claims.getIssuer());
    } catch (JwtException e) {
        return false;
    }
}
```

### Filter Authentication (JwtAuthenticationFilter.java)
```java
protected void doFilterInternal(request, response, filterChain) {
    String authHeader = request.getHeader("Authorization");

    if (authHeader != null && authHeader.startsWith("Bearer ")) {
        String token = authHeader.substring(7);

        if (jwtService.isTokenValid(token)) {
            String userId = jwtService.extractUserId(token);

            UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                    userId, null, new ArrayList<>()
                );

            SecurityContextHolder.getContext()
                .setAuthentication(authToken);
        }
    }

    filterChain.doFilter(request, response);
}
```

## Security Best Practices

### 1. **Token Storage (Frontend)**
- Store in memory or sessionStorage (not localStorage)
- Never store in cookies without httpOnly flag
- Clear on logout/session end

### 2. **Token Transmission**
- Always use HTTPS in production
- Send only in Authorization header
- Never in URL parameters

### 3. **Token Expiration**
- Short expiration time (1 hour)
- Implement refresh token pattern for UX
- Force re-authentication for sensitive operations

### 4. **Secret Key Management**
- Use strong, random secret key
- Store in environment variables
- Rotate keys periodically
- Never commit to version control

### 5. **Claims Management**
- Minimal sensitive data in claims
- No passwords or secrets
- Include version/type for future changes

## Common JWT Security Issues

### Issue 1: Token Hijacking
**Problem**: Token stolen via XSS/network sniffing
**Solution**: HTTPS only, Content Security Policy, short expiration

### Issue 2: Token Replay
**Problem**: Old token reused after state change
**Solution**: Issue new tokens on security state changes (TOTP enable/disable)

### Issue 3: Algorithm Confusion
**Problem**: Attacker changes algorithm to 'none'
**Solution**: Always validate algorithm, use specific algorithm (HS256)

### Issue 4: Weak Secret
**Problem**: Guessable or short secret key
**Solution**: Use 256-bit+ random key from secure generator

## Token Lifecycle Summary

```mermaid
stateDiagram-v2
    [*] --> Generated: User Login/Register
    Generated --> Active: Token Issued
    Active --> Validated: Each Request

    Validated --> Active: Valid
    Validated --> Expired: Time Exceeded
    Validated --> Invalid: Signature Failed

    Active --> Refreshed: Near Expiration
    Active --> Replaced: Security Change (TOTP)

    Refreshed --> Active: New Token
    Replaced --> Active: New Token

    Expired --> [*]: Must Re-authenticate
    Invalid --> [*]: Must Re-authenticate
    Active --> [*]: User Logout (Client-side)
```

## Testing JWT Flow

### 1. Generate Token
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail": "user", "password": "pass"}'
# Returns: {accessToken: "eyJ..."}
```

### 2. Use Token
```bash
curl -X GET http://localhost:8080/api/protected \
  -H "Authorization: Bearer eyJ..."
```

### 3. Decode Token (for debugging)
```bash
# Using jwt.io or command line
echo "eyJ..." | base64 -d
# Shows: {"sub":"userId","username":"user",...}
```

### 4. Test Expiration
```bash
# Wait 1 hour, then try using token
curl -X GET http://localhost:8080/api/protected \
  -H "Authorization: Bearer eyJ..."
# Returns: 401 Unauthorized
```