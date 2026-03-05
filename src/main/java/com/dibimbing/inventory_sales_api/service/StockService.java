package com.dibimbing.inventory_sales_api.service;

import com.dibimbing.inventory_sales_api.dto.common.PageMeta;
import com.dibimbing.inventory_sales_api.dto.stock.StockAdjustRequest;
import com.dibimbing.inventory_sales_api.dto.stock.StockResponse;
import com.dibimbing.inventory_sales_api.entity.*;
import com.dibimbing.inventory_sales_api.exception.BadRequestException;
import com.dibimbing.inventory_sales_api.exception.NotFoundException;
import com.dibimbing.inventory_sales_api.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockService {

    private static final Logger AUDIT = LoggerFactory.getLogger("com.dibimbing.inventory_sales_api.audit");

    private final StockRepository stockRepository;
    private final StockMovementRepository stockMovementRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;

    public StockResponse getById(Long id) {
        log.debug("StockService.getById called id={}", id);

        Stock s = stockRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Stock not found id={}", id);
                    return new NotFoundException("Stock not found");
                });

        return toResponse(s);
    }

    public Page<StockResponse> list(Long warehouseId, Long productId, int page, int size) {
        log.debug("StockService.list called warehouseId={} productId={} page={} size={}", warehouseId, productId, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Stock> result;

        if (warehouseId != null && productId != null) {
            result = stockRepository.findByWarehouseIdAndProductId(warehouseId, productId, pageable);
        } else if (warehouseId != null) {
            result = stockRepository.findByWarehouseId(warehouseId, pageable);
        } else if (productId != null) {
            result = stockRepository.findByProductId(productId, pageable);
        } else {
            result = stockRepository.findAll(pageable);
        }

        return result.map(this::toResponse);
    }

    @Transactional
    public StockResponse adjust(StockAdjustRequest req) {
        log.info("StockService.adjust called warehouseId={} productId={} delta={}",
                req.getWarehouseId(), req.getProductId(), req.getDelta());

        Warehouse w = warehouseRepository.findById(req.getWarehouseId())
                .orElseThrow(() -> {
                    log.warn("Stock adjust failed: warehouse not found warehouseId={}", req.getWarehouseId());
                    return new NotFoundException("Warehouse not found");
                });

        Product p = productRepository.findById(req.getProductId())
                .orElseThrow(() -> {
                    log.warn("Stock adjust failed: product not found productId={}", req.getProductId());
                    return new NotFoundException("Product not found");
                });

        Stock stock = stockRepository.findByWarehouseIdAndProductId(w.getId(), p.getId())
                .orElseGet(() -> {
                    log.info("Stock not found for warehouseId={} productId={}, creating new stock row", w.getId(), p.getId());
                    return stockRepository.save(Stock.builder()
                            .warehouse(w)
                            .product(p)
                            .onHand(0)
                            .reserved(0)
                            .build());
                });

        long delta = req.getDelta();
        long newOnHand = stock.getOnHand() + delta;

        if (newOnHand < 0) {
            log.warn("Stock adjust rejected: onHand would be negative stockId={} onHand={} delta={}",
                    stock.getId(), stock.getOnHand(), delta);
            throw new BadRequestException("Stock onHand cannot be negative");
        }

        stock.setOnHand(newOnHand);
        Stock saved = stockRepository.save(stock);

        StockMovement mv = StockMovement.builder()
                .warehouse(w)
                .product(p)
                .type(StockMovement.Type.ADJUSTMENT)
                .qty(Math.abs(delta))
                .referenceType(StockMovement.ReferenceType.MANUAL)
                .referenceId(saved.getId())
                .note(req.getNote())
                .build();

        stockMovementRepository.save(mv);

        log.info("Stock adjusted stockId={} warehouseId={} productId={} onHandNow={} reserved={} available={}",
                saved.getId(), w.getId(), p.getId(), saved.getOnHand(), saved.getReserved(), saved.available());

        AUDIT.info("STOCK_ADJUST stockId=%d warehouseId=%d productId=%d delta=%d onHandNow=%d"
                .formatted(saved.getId(), w.getId(), p.getId(), delta, saved.getOnHand()));

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

    private StockResponse toResponse(Stock s) {
        return StockResponse.builder()
                .id(s.getId())
                .warehouseId(s.getWarehouse().getId())
                .warehouseName(s.getWarehouse().getName())
                .productId(s.getProduct().getId())
                .productSku(s.getProduct().getSku())
                .productName(s.getProduct().getName())
                .onHand(s.getOnHand())
                .reserved(s.getReserved())
                .available(s.available())
                .build();
    }
}