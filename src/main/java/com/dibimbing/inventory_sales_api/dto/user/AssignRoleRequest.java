package com.dibimbing.inventory_sales_api.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignRoleRequest {
    @NotNull(message = "roleId is required")
    private Long roleId;
}