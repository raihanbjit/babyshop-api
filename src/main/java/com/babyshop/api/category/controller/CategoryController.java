package com.babyshop.api.category.controller;

import com.babyshop.api.category.dto.CategoryListResponse;
import com.babyshop.api.category.dto.CategoryResponse;
import com.babyshop.api.category.dto.CategoryTreeResponse;
import com.babyshop.api.category.service.CategoryService;
import com.babyshop.api.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Public category controller.
 * All endpoints are publicly accessible (no authentication required).
 * Only shows active categories.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * Get all active categories.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryListResponse>>> getAllCategories() {
        log.debug("Get all active categories request");

        List<CategoryListResponse> categories = categoryService.getAllActiveCategories();

        return ResponseEntity.ok(
                ApiResponse.success("Categories retrieved successfully", categories));
    }

    /**
     * Get category tree (hierarchical).
     */
    @GetMapping("/tree")
    public ResponseEntity<ApiResponse<List<CategoryTreeResponse>>> getCategoryTree() {
        log.debug("Get category tree request");

        List<CategoryTreeResponse> tree = categoryService.getCategoryTree();

        return ResponseEntity.ok(
                ApiResponse.success("Category tree retrieved successfully", tree));
    }

    /**
     * Get category by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(@PathVariable UUID id) {
        log.debug("Get category by ID: {}", id);

        CategoryResponse category = categoryService.getCategoryById(id);

        return ResponseEntity.ok(
                ApiResponse.success("Category retrieved successfully", category));
    }

    /**
     * Get category by slug.
     */
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryBySlug(@PathVariable String slug) {
        log.debug("Get category by slug: {}", slug);

        CategoryResponse category = categoryService.getCategoryBySlug(slug);

        return ResponseEntity.ok(
                ApiResponse.success("Category retrieved successfully", category));
    }

    /**
     * Get category children.
     */
    @GetMapping("/{id}/children")
    public ResponseEntity<ApiResponse<List<CategoryListResponse>>> getCategoryChildren(@PathVariable UUID id) {
        log.debug("Get children for category: {}", id);

        List<CategoryListResponse> children = categoryService.getCategoryChildren(id);

        return ResponseEntity.ok(
                ApiResponse.success("Category children retrieved successfully", children));
    }
}

