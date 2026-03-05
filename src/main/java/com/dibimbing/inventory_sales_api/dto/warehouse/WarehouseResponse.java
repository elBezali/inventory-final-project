package com.dibimbing.inventory_sales_api.dto.warehouse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class WarehouseResponse {
    private Long id;
    private String name;
    private String address;
    private boolean isActive;
}