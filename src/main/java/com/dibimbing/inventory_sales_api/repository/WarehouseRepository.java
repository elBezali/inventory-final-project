package com.dibimbing.inventory_sales_api.repository;

import com.dibimbing.inventory_sales_api.entity.Warehouse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    boolean existsByNameIgnoreCase(String name);
    Page<Warehouse> findByNameContainingIgnoreCase(String q, Pageable pageable);
}