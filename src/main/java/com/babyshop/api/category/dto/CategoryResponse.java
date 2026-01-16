package com.babyshop.api.category.dto;

import com.babyshop.api.category.entity.Category;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for category response.
 * Complete category information.
 */
public record CategoryResponse(
        UUID id,
        String name,
        String slug,
        String description,
        UUID parentId,
        String imageUrl,
        Integer displayOrder,
        Boolean isActive,
        String metaTitle,
        String metaDescription,
        String metaKeywords,
        Instant createdAt,
        Instant updatedAt
) {
    /**
     * Factory method to create CategoryResponse from Category entity
     */
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getDescription(),
                category.getParentId(),
                category.getImageUrl(),
                category.getDisplayOrder(),
                category.getIsActive(),
                category.getMetaTitle(),
                category.getMetaDescription(),
                category.getMetaKeywords(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}

