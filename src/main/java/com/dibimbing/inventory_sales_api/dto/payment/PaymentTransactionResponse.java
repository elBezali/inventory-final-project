package com.dibimbing.inventory_sales_api.dto.payment;

import com.dibimbing.inventory_sales_api.entity.PaymentTransaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
public class PaymentTransactionResponse {
    private Long id;
    private Long orderId;

    private PaymentTransaction.Method paymentMethod;
    private PaymentTransaction.Status status;

    private BigDecimal amount;
    private LocalDateTime paidAt;

    private String providerRef;
}