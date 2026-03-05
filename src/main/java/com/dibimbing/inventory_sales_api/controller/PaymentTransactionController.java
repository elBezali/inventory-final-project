package com.dibimbing.inventory_sales_api.controller;

import com.dibimbing.inventory_sales_api.constant.ApiPath;
import com.dibimbing.inventory_sales_api.dto.common.ApiResponse;
import com.dibimbing.inventory_sales_api.dto.common.PageMeta;
import com.dibimbing.inventory_sales_api.dto.payment.PaymentTransactionCreateRequest;
import com.dibimbing.inventory_sales_api.dto.payment.PaymentTransactionResponse;
import com.dibimbing.inventory_sales_api.dto.payment.PaymentTransactionUpdateStatusRequest;
import com.dibimbing.inventory_sales_api.entity.PaymentTransaction;
import com.dibimbing.inventory_sales_api.service.PaymentTransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiPath.PAYMENT_TRANSACTIONS)
@RequiredArgsConstructor
public class PaymentTransactionController {

    private final PaymentTransactionService paymentTransactionService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ApiResponse<PaymentTransactionResponse> create(@Valid @RequestBody PaymentTransactionCreateRequest req) {
        return ApiResponse.ok("Payment transaction created", paymentTransactionService.create(req));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ApiResponse<List<PaymentTransactionResponse>> list(
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) PaymentTransaction.Status status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        Page<PaymentTransactionResponse> result = paymentTransactionService.list(orderId, status, page, limit);
        PageMeta meta = paymentTransactionService.meta(result);
        return ApiResponse.ok("Payment transactions fetched", result.getContent(), meta);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ApiResponse<PaymentTransactionResponse> get(@PathVariable Long id) {
        return ApiResponse.ok("Payment transaction fetched", paymentTransactionService.getById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    public ApiResponse<PaymentTransactionResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody PaymentTransactionUpdateStatusRequest req
    ) {
        return ApiResponse.ok("Payment transaction status updated", paymentTransactionService.updateStatus(id, req));
    }
}