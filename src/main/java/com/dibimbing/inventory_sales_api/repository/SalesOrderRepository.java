package com.dibimbing.inventory_sales_api.repository;

import com.dibimbing.inventory_sales_api.entity.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {

    Optional<SalesOrder> findByOrderNo(String orderNo);

    @Query("""
        select distinct so
        from SalesOrder so
        left join fetch so.customer
        left join fetch so.warehouse
        left join fetch so.items i
        left join fetch i.product
        where so.id = :id
    """)
    Optional<SalesOrder> findDetailById(@Param("id") Long id);
}