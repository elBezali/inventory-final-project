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
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private static final Logger AUDIT = LoggerFactory.getLogger("com.dibimbing.inventory_sales_api.audit");

    private final CategoryRepository categoryRepository;

    public CategoryResponse create(CategoryCreateRequest req) {
        log.info("CategoryService.create called name={}", req.getName());

        if (categoryRepository.findByName(req.getName()).isPresent()) {
            log.warn("Create category rejected: name already exists name={}", req.getName());
            throw new BadRequestException("Category name already exists");
        }

        Category c = Category.builder()
                .name(req.getName())
                .description(req.getDescription())
                .build();

        Category saved = categoryRepository.save(c);

        log.info("Category created categoryId={} name={}", saved.getId(), saved.getName());
        AUDIT.info("CATEGORY_CREATE categoryId=%d name=%s".formatted(saved.getId(), saved.getName()));

        return toResponse(saved);
    }

    public CategoryResponse getById(Long id) {
        log.debug("CategoryService.getById called id={}", id);

        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Category not found id={}", id);
                    return new NotFoundException("Category not found");
                });

        return toResponse(c);
    }

    public Page<CategoryResponse> list(String q, int page, int size) {
        log.debug("CategoryService.list called q='{}' page={} size={}", q, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Category> result = (q == null || q.isBlank())
                ? categoryRepository.findAll(pageable)
                : categoryRepository.findByNameContainingIgnoreCase(q, pageable);

        return result.map(this::toResponse);
    }

    public CategoryResponse update(Long id, CategoryUpdateRequest req) {
        log.info("CategoryService.update called id={} newName={}", id, req.getName());

        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Update category failed: not found id={}", id);
                    return new NotFoundException("Category not found");
                });

        if (!c.getName().equalsIgnoreCase(req.getName()) && categoryRepository.findByName(req.getName()).isPresent()) {
            log.warn("Update category rejected: name already exists name={}", req.getName());
            throw new BadRequestException("Category name already exists");
        }

        c.setName(req.getName());
        c.setDescription(req.getDescription());

        Category saved = categoryRepository.save(c);

        log.info("Category updated categoryId={} name={}", saved.getId(), saved.getName());
        AUDIT.info("CATEGORY_UPDATE categoryId=%d name=%s".formatted(saved.getId(), saved.getName()));

        return toResponse(saved);
    }

    public void delete(Long id) {
        log.info("CategoryService.delete called id={}", id);

        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Delete category failed: not found id={}", id);
                    return new NotFoundException("Category not found");
                });

        categoryRepository.delete(c);

        log.info("Category deleted categoryId={} name={}", c.getId(), c.getName());
        AUDIT.info("CATEGORY_DELETE categoryId=%d name=%s".formatted(c.getId(), c.getName()));
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