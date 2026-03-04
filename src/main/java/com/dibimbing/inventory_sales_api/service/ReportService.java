package com.dibimbing.inventory_sales_api.service;

import com.dibimbing.inventory_sales_api.dto.report.LowStockRow;
import com.dibimbing.inventory_sales_api.dto.report.TopProductRow;
import com.dibimbing.inventory_sales_api.entity.SalesOrder;
import com.dibimbing.inventory_sales_api.repository.SalesOrderRepository;
import com.dibimbing.inventory_sales_api.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final SalesOrderRepository salesOrderRepository;
    private final StockRepository stockRepository;

    public List<TopProductRow> topProducts(int limit) {
        // simple approach (bootcamp-friendly): aggregate from PAID/COMPLETED orders
        var orders = salesOrderRepository.findAll().stream()
                .filter(o -> o.getStatus() == SalesOrder.Status.PAID || o.getStatus() == SalesOrder.Status.COMPLETED || o.getStatus() == SalesOrder.Status.SHIPPED)
                .toList();

        Map<Long, Long> qtyByProduct = new HashMap<>();
        Map<Long, String> nameByProduct = new HashMap<>();

        for (var o : orders) {
            for (var it : o.getItems()) {
                qtyByProduct.merge(it.getProduct().getId(), (long) it.getQty(), Long::sum);
                nameByProduct.putIfAbsent(it.getProduct().getId(), it.getProduct().getName());
            }
        }

        return qtyByProduct.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(limit)
                .map(e -> TopProductRow.builder()
                        .productId(e.getKey())
                        .productName(nameByProduct.get(e.getKey()))
                        .totalQtySold(e.getValue())
                        .build())
                .collect(Collectors.toList());
    }

    public List<LowStockRow> lowStock(long threshold) {
        return stockRepository.findAll().stream()
                .map(s -> LowStockRow.builder()
                        .productId(s.getProduct().getId())
                        .productName(s.getProduct().getName())
                        .warehouseId(s.getWarehouse().getId())
                        .onHand(s.getOnHand())
                        .reserved(s.getReserved())
                        .available(s.available())
                        .build())
                .filter(r -> r.getAvailable() <= threshold)
                .toList();
    }
}