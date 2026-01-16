package com.babyshop.api.category.dto;

import com.babyshop.api.category.entity.Category;

import java.util.List;
import java.util.UUID;

/**
 * DTO for category list response (simplified).
 * Used for listings with basic info.
 */
public record CategoryListResponse(
        UUID id,
        String name,
        String slug,
        String imageUrl,
        Integer displayOrder,
        Boolean isActive,
        UUID parentId,
        Long productCount
) {
    /**
     * Factory method without product count
     */
    public static CategoryListResponse from(Category category) {
        return new CategoryListResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getImageUrl(),
                category.getDisplayOrder(),
                category.getIsActive(),
                category.getParentId(),
                0L
        );
    }

    /**
     * Factory method with product count
     */
    public static CategoryListResponse from(Category category, Long productCount) {
        return new CategoryListResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getImageUrl(),
                category.getDisplayOrder(),
                category.getIsActive(),
                category.getParentId(),
                productCount
        );
    }
}

