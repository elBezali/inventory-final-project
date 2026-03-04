package com.dibimbing.inventory_sales_api.service;

import com.dibimbing.inventory_sales_api.dto.auth.LoginRequest;
import com.dibimbing.inventory_sales_api.dto.auth.LoginResponse;
import com.dibimbing.inventory_sales_api.dto.auth.RegisterRequest;
import com.dibimbing.inventory_sales_api.entity.Role;
import com.dibimbing.inventory_sales_api.entity.User;
import com.dibimbing.inventory_sales_api.exception.BadRequestException;
import com.dibimbing.inventory_sales_api.repository.RoleRepository;
import com.dibimbing.inventory_sales_api.repository.UserRepository;
import com.dibimbing.inventory_sales_api.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public void register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new BadRequestException("Email already used");
        }

        Role userRole = roleRepository.findByName("USER")
                .orElseGet(() -> roleRepository.save(Role.builder().name("USER").build()));

        User u = User.builder()
                .fullName(req.getFullName())
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .status(User.Status.ACTIVE)
                .roles(Set.of(userRole))
                .build();

        userRepository.save(u);
    }

    public LoginResponse login(LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );

        User u = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid credentials"));

        String roles = u.getRoles().stream().map(Role::getName).collect(Collectors.joining(","));

        String token = jwtService.generateToken(
                u.getEmail(),
                Map.of("roles", roles)
        );

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresInMinutes(jwtService.getExpirationMinutes())
                .build();
    }
}