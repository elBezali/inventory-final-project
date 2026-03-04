package com.dibimbing.inventory_sales_api.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class LowStockRow {
    private Long productId;
    private String productName;
    private Long warehouseId;
    private Long onHand;
    private Long reserved;
    private Long available;
}