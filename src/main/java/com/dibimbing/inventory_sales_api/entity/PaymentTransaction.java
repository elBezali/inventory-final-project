package com.dibimbing.inventory_sales_api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "payment_transactions")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransaction {

    public enum Method { VA, TRANSFER, EWALLET, CASH }
    public enum Status { INITIATED, SUCCESS, FAILED, EXPIRED, REFUNDED }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private SalesOrder order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Method paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    private LocalDateTime paidAt;

    @Column(length = 120)
    private String providerRef;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) this.status = Status.INITIATED;
        if (this.amount == null) this.amount = BigDecimal.ZERO;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}