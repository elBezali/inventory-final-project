package com.dibimbing.inventory_sales_api.dto.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderItemRequest {
    @NotNull(message = "productId is required")
    private Long productId;

    @NotNull(message = "qty is required")
    @Min(value = 1, message = "qty must be >= 1")
    private Integer qty;
}