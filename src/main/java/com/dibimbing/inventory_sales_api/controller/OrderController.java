package com.dibimbing.inventory_sales_api.controller;

import com.dibimbing.inventory_sales_api.constant.ApiPath;
import com.dibimbing.inventory_sales_api.dto.common.ApiResponse;
import com.dibimbing.inventory_sales_api.dto.order.OrderCreateRequest;
import com.dibimbing.inventory_sales_api.dto.order.OrderResponse;
import com.dibimbing.inventory_sales_api.entity.PaymentTransaction;
import com.dibimbing.inventory_sales_api.service.OrderService;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiPath.ORDERS)
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ApiResponse<OrderResponse> create(@Valid @RequestBody OrderCreateRequest req) {
        return ApiResponse.ok("Order created", orderService.createOrder(req));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ApiResponse<OrderResponse> get(@PathVariable Long id) {
        return ApiResponse.ok("Order fetched", orderService.getOrder(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/payment-success")
    public ApiResponse<Object> paymentSuccess(@PathVariable Long id, @Valid @RequestBody PaymentSuccessRequest req) {
        orderService.markPaymentSuccess(id, req.getMethod(), req.getProviderRef());
        return ApiResponse.ok("Payment marked as SUCCESS, order PAID, stock finalized, shipment created", null);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/cancel")
    public ApiResponse<Object> cancel(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return ApiResponse.ok("Order cancelled & reserved released", null);
    }

    @Data
    public static class PaymentSuccessRequest {
        private PaymentTransaction.Method method;
        private String providerRef;
    }
}