package com.dibimbing.inventory_sales_api.repository;

import com.dibimbing.inventory_sales_api.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsBySku(String sku);
    Page<Product> findByNameContainingIgnoreCase(String q, Pageable pageable);
}