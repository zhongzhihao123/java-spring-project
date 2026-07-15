package com.aiplatform.user.dto;

/**
 * Token 登录响应
 */
public class TokenResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private UserResponse user;

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }
    public UserResponse getUser() { return user; }
    public void setUser(UserResponse user) { this.user = user; }
}
