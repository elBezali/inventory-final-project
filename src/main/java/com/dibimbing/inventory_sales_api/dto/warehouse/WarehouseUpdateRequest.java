package com.dibimbing.inventory_sales_api.dto.warehouse;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WarehouseUpdateRequest {
    @NotBlank(message = "name is required")
    private String name;

    private String address;

    @NotNull(message = "isActive is required")
    private Boolean isActive;
}