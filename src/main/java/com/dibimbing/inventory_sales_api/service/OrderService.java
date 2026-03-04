package com.dibimbing.inventory_sales_api.service;

import com.dibimbing.inventory_sales_api.dto.order.OrderCreateRequest;
import com.dibimbing.inventory_sales_api.dto.order.OrderResponse;
import com.dibimbing.inventory_sales_api.entity.*;
import com.dibimbing.inventory_sales_api.exception.BadRequestException;
import com.dibimbing.inventory_sales_api.exception.NotFoundException;
import com.dibimbing.inventory_sales_api.repository.*;
import com.dibimbing.inventory_sales_api.util.OrderNoGenerator;
import com.dibimbing.inventory_sales_api.util.ShipmentNoGenerator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final CustomerRepository customerRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final StockRepository stockRepository;
    private final StockMovementRepository stockMovementRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final ShipmentRepository shipmentRepository;

    @Transactional
    public OrderResponse createOrder(OrderCreateRequest req) {
        Customer customer = customerRepository.findById(req.getCustomerId())
                .orElseThrow(() -> new NotFoundException("Customer not found"));

        Warehouse wh = warehouseRepository.findById(req.getWarehouseId())
                .orElseThrow(() -> new NotFoundException("Warehouse not found"));

        SalesOrder order = SalesOrder.builder()
                .orderNo(OrderNoGenerator.next())
                .customer(customer)
                .warehouse(wh)
                .status(SalesOrder.Status.PENDING_PAYMENT)
                .totalAmount(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for (var itemReq : req.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new NotFoundException("Product not found: " + itemReq.getProductId()));

            if (!product.isActive()) {
                throw new BadRequestException("Product inactive: " + product.getId());
            }

            Stock stock = stockRepository.findByWarehouseIdAndProductId(wh.getId(), product.getId())
                    .orElseThrow(() -> new BadRequestException("Stock not found for productId " + product.getId() + " in warehouse " + wh.getId()));

            long available = stock.available();
            if (available < itemReq.getQty()) {
                throw new BadRequestException("Insufficient stock for productId " + product.getId() + ". Available=" + available);
            }

            // reserve stock
            stock.setReserved(stock.getReserved() + itemReq.getQty());
            stockRepository.save(stock);

            stockMovementRepository.save(StockMovement.builder()
                    .warehouse(wh)
                    .product(product)
                    .type(StockMovement.Type.RESERVE)
                    .qty(itemReq.getQty())
                    .referenceType(StockMovement.ReferenceType.ORDER)
                    .referenceId(null)
                    .note("Reserve for order " + order.getOrderNo())
                    .createdAt(LocalDateTime.now())
                    .build());

            BigDecimal unitPrice = product.getPrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(itemReq.getQty()));

            SalesOrderItem soi = SalesOrderItem.builder()
                    .order(order)
                    .product(product)
                    .qty(itemReq.getQty())
                    .unitPrice(unitPrice)
                    .subtotal(subtotal)
                    .build();

            order.getItems().add(soi);
            total = total.add(subtotal);
        }

        order.setTotalAmount(total);
        SalesOrder saved = salesOrderRepository.save(order);

        return toOrderResponse(saved);
    }

    @Transactional
    public void markPaymentSuccess(Long orderId, PaymentTransaction.Method method, String providerRef) {
        SalesOrder order = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        if (order.getStatus() == SalesOrder.Status.CANCELLED) {
            throw new BadRequestException("Order already cancelled");
        }
        if (order.getStatus() == SalesOrder.Status.PAID) {
            return;
        }
        if (order.getStatus() != SalesOrder.Status.PENDING_PAYMENT) {
            throw new BadRequestException("Order status invalid for payment: " + order.getStatus());
        }

        PaymentTransaction pay = PaymentTransaction.builder()
                .order(order)
                .paymentMethod(method)
                .status(PaymentTransaction.Status.SUCCESS)
                .amount(order.getTotalAmount())
                .paidAt(LocalDateTime.now())
                .providerRef(providerRef)
                .build();
        paymentTransactionRepository.save(pay);

        // finalize stock: reserved -> out (reduce onHand)
        Warehouse wh = order.getWarehouse();
        for (SalesOrderItem it : order.getItems()) {
            Product product = it.getProduct();

            Stock stock = stockRepository.findByWarehouseIdAndProductId(wh.getId(), product.getId())
                    .orElseThrow(() -> new BadRequestException("Stock missing for payment finalize"));

            long reserved = stock.getReserved();
            if (reserved < it.getQty()) {
                throw new BadRequestException("Reserved stock inconsistent for productId " + product.getId());
            }
            if (stock.getOnHand() < it.getQty()) {
                throw new BadRequestException("On hand stock insufficient during finalize for productId " + product.getId());
            }

            stock.setReserved(stock.getReserved() - it.getQty());
            stock.setOnHand(stock.getOnHand() - it.getQty());
            stockRepository.save(stock);

            stockMovementRepository.save(StockMovement.builder()
                    .warehouse(wh)
                    .product(product)
                    .type(StockMovement.Type.OUT)
                    .qty(it.getQty())
                    .referenceType(StockMovement.ReferenceType.PAYMENT)
                    .referenceId(pay.getId())
                    .note("Finalize stock for order " + order.getOrderNo())
                    .createdAt(LocalDateTime.now())
                    .build());
        }

        order.setStatus(SalesOrder.Status.PAID);
        salesOrderRepository.save(order);

        // auto-create shipment READY
        Shipment shipment = Shipment.builder()
                .order(order)
                .shipmentNo(ShipmentNoGenerator.next())
                .status(Shipment.Status.READY)
                .addressSnapshot(order.getCustomer().getAddress())
                .build();
        shipmentRepository.save(shipment);
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        SalesOrder order = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        if (order.getStatus() == SalesOrder.Status.PAID || order.getStatus() == SalesOrder.Status.SHIPPED || order.getStatus() == SalesOrder.Status.COMPLETED) {
            throw new BadRequestException("Cannot cancel after paid/shipped/completed");
        }
        if (order.getStatus() == SalesOrder.Status.CANCELLED) return;

        Warehouse wh = order.getWarehouse();
        for (SalesOrderItem it : order.getItems()) {
            Stock stock = stockRepository.findByWarehouseIdAndProductId(wh.getId(), it.getProduct().getId())
                    .orElseThrow(() -> new BadRequestException("Stock missing for cancel"));

            if (stock.getReserved() < it.getQty()) {
                throw new BadRequestException("Reserved stock inconsistent for cancel");
            }

            stock.setReserved(stock.getReserved() - it.getQty());
            stockRepository.save(stock);

            stockMovementRepository.save(StockMovement.builder()
                    .warehouse(wh)
                    .product(it.getProduct())
                    .type(StockMovement.Type.RELEASE)
                    .qty(it.getQty())
                    .referenceType(StockMovement.ReferenceType.ORDER)
                    .referenceId(order.getId())
                    .note("Release reserve for cancelled order " + order.getOrderNo())
                    .createdAt(LocalDateTime.now())
                    .build());
        }

        order.setStatus(SalesOrder.Status.CANCELLED);
        salesOrderRepository.save(order);
    }

    public OrderResponse getOrder(Long orderId) {
        SalesOrder order = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        return toOrderResponse(order);
    }

    private OrderResponse toOrderResponse(SalesOrder order) {
        List<OrderResponse.OrderResponseItem> items = order.getItems().stream().map(it ->
                OrderResponse.OrderResponseItem.builder()
                        .productId(it.getProduct().getId())
                        .productName(it.getProduct().getName())
                        .qty(it.getQty())
                        .unitPrice(it.getUnitPrice())
                        .subtotal(it.getSubtotal())
                        .build()
        ).toList();

        return OrderResponse.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .status(order.getStatus().name())
                .customerId(order.getCustomer().getId())
                .warehouseId(order.getWarehouse().getId())
                .totalAmount(order.getTotalAmount())
                .items(items)
                .build();
    }
}