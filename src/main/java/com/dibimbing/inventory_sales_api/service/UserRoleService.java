package com.dibimbing.inventory_sales_api.service;

import com.dibimbing.inventory_sales_api.dto.user.UserRoleResponse;
import com.dibimbing.inventory_sales_api.entity.*;
import com.dibimbing.inventory_sales_api.exception.BadRequestException;
import com.dibimbing.inventory_sales_api.exception.NotFoundException;
import com.dibimbing.inventory_sales_api.repository.RoleRepository;
import com.dibimbing.inventory_sales_api.repository.UserRepository;
import com.dibimbing.inventory_sales_api.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserRoleService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    @Transactional
    public UserRoleResponse assignRole(Long userId, Long roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new NotFoundException("Role not found"));

        if (userRoleRepository.existsByIdUserIdAndIdRoleId(userId, roleId)) {
            throw new BadRequestException("User already has this role");
        }

        UserRole ur = UserRole.builder()
                .id(new UserRoleId(userId, roleId))
                .user(user)
                .role(role)
                .build();

        userRoleRepository.save(ur);

        return UserRoleResponse.builder()
                .userId(userId)
                .roleId(roleId)
                .roleName(role.getName())
                .build();
    }

    @Transactional
    public void removeRole(Long userId, Long roleId) {
        // validasi user & role exist biar error message jelas
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found");
        }
        if (!roleRepository.existsById(roleId)) {
            throw new NotFoundException("Role not found");
        }

        long deleted = userRoleRepository.deleteByIdUserIdAndIdRoleId(userId, roleId);
        if (deleted == 0) {
            throw new NotFoundException("User role not found");
        }
    }

    public List<UserRoleResponse> getUserRoles(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found");
        }

        return userRoleRepository.findByIdUserId(userId).stream()
                .map(ur -> UserRoleResponse.builder()
                        .userId(ur.getUser().getId())
                        .roleId(ur.getRole().getId())
                        .roleName(ur.getRole().getName())
                        .build())
                .toList();
    }
}