package com.dibimbing.inventory_sales_api.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "fullName is required")
    private String fullName;

    @Email(message = "email must be valid")
    @NotBlank(message = "email is required")
    private String email;

    @Size(min = 6, message = "password min 6 chars")
    @NotBlank(message = "password is required")
    private String password;
}