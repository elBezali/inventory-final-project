package com.dibimbing.inventory_sales_api.service;

import com.dibimbing.inventory_sales_api.dto.common.PageMeta;
import com.dibimbing.inventory_sales_api.dto.payment.PaymentTransactionCreateRequest;
import com.dibimbing.inventory_sales_api.dto.payment.PaymentTransactionResponse;
import com.dibimbing.inventory_sales_api.dto.payment.PaymentTransactionUpdateStatusRequest;
import com.dibimbing.inventory_sales_api.entity.PaymentTransaction;
import com.dibimbing.inventory_sales_api.entity.SalesOrder;
import com.dibimbing.inventory_sales_api.exception.NotFoundException;
import com.dibimbing.inventory_sales_api.repository.PaymentTransactionRepository;
import com.dibimbing.inventory_sales_api.repository.SalesOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentTransactionService {

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final SalesOrderRepository salesOrderRepository;

    public PaymentTransactionResponse create(PaymentTransactionCreateRequest req) {
        SalesOrder order = salesOrderRepository.findById(req.getOrderId())
                .orElseThrow(() -> new NotFoundException("Order not found"));

        PaymentTransaction tx = PaymentTransaction.builder()
                .order(order)
                .paymentMethod(req.getPaymentMethod())
                .amount(req.getAmount())
                .providerRef(req.getProviderRef())
                .status(PaymentTransaction.Status.INITIATED)
                .build();

        return toResponse(paymentTransactionRepository.save(tx));
    }

    public PaymentTransactionResponse getById(Long id) {
        PaymentTransaction tx = paymentTransactionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Payment transaction not found"));
        return toResponse(tx);
    }

    public Page<PaymentTransactionResponse> list(Long orderId, PaymentTransaction.Status status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        Page<PaymentTransaction> result;
        if (orderId != null && status != null) {
            result = paymentTransactionRepository.findByOrderIdAndStatus(orderId, status, pageable);
        } else if (orderId != null) {
            result = paymentTransactionRepository.findByOrderId(orderId, pageable);
        } else if (status != null) {
            result = paymentTransactionRepository.findByStatus(status, pageable);
        } else {
            result = paymentTransactionRepository.findAll(pageable);
        }

        return result.map(this::toResponse);
    }

    public PaymentTransactionResponse updateStatus(Long id, PaymentTransactionUpdateStatusRequest req) {
        PaymentTransaction tx = paymentTransactionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Payment transaction not found"));

        tx.setStatus(req.getStatus());
        if (req.getStatus() == PaymentTransaction.Status.SUCCESS) {
            tx.setPaidAt(LocalDateTime.now());
        }

        return toResponse(paymentTransactionRepository.save(tx));
    }

    public PageMeta meta(Page<?> page) {
        return PageMeta.builder()
                .currentPage(page.getNumber())
                .pageSize(page.getSize())
                .totalItems(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    private PaymentTransactionResponse toResponse(PaymentTransaction tx) {
        return PaymentTransactionResponse.builder()
                .id(tx.getId())
                .orderId(tx.getOrder().getId())
                .paymentMethod(tx.getPaymentMethod())
                .status(tx.getStatus())
                .amount(tx.getAmount())
                .paidAt(tx.getPaidAt())
                .providerRef(tx.getProviderRef())
                .build();
    }
}