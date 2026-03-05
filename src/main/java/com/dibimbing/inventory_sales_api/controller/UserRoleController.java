package com.dibimbing.inventory_sales_api.controller;

import com.dibimbing.inventory_sales_api.constant.ApiPath;
import com.dibimbing.inventory_sales_api.dto.common.ApiResponse;
import com.dibimbing.inventory_sales_api.dto.user.AssignRoleRequest;
import com.dibimbing.inventory_sales_api.dto.user.UserRoleResponse;
import com.dibimbing.inventory_sales_api.service.UserRoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiPath.USERS)
@RequiredArgsConstructor
public class UserRoleController {

    private final UserRoleService userRoleService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{userId}/roles")
    public ApiResponse<UserRoleResponse> assignRole(
            @PathVariable Long userId,
            @Valid @RequestBody AssignRoleRequest req
    ) {
        return ApiResponse.ok("Role assigned", userRoleService.assignRole(userId, req.getRoleId()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{userId}/roles")
    public ApiResponse<List<UserRoleResponse>> getUserRoles(@PathVariable Long userId) {
        return ApiResponse.ok("User roles fetched", userRoleService.getUserRoles(userId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{userId}/roles/{roleId}")
    public ApiResponse<Object> removeRole(@PathVariable Long userId, @PathVariable Long roleId) {
        userRoleService.removeRole(userId, roleId);
        return ApiResponse.ok("Role removed", null);
    }
}