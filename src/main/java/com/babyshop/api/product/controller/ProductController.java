package com.babyshop.api.product.controller;

import com.babyshop.api.common.dto.ApiResponse;
import com.babyshop.api.product.dto.ProductListResponse;
import com.babyshop.api.product.dto.ProductResponse;
import com.babyshop.api.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Public product controller.
 * All endpoints are publicly accessible (no authentication required).
 * Only shows active products.
 *
 * Endpoints:
 * - GET /api/v1/products - Get all active products (paginated)
 * - GET /api/v1/products/{id} - Get product by ID
 * - GET /api/v1/products/slug/{slug} - Get product by slug
 * - GET /api/v1/products/category/{categoryId} - Get products by category
 * - GET /api/v1/products/featured - Get featured products
 * - GET /api/v1/products/search - Search products
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * Get all active products (paginated, sorted).
     *
     * Query params:
     * - page: Page number (0-indexed)
     * - size: Page size
     * - sort: Sort field,direction (e.g., "name,asc" or "price,desc")
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductListResponse>>> getAllProducts(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        log.debug("Get all active products request: page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<ProductListResponse> products = productService.getAllActiveProducts(pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Products retrieved successfully", products));
    }

    /**
     * Get product by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable UUID id) {
        log.debug("Get product by ID: {}", id);

        ProductResponse product = productService.getProductById(id);

        return ResponseEntity.ok(
                ApiResponse.success("Product retrieved successfully", product));
    }

    /**
     * Get product by slug (SEO-friendly).
     */
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductBySlug(@PathVariable String slug) {
        log.debug("Get product by slug: {}", slug);

        ProductResponse product = productService.getProductBySlug(slug);

        return ResponseEntity.ok(
                ApiResponse.success("Product retrieved successfully", product));
    }

    /**
     * Get products by category.
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<Page<ProductListResponse>>> getProductsByCategory(
            @PathVariable UUID categoryId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        log.debug("Get products by category: {}", categoryId);

        Page<ProductListResponse> products = productService.getProductsByCategory(categoryId, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Products retrieved successfully", products));
    }

    /**
     * Get featured products.
     */
    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<Page<ProductListResponse>>> getFeaturedProducts(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        log.debug("Get featured products request");

        Page<ProductListResponse> products = productService.getFeaturedProducts(pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Featured products retrieved successfully", products));
    }

    /**
     * Search products by name, description, or tags.
     *
     * Query param:
     * - q: Search query
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<ProductListResponse>>> searchProducts(
            @RequestParam String q,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {

        log.debug("Search products with query: {}", q);

        Page<ProductListResponse> products = productService.searchProducts(q, pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Products retrieved successfully", products));
    }
}

