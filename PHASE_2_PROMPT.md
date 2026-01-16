# 🚀 PHASE 2 IMPLEMENTATION PROMPT – PRODUCT & CATEGORY MANAGEMENT

## 📌 Context
Phase 1 (Authentication & User Management) is **COMPLETE** ✅

You are now implementing **Phase 2: Product & Category Management** for the Baby Shop Ecommerce API.

---

## 🎯 Requirements for Phase 2

### Module 2.1: Category Management

#### Business Rules
- Categories can have parent categories (hierarchical structure)
- Categories can be active/inactive
- Support soft delete
- Admin-only write operations
- Public read operations (active categories only)
- Prevent circular references in parent-child relationships

#### Endpoints Required
```
POST   /api/v1/admin/categories         [ADMIN ONLY]
GET    /api/v1/categories                [PUBLIC]
GET    /api/v1/categories/{id}           [PUBLIC]
PUT    /api/v1/admin/categories/{id}     [ADMIN ONLY]
DELETE /api/v1/admin/categories/{id}     [ADMIN ONLY - Soft delete]
GET    /api/v1/categories/{id}/children  [PUBLIC - Get subcategories]
```

#### Category Entity Fields
```java
- id (UUID, primary key)
- name (String, required, unique)
- slug (String, required, unique, URL-friendly)
- description (Text, optional)
- parentId (UUID, optional - for hierarchy)
- imageUrl (String, optional - category image)
- displayOrder (Integer, for sorting)
- isActive (Boolean, default true)
- isDeleted (Boolean, default false)
- metaTitle (String, SEO)
- metaDescription (Text, SEO)
- metaKeywords (String, SEO)
- createdAt, updatedAt, createdBy, updatedBy (Audit fields)
```

#### DTOs Required
```java
// Request DTOs
- CreateCategoryRequest (name, slug, description, parentId, imageUrl, displayOrder)
- UpdateCategoryRequest (all fields optional except slug validation)

// Response DTOs
- CategoryResponse (all fields, exclude soft delete flag)
- CategoryListResponse (id, name, slug, imageUrl, productCount)
- CategoryTreeResponse (hierarchical with children)
```

#### Validation Rules
```
Name: 
  - Required
  - 3-100 characters
  - Unique (case-insensitive)

Slug:
  - Required
  - Lowercase, alphanumeric with hyphens
  - Pattern: ^[a-z0-9]+(?:-[a-z0-9]+)*$
  - Unique
  - Auto-generate if not provided (from name)

Parent Category:
  - Must exist and be active
  - Cannot be the category itself (prevent self-reference)
  - Cannot create circular reference

Image URL:
  - Valid URL format
  - Max 500 characters
```

---

### Module 2.2: Product Management

#### Business Rules
- Products belong to one category
- Products have stock quantity tracking
- Product status: ACTIVE, OUT_OF_STOCK, DISABLED
- Price must be positive
- Support multiple images per product
- Track stock quantity changes
- Admin-only write operations
- Public can view ACTIVE products only
- Snapshot price at order time (design for this)

#### Endpoints Required
```
// Admin Endpoints
POST   /api/v1/admin/products                    [ADMIN ONLY]
PUT    /api/v1/admin/products/{id}               [ADMIN ONLY]
DELETE /api/v1/admin/products/{id}               [ADMIN ONLY - Soft delete]
PATCH  /api/v1/admin/products/{id}/stock         [ADMIN ONLY - Update stock]
PATCH  /api/v1/admin/products/{id}/status        [ADMIN ONLY - Change status]

// Public Endpoints
GET    /api/v1/products                          [PUBLIC - With filters, pagination]
GET    /api/v1/products/{id}                     [PUBLIC]
GET    /api/v1/products/category/{categoryId}    [PUBLIC]
GET    /api/v1/products/featured                 [PUBLIC - Featured products]
GET    /api/v1/products/search?q={query}         [PUBLIC - Search by name/description]
```

#### Product Entity Fields
```java
- id (UUID, primary key)
- name (String, required)
- slug (String, required, unique)
- description (Text, required)
- shortDescription (String, max 500 chars)
- price (BigDecimal, required, min 0.01)
- compareAtPrice (BigDecimal, optional - for discount display)
- costPrice (BigDecimal, optional - for profit calculation)
- sku (String, unique, optional)
- barcode (String, optional)
- stockQuantity (Integer, default 0)
- lowStockThreshold (Integer, default 10)
- status (Enum: ACTIVE, OUT_OF_STOCK, DISABLED)
- categoryId (UUID, required, foreign key)
- imageUrl (String, primary product image)
- weightGrams (Integer, for shipping calculation)
- featured (Boolean, default false)
- tags (String, comma-separated for search)
- isDeleted (Boolean, default false)
- createdAt, updatedAt, createdBy, updatedBy (Audit fields)
```

#### Product Images (Separate Table)
```java
- id (UUID, primary key)
- productId (UUID, foreign key)
- imageUrl (String, required)
- displayOrder (Integer)
- isDeleted (Boolean)
```

#### Product Status Enum
```java
public enum ProductStatus {
    ACTIVE,        // Available for sale
    OUT_OF_STOCK,  // Temporarily unavailable
    DISABLED       // Admin disabled (not visible to customers)
}
```

#### DTOs Required
```java
// Request DTOs
- CreateProductRequest (name, description, price, categoryId, sku, stockQuantity, imageUrl, tags)
- UpdateProductRequest (all fields optional)
- UpdateStockRequest (quantity, operation: ADD/SUBTRACT/SET)

// Response DTOs
- ProductResponse (all product details, category info, images)
- ProductListResponse (id, name, slug, price, imageUrl, status, stockQuantity)
- ProductDetailResponse (full details + related products suggestions)

// Filter/Search DTOs
- ProductFilterRequest (categoryId, minPrice, maxPrice, status, featured, search query)
```

#### Validation Rules
```
Name:
  - Required
  - 3-200 characters

Slug:
  - Auto-generated from name if not provided
  - Lowercase, alphanumeric with hyphens
  - Unique

Price:
  - Required
  - Min: 0.01
  - Max: 999999.99
  - 2 decimal places

Stock Quantity:
  - Min: 0
  - Default: 0

Category:
  - Must exist and be active
  - Cannot delete category with products (soft delete)

Images:
  - Valid URL format
  - Max 10 images per product
```

---

## 🔐 Security Requirements

### Authorization Rules
```java
// Category Management
- View categories: PUBLIC (authenticated or not)
- Create/Update/Delete categories: ADMIN ONLY

// Product Management
- View products: PUBLIC (authenticated or not)
- Create/Update/Delete products: ADMIN ONLY
- Update stock: ADMIN ONLY
- Change status: ADMIN ONLY
```

### Implementation Requirements
- Use `@PreAuthorize("hasRole('ADMIN')")` for admin endpoints
- Validate category/product existence before operations
- Prevent exposing deleted items to public
- Log all admin operations (create, update, delete)
- Validate user permissions in service layer (defense in depth)

---

## 📦 Package Structure Required

```
com.babyshop.api/
├── category/
│   ├── controller/
│   │   ├── CategoryController.java       [PUBLIC endpoints]
│   │   └── AdminCategoryController.java  [ADMIN endpoints]
│   ├── service/
│   │   └── CategoryService.java
│   ├── repository/
│   │   └── CategoryRepository.java
│   ├── entity/
│   │   └── Category.java
│   └── dto/
│       ├── CreateCategoryRequest.java
│       ├── UpdateCategoryRequest.java
│       ├── CategoryResponse.java
│       ├── CategoryListResponse.java
│       └── CategoryTreeResponse.java
│
├── product/
│   ├── controller/
│   │   ├── ProductController.java        [PUBLIC endpoints]
│   │   └── AdminProductController.java   [ADMIN endpoints]
│   ├── service/
│   │   ├── ProductService.java
│   │   └── ProductImageService.java
│   ├── repository/
│   │   ├── ProductRepository.java
│   │   └── ProductImageRepository.java
│   ├── entity/
│   │   ├── Product.java
│   │   ├── ProductImage.java
│   │   └── ProductStatus.java [Enum]
│   └── dto/
│       ├── CreateProductRequest.java
│       ├── UpdateProductRequest.java
│       ├── UpdateStockRequest.java
│       ├── ProductResponse.java
│       ├── ProductListResponse.java
│       ├── ProductDetailResponse.java
│       └── ProductFilterRequest.java
```

---

## 🎯 Implementation Guidelines

### Pagination & Sorting
```java
// Use Spring Data Pageable
GET /api/v1/products?page=0&size=20&sort=name,asc

Response format:
{
  "success": true,
  "data": {
    "content": [...],
    "page": 0,
    "size": 20,
    "totalElements": 150,
    "totalPages": 8,
    "last": false
  }
}
```

### Slug Generation
```java
// Auto-generate URL-friendly slug from name
"Baby Shoes - Red" → "baby-shoes-red"
"Toys & Games" → "toys-games"
```

### Stock Management
```java
// Stock update operations
- SET: Set absolute quantity
- ADD: Increase stock (restock)
- SUBTRACT: Decrease stock (manual adjustment)

// Auto status update:
- If stockQuantity == 0 → Status = OUT_OF_STOCK
- If stockQuantity > 0 and status == OUT_OF_STOCK → Status = ACTIVE
```

### Category Hierarchy
```java
// Prevent circular reference
Before setting parentId, validate:
1. Parent exists and is active
2. Parent is not the same category
3. Parent's ancestors don't include current category

// Get category tree
Return nested structure with children
```

---

## 🧪 Testing Requirements

After implementation, verify:

### Category Tests
1. ✅ Create root category (no parent)
2. ✅ Create child category
3. ✅ Get category tree
4. ✅ Update category
5. ✅ Soft delete category
6. ✅ Prevent circular reference
7. ✅ Public can view only active categories
8. ✅ Admin can create/update/delete

### Product Tests
1. ✅ Create product with valid category
2. ✅ Update product
3. ✅ Update stock (ADD, SUBTRACT, SET)
4. ✅ Change status
5. ✅ Soft delete product
6. ✅ Get products with pagination
7. ✅ Filter by category, price range, status
8. ✅ Search by name
9. ✅ Get featured products
10. ✅ Public can view only ACTIVE products
11. ✅ Admin can manage all products

---

## 📝 Deliverables

1. All entity classes
2. All DTOs (using records)
3. All repositories
4. All services with business logic
5. All controllers (public + admin)
6. Validation annotations
7. Brief design decision explanations
8. ProductStatus enum
9. Slug generation utility

---

## 🎯 Success Criteria

- [x] All endpoints working
- [x] Pagination implemented
- [x] Filtering and search working
- [x] Admin-only endpoints secured
- [x] Stock management functional
- [x] Category hierarchy working
- [x] No circular references
- [x] Soft delete implemented
- [x] Validation working
- [x] No business logic in controllers
- [x] All DTOs using records
- [x] Proper transaction management
- [x] Logging implemented

---

**Start implementing Phase 2 now. Follow all SOLID principles, security best practices, and maintain the same code quality as Phase 1.**

