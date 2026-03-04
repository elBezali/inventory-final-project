package com.dibimbing.inventory_sales_api.controller;

import com.dibimbing.inventory_sales_api.constant.ApiPath;
import com.dibimbing.inventory_sales_api.dto.common.ApiResponse;
import com.dibimbing.inventory_sales_api.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiPath.REPORTS)
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/top-products")
    public ApiResponse<Object> topProducts(@RequestParam(defaultValue = "5") int limit) {
        return ApiResponse.ok("Top products fetched", reportService.topProducts(limit));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/low-stock")
    public ApiResponse<Object> lowStock(@RequestParam(defaultValue = "5") long threshold) {
        return ApiResponse.ok("Low stock fetched", reportService.lowStock(threshold));
    }
}