package com.babyshop.api.category.repository;

import com.babyshop.api.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Category entity.
 *
 * Custom queries for:
 * - Hierarchy management
 * - Active category filtering
 * - Slug-based lookup
 * - Parent-child relationships
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    /**
     * Find category by slug (for SEO-friendly URLs)
     */
    Optional<Category> findBySlugAndIsDeletedFalse(String slug);

    /**
     * Find all active categories (not deleted)
     */
    List<Category> findAllByIsDeletedFalseOrderByDisplayOrderAsc();

    /**
     * Find all active and visible categories (public view)
     */
    List<Category> findAllByIsActiveTrueAndIsDeletedFalseOrderByDisplayOrderAsc();

    /**
     * Find all root categories (no parent)
     */
    List<Category> findAllByParentIdIsNullAndIsDeletedFalseOrderByDisplayOrderAsc();

    /**
     * Find all active root categories (public view)
     */
    List<Category> findAllByParentIdIsNullAndIsActiveTrueAndIsDeletedFalseOrderByDisplayOrderAsc();

    /**
     * Find all children of a parent category
     */
    List<Category> findAllByParentIdAndIsDeletedFalseOrderByDisplayOrderAsc(UUID parentId);

    /**
     * Find all active children of a parent category (public view)
     */
    List<Category> findAllByParentIdAndIsActiveTrueAndIsDeletedFalseOrderByDisplayOrderAsc(UUID parentId);

    /**
     * Check if category exists by name (case-insensitive)
     */
    boolean existsByNameIgnoreCaseAndIsDeletedFalse(String name);

    /**
     * Check if category exists by slug
     */
    boolean existsBySlugAndIsDeletedFalse(String slug);

    /**
     * Check if category exists by slug excluding specific ID (for updates)
     */
    boolean existsBySlugAndIdNotAndIsDeletedFalse(String slug, UUID id);

    /**
     * Check if category has any child categories
     */
    boolean existsByParentIdAndIsDeletedFalse(UUID parentId);

    /**
     * Count products in category
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.categoryId = :categoryId AND p.isDeleted = false")
    long countProductsByCategoryId(@Param("categoryId") UUID categoryId);

    /**
     * Find all ancestor IDs of a category (for circular reference detection)
     */
    @Query(value = """
        WITH RECURSIVE category_ancestors AS (
            SELECT id, parent_id, 1 as level
            FROM categories
            WHERE id = :categoryId AND is_deleted = false
            
            UNION ALL
            
            SELECT c.id, c.parent_id, ca.level + 1
            FROM categories c
            INNER JOIN category_ancestors ca ON c.id = ca.parent_id
            WHERE c.is_deleted = false AND ca.level < 10
        )
        SELECT id FROM category_ancestors WHERE id != :categoryId
        """, nativeQuery = true)
    List<UUID> findAncestorIds(@Param("categoryId") UUID categoryId);
}

