package com.aiplatform.oa.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 企业微信 API 集成服务
 * 
 * 功能：
 * 1. 获取 access_token
 * 2. 获取通讯录部门/成员列表
 * 3. 发送应用消息通知
 * 4. 发送群机器人消息
 */
@Service
public class WecomService {

    private static final Logger logger = LoggerFactory.getLogger(WecomService.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    // 企业微信配置
    @Value("${wecom.corp-id:}")
    private String corpId;

    @Value("${wecom.agent-id:}")
    private String agentId;

    @Value("${wecom.corp-secret:}")
    private String corpSecret;

    @Value("${wecom.enabled:false}")
    private boolean enabled;

    // 群机器人 Webhook（可选）
    @Value("${wecom.webhook-url:}")
    private String webhookUrl;

    // Access token 缓存
    private String cachedToken;
    private long tokenExpireTime = 0;

    /**
     * 获取 access_token（带缓存）
     */
    public String getAccessToken() {
        if (!enabled) {
            logger.info("WeCom integration disabled, returning mock token");
            return "mock-access-token";
        }

        if (cachedToken != null && System.currentTimeMillis() < tokenExpireTime) {
            return cachedToken;
        }

        try {
            String urlStr = String.format(
                "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=%s&corpsecret=%s",
                corpId, corpSecret
            );
            String response = httpGet(urlStr);
            JsonNode node = mapper.readTree(response);

            if (node.has("errcode") && node.get("errcode").asInt() == 0) {
                cachedToken = node.get("access_token").asText();
                int expiresIn = node.get("expires_in").asInt();
                tokenExpireTime = System.currentTimeMillis() + (expiresIn - 300) * 1000L; // 提前5分钟刷新
                logger.info("WeCom access_token refreshed, expires in {}s", expiresIn);
                return cachedToken;
            } else {
                logger.error("Failed to get WeCom access_token: {}", response);
                return null;
            }
        } catch (Exception e) {
            logger.error("Error getting WeCom access_token", e);
            return null;
        }
    }

    /**
     * 获取部门列表
     */
    public List<Map<String, Object>> getDepartments() {
        if (!enabled) {
            return getMockDepartments();
        }

        try {
            String token = getAccessToken();
            String urlStr = String.format(
                "https://qyapi.weixin.qq.com/cgi-bin/department/list?access_token=%s",
                token
            );
            String response = httpGet(urlStr);
            JsonNode node = mapper.readTree(response);
            logger.info("WeCom department list response: {}", response);

            List<Map<String, Object>> departments = new ArrayList<>();
            if (node.has("errcode") && node.get("errcode").asInt() != 0) {
                logger.error("WeCom API error: code={}, msg={}", node.get("errcode").asInt(), node.has("errmsg") ? node.get("errmsg").asText() : "unknown");
                return getMockDepartments();
            }
            if (node.has("department")) {
                for (JsonNode dept : node.get("department")) {
                    Map<String, Object> deptMap = new HashMap<>();
                    deptMap.put("id", dept.get("id").asInt());
                    deptMap.put("name", dept.get("name").asText());
                    if (dept.has("parentid")) deptMap.put("parentId", dept.get("parentid").asInt());
                    departments.add(deptMap);
                }
            }
            return departments;
        } catch (Exception e) {
            logger.error("Error getting WeCom departments", e);
            return getMockDepartments();
        }
    }

    /**
     * 获取部门成员列表
     */
    public List<Map<String, Object>> getDepartmentMembers(int departmentId) {
        if (!enabled) {
            return getMockMembers();
        }

        try {
            String token = getAccessToken();
            String urlStr = String.format(
                "https://qyapi.weixin.qq.com/cgi-bin/user/simplelist?access_token=%s&department_id=%d",
                token, departmentId
            );
            String response = httpGet(urlStr);
            JsonNode node = mapper.readTree(response);
            logger.info("WeCom member list response for dept {}: {}", departmentId, response);

            List<Map<String, Object>> members = new ArrayList<>();
            if (node.has("errcode") && node.get("errcode").asInt() != 0) {
                logger.error("WeCom API error: code={}, msg={}", node.get("errcode").asInt(), node.has("errmsg") ? node.get("errmsg").asText() : "unknown");
                return getMockMembers();
            }
            if (node.has("userlist")) {
                for (JsonNode user : node.get("userlist")) {
                    Map<String, Object> member = new HashMap<>();
                    member.put("userId", user.get("userid").asText());
                    member.put("name", user.get("name").asText());
                    if (user.has("department")) member.put("departments", user.get("department"));
                    members.add(member);
                }
            }
            return members;
        } catch (Exception e) {
            logger.error("Error getting WeCom department members", e);
            return getMockMembers();
        }
    }

    /**
     * 获取成员详情（通过 userid）
     */
    public Map<String, Object> getUserInfo(String userId) {
        if (!enabled) {
            return getMockUserInfo(userId);
        }

        try {
            String token = getAccessToken();
            String urlStr = String.format(
                "https://qyapi.weixin.qq.com/cgi-bin/user/get?access_token=%s&userid=%s",
                token, userId
            );
            String response = httpGet(urlStr);
            JsonNode node = mapper.readTree(response);

            Map<String, Object> userInfo = new HashMap<>();
            if (node.has("userid")) {
                userInfo.put("userId", node.get("userid").asText());
                userInfo.put("name", node.get("name").asText());
                if (node.has("position")) userInfo.put("position", node.get("position").asText());
                if (node.has("mobile")) userInfo.put("mobile", node.get("mobile").asText());
                if (node.has("email")) userInfo.put("email", node.get("email").asText());
                if (node.has("avatar")) userInfo.put("avatar", node.get("avatar").asText());
                if (node.has("department")) userInfo.put("departments", node.get("department"));
            }
            return userInfo;
        } catch (Exception e) {
            logger.error("Error getting WeCom user info", e);
            return getMockUserInfo(userId);
        }
    }

    /**
     * 发送应用消息（通过企业微信应用）
     * 优先使用Webhook，如果Webhook不可用则尝试API
     */
    public boolean sendApplicationMessage(String toUser, String title, String content) {
        if (!enabled) {
            logger.info("[WeCom Mock] Send app message to: {}, title: {}", toUser, title);
            logger.info("[WeCom Mock] Content: {}", content);
            return true;
        }

        // 优先使用Webhook（不需要IP白名单）
        if (webhookUrl != null && !webhookUrl.isEmpty()) {
            logger.info("Using webhook to send notification");
            String markdown = String.format(
                "> **%s**\n> %s",
                title, content.replace("\n", "\n> ")
            );
            return sendWebhookMessage(title, markdown);
        }

        // 尝试使用API（需要IP白名单）
        try {
            String token = getAccessToken();
            String urlStr = String.format(
                "https://qyapi.weixin.qq.com/cgi-bin/message/send?access_token=%s",
                token
            );

            Map<String, Object> body = new HashMap<>();
            body.put("touser", toUser);
            body.put("msgtype", "textcard");
            body.put("agentid", Integer.parseInt(agentId));

            Map<String, Object> textcard = new HashMap<>();
            textcard.put("title", title);
            textcard.put("description", content);
            textcard.put("url", "http://localhost:3000");
            textcard.put("btntxt", "去审批");
            body.put("textcard", textcard);

            String jsonBody = mapper.writeValueAsString(body);
            String response = httpPost(urlStr, jsonBody);
            JsonNode node = mapper.readTree(response);

            if (node.has("errcode") && node.get("errcode").asInt() == 0) {
                logger.info("WeCom message sent successfully to: {}", toUser);
                return true;
            } else {
                logger.error("Failed to send WeCom message: {}", response);
                return false;
            }
        } catch (Exception e) {
            logger.error("Error sending WeCom message", e);
            return false;
        }
    }

    /**
     * 发送群机器人消息（Webhook）
     */
    public boolean sendWebhookMessage(String title, String content) {
        if (webhookUrl == null || webhookUrl.isEmpty()) {
            logger.info("[WeCom Webhook Mock] title: {}, content: {}", title, content);
            return true;
        }

        try {
            Map<String, Object> body = new HashMap<>();
            body.put("msgtype", "markdown");

            Map<String, Object> markdown = new HashMap<>();
            markdown.put("content", "## " + title + "\n\n" + content);
            body.put("markdown", markdown);

            String jsonBody = mapper.writeValueAsString(body);
            String response = httpPost(webhookUrl, jsonBody);
            logger.info("WeCom webhook sent: {}", response);
            return true;
        } catch (Exception e) {
            logger.error("Error sending WeCom webhook", e);
            return false;
        }
    }

    /**
     * 发送审批通知（供 OAService 调用）
     */
    public void sendApprovalNotification(String approverUserId, String approverName,
                                          String applicantName, String typeName,
                                          double days, String reason, String appNo) {
        String title = "📋 新审批待处理";
        String content = String.format(
            "申请人：%s\n假期类型：%s\n天数：%.1f天\n编号：%s\n原因：%s",
            applicantName, typeName, days, appNo, reason
        );

        // 发送应用消息
        if (approverUserId != null && !approverUserId.isEmpty()) {
            sendApplicationMessage(approverUserId, title, content);
        }

        // 同时发送群机器人通知
        String markdown = String.format(
            "> **申请人**: %s\n> **假期类型**: %s\n> **天数**: %.1f天\n> **编号**: %s\n> **原因**: %s\n> 请尽快审批",
            applicantName, typeName, days, appNo, reason
        );
        sendWebhookMessage(title, markdown);
    }

    /**
     * 发送审批结果通知
     */
    public void sendResultNotification(String applicantUserId, String applicantName,
                                        String typeName, double days, boolean approved,
                                        String approverName, String comment) {
        String status = approved ? "✅ 已通过" : "❌ 已驳回";
        String title = "审批结果通知 - " + status;
        String content = String.format(
            "您的%s申请（%.1f天）%s\n审批人：%s%s",
            typeName, days, status, approverName,
            comment != null && !comment.isEmpty() ? "\n意见：" + comment : ""
        );

        // 通过应用消息发送
        if (applicantUserId != null && !applicantUserId.isEmpty()) {
            sendApplicationMessage(applicantUserId, title, content);
        }

        // 通过群机器人Webhook发送
        String markdown = String.format(
            "> **申请人**: %s\n> **假期类型**: %s\n> **天数**: %.1f天\n> **状态**: %s\n> **审批人**: %s%s",
            applicantName, typeName, days, status, approverName,
            comment != null && !comment.isEmpty() ? "\n> **意见**: " + comment : ""
        );
        sendWebhookMessage(title, markdown);
    }

    // === Mock Data ===

    private List<Map<String, Object>> getMockDepartments() {
        List<Map<String, Object>> depts = new ArrayList<>();
        depts.add(Map.of("id", 1, "name", "总部"));
        depts.add(Map.of("id", 2, "name", "技术部"));
        depts.add(Map.of("id", 3, "name", "产品部"));
        depts.add(Map.of("id", 4, "name", "市场部"));
        depts.add(Map.of("id", 5, "name", "人事部"));
        return depts;
    }

    private List<Map<String, Object>> getMockMembers() {
        List<Map<String, Object>> members = new ArrayList<>();
        members.add(Map.of("userId", "admin", "name", "管理员"));
        members.add(Map.of("userId", "zhangsan", "name", "张三"));
        members.add(Map.of("userId", "lisi", "name", "李四"));
        members.add(Map.of("userId", "wangwu", "name", "王五"));
        members.add(Map.of("userId", "zhaoliu", "name", "赵六"));
        return members;
    }

    private Map<String, Object> getMockUserInfo(String userId) {
        Map<String, Object> info = new HashMap<>();
        info.put("userId", userId);
        info.put("name", userId.equals("admin") ? "管理员" : userId);
        info.put("position", "员工");
        return info;
    }

    // === HTTP Utils ===

    private String httpGet(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(10000);

        try (java.io.InputStream is = conn.getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private String httpPost(String urlStr, String jsonBody) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(10000);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
        }

        try (java.io.InputStream is = conn.getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
