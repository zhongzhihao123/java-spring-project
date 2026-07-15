package com.aiplatform.user.dto;

import java.util.Map;

/**
 * 用户个性化设置
 */
public class UserSettingsResponse {
    private String wallpaper;
    private String theme;

    public UserSettingsResponse() {}
    public UserSettingsResponse(Map<String, Object> map) {
        this.wallpaper = (String) map.getOrDefault("wallpaper", "midnight");
        this.theme = (String) map.getOrDefault("theme", "dark");
    }

    public String getWallpaper() { return wallpaper; }
    public void setWallpaper(String wallpaper) { this.wallpaper = wallpaper; }
    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }
}
