-- V2__seed_master.sql

INSERT INTO roles (name, created_at)
VALUES ('ADMIN', NOW()) AS new
ON DUPLICATE KEY UPDATE name = new.name;

INSERT INTO roles (name, created_at)
VALUES ('USER', NOW()) AS new
ON DUPLICATE KEY UPDATE name = new.name;

INSERT INTO categories (name, description, created_at, updated_at)
VALUES ('General', 'Default category', NOW(), NOW()) AS new
ON DUPLICATE KEY UPDATE
  name = new.name,
  description = new.description,
  updated_at = NOW();

INSERT INTO warehouses (name, address, is_active, created_at, updated_at)
VALUES ('Main Warehouse', 'Jakarta', 1, NOW(), NOW()) AS new
ON DUPLICATE KEY UPDATE
  name = new.name,
  address = new.address,
  is_active = new.is_active,
  updated_at = NOW();