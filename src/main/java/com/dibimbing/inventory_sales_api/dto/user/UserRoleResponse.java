package com.dibimbing.inventory_sales_api.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UserRoleResponse {
    private Long userId;
    private Long roleId;
    private String roleName;
}