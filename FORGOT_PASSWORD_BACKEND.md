# Forgot Password Backend Implementation Guide

The frontend forgot password functionality has been implemented. The following backend endpoints need to be created to complete the feature.

## Required Endpoints

### 1. Check Email Exists
**Endpoint:** `POST /api/v1/auth/forgot-password/check-email`

**Request Body:**
```json
{
  "email": "user@example.com"
}
```

**Response:**
```json
{
  "exists": true
}
```

**Purpose:** Validates if a user with the given email exists in the system before sending OTP.

---

### 2. Send OTP
**Endpoint:** `POST /api/v1/auth/forgot-password/send-otp`

**Request Body:**
```json
{
  "email": "user@example.com"
}
```

**Response:**
```json
{
  "message": "OTP sent successfully"
}
```

**Purpose:** Sends a 6-digit OTP code to the user's email address. The OTP should be:
- 6 digits
- Stored temporarily (e.g., in Redis or database with expiration)
- Valid for a limited time (e.g., 10-15 minutes)

---

### 3. Verify OTP
**Endpoint:** `POST /api/v1/auth/forgot-password/verify-otp`

**Request Body:**
```json
{
  "email": "user@example.com",
  "code": "123456"
}
```

**Response:**
```json
{
  "valid": true
}
```

**Purpose:** Verifies that the OTP code entered by the user matches the one sent to their email.

---

### 4. Reset Password
**Endpoint:** `POST /api/v1/auth/forgot-password/reset`

**Request Body:**
```json
{
  "email": "user@example.com",
  "newPassword": "NewSecurePassword123!"
}
```

**Response:**
```json
{
  "message": "Password reset successfully"
}
```

**Purpose:** Resets the user's password after OTP verification. Should:
- Verify the OTP was previously validated
- Hash the new password using BCrypt
- Update the user's password in the database
- Invalidate any existing OTP codes for that email

## Implementation Notes

1. **OTP Storage:** Consider using Redis for temporary OTP storage with TTL, or a database table with expiration timestamps.

2. **Security:**
   - Rate limit OTP requests to prevent abuse
   - Invalidate OTP after successful password reset
   - Use secure random number generation for OTPs
   - Consider email verification before allowing password reset

3. **Email Service:** You'll need to integrate an email service (e.g., SendGrid, AWS SES, or SMTP) to send OTP codes.

4. **Error Handling:**
   - Return appropriate error messages for invalid emails, expired OTPs, etc.
   - Don't reveal if an email exists in the system (security best practice)

5. **Password Validation:** Ensure the new password meets the same requirements as registration (minimum 8 characters, etc.)

## Example Flow

1. User enters email → `check-email` endpoint validates email exists
2. If email exists → `send-otp` endpoint generates and sends OTP
3. User enters OTP → `verify-otp` endpoint validates the code
4. If OTP is valid → User can enter new password → `reset` endpoint updates password

## Frontend Integration

The frontend is already configured to call these endpoints:
- `authApi.checkEmailExists(email)`
- `authApi.sendPasswordResetOtp(email)`
- `authApi.verifyPasswordResetOtp(email, code)`
- `authApi.resetPassword(email, newPassword)`

All endpoints are located in `/api/v1/auth/forgot-password/*` and should be accessible without authentication (public endpoints).
