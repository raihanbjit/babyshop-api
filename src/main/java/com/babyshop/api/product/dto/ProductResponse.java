package com.babyshop.api.product.dto;

import com.babyshop.api.category.dto.CategoryListResponse;
import com.babyshop.api.product.entity.Product;
import com.babyshop.api.product.entity.ProductStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DTO for detailed product response.
 * Full product information with images and category.
 */
public record ProductResponse(
        UUID id,
        String name,
        String slug,
        String description,
        String shortDescription,
        BigDecimal price,
        BigDecimal compareAtPrice,
        BigDecimal costPrice,
        Integer discountPercentage,
        String sku,
        String barcode,
        Integer stockQuantity,
        Integer lowStockThreshold,
        Boolean isLowStock,
        Boolean isInStock,
        ProductStatus status,
        UUID categoryId,
        CategoryListResponse category,
        String imageUrl,
        List<String> additionalImages,
        Integer weightGrams,
        Boolean featured,
        String tags,
        Instant createdAt,
        Instant updatedAt
) {
    /**
     * Factory method from Product entity
     */
    public static ProductResponse from(
            Product product,
            CategoryListResponse category,
            List<String> additionalImages) {

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getDescription(),
                product.getShortDescription(),
                product.getPrice(),
                product.getCompareAtPrice(),
                product.getCostPrice(),
                product.getDiscountPercentage(),
                product.getSku(),
                product.getBarcode(),
                product.getStockQuantity(),
                product.getLowStockThreshold(),
                product.isLowStock(),
                product.isInStock(),
                product.getStatus(),
                product.getCategoryId(),
                category,
                product.getImageUrl(),
                additionalImages,
                product.getWeightGrams(),
                product.getFeatured(),
                product.getTags(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}

