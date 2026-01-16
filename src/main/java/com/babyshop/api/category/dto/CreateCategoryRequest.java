package com.babyshop.api.category.dto;

import jakarta.validation.constraints.*;

import java.util.UUID;

/**
 * DTO for creating a new category.
 *
 * Validation rules:
 * - Name: required, 3-100 characters, will be checked for uniqueness in service
 * - Slug: optional, auto-generated if not provided
 * - Parent: optional, validated in service layer
 */
public record CreateCategoryRequest(

        @NotBlank(message = "Category name is required")
        @Size(min = 3, max = 100, message = "Category name must be between 3 and 100 characters")
        String name,

        @Pattern(
                regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$",
                message = "Slug must be lowercase alphanumeric with hyphens only"
        )
        @Size(max = 100, message = "Slug must not exceed 100 characters")
        String slug,

        @Size(max = 2000, message = "Description must not exceed 2000 characters")
        String description,

        UUID parentId,

        @Size(max = 500, message = "Image URL must not exceed 500 characters")
        String imageUrl,

        @Min(value = 0, message = "Display order must be non-negative")
        Integer displayOrder,

        @Size(max = 255, message = "Meta title must not exceed 255 characters")
        String metaTitle,

        @Size(max = 1000, message = "Meta description must not exceed 1000 characters")
        String metaDescription,

        @Size(max = 500, message = "Meta keywords must not exceed 500 characters")
        String metaKeywords
) {
    /**
     * Compact constructor for normalization
     */
    public CreateCategoryRequest {
        name = name != null ? name.trim() : null;
        slug = slug != null ? slug.trim().toLowerCase() : null;
        description = description != null ? description.trim() : null;
        imageUrl = imageUrl != null ? imageUrl.trim() : null;
        metaTitle = metaTitle != null ? metaTitle.trim() : null;
        metaDescription = metaDescription != null ? metaDescription.trim() : null;
        metaKeywords = metaKeywords != null ? metaKeywords.trim() : null;
    }
}

