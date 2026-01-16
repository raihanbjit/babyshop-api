package com.babyshop.api.product.repository;

import com.babyshop.api.product.entity.Product;
import com.babyshop.api.product.entity.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Product entity.
 *
 * Custom queries for:
 * - Product filtering and search
 * - Category-based queries
 * - Status-based filtering
 * - Price range filtering
 * - Featured products
 * - Low stock alerts
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    /**
     * Find product by slug (for SEO-friendly URLs)
     */
    Optional<Product> findBySlugAndIsDeletedFalse(String slug);

    /**
     * Find product by SKU
     */
    Optional<Product> findBySkuAndIsDeletedFalse(String sku);

    /**
     * Find all products by category
     */
    Page<Product> findAllByCategoryIdAndIsDeletedFalse(UUID categoryId, Pageable pageable);

    /**
     * Find all active products by category (public view)
     */
    Page<Product> findAllByCategoryIdAndStatusAndIsDeletedFalse(
            UUID categoryId, ProductStatus status, Pageable pageable);

    /**
     * Find all active products (public view)
     */
    Page<Product> findAllByStatusAndIsDeletedFalse(ProductStatus status, Pageable pageable);

    /**
     * Find all products (admin view)
     */
    Page<Product> findAllByIsDeletedFalse(Pageable pageable);

    /**
     * Find featured products
     */
    Page<Product> findAllByFeaturedTrueAndStatusAndIsDeletedFalse(
            ProductStatus status, Pageable pageable);

    /**
     * Find products with low stock
     */
    @Query("SELECT p FROM Product p WHERE p.stockQuantity > 0 AND p.stockQuantity <= p.lowStockThreshold AND p.isDeleted = false")
    List<Product> findLowStockProducts();

    /**
     * Find out of stock products
     */
    List<Product> findAllByStockQuantityAndIsDeletedFalse(Integer stockQuantity);

    /**
     * Search products by name or description
     */
    @Query("""
        SELECT p FROM Product p 
        WHERE (LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) 
            OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(p.tags) LIKE LOWER(CONCAT('%', :query, '%')))
        AND p.status = :status
        AND p.isDeleted = false
        """)
    Page<Product> searchProducts(
            @Param("query") String query,
            @Param("status") ProductStatus status,
            Pageable pageable);

    /**
     * Search products with filters (admin)
     */
    @Query("""
        SELECT p FROM Product p 
        WHERE (LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) 
            OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(p.tags) LIKE LOWER(CONCAT('%', :query, '%')))
        AND p.isDeleted = false
        """)
    Page<Product> searchProductsAdmin(@Param("query") String query, Pageable pageable);

    /**
     * Filter products by price range and category
     */
    @Query("""
        SELECT p FROM Product p 
        WHERE (:categoryId IS NULL OR p.categoryId = :categoryId)
        AND (:minPrice IS NULL OR p.price >= :minPrice)
        AND (:maxPrice IS NULL OR p.price <= :maxPrice)
        AND p.status = :status
        AND p.isDeleted = false
        """)
    Page<Product> filterProducts(
            @Param("categoryId") UUID categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("status") ProductStatus status,
            Pageable pageable);

    /**
     * Check if product exists by name (case-insensitive)
     */
    boolean existsByNameIgnoreCaseAndIsDeletedFalse(String name);

    /**
     * Check if product exists by slug
     */
    boolean existsBySlugAndIsDeletedFalse(String slug);

    /**
     * Check if product exists by slug excluding specific ID (for updates)
     */
    boolean existsBySlugAndIdNotAndIsDeletedFalse(String slug, UUID id);

    /**
     * Check if SKU exists (for uniqueness validation)
     */
    boolean existsBySkuAndIsDeletedFalse(String sku);

    /**
     * Check if SKU exists excluding specific ID (for updates)
     */
    boolean existsBySkuAndIdNotAndIsDeletedFalse(String sku, UUID id);

    /**
     * Count products by category
     */
    long countByCategoryIdAndIsDeletedFalse(UUID categoryId);

    /**
     * Count active products by category
     */
    long countByCategoryIdAndStatusAndIsDeletedFalse(UUID categoryId, ProductStatus status);
}

