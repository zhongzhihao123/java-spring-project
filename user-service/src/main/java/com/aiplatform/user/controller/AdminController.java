package com.aiplatform.user.controller;

import com.aiplatform.common.dto.ApiResponse;
import com.aiplatform.user.dto.*;
import com.aiplatform.user.service.AdminService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统管理接口 — 用户 CRUD + 权限管理
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) { this.adminService = adminService; }

    /** 用户列表 */
    @GetMapping("/users")
    public ApiResponse<List<AdminUserResponse>> listUsers() {
        return ApiResponse.success(adminService.listUsers());
    }

    /** 用户详情 */
    @GetMapping("/users/{id}")
    public ApiResponse<AdminUserResponse> getUser(@PathVariable Long id) {
        return ApiResponse.success(adminService.getUser(id));
    }

    /** 创建用户 */
    @PostMapping("/users")
    public ApiResponse<AdminUserResponse> createUser(@RequestBody RegisterRequest req) {
        return ApiResponse.success(adminService.createUser(req));
    }

    /** 更新用户 */
    @PutMapping("/users/{id}")
    public ApiResponse<AdminUserResponse> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return ApiResponse.success(adminService.updateUser(id, body));
    }

    /** 删除用户 */
    @DeleteMapping("/users/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ApiResponse.success(null);
    }

    /** 重置密码 */
    @PutMapping("/users/{id}/reset-password")
    public ApiResponse<Void> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        adminService.resetPassword(id, body.get("password"));
        return ApiResponse.success(null);
    }

    /** 获取用户权限 */
    @GetMapping("/users/{id}/permissions")
    public ApiResponse<List<String>> getUserPermissions(@PathVariable Long id) {
        return ApiResponse.success(adminService.getUserPermissions(id));
    }

    /** 设置用户权限 */
    @PutMapping("/users/{id}/permissions")
    public ApiResponse<Void> setUserPermissions(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Integer> permIds = (List<Integer>) body.get("permissionIds");
        adminService.setUserPermissions(id, permIds);
        return ApiResponse.success(null);
    }

    /** 所有应用权限列表 */
    @GetMapping("/permissions")
    public ApiResponse<List<Map<String, Object>>> listPermissions() {
        return ApiResponse.success(adminService.listPermissions());
    }

    /** 忘记密码 - 验证用户名+邮箱后重置 */
    @PutMapping("/users/forgot-password")
    public ApiResponse<Void> forgotPassword(@RequestBody Map<String, String> body) {
        adminService.forgotPassword(
                body.get("username"),
                body.get("email"),
                body.get("newPassword"));
        return ApiResponse.success(null);
    }

    /** 当前用户权限（供前端鉴权用） */
    @GetMapping("/my-permissions")
    public ApiResponse<List<String>> myPermissions(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        if (userId == null) return ApiResponse.success(List.of());
        return ApiResponse.success(adminService.getUserPermissions(userId));
    }
}
