package com.dibimbing.inventory_sales_api.dto.warehouse;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WarehouseCreateRequest {
    @NotBlank(message = "name is required")
    private String name;

    private String address;
}