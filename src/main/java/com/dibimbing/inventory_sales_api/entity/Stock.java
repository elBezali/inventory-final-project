package com.dibimbing.inventory_sales_api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "stocks",
       uniqueConstraints = @UniqueConstraint(name = "uk_stocks_wh_product", columnNames = {"warehouse_id", "product_id"}))
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stock {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private long onHand;

    @Column(nullable = false)
    private long reserved;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.onHand < 0) this.onHand = 0;
        if (this.reserved < 0) this.reserved = 0;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public long available() {
        return this.onHand - this.reserved;
    }
}