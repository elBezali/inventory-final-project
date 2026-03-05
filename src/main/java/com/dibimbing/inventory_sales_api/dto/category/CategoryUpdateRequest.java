package com.dibimbing.inventory_sales_api.dto.category;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryUpdateRequest {
    @NotBlank(message = "name is required")
    private String name;

    private String description;
}