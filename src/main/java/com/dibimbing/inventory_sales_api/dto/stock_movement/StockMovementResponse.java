package com.dibimbing.inventory_sales_api.dto.stock_movement;

import com.dibimbing.inventory_sales_api.entity.StockMovement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class StockMovementResponse {
    private Long id;

    private Long warehouseId;
    private String warehouseName;

    private Long productId;
    private String productSku;
    private String productName;

    private StockMovement.Type type;
    private long qty;

    private StockMovement.ReferenceType referenceType;
    private Long referenceId;

    private String note;
    private Long createdByUserId;

    private LocalDateTime createdAt;
}