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
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentTransactionService {

    private static final Logger AUDIT = LoggerFactory.getLogger("com.dibimbing.inventory_sales_api.audit");

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final SalesOrderRepository salesOrderRepository;

    public PaymentTransactionResponse create(PaymentTransactionCreateRequest req) {
        log.info("PaymentTransactionService.create called orderId={} method={} amount={}",
                req.getOrderId(), req.getPaymentMethod(), req.getAmount());

        SalesOrder order = salesOrderRepository.findById(req.getOrderId())
                .orElseThrow(() -> {
                    log.warn("Create payment tx failed: order not found orderId={}", req.getOrderId());
                    return new NotFoundException("Order not found");
                });

        PaymentTransaction tx = PaymentTransaction.builder()
                .order(order)
                .paymentMethod(req.getPaymentMethod())
                .amount(req.getAmount())
                .providerRef(req.getProviderRef())
                .status(PaymentTransaction.Status.INITIATED)
                .build();

        PaymentTransaction saved = paymentTransactionRepository.save(tx);

        log.info("Payment tx created paymentId={} orderId={} status={}", saved.getId(), order.getId(), saved.getStatus());
        AUDIT.info("PAYMENT_TX_CREATE paymentId=%d orderId=%d status=%s amount=%s"
                .formatted(saved.getId(), order.getId(), saved.getStatus(), saved.getAmount()));

        return toResponse(saved);
    }

    public PaymentTransactionResponse getById(Long id) {
        log.debug("PaymentTransactionService.getById called id={}", id);

        PaymentTransaction tx = paymentTransactionRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Payment tx not found id={}", id);
                    return new NotFoundException("Payment transaction not found");
                });

        return toResponse(tx);
    }

    public Page<PaymentTransactionResponse> list(Long orderId, PaymentTransaction.Status status, int page, int size) {
        log.debug("PaymentTransactionService.list called orderId={} status={} page={} size={}", orderId, status, page, size);

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
        log.info("PaymentTransactionService.updateStatus called id={} newStatus={}", id, req.getStatus());

        PaymentTransaction tx = paymentTransactionRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Update payment tx failed: not found id={}", id);
                    return new NotFoundException("Payment transaction not found");
                });

        tx.setStatus(req.getStatus());
        if (req.getStatus() == PaymentTransaction.Status.SUCCESS) {
            tx.setPaidAt(LocalDateTime.now());
        }

        PaymentTransaction saved = paymentTransactionRepository.save(tx);

        log.info("Payment tx status updated id={} status={} paidAt={}", saved.getId(), saved.getStatus(), saved.getPaidAt());
        AUDIT.info("PAYMENT_TX_STATUS_UPDATE paymentId=%d orderId=%d status=%s"
                .formatted(saved.getId(), saved.getOrder().getId(), saved.getStatus()));

        return toResponse(saved);
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