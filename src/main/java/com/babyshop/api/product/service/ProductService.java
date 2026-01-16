package com.babyshop.api.product.service;

import com.babyshop.api.category.dto.CategoryListResponse;
import com.babyshop.api.category.entity.Category;
import com.babyshop.api.category.repository.CategoryRepository;
import com.babyshop.api.common.exception.BusinessException;
import com.babyshop.api.common.exception.ResourceNotFoundException;
import com.babyshop.api.common.util.SlugUtils;
import com.babyshop.api.product.dto.*;
import com.babyshop.api.product.entity.Product;
import com.babyshop.api.product.entity.ProductImage;
import com.babyshop.api.product.entity.ProductStatus;
import com.babyshop.api.product.repository.ProductImageRepository;
import com.babyshop.api.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for product management.
 *
 * Key features:
 * - Product CRUD operations
 * - Stock management with auto-status updates
 * - Image handling
 * - Search and filtering
 * - Category validation
 * - Slug auto-generation
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Create a new product.
     */
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        log.info("Creating product: {}", request.name());

        // Validate category
        Category category = validateCategory(request.categoryId());

        // Generate or validate slug
        String slug = request.slug();
        if (slug == null || slug.isBlank()) {
            slug = SlugUtils.generateSlug(request.name());
        }

        // Validate slug uniqueness
        if (productRepository.existsBySlugAndIsDeletedFalse(slug)) {
            throw new BusinessException(
                    "Product slug already exists",
                    "PRODUCT_SLUG_EXISTS",
                    HttpStatus.CONFLICT
            );
        }

        // Validate SKU uniqueness if provided
        if (request.sku() != null && productRepository.existsBySkuAndIsDeletedFalse(request.sku())) {
            throw new BusinessException(
                    "Product SKU already exists",
                    "PRODUCT_SKU_EXISTS",
                    HttpStatus.CONFLICT
            );
        }

        // Create product
        Product product = Product.builder()
                .name(request.name())
                .slug(slug)
                .description(request.description())
                .shortDescription(request.shortDescription())
                .price(request.price())
                .compareAtPrice(request.compareAtPrice())
                .costPrice(request.costPrice())
                .sku(request.sku())
                .barcode(request.barcode())
                .stockQuantity(request.stockQuantity())
                .lowStockThreshold(request.lowStockThreshold())
                .status(request.stockQuantity() > 0 ? ProductStatus.ACTIVE : ProductStatus.OUT_OF_STOCK)
                .categoryId(request.categoryId())
                .imageUrl(request.imageUrl())
                .weightGrams(request.weightGrams())
                .featured(request.featured())
                .tags(request.tags())
                .build();

        product = productRepository.save(product);

        // Save additional images if provided
        List<String> additionalImageUrls = new ArrayList<>();
        if (request.additionalImages() != null && !request.additionalImages().isEmpty()) {
            additionalImageUrls = saveProductImages(product.getId(), request.additionalImages());
        }

        log.info("Product created with ID: {}", product.getId());

        return ProductResponse.from(product, CategoryListResponse.from(category), additionalImageUrls);
    }

    /**
     * Update an existing product.
     */
    @Transactional
    public ProductResponse updateProduct(UUID id, UpdateProductRequest request) {
        log.info("Updating product: {}", id);

        Product product = findProductById(id);

        // Update name if provided
        if (request.name() != null && !request.name().isBlank()) {
            product.setName(request.name());
        }

        // Update slug if provided
        if (request.slug() != null && !request.slug().isBlank()) {
            if (!product.getSlug().equals(request.slug()) &&
                productRepository.existsBySlugAndIdNotAndIsDeletedFalse(request.slug(), id)) {
                throw new BusinessException(
                        "Product slug already exists",
                        "PRODUCT_SLUG_EXISTS",
                        HttpStatus.CONFLICT
                );
            }
            product.setSlug(request.slug());
        }

        // Update SKU if provided
        if (request.sku() != null && !request.sku().isBlank()) {
            if (!request.sku().equals(product.getSku()) &&
                productRepository.existsBySkuAndIdNotAndIsDeletedFalse(request.sku(), id)) {
                throw new BusinessException(
                        "Product SKU already exists",
                        "PRODUCT_SKU_EXISTS",
                        HttpStatus.CONFLICT
                );
            }
            product.setSku(request.sku());
        }

        // Update category if provided
        if (request.categoryId() != null) {
            validateCategory(request.categoryId());
            product.setCategoryId(request.categoryId());
        }

        // Update other fields
        if (request.description() != null) product.setDescription(request.description());
        if (request.shortDescription() != null) product.setShortDescription(request.shortDescription());
        if (request.price() != null) product.setPrice(request.price());
        if (request.compareAtPrice() != null) product.setCompareAtPrice(request.compareAtPrice());
        if (request.costPrice() != null) product.setCostPrice(request.costPrice());
        if (request.barcode() != null) product.setBarcode(request.barcode());
        if (request.lowStockThreshold() != null) product.setLowStockThreshold(request.lowStockThreshold());
        if (request.status() != null) product.setStatus(request.status());
        if (request.imageUrl() != null) product.setImageUrl(request.imageUrl());
        if (request.weightGrams() != null) product.setWeightGrams(request.weightGrams());
        if (request.featured() != null) product.setFeatured(request.featured());
        if (request.tags() != null) product.setTags(request.tags());

        product = productRepository.save(product);

        // Update additional images if provided
        List<String> additionalImageUrls = getProductImageUrls(id);
        if (request.additionalImages() != null) {
            // Delete existing images
            deleteProductImages(id);
            // Save new images
            additionalImageUrls = saveProductImages(id, request.additionalImages());
        }

        log.info("Product updated: {}", id);

        Category category = categoryRepository.findById(product.getCategoryId()).orElseThrow();
        return ProductResponse.from(product, CategoryListResponse.from(category), additionalImageUrls);
    }

    /**
     * Update product stock.
     */
    @Transactional
    public ProductResponse updateStock(UUID id, UpdateStockRequest request) {
        log.info("Updating stock for product: {}, operation: {}, quantity: {}",
                id, request.operation(), request.quantity());

        Product product = findProductById(id);

        switch (request.operation()) {
            case SET -> product.updateStock(request.quantity());
            case ADD -> product.addStock(request.quantity());
            case SUBTRACT -> product.subtractStock(request.quantity());
        }

        product = productRepository.save(product);
        log.info("Stock updated for product: {}, new quantity: {}", id, product.getStockQuantity());

        Category category = categoryRepository.findById(product.getCategoryId()).orElseThrow();
        List<String> images = getProductImageUrls(id);
        return ProductResponse.from(product, CategoryListResponse.from(category), images);
    }

    /**
     * Get product by ID.
     */
    public ProductResponse getProductById(UUID id) {
        Product product = findProductById(id);
        Category category = categoryRepository.findById(product.getCategoryId()).orElseThrow();
        List<String> images = getProductImageUrls(id);
        return ProductResponse.from(product, CategoryListResponse.from(category), images);
    }

    /**
     * Get product by slug.
     */
    public ProductResponse getProductBySlug(String slug) {
        Product product = productRepository.findBySlugAndIsDeletedFalse(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "slug", slug));
        Category category = categoryRepository.findById(product.getCategoryId()).orElseThrow();
        List<String> images = getProductImageUrls(product.getId());
        return ProductResponse.from(product, CategoryListResponse.from(category), images);
    }

    /**
     * Get all products with pagination (public view - active only).
     */
    public Page<ProductListResponse> getAllActiveProducts(Pageable pageable) {
        return productRepository.findAllByStatusAndIsDeletedFalse(ProductStatus.ACTIVE, pageable)
                .map(this::toProductListResponse);
    }

    /**
     * Get all products with pagination (admin view).
     */
    public Page<ProductListResponse> getAllProducts(Pageable pageable) {
        return productRepository.findAllByIsDeletedFalse(pageable)
                .map(this::toProductListResponse);
    }

    /**
     * Get products by category.
     */
    public Page<ProductListResponse> getProductsByCategory(UUID categoryId, Pageable pageable) {
        // Validate category exists
        validateCategory(categoryId);

        return productRepository.findAllByCategoryIdAndStatusAndIsDeletedFalse(
                        categoryId, ProductStatus.ACTIVE, pageable)
                .map(this::toProductListResponse);
    }

    /**
     * Get featured products.
     */
    public Page<ProductListResponse> getFeaturedProducts(Pageable pageable) {
        return productRepository.findAllByFeaturedTrueAndStatusAndIsDeletedFalse(
                        ProductStatus.ACTIVE, pageable)
                .map(this::toProductListResponse);
    }

    /**
     * Search products.
     */
    public Page<ProductListResponse> searchProducts(String query, Pageable pageable) {
        return productRepository.searchProducts(query, ProductStatus.ACTIVE, pageable)
                .map(this::toProductListResponse);
    }

    /**
     * Delete product (soft delete).
     */
    @Transactional
    public void deleteProduct(UUID id) {
        log.info("Deleting product: {}", id);

        Product product = findProductById(id);
        product.setIsDeleted(true);
        productRepository.save(product);

        log.info("Product deleted: {}", id);
    }

    /**
     * Get low stock products (admin).
     */
    public List<ProductListResponse> getLowStockProducts() {
        return productRepository.findLowStockProducts().stream()
                .map(this::toProductListResponse)
                .collect(Collectors.toList());
    }

    // Helper methods

    private Product findProductById(UUID id) {
        return productRepository.findById(id)
                .filter(p -> !p.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
    }

    private Category validateCategory(UUID categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .filter(c -> !c.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));

        if (!category.getIsActive()) {
            throw new BusinessException(
                    "Category is not active",
                    "CATEGORY_INACTIVE",
                    HttpStatus.BAD_REQUEST
            );
        }

        return category;
    }

    private List<String> saveProductImages(UUID productId, List<String> imageUrls) {
        List<String> savedUrls = new ArrayList<>();
        for (int i = 0; i < imageUrls.size() && i < 10; i++) {
            ProductImage image = ProductImage.builder()
                    .productId(productId)
                    .imageUrl(imageUrls.get(i))
                    .displayOrder(i)
                    .build();
            productImageRepository.save(image);
            savedUrls.add(image.getImageUrl());
        }
        return savedUrls;
    }

    private List<String> getProductImageUrls(UUID productId) {
        return productImageRepository.findAllByProductIdAndIsDeletedFalseOrderByDisplayOrderAsc(productId)
                .stream()
                .map(ProductImage::getImageUrl)
                .collect(Collectors.toList());
    }

    private void deleteProductImages(UUID productId) {
        List<ProductImage> images = productImageRepository
                .findAllByProductIdAndIsDeletedFalseOrderByDisplayOrderAsc(productId);
        images.forEach(image -> image.setIsDeleted(true));
        productImageRepository.saveAll(images);
    }

    private ProductListResponse toProductListResponse(Product product) {
        Category category = categoryRepository.findById(product.getCategoryId()).orElseThrow();
        return ProductListResponse.from(product, CategoryListResponse.from(category));
    }
}

