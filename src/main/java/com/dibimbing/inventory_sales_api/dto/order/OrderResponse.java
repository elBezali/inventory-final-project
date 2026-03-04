package com.dibimbing.inventory_sales_api.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private String orderNo;
    private String status;
    private Long customerId;
    private Long warehouseId;
    private BigDecimal totalAmount;
    private List<OrderResponseItem> items;

    @Data
    @Builder
    @AllArgsConstructor
    public static class OrderResponseItem {
        private Long productId;
        private String productName;
        private Integer qty;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
    }
}