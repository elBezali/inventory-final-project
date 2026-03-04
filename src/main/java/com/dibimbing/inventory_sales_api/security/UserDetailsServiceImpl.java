package com.dibimbing.inventory_sales_api.security;

import com.dibimbing.inventory_sales_api.entity.User;
import com.dibimbing.inventory_sales_api.exception.UnauthorizedException;
import com.dibimbing.inventory_sales_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User u = userRepository.findByEmail(username)
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (u.getStatus() != User.Status.ACTIVE) {
            throw new UnauthorizedException("Account is inactive");
        }

        return new org.springframework.security.core.userdetails.User(
                u.getEmail(),
                u.getPasswordHash(),
                u.getRoles().stream()
                        .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getName()))
                        .collect(Collectors.toSet())
        );
    }
}