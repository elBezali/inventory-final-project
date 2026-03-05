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
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private static final Logger AUDIT = LoggerFactory.getLogger("com.dibimbing.inventory_sales_api.audit");

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public void register(RegisterRequest req) {
        final String email = req.getEmail();
        log.info("AuthService.register called email={}", email);

        if (userRepository.existsByEmail(email)) {
            log.warn("Register rejected: email already used email={}", email);
            throw new BadRequestException("Email already used");
        }

        Role userRole = roleRepository.findByName("USER")
                .orElseGet(() -> {
                    log.info("Role USER not found, creating role USER");
                    return roleRepository.save(Role.builder().name("USER").build());
                });

        User u = User.builder()
                .fullName(req.getFullName())
                .email(email)
                .passwordHash(passwordEncoder.encode(req.getPassword())) 
                .status(User.Status.ACTIVE)
                .roles(Set.of(userRole))
                .build();

        User saved = userRepository.save(u);

        log.info("Register success userId={} email={}", saved.getId(), saved.getEmail());
        AUDIT.info("REGISTER_SUCCESS userId=%d email=%s".formatted(saved.getId(), saved.getEmail()));
    }

    public LoginResponse login(LoginRequest req) {
        final String email = req.getEmail();
        log.info("AuthService.login attempt email={}", email);

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );

        User u = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed: user not found email={}", email);
                    return new BadRequestException("Invalid credentials");
                });

        String roles = u.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.joining(","));

        String token = jwtService.generateToken(
                u.getEmail(),
                Map.of("roles", roles)
        );

        log.info("Login success userId={} email={} roles={}", u.getId(), u.getEmail(), roles);
        AUDIT.info("LOGIN_SUCCESS userId=%d email=%s roles=%s".formatted(u.getId(), u.getEmail(), roles));

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresInMinutes(jwtService.getExpirationMinutes())
                .build();
    }
}