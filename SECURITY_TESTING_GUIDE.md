# Security Hardening - Testing & Validation Guide

## 🧪 Comprehensive Testing Strategy

**Purpose:** Verify all security improvements work correctly without breaking existing functionality.

---

## 1. Pre-Testing Setup

### Environment Configuration

**Create test environment variables:**

```bash
# Development Testing
export JWT_SECRET="test_secret_must_be_at_least_256_bits_long_for_hs256_algorithm_testing"
export JWT_EXPIRATION=900000
export JWT_ISSUER="babyshop-api"
export JWT_AUDIENCE="babyshop-web"
export CORS_ALLOWED_ORIGINS="http://localhost:3000,http://127.0.0.1:3000"
```

**Verify application starts:**

```bash
cd d:\baby-shop-ecommerce\babyshop-api
.\gradlew bootRun
```

**Expected startup logs:**
```
✅ Loaded CorsProperties: allowedOrigins=[http://localhost:3000, http://127.0.0.1:3000]
✅ Loaded JwtProperties: issuer=babyshop-api, audience=babyshop-web
✅ SecurityFilterChain configured successfully
```

---

## 2. CORS Security Testing

### Test 2.1: Allowed Origin

**Request:**
```bash
curl -X OPTIONS http://localhost:8080/api/v1/orders \
  -H "Origin: http://localhost:3000" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: Authorization,Content-Type" \
  -v
```

**Expected Response:**
```
< HTTP/1.1 200 OK
< Access-Control-Allow-Origin: http://localhost:3000
< Access-Control-Allow-Methods: GET, POST, PUT, PATCH, DELETE, OPTIONS
< Access-Control-Allow-Headers: Authorization, Content-Type, Accept
< Access-Control-Max-Age: 3600
```

**✅ PASS Criteria:**
- Status: 200 OK
- `Access-Control-Allow-Origin` header present
- `Access-Control-Allow-Credentials` NOT present (removed for security)

---

### Test 2.2: Disallowed Origin

**Request:**
```bash
curl -X OPTIONS http://localhost:8080/api/v1/orders \
  -H "Origin: http://evil-site.com" \
  -H "Access-Control-Request-Method: POST" \
  -v
```

**Expected Response:**
```
< HTTP/1.1 403 Forbidden
(No CORS headers)
```

**✅ PASS Criteria:**
- CORS headers NOT present for disallowed origin
- Request blocked

---

### Test 2.3: No Credentials Allowed

**Request:**
```bash
curl -X GET http://localhost:8080/api/v1/orders \
  -H "Origin: http://localhost:3000" \
  -H "Authorization: Bearer valid-token" \
  -v
```

**Expected Response:**
```
< HTTP/1.1 401 Unauthorized
< Access-Control-Allow-Origin: http://localhost:3000
(No Access-Control-Allow-Credentials header)
```

**✅ PASS Criteria:**
- `Access-Control-Allow-Credentials` header ABSENT
- CORS works without credentials (JWT in Authorization header)

---

## 3. JWT Claim Validation Testing

### Test 3.1: Valid Token with All Claims

**Generate token (via login or test utility):**
```json
{
  "sub": "user@example.com",
  "iss": "babyshop-api",
  "aud": "babyshop-web",
  "exp": 1736700000,
  "iat": 1736696400
}
```

**Request:**
```bash
curl -X GET http://localhost:8080/api/v1/orders \
  -H "Authorization: Bearer <valid-token>" \
  -v
```

**Expected Response:**
```
< HTTP/1.1 200 OK (or 404 if endpoint doesn't exist yet)
(Request processed successfully)
```

**✅ PASS Criteria:**
- Token accepted
- Authentication successful
- No 401 error

---

### Test 3.2: Token with Wrong Issuer

**Generate token with wrong issuer:**
```json
{
  "sub": "user@example.com",
  "iss": "wrong-issuer",  // ❌ Wrong issuer
  "aud": "babyshop-web",
  "exp": 1736700000
}
```

**Request:**
```bash
curl -X GET http://localhost:8080/api/v1/orders \
  -H "Authorization: Bearer <wrong-issuer-token>" \
  -v
```

**Expected Response:**
```json
{
  "success": false,
  "message": "Authentication failed",
  "error": {
    "code": "AUTHENTICATION_REQUIRED",
    "details": "Invalid authentication token",
    "path": "/api/v1/orders"
  }
}
```

**Expected Server Log:**
```
WARN  JWT validation failed for request: /api/v1/orders
```

**✅ PASS Criteria:**
- Status: 401 Unauthorized
- Generic error message (no "wrong issuer" exposed)
- SecurityContext cleared
- Log shows generic error

---

### Test 3.3: Token with Wrong Audience

**Generate token with wrong audience:**
```json
{
  "sub": "user@example.com",
  "iss": "babyshop-api",
  "aud": "wrong-audience",  // ❌ Wrong audience
  "exp": 1736700000
}
```

**Request:**
```bash
curl -X GET http://localhost:8080/api/v1/orders \
  -H "Authorization: Bearer <wrong-audience-token>" \
  -v
```

**Expected Response:**
```json
{
  "success": false,
  "message": "Authentication failed",
  "error": {
    "code": "AUTHENTICATION_REQUIRED",
    "details": "Invalid authentication token",
    "path": "/api/v1/orders"
  }
}
```

**✅ PASS Criteria:**
- Status: 401 Unauthorized
- Generic error (no "wrong audience" exposed)
- Token rejected

---

### Test 3.4: Expired Token

**Generate expired token:**
```json
{
  "sub": "user@example.com",
  "iss": "babyshop-api",
  "aud": "babyshop-web",
  "exp": 1000000000  // ❌ Past date
}
```

**Request:**
```bash
curl -X GET http://localhost:8080/api/v1/orders \
  -H "Authorization: Bearer <expired-token>" \
  -v
```

**Expected Response:**
```json
{
  "success": false,
  "message": "Authentication failed",
  "error": {
    "code": "AUTHENTICATION_REQUIRED",
    "details": "Authentication token expired",
    "path": "/api/v1/orders"
  }
}
```

**Expected Server Log:**
```
WARN  JWT expired for request: /api/v1/orders - Exception: ExpiredJwtException
```

**✅ PASS Criteria:**
- Status: 401 Unauthorized
- Generic message mentioning "expired" (acceptable)
- No PII in logs
- SecurityContext cleared

---

### Test 3.5: Tampered Signature

**Generate token and modify signature:**
```
eyJhbGc...payload...tampered-signature
```

**Request:**
```bash
curl -X GET http://localhost:8080/api/v1/orders \
  -H "Authorization: Bearer <tampered-token>" \
  -v
```

**Expected Response:**
```json
{
  "success": false,
  "message": "Authentication failed",
  "error": {
    "code": "AUTHENTICATION_REQUIRED",
    "details": "Invalid authentication token",
    "path": "/api/v1/orders"
  }
}
```

**Expected Server Log:**
```
WARN  Invalid JWT signature for request: /api/v1/orders - Exception: SignatureException
```

**✅ PASS Criteria:**
- Status: 401 Unauthorized
- Generic error (no "signature" exposed to client)
- SecurityContext cleared

---

## 4. SecurityContext Clearing Testing

### Test 4.1: Invalid Token Doesn't Leave Partial Auth

**Scenario:** Token validation fails after UserDetails is loaded.

**Test Steps:**
1. Create token for valid user
2. Tamper with token signature
3. Send request
4. Verify SecurityContext is empty

**Request:**
```bash
curl -X GET http://localhost:8080/api/v1/orders \
  -H "Authorization: Bearer <tampered-valid-user-token>" \
  -v
```

**Expected Behavior:**
```java
// Internal flow:
1. Extract username from token ✅
2. Load UserDetails from database ✅
3. Validate token signature ❌ FAIL
4. SecurityContext.clearContext() ✅ CLEARED
5. Return 401 ✅
```

**✅ PASS Criteria:**
- SecurityContext is empty after request
- No partial authentication state
- 401 returned

---

### Test 4.2: Exception During Validation Clears Context

**Scenario:** Unexpected exception during token processing.

**Request:**
```bash
curl -X GET http://localhost:8080/api/v1/orders \
  -H "Authorization: Bearer malformed-garbage" \
  -v
```

**Expected Response:**
```json
{
  "success": false,
  "message": "Authentication failed",
  "error": {
    "code": "AUTHENTICATION_REQUIRED",
    "details": "Invalid authentication token",
    "path": "/api/v1/orders"
  }
}
```

**Expected Server Log:**
```
WARN  Malformed JWT for request: /api/v1/orders - Exception: MalformedJwtException
```

**✅ PASS Criteria:**
- SecurityContext cleared
- Generic error returned
- No stack traces exposed

---

## 5. PII Protection Testing

### Test 5.1: No Email in Logs

**Setup:** Enable DEBUG logging.

**Request:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "customer@example.com",
    "password": "password123"
  }' \
  -v
```

**Check Logs:**
```bash
grep -i "customer@example.com" application.log
```

**✅ PASS Criteria:**
- Email NOT found in logs
- Only request paths logged
- No PII exposure

---

### Test 5.2: No JWT Content in Logs

**Request:**
```bash
curl -X GET http://localhost:8080/api/v1/orders \
  -H "Authorization: Bearer eyJhbGc..." \
  -v
```

**Check Logs:**
```bash
grep -i "eyJhbGc" application.log
```

**✅ PASS Criteria:**
- JWT token NOT logged
- Only exception types logged
- No sensitive data in logs

---

## 6. Public Endpoint Testing

### Test 6.1: Auth Endpoints Accessible

**Request:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"pass"}' \
  -v
```

**✅ PASS Criteria:**
- No JWT required
- Request reaches controller (may return 404 if not implemented)
- No 401 error

---

### Test 6.2: Public Endpoints Accessible

**Request:**
```bash
curl -X GET http://localhost:8080/api/v1/public/products \
  -v
```

**✅ PASS Criteria:**
- No JWT required
- Filter skips public endpoints
- Request processed

---

### Test 6.3: Actuator Health Check

**Request:**
```bash
curl -X GET http://localhost:8080/actuator/health \
  -v
```

**Expected Response:**
```json
{
  "status": "UP"
}
```

**✅ PASS Criteria:**
- No JWT required
- Health check works
- Status 200 OK

---

## 7. Error Message Testing

### Test 7.1: No Specific JWT Errors Exposed

**Test Matrix:**

| Token Issue | Client Message | Server Log |
|-------------|----------------|------------|
| Expired | "Authentication token expired" | "JWT expired...ExpiredJwtException" |
| Invalid signature | "Invalid authentication token" | "Invalid JWT signature...SignatureException" |
| Malformed | "Invalid authentication token" | "Malformed JWT...MalformedJwtException" |
| Wrong issuer | "Invalid authentication token" | "JWT issuer claim invalid" |
| Wrong audience | "Invalid authentication token" | "JWT audience claim invalid" |

**✅ PASS Criteria:**
- All client messages are generic
- Server logs contain details for debugging
- No info leakage to clients

---

## 8. Regression Testing

### Test 8.1: Existing Functionality Unchanged

**Test Scenarios:**
1. ✅ Login flow works
2. ✅ JWT generation includes new claims
3. ✅ Protected endpoints require auth
4. ✅ Admin endpoints require ADMIN role
5. ✅ Public endpoints work without token
6. ✅ CORS works from allowed origins
7. ✅ Error responses still JSON format

---

## 9. Integration Testing Script

**Complete test script:**

```bash
#!/bin/bash

echo "=== Security Hardening Integration Tests ==="

API_URL="http://localhost:8080"

# Test 1: CORS from allowed origin
echo "\n[Test 1] CORS - Allowed Origin"
curl -X OPTIONS $API_URL/api/v1/orders \
  -H "Origin: http://localhost:3000" \
  -s -o /dev/null -w "Status: %{http_code}\n"

# Test 2: CORS from disallowed origin
echo "\n[Test 2] CORS - Disallowed Origin"
curl -X OPTIONS $API_URL/api/v1/orders \
  -H "Origin: http://evil-site.com" \
  -s -o /dev/null -w "Status: %{http_code}\n"

# Test 3: No token provided
echo "\n[Test 3] No Token - Should 401"
curl -X GET $API_URL/api/v1/orders \
  -s -o /dev/null -w "Status: %{http_code}\n"

# Test 4: Public endpoint - no token
echo "\n[Test 4] Public Endpoint - No Token Required"
curl -X GET $API_URL/api/v1/auth/login \
  -s -o /dev/null -w "Status: %{http_code}\n"

# Test 5: Actuator health
echo "\n[Test 5] Actuator Health"
curl -X GET $API_URL/actuator/health \
  -s | jq -r '.status'

echo "\n=== Tests Complete ==="
```

---

## 10. Manual Verification Checklist

### Configuration
- [ ] `jwt.issuer` configured in application.properties
- [ ] `jwt.audience` configured in application.properties
- [ ] `security.cors.allowed-origins` configured
- [ ] Environment variables work correctly

### Security Features
- [ ] CORS `allowCredentials` is false
- [ ] JWT tokens include issuer claim
- [ ] JWT tokens include audience claim
- [ ] Invalid issuer is rejected
- [ ] Invalid audience is rejected
- [ ] SecurityContext cleared on all error paths

### Logging
- [ ] No email addresses in logs
- [ ] No phone numbers in logs
- [ ] No JWT tokens in logs
- [ ] Only exception types logged (not messages)
- [ ] Request paths logged (no PII)

### Error Messages
- [ ] Generic errors to clients
- [ ] Detailed errors in server logs
- [ ] No JWT validation specifics exposed
- [ ] Consistent error format

### Functionality
- [ ] Login works
- [ ] JWT generation works
- [ ] JWT validation works
- [ ] Public endpoints accessible
- [ ] Protected endpoints require JWT
- [ ] Admin endpoints require ADMIN role
- [ ] CORS works correctly

---

## ✅ Test Results Template

```markdown
## Security Hardening Test Results

**Date:** [Date]
**Tester:** [Name]
**Environment:** [Dev/Staging/Prod]

### Summary
- Total Tests: 25
- Passed: __
- Failed: __
- Skipped: __

### Detailed Results

#### CORS Testing
- [ ] ✅ Test 2.1: Allowed Origin - PASS
- [ ] ✅ Test 2.2: Disallowed Origin - PASS
- [ ] ✅ Test 2.3: No Credentials - PASS

#### JWT Claim Validation
- [ ] ✅ Test 3.1: Valid Token - PASS
- [ ] ✅ Test 3.2: Wrong Issuer - PASS
- [ ] ✅ Test 3.3: Wrong Audience - PASS
- [ ] ✅ Test 3.4: Expired Token - PASS
- [ ] ✅ Test 3.5: Tampered Signature - PASS

#### SecurityContext Clearing
- [ ] ✅ Test 4.1: No Partial Auth - PASS
- [ ] ✅ Test 4.2: Exception Clears Context - PASS

#### PII Protection
- [ ] ✅ Test 5.1: No Email in Logs - PASS
- [ ] ✅ Test 5.2: No JWT in Logs - PASS

#### Public Endpoints
- [ ] ✅ Test 6.1: Auth Endpoints - PASS
- [ ] ✅ Test 6.2: Public Endpoints - PASS
- [ ] ✅ Test 6.3: Actuator Health - PASS

#### Error Messages
- [ ] ✅ Test 7.1: Generic Errors - PASS

#### Regression
- [ ] ✅ Test 8.1: Existing Functionality - PASS

### Issues Found
[None / List issues]

### Sign-Off
**Status:** ✅ APPROVED FOR PRODUCTION
**Signature:** ________________
```

---

**Testing Status:** 📋 Ready for execution
**Next Step:** Run tests and verify all pass before production deployment

