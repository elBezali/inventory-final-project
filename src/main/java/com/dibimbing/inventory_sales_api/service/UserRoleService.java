package com.dibimbing.inventory_sales_api.service;

import com.dibimbing.inventory_sales_api.dto.user.UserRoleResponse;
import com.dibimbing.inventory_sales_api.entity.*;
import com.dibimbing.inventory_sales_api.exception.BadRequestException;
import com.dibimbing.inventory_sales_api.exception.NotFoundException;
import com.dibimbing.inventory_sales_api.repository.RoleRepository;
import com.dibimbing.inventory_sales_api.repository.UserRepository;
import com.dibimbing.inventory_sales_api.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRoleService {

    private static final Logger AUDIT = LoggerFactory.getLogger("com.dibimbing.inventory_sales_api.audit");

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    @Transactional
    public UserRoleResponse assignRole(Long userId, Long roleId) {
        log.info("UserRoleService.assignRole called userId={} roleId={}", userId, roleId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Assign role failed: user not found userId={}", userId);
                    return new NotFoundException("User not found");
                });

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> {
                    log.warn("Assign role failed: role not found roleId={}", roleId);
                    return new NotFoundException("Role not found");
                });

        if (userRoleRepository.existsByIdUserIdAndIdRoleId(userId, roleId)) {
            log.warn("Assign role rejected: already has role userId={} roleId={}", userId, roleId);
            throw new BadRequestException("User already has this role");
        }

        UserRole ur = UserRole.builder()
                .id(new UserRoleId(userId, roleId))
                .user(user)
                .role(role)
                .build();

        userRoleRepository.save(ur);

        log.info("Role assigned userId={} roleId={} roleName={}", userId, roleId, role.getName());
        AUDIT.info("ROLE_ASSIGNED userId=%d roleId=%d roleName=%s".formatted(userId, roleId, role.getName()));

        return UserRoleResponse.builder()
                .userId(userId)
                .roleId(roleId)
                .roleName(role.getName())
                .build();
    }

    @Transactional
    public void removeRole(Long userId, Long roleId) {
        log.info("UserRoleService.removeRole called userId={} roleId={}", userId, roleId);

        if (!userRepository.existsById(userId)) {
            log.warn("Remove role failed: user not found userId={}", userId);
            throw new NotFoundException("User not found");
        }
        if (!roleRepository.existsById(roleId)) {
            log.warn("Remove role failed: role not found roleId={}", roleId);
            throw new NotFoundException("Role not found");
        }

        long deleted = userRoleRepository.deleteByIdUserIdAndIdRoleId(userId, roleId);
        if (deleted == 0) {
            log.warn("Remove role failed: user role not found userId={} roleId={}", userId, roleId);
            throw new NotFoundException("User role not found");
        }

        log.info("Role removed userId={} roleId={}", userId, roleId);
        AUDIT.info("ROLE_REVOKED userId=%d roleId=%d".formatted(userId, roleId));
    }

    public List<UserRoleResponse> getUserRoles(Long userId) {
        log.debug("UserRoleService.getUserRoles called userId={}", userId);

        if (!userRepository.existsById(userId)) {
            log.warn("Get user roles failed: user not found userId={}", userId);
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