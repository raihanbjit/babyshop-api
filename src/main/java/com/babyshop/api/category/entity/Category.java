package com.babyshop.api.category.entity;

import com.babyshop.api.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Category entity for hierarchical product categorization.
 *
 * Design decisions:
 * - Self-referencing for parent-child hierarchy
 * - Slug for SEO-friendly URLs
 * - Soft delete support
 * - Display order for custom sorting
 * - SEO metadata fields
 * - Active flag for visibility control
 */
@Entity
@Table(name = "categories", indexes = {
    @Index(name = "idx_categories_slug", columnList = "slug"),
    @Index(name = "idx_categories_parent_id", columnList = "parent_id"),
    @Index(name = "idx_categories_is_active", columnList = "is_active"),
    @Index(name = "idx_categories_display_order", columnList = "display_order")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category extends BaseEntity {

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "slug", nullable = false, unique = true, length = 100)
    private String slug;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "parent_id")
    private UUID parentId;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "meta_title", length = 255)
    private String metaTitle;

    @Column(name = "meta_description", columnDefinition = "TEXT")
    private String metaDescription;

    @Column(name = "meta_keywords", length = 500)
    private String metaKeywords;

    /**
     * Check if this is a root category (no parent)
     */
    public boolean isRootCategory() {
        return parentId == null;
    }

    /**
     * Pre-persist: Set default meta title if not provided
     */
    @PrePersist
    protected void onCreate() {
        if (metaTitle == null || metaTitle.isBlank()) {
            metaTitle = name;
        }
    }
}

