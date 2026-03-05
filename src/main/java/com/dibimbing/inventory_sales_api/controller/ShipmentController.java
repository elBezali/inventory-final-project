package com.dibimbing.inventory_sales_api.controller;

import com.dibimbing.inventory_sales_api.constant.ApiPath;
import com.dibimbing.inventory_sales_api.dto.common.ApiResponse;
import com.dibimbing.inventory_sales_api.dto.common.PageMeta;
import com.dibimbing.inventory_sales_api.dto.shipment.ShipmentCreateRequest;
import com.dibimbing.inventory_sales_api.dto.shipment.ShipmentResponse;
import com.dibimbing.inventory_sales_api.dto.shipment.ShipmentUpdateStatusRequest;
import com.dibimbing.inventory_sales_api.entity.Shipment;
import com.dibimbing.inventory_sales_api.service.ShipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiPath.SHIPMENTS)
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ApiResponse<ShipmentResponse> create(@Valid @RequestBody ShipmentCreateRequest req) {
        return ApiResponse.ok("Shipment created", shipmentService.create(req));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ApiResponse<List<ShipmentResponse>> list(
            @RequestParam(required = false) Shipment.Status status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        Page<ShipmentResponse> result = shipmentService.list(status, page, limit);
        PageMeta meta = shipmentService.meta(result);
        return ApiResponse.ok("Shipments fetched", result.getContent(), meta);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ApiResponse<ShipmentResponse> get(@PathVariable Long id) {
        return ApiResponse.ok("Shipment fetched", shipmentService.getById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/by-order/{orderId}")
    public ApiResponse<ShipmentResponse> getByOrder(@PathVariable Long orderId) {
        return ApiResponse.ok("Shipment fetched", shipmentService.getByOrderId(orderId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    public ApiResponse<ShipmentResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody ShipmentUpdateStatusRequest req
    ) {
        return ApiResponse.ok("Shipment status updated", shipmentService.updateStatus(id, req));
    }
}