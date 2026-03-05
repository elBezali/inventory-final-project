package com.dibimbing.inventory_sales_api.dto.payment;

import com.dibimbing.inventory_sales_api.entity.PaymentTransaction;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentTransactionCreateRequest {
    @NotNull(message = "orderId is required")
    private Long orderId;

    @NotNull(message = "paymentMethod is required")
    private PaymentTransaction.Method paymentMethod;

    @NotNull(message = "amount is required")
    private BigDecimal amount;

    private String providerRef;
}