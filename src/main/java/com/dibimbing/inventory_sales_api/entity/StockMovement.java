package com.dibimbing.inventory_sales_api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "stock_movements")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockMovement {

    public enum Type { IN, OUT, RESERVE, RELEASE, ADJUSTMENT }
    public enum ReferenceType { ORDER, PAYMENT, SHIPMENT, MANUAL }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Type type;

    @Column(nullable = false)
    private long qty;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReferenceType referenceType;

    private Long referenceId;

    @Column(length = 255)
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}