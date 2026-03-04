package com.dibimbing.inventory_sales_api.controller;

import com.dibimbing.inventory_sales_api.constant.ApiPath;
import com.dibimbing.inventory_sales_api.dto.common.ApiResponse;
import com.dibimbing.inventory_sales_api.dto.common.PageMeta;
import com.dibimbing.inventory_sales_api.dto.product.ProductCreateRequest;
import com.dibimbing.inventory_sales_api.dto.product.ProductResponse;
import com.dibimbing.inventory_sales_api.dto.product.ProductUpdateRequest;
import com.dibimbing.inventory_sales_api.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiPath.PRODUCTS)
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ApiResponse<ProductResponse> create(@Valid @RequestBody ProductCreateRequest req) {
        return ApiResponse.ok("Product created", productService.create(req));
    }

    @GetMapping
    public ApiResponse<Object> list(@RequestParam(required = false) String q,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int limit) {
        Page<ProductResponse> result = productService.list(q, page, limit);
        PageMeta meta = productService.meta(result);
        return ApiResponse.ok("Products fetched", result.getContent(), meta);
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> get(@PathVariable Long id) {
        return ApiResponse.ok("Product fetched", productService.getById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ApiResponse<ProductResponse> update(@PathVariable Long id, @Valid @RequestBody ProductUpdateRequest req) {
        return ApiResponse.ok("Product updated", productService.update(id, req));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResponse<Object> delete(@PathVariable Long id) {
        productService.deleteSoft(id);
        return ApiResponse.ok("Product soft-deleted", null);
    }
}
