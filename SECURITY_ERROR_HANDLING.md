# Security Error Handling - Complete Guide

## ✅ Overview

The security implementation now includes **comprehensive error handling** that provides proper JSON error responses for all authentication and authorization failures.

---

## 🎯 Error Handling Components

### 1. **JwtAuthenticationEntryPoint**
Handles authentication failures (401 Unauthorized)

**Triggers when:**
- No JWT token provided
- Invalid JWT token
- Expired JWT token
- Malformed JWT token
- Invalid signature

**Response Format:**
```json
{
  "success": false,
  "message": "Authentication failed",
  "error": {
    "code": "AUTHENTICATION_REQUIRED",
    "details": "Token has expired",
    "path": "/api/v1/orders",
    "timestamp": "2026-01-12T10:30:00Z"
  },
  "data": null
}
```

---

### 2. **JwtAccessDeniedHandler**
Handles authorization failures (403 Forbidden)

**Triggers when:**
- User doesn't have required role (e.g., CUSTOMER trying to access /admin/**)
- Missing permissions
- Insufficient privileges

**Response Format:**
```json
{
  "success": false,
  "message": "Access denied",
  "error": {
    "code": "ACCESS_DENIED",
    "details": "You don't have permission to access this resource",
    "path": "/api/v1/admin/users",
    "timestamp": "2026-01-12T10:30:00Z"
  },
  "data": null
}
```

---

### 3. **JwtAuthenticationFilter (Enhanced)**
Enhanced with specific error detection and messaging

**Error Types Detected:**

| Exception | Error Message | Description |
|-----------|---------------|-------------|
| `ExpiredJwtException` | "Token has expired" | Token validity period passed |
| `MalformedJwtException` | "Malformed token" | Token structure is invalid |
| `SignatureException` | "Invalid token signature" | Token signature verification failed |
| `UnsupportedJwtException` | "Unsupported token" | Token format not supported |
| `IllegalArgumentException` | "Invalid token" | Token is empty or null |
| `Exception` | "Authentication failed" | Any other error |

---

### 4. **GlobalExceptionHandler**
Catches security exceptions from controllers

**Handles:**
- `BadCredentialsException` → 401 (Invalid login credentials)
- `AuthenticationException` → 401 (General auth failure)
- `AccessDeniedException` → 403 (Authorization failure)

---

## 📋 Complete Error Scenarios

### Scenario 1: No Token Provided
```http
GET /api/v1/orders HTTP/1.1
Host: localhost:8080
```

**Response: 401 Unauthorized**
```json
{
  "success": false,
  "message": "Authentication failed",
  "error": {
    "code": "AUTHENTICATION_REQUIRED",
    "details": "Authentication required. Please provide a valid token.",
    "path": "/api/v1/orders"
  }
}
```

---

### Scenario 2: Expired Token
```http
GET /api/v1/orders HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGc...expired...
```

**Response: 401 Unauthorized**
```json
{
  "success": false,
  "message": "Authentication failed",
  "error": {
    "code": "AUTHENTICATION_REQUIRED",
    "details": "Token has expired",
    "path": "/api/v1/orders"
  }
}
```

---

### Scenario 3: Malformed Token
```http
GET /api/v1/orders HTTP/1.1
Host: localhost:8080
Authorization: Bearer invalid.token.format
```

**Response: 401 Unauthorized**
```json
{
  "success": false,
  "message": "Authentication failed",
  "error": {
    "code": "AUTHENTICATION_REQUIRED",
    "details": "Malformed token",
    "path": "/api/v1/orders"
  }
}
```

---

### Scenario 4: Invalid Signature
```http
GET /api/v1/orders HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbG...tampered.signature
```

**Response: 401 Unauthorized**
```json
{
  "success": false,
  "message": "Authentication failed",
  "error": {
    "code": "AUTHENTICATION_REQUIRED",
    "details": "Invalid token signature",
    "path": "/api/v1/orders"
  }
}
```

---

### Scenario 5: Customer Accessing Admin Endpoint
```http
GET /api/v1/admin/users HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGc...valid.customer.token
```

**Response: 403 Forbidden**
```json
{
  "success": false,
  "message": "Access denied",
  "error": {
    "code": "ACCESS_DENIED",
    "details": "You don't have permission to access this resource",
    "path": "/api/v1/admin/users"
  }
}
```

---

### Scenario 6: Invalid Login Credentials
```http
POST /api/v1/auth/login HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "wrongpassword"
}
```

**Response: 401 Unauthorized**
```json
{
  "success": false,
  "message": "Authentication failed",
  "error": {
    "code": "INVALID_CREDENTIALS",
    "details": "Invalid email or password",
    "path": "/api/v1/auth/login"
  }
}
```

---

## 🔐 Security Best Practices Implemented

### 1. **No Sensitive Information Leakage** ✅
- Error messages are user-friendly, not technical
- No stack traces exposed to clients
- No internal system details revealed
- No username enumeration (same error for invalid user/password)

### 2. **Consistent Error Format** ✅
All security errors follow the same JSON structure:
```json
{
  "success": boolean,
  "message": "string",
  "error": {
    "code": "ERROR_CODE",
    "details": "User-friendly message",
    "path": "/api/v1/endpoint",
    "timestamp": "ISO-8601 datetime"
  },
  "data": null
}
```

### 3. **Proper HTTP Status Codes** ✅
- **401 Unauthorized**: Authentication required or failed
- **403 Forbidden**: Authenticated but not authorized
- **400 Bad Request**: Validation errors
- **404 Not Found**: Resource not found
- **500 Internal Server Error**: Unexpected errors

### 4. **Logging Without Exposure** ✅
```java
// Logs include technical details for debugging
log.warn("Invalid JWT signature: {}", e.getMessage());

// But response only shows user-friendly message
"details": "Invalid token signature"
```

---

## 🧪 Test Coverage

### Unit Tests
✅ **JwtAuthenticationEntryPointTest** (4 tests)
- Returns 401 for auth failures
- Uses custom error messages
- Includes request path
- Proper JSON format

✅ **JwtAccessDeniedHandlerTest** (4 tests)
- Returns 403 for access denied
- Includes proper error codes
- User-friendly messages
- Includes request path

✅ **JwtAuthenticationFilterTest** (12 tests)
- Skips public endpoints
- Validates tokens correctly
- Sets error attributes for each exception type
- Continues filter chain
- Handles all JWT exceptions

✅ **GlobalExceptionHandlerSecurityTest** (5 tests)
- Handles BadCredentialsException
- Handles AuthenticationException
- Handles AccessDeniedException
- Includes request path
- No sensitive info leakage

### Integration Tests
✅ **SecurityConfigIntegrationTest** (14 tests)
- Public endpoints accessible
- Protected endpoints require auth
- Admin endpoints require admin role
- CORS configuration works
- Error format consistency
- Security headers present
- No sensitive info in responses

---

## 📊 Error Flow Diagram

```
Request → JwtAuthenticationFilter
    ↓
    ├─ Token Valid? → Continue to Controller
    │                      ↓
    │                      ├─ Has Permission? → Success Response
    │                      │
    │                      └─ No Permission → JwtAccessDeniedHandler
    │                                           ↓
    │                                           403 Forbidden
    │
    └─ Token Invalid → Set jwt_error attribute
                        ↓
                        Continue to SecurityFilterChain
                        ↓
                        No Authentication Set
                        ↓
                        JwtAuthenticationEntryPoint
                        ↓
                        401 Unauthorized
```

---

## 🚀 Frontend Integration Guide

### Handling Errors in Next.js

```typescript
// api/client.ts
async function apiCall(endpoint: string, options?: RequestInit) {
  const response = await fetch(`${API_URL}${endpoint}`, {
    ...options,
    headers: {
      'Authorization': `Bearer ${getToken()}`,
      'Content-Type': 'application/json',
      ...options?.headers,
    },
  });

  const data = await response.json();

  if (!response.ok) {
    // Handle security errors
    if (response.status === 401) {
      // Token expired or invalid - redirect to login
      handleAuthError(data.error);
      throw new AuthenticationError(data.error.details);
    }
    
    if (response.status === 403) {
      // Insufficient permissions - show error
      handleAuthorizationError(data.error);
      throw new AuthorizationError(data.error.details);
    }
    
    throw new ApiError(data.error.details);
  }

  return data;
}

function handleAuthError(error: ApiError) {
  // Clear token
  localStorage.removeItem('token');
  
  // Show user-friendly message
  toast.error(error.details);
  
  // Redirect to login
  router.push('/login');
}

function handleAuthorizationError(error: ApiError) {
  // Show user-friendly message
  toast.error(error.details);
  
  // Redirect to home or previous page
  router.push('/');
}
```

---

## 🔍 Debugging Security Errors

### Check Server Logs
```
2026-01-12 10:30:00 WARN  JwtAuthenticationFilter : Invalid JWT signature: JWT signature does not match locally computed signature
2026-01-12 10:30:05 WARN  JwtAuthenticationEntryPoint : Authentication failed for /api/v1/orders: Full authentication is required
2026-01-12 10:30:10 WARN  JwtAccessDeniedHandler : Access denied for /api/v1/admin/users: Access Denied
```

### Check Client Response
```bash
curl -v http://localhost:8080/api/v1/orders \
  -H "Authorization: Bearer invalid.token"

# Response
< HTTP/1.1 401 
< Content-Type: application/json
{
  "success": false,
  "message": "Authentication failed",
  "error": {
    "code": "AUTHENTICATION_REQUIRED",
    "details": "Malformed token",
    "path": "/api/v1/orders"
  }
}
```

---

## ✅ Benefits Summary

| Feature | Before | After |
|---------|--------|-------|
| **Error Format** | Inconsistent HTML/Text | ✅ Consistent JSON |
| **Error Messages** | Generic/Technical | ✅ User-friendly |
| **Status Codes** | May be wrong | ✅ Correct (401/403) |
| **Client Handling** | Difficult | ✅ Easy to parse |
| **Security** | Info leakage risk | ✅ Secure messages |
| **Logging** | Basic | ✅ Detailed with context |
| **Testing** | Limited | ✅ Comprehensive (35+ tests) |
| **Documentation** | Missing | ✅ Complete |

---

## 🎯 Conclusion

Your security implementation now has **production-ready error handling** that:

✅ **Provides clear, actionable error messages** to users
✅ **Maintains security** by not leaking sensitive information
✅ **Follows industry standards** (REST API best practices)
✅ **Is well-tested** with 35+ test cases
✅ **Is frontend-friendly** with consistent JSON responses
✅ **Includes comprehensive logging** for debugging
✅ **Handles all edge cases** (expired, malformed, invalid tokens)

**Your e-commerce API is now ready for production with robust security error handling!** 🚀

