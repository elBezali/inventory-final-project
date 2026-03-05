package com.dibimbing.inventory_sales_api.dto.payment;

import com.dibimbing.inventory_sales_api.entity.PaymentTransaction;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentTransactionUpdateStatusRequest {
    @NotNull(message = "status is required")
    private PaymentTransaction.Status status;
}