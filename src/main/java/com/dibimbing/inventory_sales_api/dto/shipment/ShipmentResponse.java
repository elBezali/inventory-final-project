package com.dibimbing.inventory_sales_api.dto.shipment;

import com.dibimbing.inventory_sales_api.entity.Shipment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class ShipmentResponse {
    private Long id;
    private Long orderId;

    private String shipmentNo;
    private Shipment.Status status;

    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;

    private String addressSnapshot;
}