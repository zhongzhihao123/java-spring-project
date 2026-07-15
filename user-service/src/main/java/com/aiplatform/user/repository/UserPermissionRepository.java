package com.aiplatform.user.repository;

import com.aiplatform.user.entity.UserPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserPermissionRepository extends JpaRepository<UserPermission, Long> {
    List<UserPermission> findByUserId(Long userId);
    void deleteByUserIdAndPermissionId(Long userId, Long permissionId);
    boolean existsByUserIdAndPermissionId(Long userId, Long permissionId);
    void deleteByUserId(Long userId);
}
