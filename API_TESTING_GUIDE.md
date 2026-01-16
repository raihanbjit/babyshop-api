# 🚀 Baby Shop API - Quick Start & Testing Guide

## ✅ Current Application Status

**Application is RUNNING successfully on port 8080!**

- Process ID: Running as Java process
- Memory Usage: ~306 MB
- Database: Connected to PostgreSQL on port 5434
- Status: All core services initialized

---

## 📋 Available API Endpoints

### Public Endpoints (No Authentication Required)

#### 1. Health Check
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/public/health" -Method Get
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Request processed successfully",
  "data": {
    "status": "UP",
    "timestamp": "2026-01-16T...",
    "service": "babyshop-api",
    "version": "0.0.1-SNAPSHOT"
  }
}
```

#### 2. API Info
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/public/info" -Method Get
```

#### 3. Spring Actuator Health
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/actuator/health" -Method Get
```

---

## 🔐 Authentication Endpoints

### 1. User Registration

**Endpoint:** `POST /api/v1/auth/register`

**PowerShell Test:**
```powershell
$registerBody = @{
    email = "john.doe@example.com"
    password = "SecurePass123!@#"
    firstName = "John"
    lastName = "Doe"
    phoneNumber = "+8801712345678"
} | ConvertTo-Json

$registerResponse = Invoke-RestMethod `
    -Uri "http://localhost:8080/api/v1/auth/register" `
    -Method Post `
    -ContentType "application/json" `
    -Body $registerBody

Write-Host "Registration Successful!" -ForegroundColor Green
$registerResponse | ConvertTo-Json -Depth 5
```

**Expected Response:**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 86400000,
    "user": {
      "id": "uuid-here",
      "email": "john.doe@example.com",
      "firstName": "John",
      "lastName": "Doe",
      "phoneNumber": "+8801712345678",
      "role": "CUSTOMER",
      "isActive": true,
      "emailVerified": false
    }
  }
}
```

### 2. User Login

**Endpoint:** `POST /api/v1/auth/login`

**PowerShell Test:**
```powershell
$loginBody = @{
    email = "john.doe@example.com"
    password = "SecurePass123!@#"
} | ConvertTo-Json

$loginResponse = Invoke-RestMethod `
    -Uri "http://localhost:8080/api/v1/auth/login" `
    -Method Post `
    -ContentType "application/json" `
    -Body $loginBody

# Save the token for authenticated requests
$token = $loginResponse.data.token
Write-Host "Login Successful! Token saved." -ForegroundColor Green
Write-Host "Token: $token"
```

---

## 🔒 Authenticated Endpoints (Require JWT Token)

### Get User Profile

**Endpoint:** `GET /api/v1/users/profile`

**PowerShell Test:**
```powershell
# Make sure you have the $token variable from login
$headers = @{
    Authorization = "Bearer $token"
}

$profile = Invoke-RestMethod `
    -Uri "http://localhost:8080/api/v1/users/profile" `
    -Method Get `
    -Headers $headers

Write-Host "Profile Retrieved!" -ForegroundColor Green
$profile | ConvertTo-Json -Depth 3
```

---

## 🧪 Complete Test Workflow

Run this complete script to test the entire authentication flow:

```powershell
Write-Host "`n=== Baby Shop API Test Suite ===`n" -ForegroundColor Cyan

# Test 1: Health Check
Write-Host "1. Testing Health Endpoint..." -ForegroundColor Yellow
try {
    $health = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/public/health" -Method Get
    Write-Host "   ✅ Health Check: $($health.data.status)" -ForegroundColor Green
} catch {
    Write-Host "   ❌ Health Check Failed" -ForegroundColor Red
}

# Test 2: Register New User
Write-Host "`n2. Testing User Registration..." -ForegroundColor Yellow
$timestamp = Get-Date -Format "HHmmss"
$registerBody = @{
    email = "testuser$timestamp@example.com"
    password = "Test123!@#"
    firstName = "Test"
    lastName = "User$timestamp"
    phoneNumber = "+880171234$timestamp"
} | ConvertTo-Json

try {
    $registerResponse = Invoke-RestMethod `
        -Uri "http://localhost:8080/api/v1/auth/register" `
        -Method Post `
        -ContentType "application/json" `
        -Body $registerBody
    
    Write-Host "   ✅ User Registered Successfully" -ForegroundColor Green
    Write-Host "   User ID: $($registerResponse.data.user.id)" -ForegroundColor Cyan
    Write-Host "   Email: $($registerResponse.data.user.email)" -ForegroundColor Cyan
    
    # Save credentials for login test
    $testEmail = $registerResponse.data.user.email
    $testPassword = "Test123!@#"
    $firstToken = $registerResponse.data.token
    
} catch {
    Write-Host "   ❌ Registration Failed: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.ErrorDetails.Message) {
        $errorDetails = $_.ErrorDetails.Message | ConvertFrom-Json
        Write-Host "   Error: $($errorDetails.message)" -ForegroundColor Red
    }
}

# Test 3: Login
Write-Host "`n3. Testing User Login..." -ForegroundColor Yellow
$loginBody = @{
    email = $testEmail
    password = $testPassword
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod `
        -Uri "http://localhost:8080/api/v1/auth/login" `
        -Method Post `
        -ContentType "application/json" `
        -Body $loginBody
    
    Write-Host "   ✅ Login Successful" -ForegroundColor Green
    $token = $loginResponse.data.token
    Write-Host "   JWT Token: $($token.Substring(0, 50))..." -ForegroundColor Cyan
    
} catch {
    Write-Host "   ❌ Login Failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Test 4: Get Profile (Authenticated)
Write-Host "`n4. Testing Get Profile (Authenticated)..." -ForegroundColor Yellow
$headers = @{
    Authorization = "Bearer $token"
}

try {
    $profile = Invoke-RestMethod `
        -Uri "http://localhost:8080/api/v1/users/profile" `
        -Method Get `
        -Headers $headers
    
    Write-Host "   ✅ Profile Retrieved" -ForegroundColor Green
    Write-Host "   Name: $($profile.data.firstName) $($profile.data.lastName)" -ForegroundColor Cyan
    Write-Host "   Email: $($profile.data.email)" -ForegroundColor Cyan
    Write-Host "   Role: $($profile.data.role)" -ForegroundColor Cyan
    
} catch {
    Write-Host "   ❌ Profile Retrieval Failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== Test Suite Complete ===`n" -ForegroundColor Cyan
```

---

## 📊 Database Verification

Check if users are being created in the database:

```powershell
$env:PGPASSWORD='1122'
& "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U postgres -h localhost -p 5434 -d babyshop_db -c "SELECT id, email, first_name, last_name, role, is_active, created_at FROM users ORDER BY created_at DESC LIMIT 5;"
```

---

## 🔍 Troubleshooting

### Check Application Logs
The application is running in the background. To see logs, check the Gradle daemon output or run:
```powershell
Get-Content .\build\tmp\bootRun.log -Tail 50 -Wait
```

### Check Application Status
```powershell
$conn = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue
if ($conn) {
    $proc = Get-Process -Id $conn.OwningProcess
    Write-Host "✅ Application Running" -ForegroundColor Green
    Write-Host "PID: $($proc.Id)" -ForegroundColor Cyan
    Write-Host "Memory: $([math]::Round($proc.WorkingSet/1MB,2)) MB" -ForegroundColor Cyan
} else {
    Write-Host "❌ Application Not Running" -ForegroundColor Red
}
```

### Stop Application
```powershell
$processId = (Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue).OwningProcess
if ($processId) {
    Stop-Process -Id $processId -Force
    Write-Host "Application stopped" -ForegroundColor Yellow
}
```

### Restart Application
```powershell
# Stop if running
$processId = (Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue).OwningProcess
if ($processId) { Stop-Process -Id $processId -Force; Start-Sleep -Seconds 2 }

# Start fresh
./gradlew bootRun
```

---

## 📝 Request/Response Examples

### Validation Error Response
```json
{
  "success": false,
  "message": "Validation failed",
  "error": {
    "code": "VALIDATION_ERROR",
    "details": "Email is required",
    "path": "/api/v1/auth/register",
    "timestamp": "2026-01-16T..."
  }
}
```

### Authentication Error Response
```json
{
  "success": false,
  "message": "Authentication failed",
  "error": {
    "code": "AUTHENTICATION_REQUIRED",
    "details": "Invalid credentials",
    "path": "/api/v1/auth/login",
    "timestamp": "2026-01-16T..."
  }
}
```

---

## 🎯 Next Steps

1. ✅ **User Authentication Module** - COMPLETE & TESTED
2. ⏳ **Product Module** - Database ready, APIs pending
3. ⏳ **Order Module** - Database ready, APIs pending
4. ⏳ **Cart Module** - Database ready, APIs pending

---

## 📞 Support

**Issues Encountered:**
1. Port 8080 in use → Run stop command above
2. Database connection errors → Verify PostgreSQL on port 5434
3. 401 Unauthorized → Check if endpoint requires authentication
4. 500 Internal Server Error → Check application logs

**Application Configuration:**
- Port: 8080
- Database: PostgreSQL 18 on port 5434
- JWT Expiration: 15 minutes (900000 ms)
- Refresh Token: 24 hours (86400000 ms)

---

**🎉 Application is ready for development and testing!**

