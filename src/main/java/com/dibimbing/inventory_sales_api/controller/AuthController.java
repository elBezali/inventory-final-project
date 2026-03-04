package com.dibimbing.inventory_sales_api.controller;

import com.dibimbing.inventory_sales_api.constant.ApiPath;
import com.dibimbing.inventory_sales_api.dto.auth.LoginRequest;
import com.dibimbing.inventory_sales_api.dto.auth.LoginResponse;
import com.dibimbing.inventory_sales_api.dto.auth.RegisterRequest;
import com.dibimbing.inventory_sales_api.dto.common.ApiResponse;
import com.dibimbing.inventory_sales_api.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiPath.AUTH)
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<Object> register(@Valid @RequestBody RegisterRequest req) {
        authService.register(req);
        return ApiResponse.ok("Register success", null);
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return ApiResponse.ok("Login success", authService.login(req));
    }
}