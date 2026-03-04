package com.dibimbing.inventory_sales_api.dto.customer;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CustomerCreateRequest {
    @NotBlank(message = "name is required")
    private String name;

    private String email;
    private String phone;
    private String address;
}
