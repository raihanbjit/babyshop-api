package com.babyshop.api.product.dto;

import com.babyshop.api.category.dto.CategoryListResponse;
import com.babyshop.api.product.entity.Product;
import com.babyshop.api.product.entity.ProductStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DTO for product list response (summary view).
 * Used for product listings with essential info.
 */
public record ProductListResponse(
        UUID id,
        String name,
        String slug,
        String shortDescription,
        BigDecimal price,
        BigDecimal compareAtPrice,
        Integer discountPercentage,
        String imageUrl,
        ProductStatus status,
        Integer stockQuantity,
        Boolean featured,
        CategoryListResponse category
) {
    /**
     * Factory method from Product entity
     */
    public static ProductListResponse from(Product product, CategoryListResponse category) {
        return new ProductListResponse(
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getShortDescription(),
                product.getPrice(),
                product.getCompareAtPrice(),
                product.getDiscountPercentage(),
                product.getImageUrl(),
                product.getStatus(),
                product.getStockQuantity(),
                product.getFeatured(),
                category
        );
    }
}

