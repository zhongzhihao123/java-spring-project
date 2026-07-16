package com.aiplatform.business.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * 健康检查 & API 信息
 * 供 Java Gateway 路由 /api/health 和 /api/info
 */
@RestController
public class HealthController {

    @GetMapping("/api/health")
    public Map<String, Object> health() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "healthy");
        result.put("service", "ai-platform");
        result.put("version", "1.0.0");
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    @GetMapping("/api/info")
    public Map<String, Object> info() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("name", "AI System Platform");
        result.put("version", "1.0.0");

        Map<String, Object> services = new LinkedHashMap<>();
        services.put("users", Map.of("url", "/api/users", "description", "用户认证与管理"));
        services.put("business", Map.of("url", "/api/business", "description", "商品与订单"));
        services.put("dbadmin", Map.of("url", "/api/dbadmin", "description", "数据库管理"));
        services.put("nlp", Map.of("url", "/api/nlp", "description", "NLP知识库问答"));
        services.put("recommend", Map.of("url", "/api/recommend", "description", "智能推荐系统"));
        services.put("cv", Map.of("url", "/api/cv", "description", "计算机视觉"));
        services.put("mlops", Map.of("url", "/api/mlops", "description", "MLOps平台"));
        result.put("services", services);

        return result;
    }
}
