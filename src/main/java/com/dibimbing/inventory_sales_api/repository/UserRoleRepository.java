package com.dibimbing.inventory_sales_api.repository;

import com.dibimbing.inventory_sales_api.entity.UserRole;
import com.dibimbing.inventory_sales_api.entity.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {
    boolean existsByIdUserIdAndIdRoleId(Long userId, Long roleId);
    List<UserRole> findByIdUserId(Long userId);
    long deleteByIdUserIdAndIdRoleId(Long userId, Long roleId);
}