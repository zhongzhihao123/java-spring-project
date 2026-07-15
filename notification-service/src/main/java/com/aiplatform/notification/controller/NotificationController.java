package com.aiplatform.notification.controller;

import com.aiplatform.common.dto.ApiResponse;
import com.aiplatform.notification.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 通知控制器 — 发送通知 / 查询消息
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/send")
    public ApiResponse<String> send(@RequestBody Map<String, Object> request) {
        return notificationService.sendNotification(request);
    }

    @GetMapping("/messages")
    public ApiResponse<String> getMessages(@RequestHeader("X-User-Id") Long userId) {
        return ApiResponse.success(notificationService.getUserMessages(userId));
    }
}
