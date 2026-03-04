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

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

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

    public ProductResponse getById(Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        return toResponse(p);
    }

    public Page<ProductResponse> list(String q, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Product> result = (q == null || q.isBlank())
                ? productRepository.findAll(pageable)
                : productRepository.findByNameContainingIgnoreCase(q, pageable);

        return result.map(this::toResponse);
    }

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

    public void deleteSoft(Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));
        p.setActive(false);
        productRepository.save(p);
    }

    public PageMeta meta(Page<?> page) {
        return PageMeta.builder()
                .currentPage(page.getNumber())
                .pageSize(page.getSize())
                .totalItems(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    private ProductResponse toResponse(Product p) {
        return ProductResponse.builder()
                .id(p.getId())
                .sku(p.getSku())
                .name(p.getName())
                .description(p.getDescription())
                .categoryId(p.getCategory().getId())
                .categoryName(p.getCategory().getName())
                .price(p.getPrice())
                .isActive(p.isActive())
                .build();
    }
}