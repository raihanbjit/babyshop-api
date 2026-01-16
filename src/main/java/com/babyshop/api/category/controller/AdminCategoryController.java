package com.babyshop.api.category.controller;

import com.babyshop.api.category.dto.CategoryListResponse;
import com.babyshop.api.category.dto.CategoryResponse;
import com.babyshop.api.category.dto.CreateCategoryRequest;
import com.babyshop.api.category.dto.UpdateCategoryRequest;
import com.babyshop.api.category.service.CategoryService;
import com.babyshop.api.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Admin category controller.
 * All endpoints require ADMIN role.
 *
 * Endpoints:
 * - POST   /api/v1/admin/categories - Create category
 * - PUT    /api/v1/admin/categories/{id} - Update category
 * - DELETE /api/v1/admin/categories/{id} - Delete category
 * - GET    /api/v1/admin/categories - Get all categories (including inactive)
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/categories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCategoryController {

    private final CategoryService categoryService;

    /**
     * Create a new category (ADMIN only).
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CreateCategoryRequest request) {

        log.info("Admin creating category: {}", request.name());

        CategoryResponse category = categoryService.createCategory(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Category created successfully", category));
    }

    /**
     * Update a category (ADMIN only).
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCategoryRequest request) {

        log.info("Admin updating category: {}", id);

        CategoryResponse category = categoryService.updateCategory(id, request);

        return ResponseEntity.ok(
                ApiResponse.success("Category updated successfully", category));
    }

    /**
     * Delete a category (ADMIN only).
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable UUID id) {
        log.info("Admin deleting category: {}", id);

        categoryService.deleteCategory(id);

        return ResponseEntity.ok(
                ApiResponse.success("Category deleted successfully", null));
    }

    /**
     * Get all categories including inactive (ADMIN only).
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<CategoryListResponse>>> getAllCategories() {
        log.debug("Admin getting all categories");

        List<CategoryListResponse> categories = categoryService.getAllCategories();

        return ResponseEntity.ok(
                ApiResponse.success("Categories retrieved successfully", categories));
    }
}

