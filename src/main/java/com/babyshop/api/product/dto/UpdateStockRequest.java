package com.babyshop.api.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for updating product stock quantity.
 */
public record UpdateStockRequest(

        @NotNull(message = "Stock operation is required")
        StockOperation operation,

        @NotNull(message = "Quantity is required")
        @Min(value = 0, message = "Quantity must be non-negative")
        Integer quantity
) {
}

