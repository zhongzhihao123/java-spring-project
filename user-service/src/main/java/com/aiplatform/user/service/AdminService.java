package com.aiplatform.user.service;

import com.aiplatform.common.exception.BusinessException;
import com.aiplatform.user.dto.AdminUserResponse;
import com.aiplatform.user.dto.RegisterRequest;
import com.aiplatform.user.entity.AppPermission;
import com.aiplatform.user.entity.User;
import com.aiplatform.user.entity.UserPermission;
import com.aiplatform.user.repository.AppPermissionRepository;
import com.aiplatform.user.repository.UserPermissionRepository;
import com.aiplatform.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 系统管理服务 — 用户管理 + 权限配置
 */
@Service
public class AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminService.class);
    private final UserRepository userRepository;
    private final AppPermissionRepository appPermRepo;
    private final UserPermissionRepository userPermRepo;

    public AdminService(UserRepository userRepository,
                        AppPermissionRepository appPermRepo,
                        UserPermissionRepository userPermRepo) {
        this.userRepository = userRepository;
        this.appPermRepo = appPermRepo;
        this.userPermRepo = userPermRepo;
    }

    /** 用户列表 */
    public List<AdminUserResponse> listUsers() {
        return userRepository.findAll().stream()
                .map(this::toAdminResponse)
                .collect(Collectors.toList());
    }

    /** 用户详情 */
    public AdminUserResponse getUser(Long id) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("用户", id));
        return toAdminResponse(u);
    }

    /** 创建用户 */
    @Transactional
    public AdminUserResponse createUser(RegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername()))
            throw BusinessException.conflict("用户名已存在");
        if (userRepository.existsByEmail(req.getEmail()))
            throw BusinessException.conflict("邮箱已注册");

        User u = new User();
        u.setUsername(req.getUsername());
        u.setEmail(req.getEmail());
        u.setPassword("HASH_" + (req.getPassword() != null ? req.getPassword() : "123456"));
        u.setDisplayName(req.getDisplayName() != null ? req.getDisplayName() : req.getUsername());
        u.setRole(req.getRole() != null ? req.getRole() : "user");
        u.setEnabled(true);
        u = userRepository.save(u);
        log.info("管理员创建用户: {}", u.getUsername());
        return toAdminResponse(u);
    }

    /** 更新用户 */
    @Transactional
    public AdminUserResponse updateUser(Long id, Map<String, Object> body) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("用户", id));
        if (body.containsKey("displayName")) u.setDisplayName((String) body.get("displayName"));
        if (body.containsKey("email")) u.setEmail((String) body.get("email"));
        if (body.containsKey("role")) u.setRole((String) body.get("role"));
        if (body.containsKey("enabled")) u.setEnabled((Boolean) body.get("enabled"));
        userRepository.save(u);
        return toAdminResponse(u);
    }

    /** 删除用户 */
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id))
            throw BusinessException.notFound("用户", id);
        userPermRepo.deleteByUserId(id);
        userRepository.deleteById(id);
        log.info("管理员删除用户 ID: {}", id);
    }

    /** 重置密码 */
    @Transactional
    public void resetPassword(Long id, String newPassword) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("用户", id));
        u.setPassword("HASH_" + (newPassword != null ? newPassword : "123456"));
        userRepository.save(u);
        log.info("管理员重置密码: {}", u.getUsername());
    }

    /** 忘记密码 - 验证用户名+邮箱后重置 */
    @Transactional
    public void forgotPassword(String username, String email, String newPassword) {
        User u = userRepository.findByUsername(username)
                .orElseThrow(() -> BusinessException.badRequest("用户名或邮箱不匹配"));
        if (!u.getEmail().equals(email)) {
            throw BusinessException.badRequest("用户名或邮箱不匹配");
        }
        u.setPassword("HASH_" + newPassword);
        userRepository.save(u);
        log.info("用户 {} 通过忘记密码重置了密码", username);
    }

    /** 获取用户权限 key 列表 */
    public List<String> getUserPermissions(Long userId) {
        List<UserPermission> ups = userPermRepo.findByUserId(userId);
        List<Long> permIds = ups.stream().map(UserPermission::getPermissionId).toList();
        if (permIds.isEmpty()) return List.of();
        return appPermRepo.findAllById(permIds).stream()
                .map(AppPermission::getAppKey)
                .collect(Collectors.toList());
    }

    /** 设置用户权限 */
    @Transactional
    public void setUserPermissions(Long userId, List<Integer> permissionIds) {
        userPermRepo.deleteByUserId(userId);
        if (permissionIds != null && !permissionIds.isEmpty()) {
            for (Integer pid : permissionIds) {
                UserPermission up = new UserPermission();
                up.setUserId(userId);
                up.setPermissionId(pid.longValue());
                up.setGrantedBy("admin");
                userPermRepo.save(up);
            }
        }
        log.info("更新用户 {} 权限: {} 项", userId, permissionIds != null ? permissionIds.size() : 0);
    }

    /** 所有应用权限列表 */
    public List<Map<String, Object>> listPermissions() {
        return appPermRepo.findAll().stream().map(p -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", p.getId());
            m.put("appKey", p.getAppKey());
            m.put("appName", p.getAppName());
            m.put("description", p.getDescription());
            m.put("icon", p.getIcon());
            m.put("color", p.getColor());
            return m;
        }).collect(Collectors.toList());
    }

    private AdminUserResponse toAdminResponse(User u) {
        AdminUserResponse r = new AdminUserResponse();
        r.setId(u.getId());
        r.setUsername(u.getUsername());
        r.setEmail(u.getEmail());
        r.setDisplayName(u.getDisplayName());
        r.setRole(u.getRole());
        r.setEnabled(u.getEnabled());
        r.setCreatedAt(u.getCreatedAt() != null ? u.getCreatedAt().toString() : null);
        r.setPermissionKeys(getUserPermissions(u.getId()));
        return r;
    }
}
