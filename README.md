# Baby Shop E-commerce API

**Production-ready, secure e-commerce backend for Baby Shop Bangladesh**

## 🚀 Tech Stack

- **Backend:** Java 21, Spring Boot 4.x
- **Database:** PostgreSQL 16
- **Cache:** Redis 7
- **Authentication:** JWT-based Spring Security
- **Database Migration:** Flyway
- **Build Tool:** Gradle
- **Containerization:** Docker (multi-stage, non-root)

## 🔒 Security Features

- ✅ JWT-based stateless authentication
- ✅ BCrypt password hashing (strength 12)
- ✅ Role-based access control (RBAC)
- ✅ CORS protection (environment-specific)
- ✅ SQL injection prevention (JPA parameterized queries)
- ✅ Soft delete for data integrity
- ✅ Comprehensive audit logging
- ✅ Input validation at all layers
- ✅ No sensitive data in logs
- ✅ Non-root Docker container

## 📁 Project Structure

```
src/main/java/com/babyshop/api/
├── common/                 # Shared components
│   ├── controller/        # Common controllers (health, etc.)
│   ├── dto/              # Common DTOs (ApiResponse, ApiError)
│   ├── entity/           # Base entity classes
│   └── exception/        # Global exception handling
├── config/               # Configuration classes
│   ├── SecurityConfig.java
│   ├── JpaAuditingConfig.java
│   └── CacheConfig.java
├── security/             # Security components
│   ├── JwtService.java
│   └── JwtAuthenticationFilter.java
└── user/                 # User module (package-by-feature)
    ├── entity/
    ├── repository/
    ├── service/
    └── controller/
```

## 🛠️ Prerequisites

- **Java 21** (OpenJDK or Eclipse Temurin)
- **Docker** & **Docker Compose**
- **Gradle** (included via wrapper)

## 🏃 Quick Start

### Option 1: Using Docker Compose (Recommended)

```powershell
# Start all services (PostgreSQL, Redis, API)
docker-compose up -d

# View logs
docker-compose logs -f api

# Stop all services
docker-compose down

# Stop and remove volumes (clean slate)
docker-compose down -v
```

The API will be available at: `http://localhost:8080`

### Option 2: Local Development

1. **Start dependencies**
```powershell
# Start only PostgreSQL and Redis
docker-compose up -d postgres redis
```

2. **Set environment variables**
```powershell
$env:SPRING_PROFILES_ACTIVE="dev"
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/babyshop_db"
$env:SPRING_DATASOURCE_USERNAME="babyshop_user"
$env:SPRING_DATASOURCE_PASSWORD="babyshop_password_dev_only"
$env:JWT_SECRET="dev_jwt_secret_change_in_production_must_be_at_least_256_bits_long_for_HS256_algorithm"
$env:REDIS_PASSWORD="redis_password_dev_only"
```

3. **Run the application**
```powershell
# Using Gradle wrapper
.\gradlew bootRun

# Or build JAR and run
.\gradlew bootJar
java -jar build\libs\babyshop-api-0.0.1-SNAPSHOT.jar
```

## 🧪 Testing

```powershell
# Run all tests
.\gradlew test

# Run with coverage
.\gradlew test jacocoTestReport

# Run specific test
.\gradlew test --tests "UserServiceTest"
```

## 📊 Database Migrations

Flyway migrations are located in `src/main/resources/db/migration/`

- `V1__Initial_Schema.sql` - Base schema with audit fields, users, products, orders, etc.

Migrations run automatically on application startup.

## 🔐 Environment Variables

### Required in Production

| Variable | Description | Example |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Active profile | `prod` |
| `SPRING_DATASOURCE_URL` | Database URL | `jdbc:postgresql://db:5432/babyshop_db` |
| `SPRING_DATASOURCE_USERNAME` | DB username | `babyshop_user` |
| `SPRING_DATASOURCE_PASSWORD` | DB password | `<strong-password>` |
| `JWT_SECRET` | JWT signing key (256+ bits) | `<base64-encoded-secret>` |
| `JWT_EXPIRATION` | Access token TTL (ms) | `3600000` (1 hour) |
| `JWT_REFRESH_EXPIRATION` | Refresh token TTL (ms) | `86400000` (24 hours) |
| `REDIS_HOST` | Redis host | `redis` |
| `REDIS_PASSWORD` | Redis password | `<strong-password>` |
| `CORS_ALLOWED_ORIGINS` | Allowed frontend origins | `https://babyshop.com.bd` |

### Optional

| Variable | Description | Default |
|----------|-------------|---------|
| `SERVER_PORT` | API port | `8080` |
| `SSL_KEY_STORE_PATH` | SSL keystore path | - |
| `SSL_KEY_STORE_PASSWORD` | SSL keystore password | - |

## 🌍 API Endpoints

### Public Endpoints (No Auth Required)

- `GET /api/v1/public/health` - Health check
- `GET /api/v1/public/info` - Service info
- `POST /api/v1/auth/register` - User registration (TODO)
- `POST /api/v1/auth/login` - User login (TODO)
- `GET /api/v1/products/**` - Browse products (TODO)
- `GET /api/v1/categories/**` - Browse categories (TODO)

### Protected Endpoints (Auth Required)

- `GET /api/v1/users/me` - Get current user (TODO)
- `POST /api/v1/cart` - Add to cart (TODO)
- `POST /api/v1/orders` - Create order (TODO)

### Admin Endpoints

- `POST /api/v1/admin/products` - Create product (TODO)
- `GET /api/v1/admin/orders` - Manage orders (TODO)

## 🔍 Monitoring & Health

- **Health Check:** `GET /actuator/health`
- **Liveness Probe:** `GET /actuator/health/liveness`
- **Readiness Probe:** `GET /actuator/health/readiness`

## 🐛 Debugging

```powershell
# Enable debug logging
$env:LOGGING_LEVEL_COM_BABYSHOP_API="DEBUG"

# View SQL queries
$env:LOGGING_LEVEL_ORG_HIBERNATE_SQL="DEBUG"

# Spring Security debug
$env:LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_SECURITY="DEBUG"
```

## 📝 Development Guidelines

### Code Style
- Follow Java naming conventions
- Use Lombok to reduce boilerplate
- Package-by-feature structure
- Comprehensive JavaDoc for public APIs

### Security Rules
1. ❌ Never commit secrets to Git
2. ❌ Never trust client-side data
3. ❌ Never expose internal IDs (use UUIDs)
4. ✅ Always validate input
5. ✅ Always use parameterized queries
6. ✅ Always log security events (without sensitive data)

### Testing Rules
- Unit tests for services
- Integration tests for repositories
- Controller tests with MockMvc
- Minimum 80% code coverage

## 🚢 Deployment

### Build Docker Image

```powershell
# Build image
docker build -t babyshop-api:latest .

# Run container
docker run -p 8080:8080 `
  -e SPRING_PROFILES_ACTIVE=prod `
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host:5432/db `
  -e SPRING_DATASOURCE_USERNAME=user `
  -e SPRING_DATASOURCE_PASSWORD=pass `
  -e JWT_SECRET=your-secret `
  babyshop-api:latest
```

### Production Checklist

- [ ] Change all default passwords
- [ ] Generate strong JWT secret (256+ bits)
- [ ] Configure SSL/TLS certificates
- [ ] Set up database backups
- [ ] Configure monitoring and alerting
- [ ] Set up log aggregation
- [ ] Review and restrict CORS origins
- [ ] Enable rate limiting
- [ ] Review security headers
- [ ] Disable debug logging
- [ ] Set up CI/CD pipeline

## 📚 Next Steps

1. ✅ **Foundation Setup** (COMPLETED)
   - Database schema
   - Security configuration
   - Base entities and DTOs
   - Exception handling

2. 🔄 **Authentication Module** (IN PROGRESS)
   - User registration
   - Login/logout
   - JWT refresh
   - Email verification
   - Password reset

3. 📦 **Product Management**
   - Product CRUD
   - Category management
   - Image upload
   - Inventory tracking

4. 🛒 **Shopping Cart & Checkout**
   - Cart management
   - Order creation
   - Address management

5. 💰 **Payment Integration**
   - SSLCommerz integration
   - Payment verification
   - Order confirmation

6. 📊 **Admin Panel APIs**
   - Dashboard
   - Order management
   - User management
   - Analytics

## 🤝 Contributing

This is a private project. For questions or suggestions, contact the development team.

## 📄 License

Proprietary - All rights reserved

---

**Built with ❤️ for Baby Shop Bangladesh**

