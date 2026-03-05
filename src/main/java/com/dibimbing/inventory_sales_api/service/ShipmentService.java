package com.dibimbing.inventory_sales_api.service;

import com.dibimbing.inventory_sales_api.dto.common.PageMeta;
import com.dibimbing.inventory_sales_api.dto.shipment.ShipmentCreateRequest;
import com.dibimbing.inventory_sales_api.dto.shipment.ShipmentResponse;
import com.dibimbing.inventory_sales_api.dto.shipment.ShipmentUpdateStatusRequest;
import com.dibimbing.inventory_sales_api.entity.SalesOrder;
import com.dibimbing.inventory_sales_api.entity.Shipment;
import com.dibimbing.inventory_sales_api.exception.BadRequestException;
import com.dibimbing.inventory_sales_api.exception.NotFoundException;
import com.dibimbing.inventory_sales_api.repository.SalesOrderRepository;
import com.dibimbing.inventory_sales_api.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShipmentService {

    private static final Logger AUDIT = LoggerFactory.getLogger("com.dibimbing.inventory_sales_api.audit");

    private final ShipmentRepository shipmentRepository;
    private final SalesOrderRepository salesOrderRepository;

    public ShipmentResponse create(ShipmentCreateRequest req) {
        log.info("ShipmentService.create called orderId={}", req.getOrderId());

        SalesOrder order = salesOrderRepository.findById(req.getOrderId())
                .orElseThrow(() -> {
                    log.warn("Create shipment failed: order not found orderId={}", req.getOrderId());
                    return new NotFoundException("Order not found");
                });

        if (shipmentRepository.findByOrderId(order.getId()).isPresent()) {
            log.warn("Create shipment rejected: already exists for orderId={}", order.getId());
            throw new BadRequestException("Shipment for this order already exists");
        }

        Shipment sh = Shipment.builder()
                .order(order)
                .shipmentNo(generateShipmentNo())
                .status(Shipment.Status.READY)
                .addressSnapshot(req.getAddressSnapshot())
                .build();

        Shipment saved = shipmentRepository.save(sh);

        log.info("Shipment created shipmentId={} shipmentNo={} orderId={}", saved.getId(), saved.getShipmentNo(), order.getId());
        AUDIT.info("SHIPMENT_CREATE shipmentId=%d shipmentNo=%s orderId=%d"
                .formatted(saved.getId(), saved.getShipmentNo(), order.getId()));

        return toResponse(saved);
    }

    public ShipmentResponse getById(Long id) {
        log.debug("ShipmentService.getById called id={}", id);

        Shipment sh = shipmentRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Shipment not found id={}", id);
                    return new NotFoundException("Shipment not found");
                });

        return toResponse(sh);
    }

    public ShipmentResponse getByOrderId(Long orderId) {
        log.debug("ShipmentService.getByOrderId called orderId={}", orderId);

        Shipment sh = shipmentRepository.findByOrderId(orderId)
                .orElseThrow(() -> {
                    log.warn("Shipment not found for orderId={}", orderId);
                    return new NotFoundException("Shipment not found for this order");
                });

        return toResponse(sh);
    }

    public Page<ShipmentResponse> list(Shipment.Status status, int page, int size) {
        log.debug("ShipmentService.list called status={} page={} size={}", status, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Shipment> result = (status == null)
                ? shipmentRepository.findAll(pageable)
                : shipmentRepository.findByStatus(status, pageable);

        return result.map(this::toResponse);
    }

    public ShipmentResponse updateStatus(Long id, ShipmentUpdateStatusRequest req) {
        Shipment.Status newStatus = req.getStatus();
        log.info("ShipmentService.updateStatus called id={} newStatus={}", id, newStatus);

        Shipment sh = shipmentRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Update shipment failed: not found id={}", id);
                    return new NotFoundException("Shipment not found");
                });

        Shipment.Status oldStatus = sh.getStatus();
        sh.setStatus(newStatus);

        if (newStatus == Shipment.Status.SHIPPED && sh.getShippedAt() == null) {
            sh.setShippedAt(LocalDateTime.now());
        }
        if (newStatus == Shipment.Status.DELIVERED && sh.getDeliveredAt() == null) {
            sh.setDeliveredAt(LocalDateTime.now());
        }

        Shipment saved = shipmentRepository.save(sh);

        log.info("Shipment status updated shipmentId={} shipmentNo={} {} -> {}", saved.getId(), saved.getShipmentNo(), oldStatus, newStatus);
        AUDIT.info("SHIPMENT_STATUS_UPDATE shipmentId=%d shipmentNo=%s %s->%s"
                .formatted(saved.getId(), saved.getShipmentNo(), oldStatus, newStatus));

        return toResponse(saved);
    }

    public PageMeta meta(Page<?> page) {
        return PageMeta.builder()
                .currentPage(page.getNumber())
                .pageSize(page.getSize())
                .totalItems(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    private ShipmentResponse toResponse(Shipment sh) {
        return ShipmentResponse.builder()
                .id(sh.getId())
                .orderId(sh.getOrder().getId())
                .shipmentNo(sh.getShipmentNo())
                .status(sh.getStatus())
                .shippedAt(sh.getShippedAt())
                .deliveredAt(sh.getDeliveredAt())
                .addressSnapshot(sh.getAddressSnapshot())
                .build();
    }

    private String generateShipmentNo() {
        return "SHP-" + System.currentTimeMillis();
    }
}