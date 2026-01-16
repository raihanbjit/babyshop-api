# Baby Shop Ecommerce Backend - Complete Implementation

## 📋 Overview

This document provides a complete overview of the production-ready Baby Shop Ecommerce Backend implementation following Spring Boot 3+, Spring Security 6, and clean architecture principles.

## 🏗️ Architecture

### Package Structure
```
com.babyshop.api/
├── common/                     # Shared utilities
│   ├── controller/             # Health checks, etc.
│   ├── dto/                    # ApiResponse, ApiError
│   ├── entity/                 # BaseEntity
│   └── exception/              # Global exception handling
├── config/                     # Configuration classes
│   ├── SecurityConfig.java
│   ├── JwtProperties.java
│   ├── CorsProperties.java
│   └── JpaAuditingConfig.java
├── security/                   # JWT & Security components
│   ├── JwtService.java
│   ├── JwtAuthenticationFilter.java
│   ├── JwtAuthenticationEntryPoint.java
│   └── JwtAccessDeniedHandler.java
├── user/                       # User & Authentication Module
│   ├── entity/
│   │   ├── User.java
│   │   └── UserRole.java
│   ├── dto/
│   │   ├── RegisterRequest.java
│   │   ├── LoginRequest.java
│   │   ├── AuthResponse.java
│   │   └── UserResponse.java
│   ├── repository/
│   │   └── UserRepository.java
│   ├── service/
│   │   ├── AuthService.java
│   │   ├── UserService.java
│   │   └── CustomUserDetailsService.java
│   └── controller/
│       ├── AuthController.java
│       └── UserController.java
├── product/                    # Product Module
│   ├── entity/
│   │   ├── Category.java
│   │   ├── Product.java
│   │   └── ProductStatus.java
│   ├── dto/
│   │   ├── CategoryRequest.java
│   │   ├── CategoryResponse.java
│   │   ├── ProductRequest.java
│   │   └── ProductResponse.java
│   ├── repository/
│   │   ├── CategoryRepository.java
│   │   └── ProductRepository.java
│   ├── service/
│   │   ├── CategoryService.java
│   │   └── ProductService.java
│   └── controller/
│       ├── CategoryController.java          # Public
│       ├── AdminCategoryController.java     # Admin
│       ├── ProductController.java           # Public
│       └── AdminProductController.java      # Admin
└── order/                      # Order Module
    ├── entity/
    │   ├── Order.java
    │   ├── OrderItem.java
    │   ├── OrderStatus.java
    │   └── PaymentMethod.java
    ├── dto/
    │   ├── CreateOrderRequest.java
    │   ├── OrderItemRequest.java
    │   ├── OrderResponse.java
    │   └── OrderItemResponse.java
    ├── repository/
    │   ├── OrderRepository.java
    │   └── OrderItemRepository.java
    ├── service/
    │   └── OrderService.java
    └── controller/
        ├── OrderController.java             # Customer
        └── AdminOrderController.java        # Admin
```

## 🔐 Security Implementation

### JWT-Based Authentication
- **Stateless**: No server-side sessions
- **Token Expiry**: Configurable via `application.properties`
- **Role-Based Access Control**: CUSTOMER, ADMIN, SUPER_ADMIN
- **Method-Level Security**: Using `@PreAuthorize`

### Password Security
- **BCrypt** with default strength (10 rounds)
- Password validation: min 8 chars, must contain digit, lowercase, uppercase, special character

### API Security Matrix

| Endpoint | Public | Customer | Admin |
|----------|--------|----------|-------|
| POST /api/v1/auth/register | ✅ | ✅ | ✅ |
| POST /api/v1/auth/login | ✅ | ✅ | ✅ |
| GET /api/v1/users/me | ❌ | ✅ | ✅ |
| GET /api/v1/products/** | ✅ | ✅ | ✅ |
| GET /api/v1/categories/** | ✅ | ✅ | ✅ |
| POST /api/v1/orders | ❌ | ✅ | ✅ |
| GET /api/v1/orders | ❌ | ✅ (own) | ✅ |
| POST /api/v1/admin/** | ❌ | ❌ | ✅ |

## 📦 Module Details

### 1️⃣ User & Authentication Module

#### Features
- ✅ User registration with validation
- ✅ Email uniqueness check
- ✅ Secure password hashing (BCrypt)
- ✅ JWT token generation
- ✅ Login with email/password
- ✅ Role-based access control
- ✅ User profile retrieval

#### Entities
- **User**: Email, password, name, phone, role, active status
- **UserRole**: CUSTOMER, ADMIN, SUPER_ADMIN

#### DTOs
- **RegisterRequest**: Java record with validation
- **LoginRequest**: Email + password
- **AuthResponse**: Token + user info
- **UserResponse**: Safe user data (no password)

#### Key Design Decisions
1. **Records for DTOs**: Immutable, concise, null-safe
2. **No entity exposure**: Always use DTOs in responses
3. **Email as username**: Simpler for users
4. **Soft delete**: User data retained for audit

### 2️⃣ Product Module

#### Features
- ✅ Category management (CRUD)
- ✅ Product management (CRUD)
- ✅ Product status: ACTIVE, OUT_OF_STOCK, DISABLED
- ✅ Stock quantity tracking
- ✅ Auto slug generation
- ✅ Pagination & sorting
- ✅ Product search
- ✅ Featured products
- ✅ Public browsing / Admin management separation

#### Entities
- **Category**: Name, slug, description, image, active status
- **Product**: Name, price, SKU, stock, status, category

#### Stock Management
```java
// Atomic stock reduction (used by order service)
productService.reduceStock(productId, quantity);

// Auto status update on stock change
if (quantity == 0) {
    status = OUT_OF_STOCK;
} else if (quantity > 0 && status == OUT_OF_STOCK) {
    status = ACTIVE;
}
```

#### Key Design Decisions
1. **Slug generation**: URL-friendly, unique identifiers
2. **Lazy loading**: Category loaded only when needed
3. **Stock validation**: Prevents negative stock
4. **Price snapshot**: Order items store price at purchase time

### 3️⃣ Order Module (Cash on Delivery)

#### Features
- ✅ Create order with validation
- ✅ Cash on Delivery payment
- ✅ Order status lifecycle: CREATED → CONFIRMED → COMPLETED
- ✅ Order cancellation (restores stock)
- ✅ Price snapshot (prevents manipulation)
- ✅ Atomic stock reduction
- ✅ Order ownership validation
- ✅ Shipping address capture
- ✅ Admin order management

#### Entities
- **Order**: Reference, user, status, payment method, totals, shipping address
- **OrderItem**: Product snapshot, quantity, unit price, subtotal

#### Order Lifecycle
```
CREATED (Customer creates order)
   ↓
CONFIRMED (Admin confirms)
   ↓
COMPLETED (Order delivered)

CANCELLED (Can cancel from CREATED or CONFIRMED)
```

#### Price Snapshot Pattern
```java
// CRITICAL: Price is captured at order time
OrderItem orderItem = OrderItem.builder()
    .productName(product.getName())
    .unitPrice(product.getPrice())  // SNAPSHOT HERE
    .quantity(quantity)
    .build();
```

**Why?**
- Product prices may change after order
- Order total must remain immutable
- Prevents frontend price manipulation
- Audit trail for disputes

#### Payment Readiness
Current implementation:
- Only `CASH_ON_DELIVERY`
- Order stores: `totalAmount`, `currency`, `orderReference`

Future integration:
- Add payment gateway enum values
- Add `paymentStatus` field
- Add `paymentTransactionId` field
- Keep order logic decoupled

#### Key Design Decisions
1. **Order reference**: Human-readable (ORD-12345678)
2. **Ownership validation**: Users can only access their orders
3. **Stock restoration**: On cancellation, stock is returned
4. **Admin separation**: Different endpoints for admin vs customer

## 🗄️ Database Schema

### Tables Created (V2 Migration)
1. **categories**: Product categorization
2. **products**: Product catalog with stock
3. **orders**: Order header with shipping
4. **order_items**: Order line items with price snapshot

### Indexes
- Slug fields (fast lookups)
- Foreign keys (join performance)
- Status fields (filtering)
- Soft delete flags

## 🔧 Configuration

### Required Properties
```properties
# JWT
jwt.secret=your-secret-key-here
jwt.expiration=86400000

# CORS
cors.allowed-origins=http://localhost:3000

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/babyshop
spring.datasource.username=postgres
spring.datasource.password=password
```

## 🚀 API Endpoints

### Authentication
```
POST   /api/v1/auth/register      # Register new user
POST   /api/v1/auth/login         # Login and get JWT
GET    /api/v1/users/me           # Get current user profile
```

### Products (Public)
```
GET    /api/v1/products                    # List active products
GET    /api/v1/products/{id}               # Get product details
GET    /api/v1/products/slug/{slug}        # Get by slug
GET    /api/v1/products/category/{id}     # Products by category
GET    /api/v1/products/featured           # Featured products
GET    /api/v1/products/search?keyword=   # Search products
```

### Products (Admin)
```
GET    /api/v1/admin/products              # All products (including inactive)
POST   /api/v1/admin/products              # Create product
PUT    /api/v1/admin/products/{id}         # Update product
PATCH  /api/v1/admin/products/{id}/status  # Update status
PATCH  /api/v1/admin/products/{id}/stock   # Update stock
DELETE /api/v1/admin/products/{id}         # Delete product
```

### Categories (Public)
```
GET    /api/v1/categories                  # Active categories
GET    /api/v1/categories/{id}             # Get category
GET    /api/v1/categories/slug/{slug}      # Get by slug
```

### Categories (Admin)
```
GET    /api/v1/admin/categories            # All categories
POST   /api/v1/admin/categories            # Create category
PUT    /api/v1/admin/categories/{id}       # Update category
PATCH  /api/v1/admin/categories/{id}/toggle-status
DELETE /api/v1/admin/categories/{id}       # Delete category
```

### Orders (Customer)
```
POST   /api/v1/orders                      # Create order
GET    /api/v1/orders                      # My orders
GET    /api/v1/orders/{id}                 # Order details
GET    /api/v1/orders/reference/{ref}      # Get by reference
GET    /api/v1/orders/status/{status}      # Filter by status
POST   /api/v1/orders/{id}/cancel          # Cancel order
```

### Orders (Admin)
```
GET    /api/v1/admin/orders                # All orders
GET    /api/v1/admin/orders/{id}           # Order details
GET    /api/v1/admin/orders/status/{status}
POST   /api/v1/admin/orders/{id}/confirm   # Confirm order
POST   /api/v1/admin/orders/{id}/complete  # Complete order
POST   /api/v1/admin/orders/{id}/cancel    # Cancel order
```

## 📊 Response Format

All APIs return standardized responses:

### Success Response
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... },
  "timestamp": "2026-01-15T10:30:00Z"
}
```

### Error Response
```json
{
  "success": false,
  "message": "Error message",
  "error": {
    "code": "ERROR_CODE",
    "details": "Detailed message",
    "path": "/api/v1/...",
    "validationErrors": {
      "field": "error message"
    }
  },
  "timestamp": "2026-01-15T10:30:00Z"
}
```

## 🛡️ Security Best Practices Implemented

1. ✅ **No business logic in controllers**: All logic in service layer
2. ✅ **DTO separation**: Never expose entities in APIs
3. ✅ **Price snapshot**: Order items store prices at order time
4. ✅ **Ownership validation**: Users can only access their own orders
5. ✅ **Stock atomicity**: Transactional stock updates
6. ✅ **Soft delete**: Data retained for audit
7. ✅ **Input validation**: Jakarta Validation on all DTOs
8. ✅ **Method-level security**: @PreAuthorize on sensitive operations
9. ✅ **Exception handling**: Global handler with structured responses
10. ✅ **Audit fields**: CreatedBy, UpdatedBy tracked automatically

## 🧪 Testing Recommendations

### Unit Tests
- Service layer logic
- DTO validation
- Entity business methods

### Integration Tests
- Controller endpoints
- Database operations
- Security rules

### Test Data
Use the existing test infrastructure and create:
- Test users (CUSTOMER, ADMIN roles)
- Test categories and products
- Test order scenarios

## 🔮 Future Enhancements (Not Implemented)

### Payment Gateway Integration
When ready:
1. Add `PaymentStatus` enum
2. Add `paymentTransactionId` to Order
3. Add payment webhook endpoints
4. Update order status based on payment
5. Add refund handling

### Additional Features
- Product reviews & ratings
- Wishlist
- Shopping cart persistence
- Coupon codes & discounts
- Email notifications
- Inventory alerts
- Order tracking
- Multi-image support
- Product variants (size, color)

## 📝 Notes

### Why Records for DTOs?
- **Immutable by default**: Thread-safe, predictable
- **Concise syntax**: Less boilerplate than Lombok
- **Null-safe**: Explicit handling
- **Modern Java**: Best practice in Java 17+

### Why Separate Controllers?
- **Clear separation**: Public vs Admin operations
- **Security**: Different authorization rules
- **API documentation**: Clearer for consumers
- **Evolution**: Easier to version independently

### Why Price Snapshot?
- **Data integrity**: Order totals never change
- **Security**: Prevents price manipulation
- **Audit**: Historical record of what customer paid
- **Business logic**: Promotions, taxes calculated at order time

## ✅ Checklist

- [x] User registration & authentication
- [x] JWT-based security
- [x] Role-based access control
- [x] Category CRUD
- [x] Product CRUD
- [x] Stock management
- [x] Order creation
- [x] Order lifecycle management
- [x] Price snapshot pattern
- [x] Ownership validation
- [x] Global exception handling
- [x] DTO validation
- [x] Database migrations
- [x] Clean architecture
- [x] Production-ready code

## 🚀 Next Steps

1. **Build the project**: `./gradlew build`
2. **Run migrations**: Flyway will auto-run on startup
3. **Start application**: `./gradlew bootRun`
4. **Test endpoints**: Use Postman/Insomnia
5. **Create admin user**: Use registration API, then update role in DB
6. **Add test data**: Categories → Products → Orders

## 📚 Key Files Reference

| Component | File Path |
|-----------|-----------|
| Security Config | `config/SecurityConfig.java` |
| JWT Service | `security/JwtService.java` |
| Auth Controller | `user/controller/AuthController.java` |
| Product Service | `product/service/ProductService.java` |
| Order Service | `order/service/OrderService.java` |
| Global Exception Handler | `common/exception/GlobalExceptionHandler.java` |
| Database Migration | `resources/db/migration/V2__Create_Product_And_Order_Tables.sql` |

---

**Implementation Date**: January 15, 2026  
**Spring Boot Version**: 3.x  
**Java Version**: 21  
**Security**: Spring Security 6 with JWT

