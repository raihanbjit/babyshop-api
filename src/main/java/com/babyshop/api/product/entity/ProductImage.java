package com.babyshop.api.product.entity;

import com.babyshop.api.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Product image entity for storing multiple product images.
 *
 * Design decisions:
 * - One-to-many relationship with Product
 * - Display order for image gallery sorting
 * - Soft delete support
 * - URL-based storage (ready for CDN integration)
 */
@Entity
@Table(name = "product_images", indexes = {
    @Index(name = "idx_product_images_product_id", columnList = "product_id"),
    @Index(name = "idx_product_images_display_order", columnList = "display_order")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImage extends BaseEntity {

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "alt_text", length = 255)
    private String altText;
}

