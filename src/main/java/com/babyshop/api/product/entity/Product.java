package com.babyshop.api.product.entity;

import com.babyshop.api.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Product entity for e-commerce catalog.
 *
 * Design decisions:
 * - Price stored as BigDecimal for precision
 * - Slug for SEO-friendly URLs
 * - Stock quantity tracking
 * - Status-driven availability
 * - Category relationship (many-to-one)
 * - Featured flag for homepage display
 * - Cost price for profit calculation
 * - Compare price for discount display
 *
 * Important: Price snapshot should be taken at order time
 * (orders should not reference product price directly)
 */
@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_products_slug", columnList = "slug"),
    @Index(name = "idx_products_category_id", columnList = "category_id"),
    @Index(name = "idx_products_status", columnList = "status"),
    @Index(name = "idx_products_featured", columnList = "featured"),
    @Index(name = "idx_products_sku", columnList = "sku")
})
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

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "short_description", length = 500)
    private String shortDescription;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "compare_at_price", precision = 10, scale = 2)
    private BigDecimal compareAtPrice;

    @Column(name = "cost_price", precision = 10, scale = 2)
    private BigDecimal costPrice;

    @Column(name = "sku", length = 100, unique = true)
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

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "weight_grams")
    private Integer weightGrams;

    @Column(name = "featured")
    @Builder.Default
    private Boolean featured = false;

    @Column(name = "tags", length = 500)
    private String tags;

    /**
     * Check if product is in stock
     */
    public boolean isInStock() {
        return stockQuantity != null && stockQuantity > 0;
    }

    /**
     * Check if stock is low
     */
    public boolean isLowStock() {
        return stockQuantity != null && lowStockThreshold != null
               && stockQuantity > 0 && stockQuantity <= lowStockThreshold;
    }

    /**
     * Check if product is available for purchase
     */
    public boolean isAvailable() {
        return status == ProductStatus.ACTIVE && isInStock();
    }

    /**
     * Update stock quantity and auto-adjust status
     */
    public void updateStock(int newQuantity) {
        this.stockQuantity = newQuantity;
        autoUpdateStatus();
    }

    /**
     * Add stock quantity
     */
    public void addStock(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Cannot add negative stock");
        }
        this.stockQuantity += quantity;
        autoUpdateStatus();
    }

    /**
     * Subtract stock quantity
     */
    public void subtractStock(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Cannot subtract negative stock");
        }
        if (this.stockQuantity < quantity) {
            throw new IllegalArgumentException("Insufficient stock");
        }
        this.stockQuantity -= quantity;
        autoUpdateStatus();
    }

    /**
     * Auto-update status based on stock quantity
     */
    private void autoUpdateStatus() {
        if (this.stockQuantity == 0 && this.status == ProductStatus.ACTIVE) {
            this.status = ProductStatus.OUT_OF_STOCK;
        } else if (this.stockQuantity > 0 && this.status == ProductStatus.OUT_OF_STOCK) {
            this.status = ProductStatus.ACTIVE;
        }
    }

    /**
     * Calculate discount percentage if compare price exists
     */
    public Integer getDiscountPercentage() {
        if (compareAtPrice != null && compareAtPrice.compareTo(price) > 0) {
            BigDecimal discount = compareAtPrice.subtract(price);
            BigDecimal percentage = discount.divide(compareAtPrice, 2, BigDecimal.ROUND_HALF_UP)
                                           .multiply(new BigDecimal("100"));
            return percentage.intValue();
        }
        return null;
    }
}

