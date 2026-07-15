package com.aiplatform.notification.service;

import com.aiplatform.common.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通知服务 — 站内信发送与管理
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final ConcurrentHashMap<Long, StringBuilder> notifications = new ConcurrentHashMap<>();

    /** 发送站内信 */
    public void sendInternal(Long userId, String title, String content) {
        var sb = notifications.computeIfAbsent(userId, k -> new StringBuilder());
        var msg = String.format("[%s] %s: %s", LocalDateTime.now(), title, content);
        sb.append(msg).append("\n");
        log.info("站内信发送 -> 用户{}: {}", userId, title);
    }

    /** 获取用户消息列表 */
    public String getUserMessages(Long userId) {
        var sb = notifications.get(userId);
        return sb != null ? sb.toString() : "暂无消息";
    }

    /** 通用通知 API */
    public ApiResponse<String> sendNotification(Map<String, Object> request) {
        var userId = Long.valueOf(request.getOrDefault("userId", 0).toString());
        var title = request.getOrDefault("title", "通知").toString();
        var content = request.getOrDefault("content", "").toString();
        sendInternal(userId, title, content);
        return ApiResponse.success("通知已发送");
    }
}
