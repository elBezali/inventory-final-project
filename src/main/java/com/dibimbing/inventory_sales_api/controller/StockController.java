package com.dibimbing.inventory_sales_api.controller;

import com.dibimbing.inventory_sales_api.constant.ApiPath;
import com.dibimbing.inventory_sales_api.dto.common.ApiResponse;
import com.dibimbing.inventory_sales_api.dto.common.PageMeta;
import com.dibimbing.inventory_sales_api.dto.stock.StockAdjustRequest;
import com.dibimbing.inventory_sales_api.dto.stock.StockResponse;
import com.dibimbing.inventory_sales_api.service.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiPath.STOCKS)
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @GetMapping
    public ApiResponse<List<StockResponse>> list(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        Page<StockResponse> result = stockService.list(warehouseId, productId, page, limit);
        PageMeta meta = stockService.meta(result);
        return ApiResponse.ok("Stocks fetched", result.getContent(), meta);
    }

    @GetMapping("/{id}")
    public ApiResponse<StockResponse> get(@PathVariable Long id) {
        return ApiResponse.ok("Stock fetched", stockService.getById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/adjust")
    public ApiResponse<StockResponse> adjust(@Valid @RequestBody StockAdjustRequest req) {
        return ApiResponse.ok("Stock adjusted", stockService.adjust(req));
    }
}