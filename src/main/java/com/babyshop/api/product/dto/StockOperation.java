package com.babyshop.api.product.dto;

/**
 * Stock operation type for product stock updates.
 */
public enum StockOperation {
    /**
     * Set absolute stock quantity
     */
    SET,

    /**
     * Add to current stock (restock)
     */
    ADD,

    /**
     * Subtract from current stock
     */
    SUBTRACT
}

