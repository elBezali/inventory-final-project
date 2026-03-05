package com.dibimbing.inventory_sales_api.controller;

import com.dibimbing.inventory_sales_api.constant.ApiPath;
import com.dibimbing.inventory_sales_api.dto.common.ApiResponse;
import com.dibimbing.inventory_sales_api.dto.common.PageMeta;
import com.dibimbing.inventory_sales_api.dto.warehouse.WarehouseCreateRequest;
import com.dibimbing.inventory_sales_api.dto.warehouse.WarehouseResponse;
import com.dibimbing.inventory_sales_api.dto.warehouse.WarehouseUpdateRequest;
import com.dibimbing.inventory_sales_api.service.WarehouseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiPath.WAREHOUSES)
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ApiResponse<WarehouseResponse> create(@Valid @RequestBody WarehouseCreateRequest req) {
        return ApiResponse.ok("Warehouse created", warehouseService.create(req));
    }

    @GetMapping
    public ApiResponse<List<WarehouseResponse>> list(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        Page<WarehouseResponse> result = warehouseService.list(q, page, limit);
        PageMeta meta = warehouseService.meta(result);
        return ApiResponse.ok("Warehouses fetched", result.getContent(), meta);
    }

    @GetMapping("/{id}")
    public ApiResponse<WarehouseResponse> get(@PathVariable Long id) {
        return ApiResponse.ok("Warehouse fetched", warehouseService.getById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ApiResponse<WarehouseResponse> update(@PathVariable Long id, @Valid @RequestBody WarehouseUpdateRequest req) {
        return ApiResponse.ok("Warehouse updated", warehouseService.update(id, req));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResponse<Object> deleteSoft(@PathVariable Long id) {
        warehouseService.deleteSoft(id);
        return ApiResponse.ok("Warehouse soft-deactivated", null);
    }
}