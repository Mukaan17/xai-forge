# Subplan 5: Security Enhancements

## Objective
Enhance security infrastructure with API key authentication, two-factor authentication, improved session management, and updated security configuration to support multiple authentication methods.

## Prerequisites
- Subplan 4 completed (controllers exist)
- Spring Security 6.x configured
- JWT authentication working
- Google Authenticator library available

## Tasks

### 5.1 Create ApiKeyAuthenticationFilter
**File**: `backend/src/main/java/com/example/xaiapp/security/ApiKeyAuthenticationFilter.java`
- Extends: `OncePerRequestFilter`
- Check for `X-API-Key` header
- Validate key via ApiKeyService
- Create ApiKeyAuthenticationToken
- Set in SecurityContext
- Update last used timestamp
- Filter order: Before JWT filter

### 5.2 Create ApiKeyAuthenticationToken
**File**: `backend/src/main/java/com/example/xaiapp/security/ApiKeyAuthenticationToken.java`
- Extends: `AbstractAuthenticationToken`
- Fields: userId, apiKeyId, authorities (from permissions)
- Custom authentication token for API key auth

### 5.3 Create TwoFactorAuthService
**File**: `backend/src/main/java/com/example/xaiapp/security/TwoFactorAuthService.java`
- Methods: generateSecret, generateQRCodeDataUri, verifyCode, generateBackupCodes, hashBackupCodes, verifyBackupCode
- Uses Google Authenticator library
- Configuration: Issuer name from properties

### 5.4 Update AuthService for 2FA
**File**: `backend/src/main/java/com/example/xaiapp/service/AuthService.java`
- Add 2FA check in login method
- If user has 2FA enabled, require code
- Return special response indicating 2FA required
- Verify code before issuing JWT
- Add backup code support in login

### 5.5 Update JwtAuthenticationFilter for 2FA
**File**: `backend/src/main/java/com/example/xaiapp/security/JwtAuthenticationFilter.java`
- Add check: If user has 2FA enabled, verify it was used during login
- Store 2FA verification in JWT claims
- Reject tokens without 2FA claim if user has 2FA enabled

### 5.6 Create Session Management Integration
- Update `JwtTokenProvider.java` to include session token in JWT (jti claim)
- Update `AuthService.java` to create UserSession on login
- Update `JwtAuthenticationFilter.java` to update session last active on each request

### 5.7 Update SecurityConfig
**File**: `backend/src/main/java/com/example/xaiapp/config/SecurityConfig.java`
- Add ApiKeyAuthenticationFilter to filter chain (before JWT)
- Configure filter order
- Update CORS configuration
- Add API key authentication provider (if needed)
- Configure session management

### 5.8 Create Permission Evaluation (Optional)
**File**: `backend/src/main/java/com/example/xaiapp/security/PermissionEvaluator.java`
- Custom permission evaluator for API key permissions
- Check permissions like "datasets:read", "models:write"
- Use @PreAuthorize annotations in controllers

### 5.9 Update UserDetailsService
**File**: `backend/src/main/java/com/example/xaiapp/security/UserDetailsServiceImpl.java`
- Add check for account locked status
- Add check for 2FA requirement
- Add check for active status

### 5.10 Create Rate Limiting (Optional)
**Files**: `RateLimitFilter.java`
- Rate limit API key requests
- Rate limit login attempts
- Use Redis or in-memory cache

### 5.11 Add Security Headers
**File**: `SecurityConfig.java`
- Add security headers: X-Content-Type-Options, X-Frame-Options, X-XSS-Protection, Strict-Transport-Security, Content-Security-Policy

### 5.12 Create Security Utilities
**Files**:
- `IpAddressUtils.java` - IP extraction from requests
- `PasswordValidator.java` - Password strength validation

### 5.13 Update Application Properties
**File**: `application.properties`
- Add: app.2fa.issuer=XAI-Forge
- Add: app.session.expiration-hours=24
- Add: app.api-key.max-per-user=10
- Add: app.security.rate-limit.enabled=true (optional)

## Validation Checklist
- [ ] API key authentication works
- [ ] JWT authentication still works
- [ ] Both can be used (API key takes precedence)
- [ ] 2FA setup works (QR code generation)
- [ ] 2FA verification works
- [ ] Backup codes work
- [ ] Session creation on login works
- [ ] Session tracking works
- [ ] Session revocation works
- [ ] Account lockout works (after failed attempts)
- [ ] Security headers present
- [ ] CORS configured correctly
- [ ] No security vulnerabilities introduced

## Dependencies
- Subplan 4 (controllers must exist)
- Google Authenticator library (com.warrenstrange:googleauth)
- Spring Security 6.x

## Next Subplan
Subplan 6: Frontend API Integration (can run parallel with Subplan 5, but Subplan 5 should complete first)
