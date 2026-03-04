package com.dibimbing.inventory_sales_api.repository;

import com.dibimbing.inventory_sales_api.entity.SalesOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalesOrderItemRepository extends JpaRepository<SalesOrderItem, Long> {
}