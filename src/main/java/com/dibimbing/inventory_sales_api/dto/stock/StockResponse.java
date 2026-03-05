package com.dibimbing.inventory_sales_api.dto.stock;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class StockResponse {
    private Long id;

    private Long warehouseId;
    private String warehouseName;

    private Long productId;
    private String productSku;
    private String productName;

    private long onHand;
    private long reserved;
    private long available;
}