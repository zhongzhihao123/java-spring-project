package com.aiplatform.user.service;

import com.aiplatform.common.exception.BusinessException;
import com.aiplatform.user.dto.*;
import com.aiplatform.user.entity.User;
import com.aiplatform.user.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * 用户服务 — 注册 / 登录 / JWT 签发
 *
 * 所有认证操作由 Java 微服务负责，
 * 前端通过 Java Gateway (:8100) 调用。
 */
@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    @Value("${jwt.secret:ai-platform-jwt-secret-key-min-256-bits-long-2024}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    /** 用户注册 */
    public UserResponse register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw BusinessException.conflict("用户名已存在");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw BusinessException.conflict("邮箱已注册");
        }

        var user = new User();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPassword(hashPassword(req.getPassword()));
        user.setDisplayName(req.getDisplayName() != null ? req.getDisplayName() : req.getUsername());

        user = userRepository.save(user);
        log.info("新用户注册: {}", user.getUsername());
        return toResponse(user);
    }

    /** 用户登录 — 验证凭据并签发 JWT */
    public TokenResponse login(LoginRequest req) {
        var user = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> BusinessException.badRequest("用户名或密码错误"));

        if (!verifyPassword(req.getPassword(), user.getPassword())) {
            throw BusinessException.badRequest("用户名或密码错误");
        }
        if (!user.getEnabled()) {
            throw BusinessException.forbidden("账号已被禁用");
        }

        String token = generateToken(user);
        log.info("用户登录: {}", user.getUsername());

        var tokenResp = new TokenResponse();
        tokenResp.setAccessToken(token);
        tokenResp.setRefreshToken(token);
        tokenResp.setUser(toResponse(user));
        return tokenResp;
    }

    /** 获取当前用户信息 */
    public UserResponse getCurrentUser(Long userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.notFound("用户", userId));
        return toResponse(user);
    }

    /** 生成 JWT Token */
    private String generateToken(User user) {
        var now = new Date();
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("username", user.getUsername())
                .claim("role", user.getRole())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + jwtExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    /** 模拟密码哈希（生产环境应使用 BCrypt） */
    private String hashPassword(String raw) {
        return "HASH_" + raw;
    }

    /** 模拟密码校验（生产环境应使用 BCrypt） */
    private boolean verifyPassword(String raw, String stored) {
        return ("HASH_" + raw).equals(stored);
    }

    /** 实体 → 响应 DTO 转换 */
    private UserResponse toResponse(User user) {
        var resp = new UserResponse();
        resp.setId(user.getId());
        resp.setUsername(user.getUsername());
        resp.setEmail(user.getEmail());
        resp.setDisplayName(user.getDisplayName());
        resp.setRole(user.getRole());
        resp.setEnabled(user.getEnabled());
        return resp;
    }
}
