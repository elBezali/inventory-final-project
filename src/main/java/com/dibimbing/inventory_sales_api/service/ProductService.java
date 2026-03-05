package com.dibimbing.inventory_sales_api.service;

import com.dibimbing.inventory_sales_api.dto.common.PageMeta;
import com.dibimbing.inventory_sales_api.dto.product.ProductCreateRequest;
import com.dibimbing.inventory_sales_api.dto.product.ProductResponse;
import com.dibimbing.inventory_sales_api.dto.product.ProductUpdateRequest;
import com.dibimbing.inventory_sales_api.entity.Category;
import com.dibimbing.inventory_sales_api.entity.Product;
import com.dibimbing.inventory_sales_api.exception.BadRequestException;
import com.dibimbing.inventory_sales_api.exception.NotFoundException;
import com.dibimbing.inventory_sales_api.repository.CategoryRepository;
import com.dibimbing.inventory_sales_api.repository.ProductRepository;
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
public class ProductService {

    private static final Logger AUDIT = LoggerFactory.getLogger("com.dibimbing.inventory_sales_api.audit");

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public ProductResponse create(ProductCreateRequest req) {
        log.info("ProductService.create called sku={} name={} categoryId={}", req.getSku(), req.getName(), req.getCategoryId());

        if (productRepository.existsBySku(req.getSku())) {
            log.warn("Create product rejected: SKU exists sku={}", req.getSku());
            throw new BadRequestException("SKU already exists");
        }

        Category cat = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> {
                    log.warn("Create product failed: category not found categoryId={}", req.getCategoryId());
                    return new NotFoundException("Category not found");
                });

        Product p = Product.builder()
                .sku(req.getSku())
                .name(req.getName())
                .description(req.getDescription())
                .category(cat)
                .price(req.getPrice())
                .isActive(true)
                .build();

        Product saved = productRepository.save(p);

        log.info("Product created productId={} sku={}", saved.getId(), saved.getSku());
        AUDIT.info("PRODUCT_CREATE productId=%d sku=%s name=%s".formatted(saved.getId(), saved.getSku(), saved.getName()));

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        log.debug("ProductService.getById called id={}", id);

        Product p = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Product not found id={}", id);
                    return new NotFoundException("Product not found");
                });

        return toResponse(p);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> list(String q, int page, int size) {
        int safeSize = (size <= 0) ? 10 : size;
        int zeroBasedPage = (page <= 1) ? 0 : (page - 1);

        log.debug("ProductService.list called q='{}' page={} size={}", q, zeroBasedPage, safeSize);

        Pageable pageable = PageRequest.of(zeroBasedPage, safeSize, Sort.by(Sort.Direction.DESC, "id"));
        Page<Product> result = (q == null || q.isBlank())
                ? productRepository.findAll(pageable)
                : productRepository.findByNameContainingIgnoreCase(q, pageable);

        return result.map(this::toResponse);
    }

    @Transactional
    public ProductResponse update(Long id, ProductUpdateRequest req) {
        log.info("ProductService.update called id={} categoryId={} isActive={}", id, req.getCategoryId(), req.getIsActive());

        Product p = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Update product failed: not found id={}", id);
                    return new NotFoundException("Product not found");
                });

        Category cat = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> {
                    log.warn("Update product failed: category not found categoryId={}", req.getCategoryId());
                    return new NotFoundException("Category not found");
                });

        p.setName(req.getName());
        p.setDescription(req.getDescription());
        p.setCategory(cat);
        p.setPrice(req.getPrice());
        p.setActive(req.getIsActive());

        Product saved = productRepository.save(p);

        log.info("Product updated productId={} sku={}", saved.getId(), saved.getSku());
        AUDIT.info("PRODUCT_UPDATE productId=%d sku=%s name=%s isActive=%s"
                .formatted(saved.getId(), saved.getSku(), saved.getName(), saved.isActive()));

        return toResponse(saved);
    }

    @Transactional
    public void deleteSoft(Long id) {
        log.info("ProductService.deleteSoft called id={}", id);

        Product p = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Delete product failed: not found id={}", id);
                    return new NotFoundException("Product not found");
                });

        p.setActive(false);
        productRepository.save(p);

        log.info("Product soft-deleted productId={} sku={}", p.getId(), p.getSku());
        AUDIT.info("PRODUCT_DELETE_SOFT productId=%d sku=%s name=%s".formatted(p.getId(), p.getSku(), p.getName()));
    }

    public PageMeta meta(Page<?> page) {
        return PageMeta.builder()
                .currentPage(page.getNumber() + 1) 
                .pageSize(page.getSize())
                .totalItems(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    private ProductResponse toResponse(Product p) {
        Category c = p.getCategory();
        return ProductResponse.builder()
                .id(p.getId())
                .sku(p.getSku())
                .name(p.getName())
                .description(p.getDescription())
                .categoryId(c != null ? c.getId() : null)
                .categoryName(c != null ? c.getName() : null)
                .price(p.getPrice())
                .isActive(p.isActive())
                .build();
    }
}