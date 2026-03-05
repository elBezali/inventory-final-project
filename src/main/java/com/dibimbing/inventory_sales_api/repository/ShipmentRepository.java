package com.dibimbing.inventory_sales_api.repository;

import com.dibimbing.inventory_sales_api.entity.Shipment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    Optional<Shipment> findByOrderId(Long orderId);
    Page<Shipment> findByStatus(Shipment.Status status, Pageable pageable);
}