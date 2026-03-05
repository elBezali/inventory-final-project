package com.dibimbing.inventory_sales_api.service;

import com.dibimbing.inventory_sales_api.dto.common.PageMeta;
import com.dibimbing.inventory_sales_api.dto.stock_movement.StockMovementResponse;
import com.dibimbing.inventory_sales_api.entity.StockMovement;
import com.dibimbing.inventory_sales_api.exception.NotFoundException;
import com.dibimbing.inventory_sales_api.repository.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockMovementService {

    private final StockMovementRepository stockMovementRepository;

    public StockMovementResponse getById(Long id) {
        StockMovement mv = stockMovementRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Stock movement not found"));
        return toResponse(mv);
    }

    public Page<StockMovementResponse> list(Long warehouseId, Long productId, StockMovement.Type type, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        Page<StockMovement> result;
        if (warehouseId != null && productId != null && type != null) {
            result = stockMovementRepository.findByWarehouseIdAndProductIdAndType(warehouseId, productId, type, pageable);
        } else if (warehouseId != null && productId != null) {
            result = stockMovementRepository.findByWarehouseIdAndProductId(warehouseId, productId, pageable);
        } else if (warehouseId != null && type != null) {
            result = stockMovementRepository.findByWarehouseIdAndType(warehouseId, type, pageable);
        } else if (productId != null && type != null) {
            result = stockMovementRepository.findByProductIdAndType(productId, type, pageable);
        } else if (warehouseId != null) {
            result = stockMovementRepository.findByWarehouseId(warehouseId, pageable);
        } else if (productId != null) {
            result = stockMovementRepository.findByProductId(productId, pageable);
        } else if (type != null) {
            result = stockMovementRepository.findByType(type, pageable);
        } else {
            result = stockMovementRepository.findAll(pageable);
        }

        return result.map(this::toResponse);
    }

    public PageMeta meta(Page<?> page) {
        return PageMeta.builder()
                .currentPage(page.getNumber())
                .pageSize(page.getSize())
                .totalItems(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    private StockMovementResponse toResponse(StockMovement mv) {
        return StockMovementResponse.builder()
                .id(mv.getId())
                .warehouseId(mv.getWarehouse().getId())
                .warehouseName(mv.getWarehouse().getName())
                .productId(mv.getProduct().getId())
                .productSku(mv.getProduct().getSku())
                .productName(mv.getProduct().getName())
                .type(mv.getType())
                .qty(mv.getQty())
                .referenceType(mv.getReferenceType())
                .referenceId(mv.getReferenceId())
                .note(mv.getNote())
                .createdByUserId(mv.getCreatedBy() != null ? mv.getCreatedBy().getId() : null)
                .createdAt(mv.getCreatedAt())
                .build();
    }
}