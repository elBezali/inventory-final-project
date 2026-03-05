package com.dibimbing.inventory_sales_api.repository;

import com.dibimbing.inventory_sales_api.entity.PaymentTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    List<PaymentTransaction> findByOrderId(Long orderId);

    Page<PaymentTransaction> findByOrderId(Long orderId, Pageable pageable);
    Page<PaymentTransaction> findByStatus(PaymentTransaction.Status status, Pageable pageable);
    Page<PaymentTransaction> findByOrderIdAndStatus(Long orderId, PaymentTransaction.Status status, Pageable pageable);
}