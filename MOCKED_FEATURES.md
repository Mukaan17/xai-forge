# Mocked Features - To Be Replaced

This document lists all mocked features that need to be replaced with actual backend API calls.

## Forgot Password Flow

### 1. Email Validation
**Location:** `frontend/src/features/auth/pages/LoginPage.tsx` - `handleCheckEmail` function

**Current Implementation:**
- Mocked to always return `true` for any email
- Simulates a 1-second delay

**To Replace:**
- Replace with actual API call: `authApi.checkEmailExists(email)`
- The backend endpoint should be: `POST /api/v1/auth/forgot-password/check-email`
- Should return `{ exists: boolean }`

**Backend Requirements:**
- Endpoint: `POST /api/v1/auth/forgot-password/check-email`
- Request Body: `{ email: string }`
- Response: `{ exists: boolean }`
- Should check if a user with the given email exists in the database

---

### 2. Send OTP
**Location:** `frontend/src/features/auth/pages/LoginPage.tsx` - `handleCheckEmail` function

**Current Implementation:**
- Mocked to simulate sending OTP
- Simulates a 1-second delay
- Logs the OTP to console for testing (OTP: `123456`)

**To Replace:**
- Replace with actual API call: `authApi.sendPasswordResetOtp(email)`
- The backend endpoint should be: `POST /api/v1/auth/forgot-password/send-otp`
- Should send a 6-digit OTP to the user's email

**Backend Requirements:**
- Endpoint: `POST /api/v1/auth/forgot-password/send-otp`
- Request Body: `{ email: string }`
- Response: `{ message: string }`
- Should:
  - Generate a 6-digit OTP
  - Store it temporarily (e.g., in Redis with TTL of 10-15 minutes)
  - Send it to the user's email address
  - Associate it with the email address

---

### 3. Verify OTP
**Location:** `frontend/src/features/auth/pages/LoginPage.tsx` - `handleVerifyCode` function

**Current Implementation:**
- Mocked to accept OTP: `123456`
- Simulates a 1-second delay
- Returns `true` only if code matches `123456`, otherwise `false`

**To Replace:**
- Replace with actual API call: `authApi.verifyPasswordResetOtp(email, code)`
- The backend endpoint should be: `POST /api/v1/auth/forgot-password/verify-otp`
- Should verify the OTP code against the stored OTP for that email

**Backend Requirements:**
- Endpoint: `POST /api/v1/auth/forgot-password/verify-otp`
- Request Body: `{ email: string, code: string }`
- Response: `{ valid: boolean }`
- Should:
  - Check if the OTP exists for the given email
  - Verify the code matches
  - Check if the OTP has expired
  - Optionally mark the OTP as used to prevent reuse

---

### 4. Reset Password
**Location:** `frontend/src/features/auth/pages/LoginPage.tsx` - `handleResetPassword` function

**Current Implementation:**
- Mocked to simulate password reset
- Simulates a 1-second delay
- Logs the new password to console

**To Replace:**
- Replace with actual API call: `authApi.resetPassword(email, newPassword)`
- The backend endpoint should be: `POST /api/v1/auth/forgot-password/reset`
- Should update the user's password after OTP verification

**Backend Requirements:**
- Endpoint: `POST /api/v1/auth/forgot-password/reset`
- Request Body: `{ email: string, newPassword: string }`
- Response: `{ message: string }`
- Should:
  - Verify that OTP was previously validated for this email
  - Hash the new password using BCrypt
  - Update the user's password in the database
  - Invalidate any existing OTP codes for that email
  - Optionally invalidate all existing sessions for security

---

## Testing Instructions

### Current Mocked OTP
For testing purposes, use the OTP code: **`123456`**

This will work for any email address when testing the forgot password flow.

### Testing Flow
1. Click "Forgot password?" on the login page
2. Enter any email address (e.g., `test@example.com`)
3. Click "Send Verification Code"
4. Enter the OTP: `123456`
5. The OTP will be verified and you can proceed to set a new password

---

## Notes

- All mocked functions include `console.log` statements for debugging
- Remove these console logs when implementing the actual API calls
- The mocked delays (setTimeout) simulate network latency - remove these when using real API calls
- Ensure proper error handling when replacing with actual API calls
- The actual API calls should use the existing `authApi` methods defined in `frontend/src/features/auth/api/authApi.ts`
