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
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private static final Logger AUDIT = LoggerFactory.getLogger("com.dibimbing.inventory_sales_api.audit");

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
        log.info("OrderService.createOrder called customerId={} warehouseId={} items={}",
                req.getCustomerId(), req.getWarehouseId(),
                req.getItems() == null ? 0 : req.getItems().size());

        Customer customer = customerRepository.findById(req.getCustomerId())
                .orElseThrow(() -> {
                    log.warn("Create order failed: customer not found customerId={}", req.getCustomerId());
                    return new NotFoundException("Customer not found");
                });

        Warehouse wh = warehouseRepository.findById(req.getWarehouseId())
                .orElseThrow(() -> {
                    log.warn("Create order failed: warehouse not found warehouseId={}", req.getWarehouseId());
                    return new NotFoundException("Warehouse not found");
                });

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
                    .orElseThrow(() -> {
                        log.warn("Create order failed: product not found productId={}", itemReq.getProductId());
                        return new NotFoundException("Product not found: " + itemReq.getProductId());
                    });

            if (!product.isActive()) {
                log.warn("Create order rejected: product inactive productId={}", product.getId());
                throw new BadRequestException("Product inactive: " + product.getId());
            }

            Stock stock = stockRepository.findByWarehouseIdAndProductId(wh.getId(), product.getId())
                    .orElseThrow(() -> {
                        log.warn("Create order failed: stock not found warehouseId={} productId={}", wh.getId(), product.getId());
                        return new BadRequestException("Stock not found for productId " + product.getId() + " in warehouse " + wh.getId());
                    });

            long available = stock.available();
            if (available < itemReq.getQty()) {
                log.warn("Create order rejected: insufficient stock productId={} warehouseId={} available={} reqQty={}",
                        product.getId(), wh.getId(), available, itemReq.getQty());
                throw new BadRequestException("Insufficient stock for productId " + product.getId() + ". Available=" + available);
            }

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

            log.debug("Reserve OK orderNo={} productId={} qty={} onHand={} reserved={} availableNow={}",
                    order.getOrderNo(), product.getId(), itemReq.getQty(),
                    stock.getOnHand(), stock.getReserved(), stock.available());
        }

        order.setTotalAmount(total);
        SalesOrder saved = salesOrderRepository.save(order);

        log.info("Order created orderId={} orderNo={} total={}", saved.getId(), saved.getOrderNo(), saved.getTotalAmount());
        AUDIT.info("ORDER_CREATE orderId=%d orderNo=%s customerId=%d warehouseId=%d total=%s"
                .formatted(saved.getId(), saved.getOrderNo(), customer.getId(), wh.getId(), saved.getTotalAmount()));

        return toOrderResponse(saved);
    }

    @Transactional
    public void markPaymentSuccess(Long orderId, PaymentTransaction.Method method, String providerRef) {
        log.info("OrderService.markPaymentSuccess called orderId={} method={} providerRef={}", orderId, method, providerRef);

        SalesOrder order = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.warn("Payment success failed: order not found orderId={}", orderId);
                    return new NotFoundException("Order not found");
                });

        if (order.getStatus() == SalesOrder.Status.CANCELLED) {
            log.warn("Payment success rejected: order cancelled orderId={}", orderId);
            throw new BadRequestException("Order already cancelled");
        }

        if (order.getStatus() == SalesOrder.Status.PAID) {
            log.info("Payment success ignored: order already PAID orderId={}", orderId);
            return;
        }

        if (order.getStatus() != SalesOrder.Status.PENDING_PAYMENT) {
            log.warn("Payment success rejected: invalid status orderId={} status={}", orderId, order.getStatus());
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

        PaymentTransaction savedPay = paymentTransactionRepository.save(pay);

        Warehouse wh = order.getWarehouse();
        for (SalesOrderItem it : order.getItems()) {
            Product product = it.getProduct();

            Stock stock = stockRepository.findByWarehouseIdAndProductId(wh.getId(), product.getId())
                    .orElseThrow(() -> {
                        log.error("Finalize stock failed: stock missing warehouseId={} productId={}", wh.getId(), product.getId());
                        return new BadRequestException("Stock missing for payment finalize");
                    });

            long reserved = stock.getReserved();
            if (reserved < it.getQty()) {
                log.error("Finalize stock failed: reserved inconsistent orderId={} productId={} reserved={} qty={}",
                        orderId, product.getId(), reserved, it.getQty());
                throw new BadRequestException("Reserved stock inconsistent for productId " + product.getId());
            }

            if (stock.getOnHand() < it.getQty()) {
                log.error("Finalize stock failed: onHand insufficient orderId={} productId={} onHand={} qty={}",
                        orderId, product.getId(), stock.getOnHand(), it.getQty());
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
                    .referenceId(savedPay.getId())
                    .note("Finalize stock for order " + order.getOrderNo())
                    .createdAt(LocalDateTime.now())
                    .build());

            log.debug("Finalize stock OK orderNo={} productId={} qty={} onHandNow={} reservedNow={}",
                    order.getOrderNo(), product.getId(), it.getQty(), stock.getOnHand(), stock.getReserved());
        }

        order.setStatus(SalesOrder.Status.PAID);
        salesOrderRepository.save(order);

        Shipment shipment = Shipment.builder()
                .order(order)
                .shipmentNo(ShipmentNoGenerator.next())
                .status(Shipment.Status.READY)
                .addressSnapshot(order.getCustomer().getAddress())
                .build();
        Shipment savedShipment = shipmentRepository.save(shipment);

        log.info("Payment success processed orderId={} paymentId={} shipmentNo={}", orderId, savedPay.getId(), savedShipment.getShipmentNo());
        AUDIT.info("PAYMENT_SUCCESS orderId=%d orderNo=%s paymentId=%d method=%s amount=%s shipmentNo=%s"
                .formatted(order.getId(), order.getOrderNo(), savedPay.getId(), method, savedPay.getAmount(), savedShipment.getShipmentNo()));
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        log.info("OrderService.cancelOrder called orderId={}", orderId);

        SalesOrder order = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.warn("Cancel order failed: not found orderId={}", orderId);
                    return new NotFoundException("Order not found");
                });

        if (order.getStatus() == SalesOrder.Status.PAID
                || order.getStatus() == SalesOrder.Status.SHIPPED
                || order.getStatus() == SalesOrder.Status.COMPLETED) {
            log.warn("Cancel order rejected: status too late orderId={} status={}", orderId, order.getStatus());
            throw new BadRequestException("Cannot cancel after paid/shipped/completed");
        }

        if (order.getStatus() == SalesOrder.Status.CANCELLED) {
            log.info("Cancel order ignored: already cancelled orderId={}", orderId);
            return;
        }

        Warehouse wh = order.getWarehouse();
        for (SalesOrderItem it : order.getItems()) {
            Stock stock = stockRepository.findByWarehouseIdAndProductId(wh.getId(), it.getProduct().getId())
                    .orElseThrow(() -> {
                        log.error("Cancel order failed: stock missing warehouseId={} productId={}", wh.getId(), it.getProduct().getId());
                        return new BadRequestException("Stock missing for cancel");
                    });

            if (stock.getReserved() < it.getQty()) {
                log.error("Cancel order failed: reserved inconsistent orderId={} productId={} reserved={} qty={}",
                        orderId, it.getProduct().getId(), stock.getReserved(), it.getQty());
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

            log.debug("Release reserve OK orderNo={} productId={} qty={} reservedNow={}",
                    order.getOrderNo(), it.getProduct().getId(), it.getQty(), stock.getReserved());
        }

        order.setStatus(SalesOrder.Status.CANCELLED);
        salesOrderRepository.save(order);

        log.info("Order cancelled orderId={} orderNo={}", order.getId(), order.getOrderNo());
        AUDIT.info("ORDER_CANCELLED orderId=%d orderNo=%s".formatted(order.getId(), order.getOrderNo()));
    }

    public OrderResponse getOrder(Long orderId) {
        log.debug("OrderService.getOrder called orderId={}", orderId);

        SalesOrder order = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.warn("Get order failed: not found orderId={}", orderId);
                    return new NotFoundException("Order not found");
                });

        return toOrderResponse(order);
    }

    private OrderResponse toOrderResponse(SalesOrder order) {
        List<OrderResponse.OrderResponseItem> items = order.getItems().stream()
                .map(it -> OrderResponse.OrderResponseItem.builder()
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