-- ============================================================================
-- Initial Database Schema - Audit & Foundation
-- Version: V1__Initial_Schema.sql
-- Description: Base tables with audit fields, UUID support, and indexes
-- ============================================================================

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm"; -- For text search optimization

-- Set timezone to UTC
SET timezone = 'UTC';

-- ============================================================================
-- Helper Functions
-- ============================================================================

-- Function to update updated_at timestamp automatically
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- USERS TABLE
-- ============================================================================
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20),
    role VARCHAR(50) NOT NULL DEFAULT 'CUSTOMER', -- CUSTOMER, ADMIN, SUPER_ADMIN
    email_verified BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    is_deleted BOOLEAN DEFAULT FALSE,
    last_login_at TIMESTAMP WITH TIME ZONE,

    -- Audit fields
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by UUID,
    updated_by UUID
);

-- Indexes for users table
CREATE INDEX idx_users_email ON users(email) WHERE is_deleted = FALSE;
CREATE INDEX idx_users_role ON users(role) WHERE is_deleted = FALSE;
CREATE INDEX idx_users_phone ON users(phone_number) WHERE is_deleted = FALSE;
CREATE INDEX idx_users_active ON users(is_active, is_deleted);

-- Trigger for updating updated_at
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- REFRESH TOKENS TABLE (for JWT refresh token management)
-- ============================================================================
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(500) NOT NULL UNIQUE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    is_revoked BOOLEAN DEFAULT FALSE,

    -- Audit fields
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    revoked_at TIMESTAMP WITH TIME ZONE,
    ip_address VARCHAR(45),
    user_agent TEXT
);

-- Indexes for refresh_tokens table
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token) WHERE is_revoked = FALSE;
CREATE INDEX idx_refresh_tokens_expires ON refresh_tokens(expires_at) WHERE is_revoked = FALSE;

-- ============================================================================
-- CATEGORIES TABLE
-- ============================================================================
CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    parent_id UUID REFERENCES categories(id) ON DELETE SET NULL,
    image_url VARCHAR(500),
    display_order INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    is_deleted BOOLEAN DEFAULT FALSE,

    -- SEO fields
    meta_title VARCHAR(255),
    meta_description TEXT,
    meta_keywords VARCHAR(500),

    -- Audit fields
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id)
);

-- Indexes for categories table
CREATE INDEX idx_categories_slug ON categories(slug) WHERE is_deleted = FALSE;
CREATE INDEX idx_categories_parent ON categories(parent_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_categories_active ON categories(is_active, is_deleted);
CREATE INDEX idx_categories_order ON categories(display_order);

-- Trigger for updating updated_at
CREATE TRIGGER update_categories_updated_at BEFORE UPDATE ON categories
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- PRODUCTS TABLE
-- ============================================================================
CREATE TABLE products (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    short_description VARCHAR(500),
    category_id UUID REFERENCES categories(id) ON DELETE SET NULL,

    -- Pricing (stored in smallest currency unit - paisa for BDT)
    price_bdt BIGINT NOT NULL, -- Price in paisa (1 BDT = 100 paisa)
    compare_at_price_bdt BIGINT, -- Original price for discount display
    cost_price_bdt BIGINT, -- Cost for margin calculation

    -- Inventory
    sku VARCHAR(100) UNIQUE,
    barcode VARCHAR(100),
    track_inventory BOOLEAN DEFAULT TRUE,
    stock_quantity INTEGER DEFAULT 0,
    low_stock_threshold INTEGER DEFAULT 10,
    allow_backorder BOOLEAN DEFAULT FALSE,

    -- Product attributes
    weight_grams INTEGER, -- Weight in grams
    dimensions_cm VARCHAR(50), -- e.g., "10x20x5"
    is_featured BOOLEAN DEFAULT FALSE,
    is_new_arrival BOOLEAN DEFAULT FALSE,
    is_bestseller BOOLEAN DEFAULT FALSE,

    -- Status
    status VARCHAR(50) DEFAULT 'DRAFT', -- DRAFT, ACTIVE, ARCHIVED
    is_deleted BOOLEAN DEFAULT FALSE,
    published_at TIMESTAMP WITH TIME ZONE,

    -- SEO fields
    meta_title VARCHAR(255),
    meta_description TEXT,
    meta_keywords VARCHAR(500),

    -- Audit fields
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by UUID REFERENCES users(id),
    updated_by UUID REFERENCES users(id)
);

-- Indexes for products table
CREATE INDEX idx_products_slug ON products(slug) WHERE is_deleted = FALSE;
CREATE INDEX idx_products_category ON products(category_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_products_sku ON products(sku) WHERE is_deleted = FALSE;
CREATE INDEX idx_products_status ON products(status, is_deleted);
CREATE INDEX idx_products_featured ON products(is_featured) WHERE is_deleted = FALSE;
CREATE INDEX idx_products_price ON products(price_bdt) WHERE is_deleted = FALSE;
CREATE INDEX idx_products_name_trgm ON products USING gin(name gin_trgm_ops); -- For text search

-- Trigger for updating updated_at
CREATE TRIGGER update_products_updated_at BEFORE UPDATE ON products
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- PRODUCT IMAGES TABLE
-- ============================================================================
CREATE TABLE product_images (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    image_url VARCHAR(500) NOT NULL,
    alt_text VARCHAR(255),
    display_order INTEGER DEFAULT 0,
    is_primary BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Indexes for product_images table
CREATE INDEX idx_product_images_product ON product_images(product_id);
CREATE INDEX idx_product_images_primary ON product_images(product_id, is_primary);

-- ============================================================================
-- ADDRESSES TABLE
-- ============================================================================
CREATE TABLE addresses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    address_type VARCHAR(50) DEFAULT 'SHIPPING', -- SHIPPING, BILLING, BOTH
    full_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255),
    city VARCHAR(100) NOT NULL,
    state_division VARCHAR(100), -- Division in Bangladesh context
    postal_code VARCHAR(20),
    country VARCHAR(100) DEFAULT 'Bangladesh',
    is_default BOOLEAN DEFAULT FALSE,
    is_deleted BOOLEAN DEFAULT FALSE,

    -- Audit fields
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Indexes for addresses table
CREATE INDEX idx_addresses_user ON addresses(user_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_addresses_default ON addresses(user_id, is_default) WHERE is_deleted = FALSE;

-- Trigger for updating updated_at
CREATE TRIGGER update_addresses_updated_at BEFORE UPDATE ON addresses
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- CARTS TABLE
-- ============================================================================
CREATE TABLE carts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    session_id VARCHAR(255), -- For guest users
    expires_at TIMESTAMP WITH TIME ZONE,
    is_active BOOLEAN DEFAULT TRUE,

    -- Audit fields
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,

    CONSTRAINT cart_user_or_session CHECK (user_id IS NOT NULL OR session_id IS NOT NULL)
);

-- Indexes for carts table
CREATE INDEX idx_carts_user ON carts(user_id) WHERE is_active = TRUE;
CREATE INDEX idx_carts_session ON carts(session_id) WHERE is_active = TRUE;
CREATE INDEX idx_carts_expires ON carts(expires_at) WHERE is_active = TRUE;

-- Trigger for updating updated_at
CREATE TRIGGER update_carts_updated_at BEFORE UPDATE ON carts
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- CART ITEMS TABLE
-- ============================================================================
CREATE TABLE cart_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    cart_id UUID NOT NULL REFERENCES carts(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    price_at_addition_bdt BIGINT NOT NULL, -- Price when added to cart (in paisa)

    -- Audit fields
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,

    UNIQUE(cart_id, product_id)
);

-- Indexes for cart_items table
CREATE INDEX idx_cart_items_cart ON cart_items(cart_id);
CREATE INDEX idx_cart_items_product ON cart_items(product_id);

-- Trigger for updating updated_at
CREATE TRIGGER update_cart_items_updated_at BEFORE UPDATE ON cart_items
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- ORDERS TABLE
-- ============================================================================
CREATE TABLE orders (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_number VARCHAR(50) NOT NULL UNIQUE, -- Human-readable order number
    user_id UUID NOT NULL REFERENCES users(id),

    -- Order status
    status VARCHAR(50) DEFAULT 'PENDING', -- PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED, REFUNDED
    payment_status VARCHAR(50) DEFAULT 'UNPAID', -- UNPAID, PAID, PARTIALLY_REFUNDED, REFUNDED, FAILED
    fulfillment_status VARCHAR(50) DEFAULT 'UNFULFILLED', -- UNFULFILLED, PARTIALLY_FULFILLED, FULFILLED

    -- Pricing (in paisa)
    subtotal_bdt BIGINT NOT NULL,
    discount_bdt BIGINT DEFAULT 0,
    shipping_cost_bdt BIGINT DEFAULT 0,
    tax_bdt BIGINT DEFAULT 0,
    total_bdt BIGINT NOT NULL,

    -- Shipping address (denormalized for historical record)
    shipping_full_name VARCHAR(255) NOT NULL,
    shipping_phone VARCHAR(20) NOT NULL,
    shipping_address_line1 VARCHAR(255) NOT NULL,
    shipping_address_line2 VARCHAR(255),
    shipping_city VARCHAR(100) NOT NULL,
    shipping_state_division VARCHAR(100),
    shipping_postal_code VARCHAR(20),
    shipping_country VARCHAR(100) DEFAULT 'Bangladesh',

    -- Billing address (can be same as shipping)
    billing_full_name VARCHAR(255),
    billing_phone VARCHAR(20),
    billing_address_line1 VARCHAR(255),
    billing_address_line2 VARCHAR(255),
    billing_city VARCHAR(100),
    billing_state_division VARCHAR(100),
    billing_postal_code VARCHAR(20),
    billing_country VARCHAR(100),

    -- Payment details
    payment_method VARCHAR(50), -- BKASH, NAGAD, ROCKET, CARD, COD
    payment_gateway VARCHAR(50), -- SSLCOMMERZ, STRIPE, PAYPAL
    transaction_id VARCHAR(255), -- Payment gateway transaction ID

    -- Additional info
    customer_note TEXT,
    admin_note TEXT,

    -- Timestamps
    confirmed_at TIMESTAMP WITH TIME ZONE,
    shipped_at TIMESTAMP WITH TIME ZONE,
    delivered_at TIMESTAMP WITH TIME ZONE,
    cancelled_at TIMESTAMP WITH TIME ZONE,

    -- Audit fields
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    created_by UUID,
    updated_by UUID
);

-- Indexes for orders table
CREATE INDEX idx_orders_number ON orders(order_number);
CREATE INDEX idx_orders_user ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_payment_status ON orders(payment_status);
CREATE INDEX idx_orders_transaction ON orders(transaction_id);
CREATE INDEX idx_orders_created ON orders(created_at DESC);

-- Trigger for updating updated_at
CREATE TRIGGER update_orders_updated_at BEFORE UPDATE ON orders
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- ORDER ITEMS TABLE
-- ============================================================================
CREATE TABLE order_items (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id),

    -- Product details (denormalized for historical record)
    product_name VARCHAR(255) NOT NULL,
    product_sku VARCHAR(100),

    -- Pricing (in paisa)
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price_bdt BIGINT NOT NULL,
    subtotal_bdt BIGINT NOT NULL,
    discount_bdt BIGINT DEFAULT 0,
    total_bdt BIGINT NOT NULL,

    -- Audit fields
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Indexes for order_items table
CREATE INDEX idx_order_items_order ON order_items(order_id);
CREATE INDEX idx_order_items_product ON order_items(product_id);

-- ============================================================================
-- PAYMENT TRANSACTIONS TABLE (for audit trail)
-- ============================================================================
CREATE TABLE payment_transactions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,

    -- Transaction details
    transaction_id VARCHAR(255) NOT NULL UNIQUE, -- Gateway transaction ID
    payment_gateway VARCHAR(50) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    amount_bdt BIGINT NOT NULL, -- Amount in paisa
    currency VARCHAR(10) DEFAULT 'BDT',

    -- Status
    status VARCHAR(50) NOT NULL, -- INITIATED, SUCCESS, FAILED, CANCELLED, REFUNDED

    -- Gateway response (store full response for debugging)
    gateway_request_data JSONB,
    gateway_response_data JSONB,

    -- Verification
    is_verified BOOLEAN DEFAULT FALSE,
    verified_at TIMESTAMP WITH TIME ZONE,

    -- IP and security
    ip_address VARCHAR(45),
    user_agent TEXT,

    -- Audit fields
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Indexes for payment_transactions table
CREATE INDEX idx_payment_transactions_order ON payment_transactions(order_id);
CREATE INDEX idx_payment_transactions_tid ON payment_transactions(transaction_id);
CREATE INDEX idx_payment_transactions_status ON payment_transactions(status);
CREATE INDEX idx_payment_transactions_created ON payment_transactions(created_at DESC);

-- Trigger for updating updated_at
CREATE TRIGGER update_payment_transactions_updated_at BEFORE UPDATE ON payment_transactions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- AUDIT LOG TABLE (for tracking all important changes)
-- ============================================================================
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    entity_type VARCHAR(100) NOT NULL, -- e.g., USER, PRODUCT, ORDER
    entity_id UUID NOT NULL,
    action VARCHAR(50) NOT NULL, -- CREATE, UPDATE, DELETE
    performed_by UUID REFERENCES users(id),

    -- Change details
    old_values JSONB,
    new_values JSONB,

    -- Context
    ip_address VARCHAR(45),
    user_agent TEXT,

    -- Timestamp
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- Indexes for audit_logs table
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_user ON audit_logs(performed_by);
CREATE INDEX idx_audit_logs_created ON audit_logs(created_at DESC);

-- ============================================================================
-- Insert default admin user (password: Admin@123 - CHANGE IN PRODUCTION)
-- Password hash is BCrypt hash of "Admin@123"
-- ============================================================================
INSERT INTO users (id, email, password_hash, first_name, last_name, role, email_verified, is_active)
VALUES (
    uuid_generate_v4(),
    'admin@babyshop.com',
    '$2a$10$XQqP8wOvRQXZJJ2X5J5X5Oe5X5X5X5X5X5X5X5X5X5X5X5X5X5X5X', -- BCrypt hash placeholder
    'Super',
    'Admin',
    'SUPER_ADMIN',
    TRUE,
    TRUE
);

-- ============================================================================
-- END OF MIGRATION
-- ============================================================================

