package com.dibimbing.inventory_sales_api.service;

import com.dibimbing.inventory_sales_api.dto.category.CategoryCreateRequest;
import com.dibimbing.inventory_sales_api.dto.category.CategoryResponse;
import com.dibimbing.inventory_sales_api.dto.category.CategoryUpdateRequest;
import com.dibimbing.inventory_sales_api.dto.common.PageMeta;
import com.dibimbing.inventory_sales_api.entity.Category;
import com.dibimbing.inventory_sales_api.exception.BadRequestException;
import com.dibimbing.inventory_sales_api.exception.NotFoundException;
import com.dibimbing.inventory_sales_api.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryResponse create(CategoryCreateRequest req) {
        if (categoryRepository.findByName(req.getName()).isPresent()) {
            throw new BadRequestException("Category name already exists");
        }

        Category c = Category.builder()
                .name(req.getName())
                .description(req.getDescription())
                .build();

        return toResponse(categoryRepository.save(c));
    }

    public CategoryResponse getById(Long id) {
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found"));
        return toResponse(c);
    }

    public Page<CategoryResponse> list(String q, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        Page<Category> result = (q == null || q.isBlank())
                ? categoryRepository.findAll(pageable)
                : categoryRepository.findByNameContainingIgnoreCase(q, pageable);

        return result.map(this::toResponse);
    }

    public CategoryResponse update(Long id, CategoryUpdateRequest req) {
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found"));

        if (!c.getName().equalsIgnoreCase(req.getName())
                && categoryRepository.findByName(req.getName()).isPresent()) {
            throw new BadRequestException("Category name already exists");
        }

        c.setName(req.getName());
        c.setDescription(req.getDescription());

        return toResponse(categoryRepository.save(c));
    }

    public void delete(Long id) {
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found"));
        categoryRepository.delete(c);
    }

    public PageMeta meta(Page<?> page) {
        return PageMeta.builder()
                .currentPage(page.getNumber())
                .pageSize(page.getSize())
                .totalItems(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    private CategoryResponse toResponse(Category c) {
        return CategoryResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .description(c.getDescription())
                .build();
    }
}