package com.dibimbing.inventory_sales_api.service;

import com.dibimbing.inventory_sales_api.dto.common.PageMeta;
import com.dibimbing.inventory_sales_api.dto.warehouse.WarehouseCreateRequest;
import com.dibimbing.inventory_sales_api.dto.warehouse.WarehouseResponse;
import com.dibimbing.inventory_sales_api.dto.warehouse.WarehouseUpdateRequest;
import com.dibimbing.inventory_sales_api.entity.Warehouse;
import com.dibimbing.inventory_sales_api.exception.BadRequestException;
import com.dibimbing.inventory_sales_api.exception.NotFoundException;
import com.dibimbing.inventory_sales_api.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WarehouseService {

    private static final Logger AUDIT = LoggerFactory.getLogger("com.dibimbing.inventory_sales_api.audit");

    private final WarehouseRepository warehouseRepository;

    public WarehouseResponse create(WarehouseCreateRequest req) {
        log.info("WarehouseService.create called name={}", req.getName());

        if (warehouseRepository.existsByNameIgnoreCase(req.getName())) {
            log.warn("Create warehouse rejected: name exists name={}", req.getName());
            throw new BadRequestException("Warehouse name already exists");
        }

        Warehouse w = Warehouse.builder()
                .name(req.getName())
                .address(req.getAddress())
                .isActive(true)
                .build();

        Warehouse saved = warehouseRepository.save(w);

        log.info("Warehouse created warehouseId={} name={}", saved.getId(), saved.getName());
        AUDIT.info("WAREHOUSE_CREATE warehouseId=%d name=%s".formatted(saved.getId(), saved.getName()));

        return toResponse(saved);
    }

    public WarehouseResponse getById(Long id) {
        log.debug("WarehouseService.getById called id={}", id);

        Warehouse w = warehouseRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Warehouse not found id={}", id);
                    return new NotFoundException("Warehouse not found");
                });

        return toResponse(w);
    }

    public Page<WarehouseResponse> list(String q, int page, int size) {
        log.debug("WarehouseService.list called q='{}' page={} size={}", q, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Warehouse> result = (q == null || q.isBlank())
                ? warehouseRepository.findAll(pageable)
                : warehouseRepository.findByNameContainingIgnoreCase(q, pageable);

        return result.map(this::toResponse);
    }

    public WarehouseResponse update(Long id, WarehouseUpdateRequest req) {
        log.info("WarehouseService.update called id={} newName={} isActive={}", id, req.getName(), req.getIsActive());

        Warehouse w = warehouseRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Update warehouse failed: not found id={}", id);
                    return new NotFoundException("Warehouse not found");
                });

        if (!w.getName().equalsIgnoreCase(req.getName()) && warehouseRepository.existsByNameIgnoreCase(req.getName())) {
            log.warn("Update warehouse rejected: name exists name={}", req.getName());
            throw new BadRequestException("Warehouse name already exists");
        }

        w.setName(req.getName());
        w.setAddress(req.getAddress());
        w.setActive(req.getIsActive());

        Warehouse saved = warehouseRepository.save(w);

        log.info("Warehouse updated warehouseId={} name={} isActive={}", saved.getId(), saved.getName(), saved.isActive());
        AUDIT.info("WAREHOUSE_UPDATE warehouseId=%d name=%s isActive=%s".formatted(saved.getId(), saved.getName(), saved.isActive()));

        return toResponse(saved);
    }

    public void deleteSoft(Long id) {
        log.info("WarehouseService.deleteSoft called id={}", id);

        Warehouse w = warehouseRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Delete warehouse failed: not found id={}", id);
                    return new NotFoundException("Warehouse not found");
                });

        w.setActive(false);
        warehouseRepository.save(w);

        log.info("Warehouse soft-deleted warehouseId={} name={}", w.getId(), w.getName());
        AUDIT.info("WAREHOUSE_DELETE_SOFT warehouseId=%d name=%s".formatted(w.getId(), w.getName()));
    }

    public PageMeta meta(Page<?> page) {
        return PageMeta.builder()
                .currentPage(page.getNumber())
                .pageSize(page.getSize())
                .totalItems(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    private WarehouseResponse toResponse(Warehouse w) {
        return WarehouseResponse.builder()
                .id(w.getId())
                .name(w.getName())
                .address(w.getAddress())
                .isActive(w.isActive())
                .build();
    }
}