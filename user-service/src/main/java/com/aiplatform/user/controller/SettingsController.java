package com.aiplatform.user.controller;

import com.aiplatform.common.dto.ApiResponse;
import com.aiplatform.user.dto.UserSettingsResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户设置接口 — 壁纸/主题等个性化配置
 */
@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private final JdbcTemplate jdbc;

    public SettingsController(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    /** 获取当前用户设置 */
    @GetMapping
    public ApiResponse<UserSettingsResponse> getSettings(@RequestHeader("X-User-Id") Long userId) {
        var row = jdbc.queryForList(
            "SELECT wallpaper, theme FROM user_settings WHERE user_id = ?", userId);
        if (row.isEmpty()) {
            jdbc.update("INSERT IGNORE INTO user_settings (user_id, wallpaper) VALUES (?, 'midnight')", userId);
            return ApiResponse.success(new UserSettingsResponse(Map.of("wallpaper", "midnight", "theme", "dark")));
        }
        return ApiResponse.success(new UserSettingsResponse(row.get(0)));
    }

    /** 更新用户设置 */
    @PutMapping
    public ApiResponse<UserSettingsResponse> updateSettings(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody Map<String, String> body) {
        if (body.containsKey("wallpaper")) {
            jdbc.update("INSERT INTO user_settings (user_id, wallpaper) VALUES (?, ?) ON DUPLICATE KEY UPDATE wallpaper = ?",
                userId, body.get("wallpaper"), body.get("wallpaper"));
        }
        if (body.containsKey("theme")) {
            jdbc.update("INSERT INTO user_settings (user_id, theme) VALUES (?, ?) ON DUPLICATE KEY UPDATE theme = ?",
                userId, body.get("theme"), body.get("theme"));
        }
        return getSettings(userId);
    }
}
