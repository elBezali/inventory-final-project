package com.dibimbing.inventory_sales_api.dto.shipment;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ShipmentCreateRequest {
    @NotNull(message = "orderId is required")
    private Long orderId;

    // snapshot alamat (string) - sesuai entity kamu
    private String addressSnapshot;
}