package com.dibimbing.inventory_sales_api.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class OrderCreateRequest {
    @NotNull(message = "customerId is required")
    private Long customerId;

    @NotNull(message = "warehouseId is required")
    private Long warehouseId;

    @Valid
    @NotEmpty(message = "items cannot be empty")
    private List<OrderItemRequest> items;
}
