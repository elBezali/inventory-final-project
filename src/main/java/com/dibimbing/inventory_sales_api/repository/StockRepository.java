package com.dibimbing.inventory_sales_api.repository;

import com.dibimbing.inventory_sales_api.entity.Stock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findByWarehouseIdAndProductId(Long warehouseId, Long productId);

    Page<Stock> findByWarehouseId(Long warehouseId, Pageable pageable);
    Page<Stock> findByProductId(Long productId, Pageable pageable);
    Page<Stock> findByWarehouseIdAndProductId(Long warehouseId, Long productId, Pageable pageable);
}