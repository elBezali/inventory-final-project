package com.dibimbing.inventory_sales_api.repository;

import com.dibimbing.inventory_sales_api.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findByWarehouseIdAndProductId(Long warehouseId, Long productId);
}