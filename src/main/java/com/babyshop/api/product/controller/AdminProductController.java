package com.babyshop.api.product.controller;

import com.babyshop.api.common.dto.ApiResponse;
import com.babyshop.api.product.dto.*;
import com.babyshop.api.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Admin product controller.
 * All endpoints require ADMIN role.
 *
 * Endpoints:
 * - POST   /api/v1/admin/products - Create product
 * - PUT    /api/v1/admin/products/{id} - Update product
 * - DELETE /api/v1/admin/products/{id} - Delete product
 * - PATCH  /api/v1/admin/products/{id}/stock - Update stock
 * - GET    /api/v1/admin/products - Get all products (including inactive)
 * - GET    /api/v1/admin/products/low-stock - Get low stock products
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {

    private final ProductService productService;

    /**
     * Create a new product (ADMIN only).
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody CreateProductRequest request) {

        log.info("Admin creating product: {}", request.name());

        ProductResponse product = productService.createProduct(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created successfully", product));
    }

    /**
     * Update a product (ADMIN only).
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProductRequest request) {

        log.info("Admin updating product: {}", id);

        ProductResponse product = productService.updateProduct(id, request);

        return ResponseEntity.ok(
                ApiResponse.success("Product updated successfully", product));
    }

    /**
     * Update product stock (ADMIN only).
     *
     * Operations:
     * - SET: Set absolute quantity
     * - ADD: Add to current quantity
     * - SUBTRACT: Subtract from current quantity
     */
    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> updateStock(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStockRequest request) {

        log.info("Admin updating stock for product: {}", id);

        ProductResponse product = productService.updateStock(id, request);

        return ResponseEntity.ok(
                ApiResponse.success("Stock updated successfully", product));
    }

    /**
     * Delete a product (ADMIN only - soft delete).
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable UUID id) {
        log.info("Admin deleting product: {}", id);

        productService.deleteProduct(id);

        return ResponseEntity.ok(
                ApiResponse.success("Product deleted successfully", null));
    }

    /**
     * Get all products including inactive (ADMIN only).
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<ProductListResponse>>> getAllProducts(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        log.debug("Admin getting all products");

        Page<ProductListResponse> products = productService.getAllProducts(pageable);

        return ResponseEntity.ok(
                ApiResponse.success("Products retrieved successfully", products));
    }

    /**
     * Get low stock products (ADMIN only).
     * Products with stock > 0 but <= lowStockThreshold.
     */
    @GetMapping("/low-stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ProductListResponse>>> getLowStockProducts() {
        log.debug("Admin getting low stock products");

        List<ProductListResponse> products = productService.getLowStockProducts();

        return ResponseEntity.ok(
                ApiResponse.success("Low stock products retrieved successfully", products));
    }
}

