package com.dibimbing.inventory_sales_api.dto.stock;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StockAdjustRequest {
    @NotNull(message = "warehouseId is required")
    private Long warehouseId;

    @NotNull(message = "productId is required")
    private Long productId;

    @NotNull(message = "delta is required")
    private Long delta;

    private String note;
}