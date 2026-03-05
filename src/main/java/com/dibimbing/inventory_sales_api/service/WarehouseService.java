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
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;

    public WarehouseResponse create(WarehouseCreateRequest req) {
        if (warehouseRepository.existsByNameIgnoreCase(req.getName())) {
            throw new BadRequestException("Warehouse name already exists");
        }

        Warehouse w = Warehouse.builder()
                .name(req.getName())
                .address(req.getAddress())
                .isActive(true)
                .build();

        return toResponse(warehouseRepository.save(w));
    }

    public WarehouseResponse getById(Long id) {
        Warehouse w = warehouseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Warehouse not found"));
        return toResponse(w);
    }

    public Page<WarehouseResponse> list(String q, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        Page<Warehouse> result = (q == null || q.isBlank())
                ? warehouseRepository.findAll(pageable)
                : warehouseRepository.findByNameContainingIgnoreCase(q, pageable);

        return result.map(this::toResponse);
    }

    public WarehouseResponse update(Long id, WarehouseUpdateRequest req) {
        Warehouse w = warehouseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Warehouse not found"));

        if (!w.getName().equalsIgnoreCase(req.getName())
                && warehouseRepository.existsByNameIgnoreCase(req.getName())) {
            throw new BadRequestException("Warehouse name already exists");
        }

        w.setName(req.getName());
        w.setAddress(req.getAddress());
        w.setActive(req.getIsActive());

        return toResponse(warehouseRepository.save(w));
    }

    public void deleteSoft(Long id) {
        Warehouse w = warehouseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Warehouse not found"));
        w.setActive(false);
        warehouseRepository.save(w);
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