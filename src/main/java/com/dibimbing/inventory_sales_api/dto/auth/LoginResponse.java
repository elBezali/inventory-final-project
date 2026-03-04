package com.dibimbing.inventory_sales_api.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String tokenType;
    private long expiresInMinutes;
}