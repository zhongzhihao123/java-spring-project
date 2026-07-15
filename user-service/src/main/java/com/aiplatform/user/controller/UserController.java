package com.aiplatform.user.controller;

import com.aiplatform.common.dto.ApiResponse;
import com.aiplatform.user.dto.*;
import com.aiplatform.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器 — 注册 / 登录 / 用户信息
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ApiResponse<UserResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ApiResponse.success("注册成功", userService.register(req));
    }

    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest req) {
        return ApiResponse.success("登录成功", userService.login(req));
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> me(@RequestHeader("X-User-Id") Long userId) {
        return ApiResponse.success(userService.getCurrentUser(userId));
    }
}
