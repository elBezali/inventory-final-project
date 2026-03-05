package com.dibimbing.inventory_sales_api.repository;

import com.dibimbing.inventory_sales_api.entity.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    Page<StockMovement> findByWarehouseId(Long warehouseId, Pageable pageable);
    Page<StockMovement> findByProductId(Long productId, Pageable pageable);
    Page<StockMovement> findByType(StockMovement.Type type, Pageable pageable);

    Page<StockMovement> findByWarehouseIdAndProductId(Long warehouseId, Long productId, Pageable pageable);
    Page<StockMovement> findByWarehouseIdAndType(Long warehouseId, StockMovement.Type type, Pageable pageable);
    Page<StockMovement> findByProductIdAndType(Long productId, StockMovement.Type type, Pageable pageable);

    Page<StockMovement> findByWarehouseIdAndProductIdAndType(Long warehouseId, Long productId, StockMovement.Type type, Pageable pageable);
}