package com.babyshop.api.product.repository;

import com.babyshop.api.product.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for ProductImage entity.
 */
@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, UUID> {

    /**
     * Find all images for a product
     */
    List<ProductImage> findAllByProductIdAndIsDeletedFalseOrderByDisplayOrderAsc(UUID productId);

    /**
     * Count images for a product
     */
    long countByProductIdAndIsDeletedFalse(UUID productId);

    /**
     * Delete all images for a product (soft delete)
     */
    void deleteByProductId(UUID productId);
}

