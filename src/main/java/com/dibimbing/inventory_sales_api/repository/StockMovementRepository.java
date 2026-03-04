package com.dibimbing.inventory_sales_api.repository;

import com.dibimbing.inventory_sales_api.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
}