-- V1__init_schema.sql
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS roles (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL,
  created_at DATETIME NOT NULL,
  UNIQUE KEY uk_roles_name (name)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  full_name VARCHAR(150) NOT NULL,
  email VARCHAR(190) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  status VARCHAR(20) NOT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS user_roles (
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, role_id),
  CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS categories (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(150) NOT NULL,
  description TEXT,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  UNIQUE KEY uk_categories_name (name)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS products (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  sku VARCHAR(80) NOT NULL,
  name VARCHAR(200) NOT NULL,
  category_id BIGINT NOT NULL,
  price DECIMAL(15,2) NOT NULL,
  is_active TINYINT(1) NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  UNIQUE KEY uk_products_sku (sku),
  KEY idx_products_category (category_id),
  CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS warehouses (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(150) NOT NULL,
  address TEXT,
  is_active TINYINT(1) NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  UNIQUE KEY uk_warehouses_name (name)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS stocks (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  warehouse_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  on_hand BIGINT NOT NULL DEFAULT 0,
  reserved BIGINT NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  UNIQUE KEY uk_stocks_wh_product (warehouse_id, product_id),
  KEY idx_stocks_product (product_id),
  CONSTRAINT fk_stocks_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouses(id),
  CONSTRAINT fk_stocks_product FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS customers (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(200) NOT NULL,
  email VARCHAR(190),
  phone VARCHAR(50),
  address TEXT,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  KEY idx_customers_email (email)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS sales_orders (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_no VARCHAR(50) NOT NULL,
  customer_id BIGINT NOT NULL,
  warehouse_id BIGINT NOT NULL,
  status VARCHAR(30) NOT NULL,
  total_amount DECIMAL(15,2) NOT NULL DEFAULT 0.00,
  created_by BIGINT NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  UNIQUE KEY uk_sales_orders_order_no (order_no),
  KEY idx_sales_orders_customer (customer_id),
  KEY idx_sales_orders_warehouse (warehouse_id),
  KEY idx_sales_orders_created_by (created_by),
  CONSTRAINT fk_sales_orders_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
  CONSTRAINT fk_sales_orders_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouses(id),
  CONSTRAINT fk_sales_orders_created_by FOREIGN KEY (created_by) REFERENCES users(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS sales_order_items (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  qty BIGINT NOT NULL,
  unit_price DECIMAL(15,2) NOT NULL,
  subtotal DECIMAL(15,2) NOT NULL,
  KEY idx_so_items_order (order_id),
  KEY idx_so_items_product (product_id),
  CONSTRAINT fk_so_items_order FOREIGN KEY (order_id) REFERENCES sales_orders(id) ON DELETE CASCADE,
  CONSTRAINT fk_so_items_product FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS payment_transactions (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  payment_method VARCHAR(30) NOT NULL,
  status VARCHAR(30) NOT NULL,
  amount DECIMAL(15,2) NOT NULL,
  paid_at DATETIME NULL,
  provider_ref VARCHAR(120),
  created_at DATETIME NOT NULL,
  KEY idx_payments_order (order_id),
  CONSTRAINT fk_payments_order FOREIGN KEY (order_id) REFERENCES sales_orders(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS shipments (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  shipment_no VARCHAR(60) NOT NULL,
  status VARCHAR(30) NOT NULL,
  shipped_at DATETIME NULL,
  delivered_at DATETIME NULL,
  address_snapshot TEXT,
  created_at DATETIME NOT NULL,
  UNIQUE KEY uk_shipments_shipment_no (shipment_no),
  KEY idx_shipments_order (order_id),
  CONSTRAINT fk_shipments_order FOREIGN KEY (order_id) REFERENCES sales_orders(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS stock_movements (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  warehouse_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  type VARCHAR(20) NOT NULL,
  qty BIGINT NOT NULL,
  reference_type VARCHAR(20) NOT NULL,
  reference_id BIGINT NULL,
  note VARCHAR(255) NULL,
  created_by BIGINT NULL,
  created_at DATETIME NOT NULL,
  KEY idx_stock_mov_wh (warehouse_id),
  KEY idx_stock_mov_product (product_id),
  KEY idx_stock_mov_ref (reference_type, reference_id),
  CONSTRAINT fk_stock_mov_warehouse FOREIGN KEY (warehouse_id) REFERENCES warehouses(id),
  CONSTRAINT fk_stock_mov_product FOREIGN KEY (product_id) REFERENCES products(id),
  CONSTRAINT fk_stock_mov_created_by FOREIGN KEY (created_by) REFERENCES users(id)
) ENGINE=InnoDB;

SET FOREIGN_KEY_CHECKS = 1;