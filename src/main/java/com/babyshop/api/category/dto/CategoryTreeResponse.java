package com.babyshop.api.category.dto;

import com.babyshop.api.category.entity.Category;

import java.util.List;
import java.util.UUID;

/**
 * DTO for hierarchical category tree response.
 * Includes children for nested structure.
 */
public record CategoryTreeResponse(
        UUID id,
        String name,
        String slug,
        String imageUrl,
        Integer displayOrder,
        List<CategoryTreeResponse> children
) {
    /**
     * Factory method for leaf node (no children)
     */
    public static CategoryTreeResponse from(Category category) {
        return new CategoryTreeResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getImageUrl(),
                category.getDisplayOrder(),
                List.of()
        );
    }

    /**
     * Factory method with children
     */
    public static CategoryTreeResponse from(Category category, List<CategoryTreeResponse> children) {
        return new CategoryTreeResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getImageUrl(),
                category.getDisplayOrder(),
                children != null ? children : List.of()
        );
    }
}

