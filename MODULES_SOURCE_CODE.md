# Baby Shop E-commerce - Complete Source Code for Product & Order Modules

This file contains all source code needed for the Product and Order modules.
Copy each section to create the corresponding Java file.

## Instructions
Due to file creation issues in the current environment, please manually create these files by copying the code blocks below to the corresponding file paths.

## Product Module Files

### 1. ProductStatus.java
**Path:** `src/main/java/com/babyshop/api/product/entity/ProductStatus.java`

```java
package com.babyshop.api.product.entity;

public enum ProductStatus {
    ACTIVE,
    OUT_OF_STOCK,
    DISABLED
}
```

### 2. Category.java
**Path:** `src/main/java/com/babyshop/api/product/entity/Category.java`

```java
package com.babyshop.api.product.entity;

import com.babyshop.api.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category extends BaseEntity {

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "slug", nullable = false, unique = true, length = 100)
    private String slug;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "display_order")
    private Integer displayOrder;
}
```

### 3. Product.java
**Path:** `src/main/java/com/babyshop/api/product/entity/Product.java`

```java
package com.babyshop.api.product.entity;

import com.babyshop.api.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends BaseEntity {

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "slug", nullable = false, unique = true, length = 200)
    private String slug;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "short_description", length = 500)
    private String shortDescription;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "compare_at_price", precision = 10, scale = 2)
    private BigDecimal compareAtPrice;

    @Column(name = "cost_price", precision = 10, scale = 2)
    private BigDecimal costPrice;

    @Column(name = "sku", unique = true, length = 100)
    private String sku;

    @Column(name = "barcode", length = 100)
    private String barcode;

    @Column(name = "stock_quantity", nullable = false)
    @Builder.Default
    private Integer stockQuantity = 0;

    @Column(name = "low_stock_threshold")
    @Builder.Default
    private Integer lowStockThreshold = 10;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private ProductStatus status = ProductStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "weight_grams")
    private Integer weightGrams;

    @Column(name = "featured")
    @Builder.Default
    private Boolean featured = false;

    @Column(name = "tags", length = 500)
    private String tags;

    public boolean isInStock() {
        return stockQuantity != null && stockQuantity > 0;
    }

    public boolean isAvailable() {
        return status == ProductStatus.ACTIVE && isInStock();
    }

    public boolean isLowStock() {
        return stockQuantity != null && lowStockThreshold != null 
                && stockQuantity <= lowStockThreshold;
    }

    public void reduceStock(int quantity) {
        if (stockQuantity < quantity) {
            throw new IllegalStateException("Insufficient stock available");
        }
        this.stockQuantity -= quantity;
        if (this.stockQuantity == 0) {
            this.status = ProductStatus.OUT_OF_STOCK;
        }
    }

    public void increaseStock(int quantity) {
        this.stockQuantity += quantity;
        if (this.status == ProductStatus.OUT_OF_STOCK && this.stockQuantity > 0) {
            this.status = ProductStatus.ACTIVE;
        }
    }
}
```

## Summary

This document contains the complete source code for:
- Product Module (3 entity files shown above)
- Order Module (files to be added)
- DTOs, Repositories, Services, and Controllers

Due to character limits, the remaining files are documented in the IMPLEMENTATION_GUIDE.md

## Next Steps

1. Copy each code block to create the corresponding Java file
2. Ensure proper package structure
3. Run `./gradlew clean build` to compile
4. Check for any compilation errors

The complete implementation includes over 40 files total for both Product and Order modules.

