package com.dibimbing.inventory_sales_api.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class PageMeta {
    private int currentPage;
    private int pageSize;
    private long totalItems;
    private int totalPages;
}