# API cURL Examples

Các ví dụ cURL commands để test các authentication endpoints.

## Base URL
```
http://localhost:8080
```

## 1. Register (POST /api/auth/register)

### Request
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -H "X-Trace-Id: trace-123" \
  -H "X-Request-Id: request-456" \
  -d '{
    "username": "newuser",
    "email": "newuser@example.com",
    "password": "password123",
    "confirmPassword": "password123",
    "fullName": "New User",
    "phone": "+1234567890"
  }'
```

### Response Example
```json
{
  "success": true,
  "message": "Registration successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "dGVzdC1yZWZyZXNoLXRva2Vu...",
    "tokenType": "Bearer",
    "expiresIn": 900,
    "userId": 1,
    "username": "newuser",
    "email": "newuser@example.com",
    "fullName": "New User"
  }
}
```

## 2. Login (POST /api/auth/login)

### Request
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -H "X-Trace-Id: trace-123" \
  -H "X-Request-Id: request-456" \
  -d '{
    "username": "admin",
    "password": "password123",
    "deviceId": "device-001",
    "channel": "WEB"
  }'
```

### Response Example
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "dGVzdC1yZWZyZXNoLXRva2Vu...",
    "tokenType": "Bearer",
    "expiresIn": 900,
    "userId": 1,
    "username": "admin",
    "email": "admin@example.com",
    "fullName": "Admin User"
  }
}
```

## 3. Refresh Token (POST /api/auth/refresh)

### Request
```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -H "X-Trace-Id: trace-123" \
  -H "X-Request-Id: request-456" \
  -d '{
    "refreshToken": "dGVzdC1yZWZyZXNoLXRva2Vu..."
  }'
```

### Response Example
```json
{
  "success": true,
  "message": "Token refreshed successfully",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 900
  }
}
```

## 4. Logout (POST /api/auth/logout)

### Request
```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Content-Type: application/json" \
  -H "X-Trace-Id: trace-123" \
  -H "X-Request-Id: request-456" \
  -d '{
    "refreshToken": "dGVzdC1yZWZyZXNoLXRva2Vu..."
  }'
```

### Response Example
```json
{
  "success": true,
  "message": "Logout successful"
}
```

## 5. Logout All Sessions (POST /api/auth/logout-all)

### Request
```bash
curl -X POST http://localhost:8080/api/auth/logout-all \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "X-Trace-Id: trace-123" \
  -H "X-Request-Id: request-456"
```

### Response Example
```json
{
  "success": true,
  "message": "Logged out from all sessions"
}
```

## 6. Change Password (POST /api/auth/change-password)

### Request
```bash
curl -X POST http://localhost:8080/api/auth/change-password \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "X-Trace-Id: trace-123" \
  -H "X-Request-Id: request-456" \
  -d '{
    "currentPassword": "oldpassword123",
    "newPassword": "newpassword456",
    "confirmPassword": "newpassword456"
  }'
```

### Response Example
```json
{
  "success": true,
  "message": "Password changed successfully"
}
```

## Error Response Examples

### Invalid Credentials (401)
```json
{
  "success": false,
  "errorCode": "AUTH_401_INVALID_CREDENTIALS",
  "message": "Invalid username or password"
}
```

### Invalid Token (401)
```json
{
  "success": false,
  "errorCode": "AUTH_401_INVALID_TOKEN",
  "message": "Invalid or expired token"
}
```

### Expired Token (401)
```json
{
  "success": false,
  "errorCode": "AUTH_401_EXPIRED_TOKEN",
  "message": "Token has expired"
}
```

### Account Locked (403)
```json
{
  "success": false,
  "errorCode": "AUTH_403_ACCOUNT_LOCKED",
  "message": "Account is locked"
}
```

### Validation Error (400)
```json
{
  "success": false,
  "errorCode": "VAL_400_VALIDATION_ERROR",
  "message": "Validation failed",
  "data": {
    "username": "Username is required",
    "password": "Password must be at least 8 characters"
  }
}
```

## Complete Test Flow

### Step 1: Register (Optional - if creating new account)
```bash
# Register new user
RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newuser",
    "email": "newuser@example.com",
    "password": "password123",
    "confirmPassword": "password123",
    "fullName": "New User"
  }')

# Extract tokens
ACCESS_TOKEN=$(echo $RESPONSE | jq -r '.data.accessToken')
REFRESH_TOKEN=$(echo $RESPONSE | jq -r '.data.refreshToken')

echo "Access Token: $ACCESS_TOKEN"
echo "Refresh Token: $REFRESH_TOKEN"
```

### Step 2: Login
```bash
# Save response to variable
RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "password123"
  }')

# Extract tokens (requires jq)
ACCESS_TOKEN=$(echo $RESPONSE | jq -r '.data.accessToken')
REFRESH_TOKEN=$(echo $RESPONSE | jq -r '.data.refreshToken')

echo "Access Token: $ACCESS_TOKEN"
echo "Refresh Token: $REFRESH_TOKEN"
```

### Step 3: Use Access Token for Protected Endpoints
```bash
curl -X GET http://localhost:8080/api/v1/users/1 \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json"
```

### Step 4: Refresh Access Token
```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d "{
    \"refreshToken\": \"$REFRESH_TOKEN\"
  }"
```

### Step 5: Change Password
```bash
curl -X POST http://localhost:8080/api/auth/change-password \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "currentPassword": "password123",
    "newPassword": "newpassword456",
    "confirmPassword": "newpassword456"
  }'
```

### Step 6: Logout
```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Content-Type: application/json" \
  -d "{
    \"refreshToken\": \"$REFRESH_TOKEN\"
  }"
```

## Notes

1. **Headers**:
   - `Content-Type: application/json` - Required for POST requests with body
   - `Authorization: Bearer <token>` - Required for protected endpoints
   - `X-Trace-Id` and `X-Request-Id` - Optional, for request tracking

2. **Token Format**:
   - Access tokens expire in 15 minutes (configurable)
   - Refresh tokens expire in 7 days (configurable)
   - Use refresh token to get new access token

3. **Authentication**:
   - Public endpoints: `/api/auth/register`, `/api/auth/login`, `/api/auth/refresh`, `/api/auth/logout`
   - Protected endpoints: `/api/auth/logout-all`, `/api/auth/change-password`, `/api/**`

4. **Registration Requirements**:
   - Username: 3-50 characters, alphanumeric and underscores only
   - Email: Valid email format
   - Password: Minimum 8 characters
   - Password and confirm password must match
   - Full name: Required, 1-100 characters
   - Phone: Optional, valid phone number format

5. **Password Requirements**:
   - Minimum 8 characters for new password
   - Current password must match
   - New password and confirm password must match

