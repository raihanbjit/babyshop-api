package com.babyshop.api.product.dto;

import com.babyshop.api.product.entity.ProductStatus;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * DTO for updating an existing product.
 * All fields are optional for partial updates.
 */
public record UpdateProductRequest(

        @Size(min = 3, max = 200, message = "Product name must be between 3 and 200 characters")
        String name,

        @Pattern(
                regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$",
                message = "Slug must be lowercase alphanumeric with hyphens only"
        )
        @Size(max = 200, message = "Slug must not exceed 200 characters")
        String slug,

        @Size(min = 10, max = 10000, message = "Description must be between 10 and 10000 characters")
        String description,

        @Size(max = 500, message = "Short description must not exceed 500 characters")
        String shortDescription,

        @DecimalMin(value = "0.01", message = "Price must be at least 0.01")
        @DecimalMax(value = "999999.99", message = "Price must not exceed 999999.99")
        @Digits(integer = 7, fraction = 2, message = "Price must have at most 2 decimal places")
        BigDecimal price,

        @DecimalMin(value = "0.01", message = "Compare price must be at least 0.01")
        @Digits(integer = 7, fraction = 2, message = "Compare price must have at most 2 decimal places")
        BigDecimal compareAtPrice,

        @DecimalMin(value = "0.01", message = "Cost price must be at least 0.01")
        @Digits(integer = 7, fraction = 2, message = "Cost price must have at most 2 decimal places")
        BigDecimal costPrice,

        @Size(max = 100, message = "SKU must not exceed 100 characters")
        String sku,

        @Size(max = 100, message = "Barcode must not exceed 100 characters")
        String barcode,

        UUID categoryId,

        @Min(value = 0, message = "Low stock threshold must be non-negative")
        Integer lowStockThreshold,

        ProductStatus status,

        @Size(max = 500, message = "Image URL must not exceed 500 characters")
        String imageUrl,

        @Min(value = 0, message = "Weight must be non-negative")
        Integer weightGrams,

        Boolean featured,

        @Size(max = 500, message = "Tags must not exceed 500 characters")
        String tags,

        @Size(max = 10, message = "Maximum 10 additional images allowed")
        List<String> additionalImages
) {
    public UpdateProductRequest {
        name = name != null ? name.trim() : null;
        slug = slug != null ? slug.trim().toLowerCase() : null;
        description = description != null ? description.trim() : null;
        shortDescription = shortDescription != null ? shortDescription.trim() : null;
        sku = sku != null ? sku.trim().toUpperCase() : null;
        barcode = barcode != null ? barcode.trim() : null;
        imageUrl = imageUrl != null ? imageUrl.trim() : null;
        tags = tags != null ? tags.trim() : null;
    }
}

