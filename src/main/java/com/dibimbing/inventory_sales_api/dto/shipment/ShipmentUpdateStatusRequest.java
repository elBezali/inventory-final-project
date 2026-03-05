package com.dibimbing.inventory_sales_api.dto.shipment;

import com.dibimbing.inventory_sales_api.entity.Shipment;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ShipmentUpdateStatusRequest {
    @NotNull(message = "status is required")
    private Shipment.Status status;
}