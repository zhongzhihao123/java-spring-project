package com.aiplatform.user.repository;

import com.aiplatform.user.entity.AppPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppPermissionRepository extends JpaRepository<AppPermission, Long> {
    Optional<AppPermission> findByAppKey(String appKey);
}
