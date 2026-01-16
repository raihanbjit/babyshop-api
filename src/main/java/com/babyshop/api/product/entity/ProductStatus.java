package com.babyshop.api.product.entity;

/**
 * Product status enumeration.
 *
 * Status lifecycle:
 * - ACTIVE: Product is available for purchase
 * - OUT_OF_STOCK: Temporarily unavailable (stock = 0)
 * - DISABLED: Admin disabled (not visible to customers)
 */
public enum ProductStatus {
    /**
     * Product is active and available for purchase
     */
    ACTIVE,

    /**
     * Product is temporarily out of stock
     * Auto-set when stockQuantity reaches 0
     */
    OUT_OF_STOCK,

    /**
     * Product is disabled by admin
     * Not visible to customers
     */
    DISABLED
}

