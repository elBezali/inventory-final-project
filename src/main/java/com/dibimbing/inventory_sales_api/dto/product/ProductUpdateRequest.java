package com.dibimbing.inventory_sales_api.dto.product;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductUpdateRequest {
    @NotBlank(message = "name is required")
    private String name;

    private String description;

    @NotNull(message = "categoryId is required")
    private Long categoryId;

    @NotNull(message = "price is required")
    @Min(value = 0, message = "price cannot be negative")
    private BigDecimal price;

    @NotNull(message = "isActive is required")
    private Boolean isActive;
}