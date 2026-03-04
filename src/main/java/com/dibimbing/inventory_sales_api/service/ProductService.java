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
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public ProductResponse create(ProductCreateRequest req) {
        if (productRepository.existsBySku(req.getSku())) {
            throw new BadRequestException("SKU already exists");
        }

        Category cat = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category not found"));

        Product p = Product.builder()
                .sku(req.getSku())
                .name(req.getName())
                .description(req.getDescription())
                .category(cat)
                .price(req.getPrice())
                .isActive(true)
                .build();

        Product saved = productRepository.save(p);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        return toResponse(p);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> list(String q, int page, int size) {
        int safeSize = (size <= 0) ? 10 : size;

        int zeroBasedPage = (page <= 1) ? 0 : (page - 1);

        Pageable pageable = PageRequest.of(zeroBasedPage, safeSize, Sort.by(Sort.Direction.DESC, "id"));

        Page<Product> result = (q == null || q.isBlank())
                ? productRepository.findAll(pageable)
                : productRepository.findByNameContainingIgnoreCase(q, pageable);

        return result.map(this::toResponse);
    }

    @Transactional
    public ProductResponse update(Long id, ProductUpdateRequest req) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        Category cat = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Category not found"));

        p.setName(req.getName());
        p.setDescription(req.getDescription());
        p.setCategory(cat);
        p.setPrice(req.getPrice());
        p.setActive(req.getIsActive());

        return toResponse(productRepository.save(p));
    }

    @Transactional
    public void deleteSoft(Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        p.setActive(false);
        productRepository.save(p);
    }

    public PageMeta meta(Page<?> page) {
        return PageMeta.builder()
                .currentPage(page.getNumber() + 1) // 0-based -> 1-based
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