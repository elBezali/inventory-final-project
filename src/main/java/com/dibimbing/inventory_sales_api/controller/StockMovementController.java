package com.dibimbing.inventory_sales_api.controller;

import com.dibimbing.inventory_sales_api.constant.ApiPath;
import com.dibimbing.inventory_sales_api.dto.common.ApiResponse;
import com.dibimbing.inventory_sales_api.dto.common.PageMeta;
import com.dibimbing.inventory_sales_api.dto.stock_movement.StockMovementResponse;
import com.dibimbing.inventory_sales_api.entity.StockMovement;
import com.dibimbing.inventory_sales_api.service.StockMovementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiPath.STOCK_MOVEMENTS)
@RequiredArgsConstructor
public class StockMovementController {

    private final StockMovementService stockMovementService;

    @GetMapping
    public ApiResponse<List<StockMovementResponse>> list(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) StockMovement.Type type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        Page<StockMovementResponse> result = stockMovementService.list(warehouseId, productId, type, page, limit);
        PageMeta meta = stockMovementService.meta(result);
        return ApiResponse.ok("Stock movements fetched", result.getContent(), meta);
    }

    @GetMapping("/{id}")
    public ApiResponse<StockMovementResponse> get(@PathVariable Long id) {
        return ApiResponse.ok("Stock movement fetched", stockMovementService.getById(id));
    }
}