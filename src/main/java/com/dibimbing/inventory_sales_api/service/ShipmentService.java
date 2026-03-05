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
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final SalesOrderRepository salesOrderRepository;

    public ShipmentResponse create(ShipmentCreateRequest req) {
        SalesOrder order = salesOrderRepository.findById(req.getOrderId())
                .orElseThrow(() -> new NotFoundException("Order not found"));

        if (shipmentRepository.findByOrderId(order.getId()).isPresent()) {
            throw new BadRequestException("Shipment for this order already exists");
        }

        Shipment sh = Shipment.builder()
                .order(order)
                .shipmentNo(generateShipmentNo())
                .status(Shipment.Status.READY)
                .addressSnapshot(req.getAddressSnapshot())
                .build();

        return toResponse(shipmentRepository.save(sh));
    }

    public ShipmentResponse getById(Long id) {
        Shipment sh = shipmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Shipment not found"));
        return toResponse(sh);
    }

    public ShipmentResponse getByOrderId(Long orderId) {
        Shipment sh = shipmentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NotFoundException("Shipment not found for this order"));
        return toResponse(sh);
    }

    public Page<ShipmentResponse> list(Shipment.Status status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        Page<Shipment> result = (status == null)
                ? shipmentRepository.findAll(pageable)
                : shipmentRepository.findByStatus(status, pageable);

        return result.map(this::toResponse);
    }

    public ShipmentResponse updateStatus(Long id, ShipmentUpdateStatusRequest req) {
        Shipment sh = shipmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Shipment not found"));

        Shipment.Status newStatus = req.getStatus();
        sh.setStatus(newStatus);

        if (newStatus == Shipment.Status.SHIPPED && sh.getShippedAt() == null) {
            sh.setShippedAt(LocalDateTime.now());
        }
        if (newStatus == Shipment.Status.DELIVERED && sh.getDeliveredAt() == null) {
            sh.setDeliveredAt(LocalDateTime.now());
        }

        return toResponse(shipmentRepository.save(sh));
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
        // simple unique no (senada: tanpa util khusus)
        return "SHP-" + System.currentTimeMillis();
    }
}