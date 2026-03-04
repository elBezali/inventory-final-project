INSERT INTO roles (name, created_at) VALUES ('ADMIN', NOW()) ON DUPLICATE KEY UPDATE name=name;
INSERT INTO roles (name, created_at) VALUES ('USER', NOW()) ON DUPLICATE KEY UPDATE name=name;

INSERT INTO categories (name, description, created_at, updated_at)
VALUES ('General', 'Default category', NOW(), NOW())
ON DUPLICATE KEY UPDATE name=name;

INSERT INTO warehouses (name, address, is_active, created_at, updated_at)
VALUES ('Main Warehouse', 'Jakarta', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE name=name;