package com.dibimbing.inventory_sales_api.controller;

import com.dibimbing.inventory_sales_api.constant.ApiPath;
import com.dibimbing.inventory_sales_api.dto.category.CategoryCreateRequest;
import com.dibimbing.inventory_sales_api.dto.category.CategoryResponse;
import com.dibimbing.inventory_sales_api.dto.category.CategoryUpdateRequest;
import com.dibimbing.inventory_sales_api.dto.common.ApiResponse;
import com.dibimbing.inventory_sales_api.dto.common.PageMeta;
import com.dibimbing.inventory_sales_api.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiPath.CATEGORIES)
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ApiResponse<CategoryResponse> create(@Valid @RequestBody CategoryCreateRequest req) {
        return ApiResponse.ok("Category created", categoryService.create(req));
    }

    @GetMapping
    public ApiResponse<List<CategoryResponse>> list(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        Page<CategoryResponse> result = categoryService.list(q, page, limit);
        PageMeta meta = categoryService.meta(result);
        return ApiResponse.ok("Categories fetched", result.getContent(), meta);
    }

    @GetMapping("/{id}")
    public ApiResponse<CategoryResponse> get(@PathVariable Long id) {
        return ApiResponse.ok("Category fetched", categoryService.getById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ApiResponse<CategoryResponse> update(@PathVariable Long id, @Valid @RequestBody CategoryUpdateRequest req) {
        return ApiResponse.ok("Category updated", categoryService.update(id, req));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ApiResponse<Object> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ApiResponse.ok("Category deleted", null);
    }
}