package com.babyshop.api.category.service;

import com.babyshop.api.category.dto.*;
import com.babyshop.api.category.entity.Category;
import com.babyshop.api.category.repository.CategoryRepository;
import com.babyshop.api.common.exception.BusinessException;
import com.babyshop.api.common.exception.ResourceNotFoundException;
import com.babyshop.api.common.util.SlugUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for category management.
 *
 * Key features:
 * - Hierarchical category support
 * - Circular reference prevention
 * - Slug auto-generation
 * - Active/inactive filtering
 * - Soft delete support
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * Create a new category.
     *
     * Validations:
     * - Name uniqueness
     * - Slug uniqueness (auto-generate if not provided)
     * - Parent existence and validity
     * - No circular references
     */
    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        log.info("Creating category: {}", request.name());

        // Validate name uniqueness
        if (categoryRepository.existsByNameIgnoreCaseAndIsDeletedFalse(request.name())) {
            throw new BusinessException(
                    "Category name already exists",
                    "CATEGORY_NAME_EXISTS",
                    HttpStatus.CONFLICT
            );
        }

        // Generate or validate slug
        String slug = request.slug();
        if (slug == null || slug.isBlank()) {
            slug = SlugUtils.generateSlug(request.name());
        }

        // Validate slug uniqueness
        if (categoryRepository.existsBySlugAndIsDeletedFalse(slug)) {
            throw new BusinessException(
                    "Category slug already exists",
                    "CATEGORY_SLUG_EXISTS",
                    HttpStatus.CONFLICT
            );
        }

        // Validate parent if provided
        if (request.parentId() != null) {
            validateParentCategory(request.parentId(), null);
        }

        // Create category
        Category category = Category.builder()
                .name(request.name())
                .slug(slug)
                .description(request.description())
                .parentId(request.parentId())
                .imageUrl(request.imageUrl())
                .displayOrder(request.displayOrder() != null ? request.displayOrder() : 0)
                .isActive(true)
                .metaTitle(request.metaTitle())
                .metaDescription(request.metaDescription())
                .metaKeywords(request.metaKeywords())
                .build();

        category = categoryRepository.save(category);
        log.info("Category created with ID: {}", category.getId());

        return CategoryResponse.from(category);
    }

    /**
     * Update an existing category.
     */
    @Transactional
    public CategoryResponse updateCategory(UUID id, UpdateCategoryRequest request) {
        log.info("Updating category: {}", id);

        Category category = findCategoryById(id);

        // Update name if provided
        if (request.name() != null && !request.name().isBlank()) {
            if (!category.getName().equalsIgnoreCase(request.name()) &&
                categoryRepository.existsByNameIgnoreCaseAndIsDeletedFalse(request.name())) {
                throw new BusinessException(
                        "Category name already exists",
                        "CATEGORY_NAME_EXISTS",
                        HttpStatus.CONFLICT
                );
            }
            category.setName(request.name());
        }

        // Update slug if provided
        if (request.slug() != null && !request.slug().isBlank()) {
            if (!category.getSlug().equals(request.slug()) &&
                categoryRepository.existsBySlugAndIdNotAndIsDeletedFalse(request.slug(), id)) {
                throw new BusinessException(
                        "Category slug already exists",
                        "CATEGORY_SLUG_EXISTS",
                        HttpStatus.CONFLICT
                );
            }
            category.setSlug(request.slug());
        }

        // Update parent if provided
        if (request.parentId() != null) {
            validateParentCategory(request.parentId(), id);
            category.setParentId(request.parentId());
        }

        // Update other fields
        if (request.description() != null) category.setDescription(request.description());
        if (request.imageUrl() != null) category.setImageUrl(request.imageUrl());
        if (request.displayOrder() != null) category.setDisplayOrder(request.displayOrder());
        if (request.isActive() != null) category.setIsActive(request.isActive());
        if (request.metaTitle() != null) category.setMetaTitle(request.metaTitle());
        if (request.metaDescription() != null) category.setMetaDescription(request.metaDescription());
        if (request.metaKeywords() != null) category.setMetaKeywords(request.metaKeywords());

        category = categoryRepository.save(category);
        log.info("Category updated: {}", id);

        return CategoryResponse.from(category);
    }

    /**
     * Get category by ID.
     */
    public CategoryResponse getCategoryById(UUID id) {
        Category category = findCategoryById(id);
        return CategoryResponse.from(category);
    }

    /**
     * Get category by slug.
     */
    public CategoryResponse getCategoryBySlug(String slug) {
        Category category = categoryRepository.findBySlugAndIsDeletedFalse(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "slug", slug));
        return CategoryResponse.from(category);
    }

    /**
     * Get all categories (admin view).
     */
    public List<CategoryListResponse> getAllCategories() {
        return categoryRepository.findAllByIsDeletedFalseOrderByDisplayOrderAsc()
                .stream()
                .map(CategoryListResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Get all active categories (public view).
     */
    public List<CategoryListResponse> getAllActiveCategories() {
        return categoryRepository.findAllByIsActiveTrueAndIsDeletedFalseOrderByDisplayOrderAsc()
                .stream()
                .map(CategoryListResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Get category tree (hierarchical structure).
     */
    public List<CategoryTreeResponse> getCategoryTree() {
        List<Category> rootCategories = categoryRepository
                .findAllByParentIdIsNullAndIsActiveTrueAndIsDeletedFalseOrderByDisplayOrderAsc();

        return rootCategories.stream()
                .map(this::buildCategoryTree)
                .collect(Collectors.toList());
    }

    /**
     * Get children of a category.
     */
    public List<CategoryListResponse> getCategoryChildren(UUID parentId) {
        // Validate parent exists
        findCategoryById(parentId);

        return categoryRepository
                .findAllByParentIdAndIsActiveTrueAndIsDeletedFalseOrderByDisplayOrderAsc(parentId)
                .stream()
                .map(CategoryListResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Delete category (soft delete).
     */
    @Transactional
    public void deleteCategory(UUID id) {
        log.info("Deleting category: {}", id);

        Category category = findCategoryById(id);

        // Check if category has children
        if (categoryRepository.existsByParentIdAndIsDeletedFalse(id)) {
            throw new BusinessException(
                    "Cannot delete category with subcategories",
                    "CATEGORY_HAS_CHILDREN",
                    HttpStatus.CONFLICT
            );
        }

        // Check if category has products
        long productCount = categoryRepository.countProductsByCategoryId(id);
        if (productCount > 0) {
            throw new BusinessException(
                    "Cannot delete category with products",
                    "CATEGORY_HAS_PRODUCTS",
                    HttpStatus.CONFLICT
            );
        }

        category.setIsDeleted(true);
        categoryRepository.save(category);

        log.info("Category deleted: {}", id);
    }

    /**
     * Build category tree recursively.
     */
    private CategoryTreeResponse buildCategoryTree(Category category) {
        List<Category> children = categoryRepository
                .findAllByParentIdAndIsActiveTrueAndIsDeletedFalseOrderByDisplayOrderAsc(category.getId());

        List<CategoryTreeResponse> childrenTree = children.stream()
                .map(this::buildCategoryTree)
                .collect(Collectors.toList());

        return CategoryTreeResponse.from(category, childrenTree);
    }

    /**
     * Validate parent category.
     *
     * Rules:
     * - Parent must exist and be active
     * - Parent cannot be the category itself
     * - No circular references
     */
    private void validateParentCategory(UUID parentId, UUID currentCategoryId) {
        // Check parent exists
        Category parent = categoryRepository.findById(parentId)
                .filter(c -> !c.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Parent category", "id", parentId));

        // Check parent is active
        if (!parent.getIsActive()) {
            throw new BusinessException(
                    "Parent category is not active",
                    "PARENT_CATEGORY_INACTIVE",
                    HttpStatus.BAD_REQUEST
            );
        }

        // Check not setting parent to itself
        if (parentId.equals(currentCategoryId)) {
            throw new BusinessException(
                    "Category cannot be its own parent",
                    "CIRCULAR_REFERENCE",
                    HttpStatus.BAD_REQUEST
            );
        }

        // Check for circular references
        if (currentCategoryId != null) {
            List<UUID> ancestors = categoryRepository.findAncestorIds(parentId);
            if (ancestors.contains(currentCategoryId)) {
                throw new BusinessException(
                        "Circular reference detected in category hierarchy",
                        "CIRCULAR_REFERENCE",
                        HttpStatus.BAD_REQUEST
                );
            }
        }
    }

    /**
     * Find category by ID or throw exception.
     */
    private Category findCategoryById(UUID id) {
        return categoryRepository.findById(id)
                .filter(c -> !c.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
    }
}

