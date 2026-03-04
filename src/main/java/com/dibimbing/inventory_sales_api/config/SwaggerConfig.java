package com.dibimbing.inventory_sales_api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(new Info()
            .title("Inventory & Sales API")
            .version("1.0.0")
            .description("Final Project - Spring Boot, JPA, MySQL, JWT, RBAC"));
    }
}
