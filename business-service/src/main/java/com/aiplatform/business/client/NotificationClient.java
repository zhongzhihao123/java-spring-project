package com.aiplatform.business.client;

import com.aiplatform.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * Feign 客户端：调用通知服务发送消息
 */
@FeignClient(name = "notification-service", url = "http://localhost:8103")
public interface NotificationClient {

    @PostMapping("/api/notifications/send")
    ApiResponse<String> sendNotification(@RequestBody Map<String, Object> request);
}
