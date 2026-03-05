package com.dibimbing.inventory_sales_api.service;

import com.dibimbing.inventory_sales_api.dto.report.LowStockRow;
import com.dibimbing.inventory_sales_api.dto.report.TopProductRow;
import com.dibimbing.inventory_sales_api.entity.SalesOrder;
import com.dibimbing.inventory_sales_api.repository.SalesOrderRepository;
import com.dibimbing.inventory_sales_api.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private static final Logger AUDIT = LoggerFactory.getLogger("com.dibimbing.inventory_sales_api.audit");

    private final SalesOrderRepository salesOrderRepository;
    private final StockRepository stockRepository;

    public List<TopProductRow> topProducts(int limit) {
        int safeLimit = Math.max(1, limit);
        log.info("ReportService.topProducts called limit={}", safeLimit);

        var orders = salesOrderRepository.findAll().stream()
                .filter(o -> o.getStatus() == SalesOrder.Status.PAID
                        || o.getStatus() == SalesOrder.Status.COMPLETED
                        || o.getStatus() == SalesOrder.Status.SHIPPED)
                .toList();

        Map<Long, Long> qtyByProduct = new HashMap<>();
        Map<Long, String> nameByProduct = new HashMap<>();

        for (var o : orders) {
            for (var it : o.getItems()) {
                qtyByProduct.merge(it.getProduct().getId(), (long) it.getQty(), Long::sum);
                nameByProduct.putIfAbsent(it.getProduct().getId(), it.getProduct().getName());
            }
        }

        List<TopProductRow> result = qtyByProduct.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(safeLimit)
                .map(e -> TopProductRow.builder()
                        .productId(e.getKey())
                        .productName(nameByProduct.get(e.getKey()))
                        .totalQtySold(e.getValue())
                        .build())
                .collect(Collectors.toList());

        log.info("ReportService.topProducts done resultSize={} fromOrders={}", result.size(), orders.size());
        AUDIT.info("REPORT_TOP_PRODUCTS limit=%d resultSize=%d".formatted(safeLimit, result.size()));

        return result;
    }

    public List<LowStockRow> lowStock(long threshold) {
        long safeThreshold = Math.max(0, threshold);
        log.info("ReportService.lowStock called threshold={}", safeThreshold);

        List<LowStockRow> result = stockRepository.findAll().stream()
                .map(s -> LowStockRow.builder()
                        .productId(s.getProduct().getId())
                        .productName(s.getProduct().getName())
                        .warehouseId(s.getWarehouse().getId())
                        .onHand(s.getOnHand())
                        .reserved(s.getReserved())
                        .available(s.available())
                        .build())
                .filter(r -> r.getAvailable() <= safeThreshold)
                .toList();

        log.info("ReportService.lowStock done resultSize={}", result.size());
        AUDIT.info("REPORT_LOW_STOCK threshold=%d resultSize=%d".formatted(safeThreshold, result.size()));

        return result;
    }
}