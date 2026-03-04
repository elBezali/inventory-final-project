package com.dibimbing.inventory_sales_api.repository;

import com.dibimbing.inventory_sales_api.entity.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {
    Optional<SalesOrder> findByOrderNo(String orderNo);
}