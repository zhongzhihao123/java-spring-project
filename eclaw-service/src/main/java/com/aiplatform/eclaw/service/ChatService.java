package com.aiplatform.eclaw.service;

import com.aiplatform.eclaw.dto.ActionIntent;
import com.aiplatform.eclaw.dto.ChatResponsePayload;
import com.aiplatform.eclaw.dto.PendingActionPayload;
import com.aiplatform.eclaw.entity.Agent;
import com.aiplatform.eclaw.entity.McpServer;
import com.aiplatform.eclaw.entity.ModelConfig;
import com.aiplatform.eclaw.entity.Session;
import com.aiplatform.eclaw.entity.Skill;
import com.aiplatform.eclaw.repository.McpServerRepository;
import com.aiplatform.eclaw.repository.ModelConfigRepository;
import com.aiplatform.eclaw.repository.SkillRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private ModelConfigRepository modelRepo;
    @Autowired
    private SkillRepository skillRepo;
    @Autowired
    private McpServerRepository mcpRepo;
    @Autowired
    private ActionIntentService actionIntentService;
    @Autowired
    private ChatModePolicyService chatModePolicyService;
    @Autowired
    private ActionExecutionService actionExecutionService;

    /**
     * 发送消息到LLM并获取回复
     */
    public ChatResponsePayload chat(Agent agent, Session session, Long modelId, String message, List<Long> skillIds, List<Long> mcpIds) {
        // 1. 确定使用的模型
        ModelConfig model = null;
        if (modelId != null) {
            model = modelRepo.findById(modelId).orElse(null);
        }
        if (model == null && agent.getModelId() != null) {
            model = modelRepo.findById(agent.getModelId()).orElse(null);
        }
        if (model == null) {
            model = modelRepo.findByIsEnabledTrue().stream().findFirst().orElse(null);
        }

        // 2. 构建系统提示词
        StringBuilder systemPrompt = new StringBuilder();
        if (agent.getSystemPrompt() != null && !agent.getSystemPrompt().isEmpty()) {
            systemPrompt.append(agent.getSystemPrompt());
        }
        // 加载技能提示词
        if (skillIds != null && !skillIds.isEmpty()) {
            List<Skill> skills = skillRepo.findAllById(skillIds);
            for (Skill skill : skills) {
                if (skill.getPrompt() != null && !skill.getPrompt().isEmpty()) {
                    systemPrompt.append("\n\n[").append(skill.getName()).append("]: ").append(skill.getPrompt());
                }
            }
        }

        // 3. 如果选择了MCP服务，先调用MCP工具获取数据库信息
        String mcpContext = "";
        if (mcpIds != null && !mcpIds.isEmpty()) {
            logger.info("MCP IDs provided: {}", mcpIds);
            mcpContext = gatherMcpContext(mcpIds, message);
            logger.info("MCP context length: {}", mcpContext.length());
            if (!mcpContext.isEmpty()) {
                systemPrompt.append("\n\n[数据库工具信息]").append(mcpContext);
                systemPrompt.append("\n请根据以上数据库信息回答用户的问题。如果需要执行SQL查询，请直接提供SQL语句。");
            }
        }

        String mode = session != null && session.getSessionMode() != null ? session.getSessionMode() : "standard";
        String modelName = model != null ? model.getName() : "未配置";
        ActionIntent actionIntent = actionIntentService.detect(message);

        if (actionIntent != null) {
            if ("standard".equals(mode)) {
                Map<String, Object> execution = actionExecutionService.execute(PendingActionPayload.from(actionIntent));
                ChatResponsePayload response = ChatResponsePayload.message((String) execution.getOrDefault("content", "动作已执行"));
                response.setTokensUsed(estimateTokens(message));
                response.setModel(modelName);
                return response;
            }
            if ("plan".equals(mode)) {
                ChatResponsePayload response = ChatResponsePayload.message(buildPlanResponse(actionIntent));
                response.setTokensUsed(estimateTokens(message));
                response.setModel(modelName);
                return response;
            }

            ChatResponsePayload response = chatModePolicyService.evaluate(mode, actionIntent, buildPlanResponse(actionIntent));
            response.setTokensUsed(estimateTokens(message));
            response.setModel(modelName);
            return response;
        }

        // 4. 调用LLM
        ChatResponsePayload result = new ChatResponsePayload();
        result.setResponseType("message");
        if (model != null && model.getApiKey() != null && !model.getApiKey().isEmpty()) {
            // 有API Key，真实调用
            try {
                Map<String, Object> llmResponse = callLLM(model, systemPrompt.toString(), message);
                result.setContent((String) llmResponse.get("content"));
                result.setTokensUsed((Integer) llmResponse.get("tokensUsed"));
                result.setModel(model.getName());
            } catch (Exception e) {
                logger.error("LLM call failed", e);
                result.setContent("LLM调用失败: " + e.getMessage() + "\n\n请检查模型配置中的API Key是否正确。");
                result.setTokensUsed(0);
                result.setModel(model.getName());
            }
        } else {
            // 无API Key，模拟回复
            String agentName = agent.getName();
            result.setContent(buildSimulatedResponse(agentName, modelName, message, systemPrompt.toString(), mcpContext));
            result.setTokensUsed(estimateTokens(message));
            result.setModel(modelName);
        }

        return result;
    }

    private String buildPlanResponse(ActionIntent actionIntent) {
        return "当前模式要求先规划后执行。\n\n建议动作：\n"
            + "- 类型：" + actionIntent.getType() + "\n"
            + "- 标题：" + actionIntent.getTitle() + "\n"
            + "- 摘要：" + actionIntent.getSummary() + "\n\n"
            + "如需继续执行，请进行确认。";
    }

    /**
     * 调用OpenAI兼容API
     */
    private Map<String, Object> callLLM(ModelConfig model, String systemPrompt, String userMessage) throws Exception {
        String apiBase = model.getApiBase();
        if (apiBase.endsWith("/")) apiBase = apiBase.substring(0, apiBase.length() - 1);
        String urlStr = apiBase + "/chat/completions";

        // 构建请求体
        List<Map<String, String>> messages = new ArrayList<>();
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            messages.add(Map.of("role", "system", "content", systemPrompt));
        }
        messages.add(Map.of("role", "user", "content", userMessage));

        Map<String, Object> body = new HashMap<>();
        body.put("model", model.getModelId());
        body.put("messages", messages);
        body.put("max_tokens", model.getMaxTokens() != null ? model.getMaxTokens() : 2048);
        body.put("temperature", model.getTemperature() != null ? model.getTemperature() : 0.7);

        String jsonBody = mapper.writeValueAsString(body);
        logger.info("Calling LLM: {} with model {}", urlStr, model.getModelId());

        // 发送请求
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(60000);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

        // Claude需要特殊处理
        if ("claude".equals(model.getProvider())) {
            conn.setRequestProperty("x-api-key", model.getApiKey());
            conn.setRequestProperty("anthropic-version", "2023-06-01");
        } else {
            conn.setRequestProperty("Authorization", "Bearer " + model.getApiKey());
        }

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
        }

        int status = conn.getResponseCode();
        java.io.InputStream is = status >= 400 ? conn.getErrorStream() : conn.getInputStream();
        String responseBody = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        logger.info("LLM response status: {}, body length: {}", status, responseBody.length());

        if (status >= 400) {
            throw new RuntimeException("LLM API error (HTTP " + status + "): " + responseBody.substring(0, Math.min(200, responseBody.length())));
        }

        // 解析响应
        JsonNode node = mapper.readTree(responseBody);
        Map<String, Object> result = new HashMap<>();

        if (node.has("choices") && node.get("choices").size() > 0) {
            JsonNode choice = node.get("choices").get(0);
            if (choice.has("message") && choice.get("message").has("content")) {
                result.put("content", choice.get("message").get("content").asText());
            }
        }

        // Token统计
        if (node.has("usage")) {
            JsonNode usage = node.get("usage");
            int totalTokens = usage.has("total_tokens") ? usage.get("total_tokens").asInt() : 0;
            result.put("tokensUsed", totalTokens);
        } else {
            result.put("tokensUsed", estimateTokens(userMessage) + estimateTokens((String) result.getOrDefault("content", "")));
        }

        return result;
    }

    /**
     * 模拟LLM回复
     */
    private String buildSimulatedResponse(String agentName, String modelName, String message, String systemPrompt, String mcpContext) {
        StringBuilder sb = new StringBuilder();
        sb.append("**[").append(agentName).append("]** 使用模型 `").append(modelName).append("` 回复：\n\n");

        // 如果有MCP上下文，显示数据库信息
        if (mcpContext != null && !mcpContext.isEmpty()) {
            sb.append("📊 **数据库信息**:\n");
            sb.append(mcpContext.replace("\n", "\n> ")).append("\n\n");
        }

        // 简单的关键词匹配生成回复
        String lower = message.toLowerCase();
        if (lower.contains("你好") || lower.contains("hello") || lower.contains("hi")) {
            sb.append("你好！我是").append(agentName).append("，很高兴为您服务。请问有什么可以帮助您的？");
        } else if (lower.contains("代码") || lower.contains("code") || lower.contains("编程")) {
            sb.append("关于代码方面的问题，我可以帮您：\n");
            sb.append("- 代码审查和重构建议\n");
            sb.append("- Bug定位和修复方案\n");
            sb.append("- 架构设计和最佳实践\n\n");
            sb.append("请提供具体的代码或描述您的需求。");
        } else if (lower.contains("文档") || lower.contains("document")) {
            sb.append("我可以帮您撰写各类文档：\n");
            sb.append("- 技术文档和API文档\n");
            sb.append("- 需求文档和设计文档\n");
            sb.append("- 会议纪要和工作总结\n\n");
            sb.append("请告诉我您需要什么类型的文档。");
        } else {
            sb.append("收到您的消息：\n> ").append(message).append("\n\n");
            sb.append("这是一个模拟回复。要获得真实的LLM回复，请在**模型管理**中配置API Key。");
        }

        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            sb.append("\n\n---\n*已加载系统提示词 (").append(systemPrompt.length()).append(" 字符)*");
        }

        return sb.toString();
    }

    /**
     * 收集MCP上下文信息
     */
    private String gatherMcpContext(List<Long> mcpIds, String message) {
        StringBuilder context = new StringBuilder();
        List<McpServer> mcps = mcpRepo.findAllById(mcpIds);
        logger.info("Gathering MCP context for {} servers", mcps.size());

        for (McpServer mcp : mcps) {
            if (!"streamable_http".equals(mcp.getTransport()) || mcp.getServerUrl() == null) {
                continue;
            }

            try {
                // 1. 获取数据库列表
                logger.info("Calling MCP tool db_list_databases on {}", mcp.getServerUrl());
                Map<String, Object> dbResult = callMcpTool(mcp.getServerUrl(), "db_list_databases", Map.of());
                logger.info("db_list_databases result: {}", dbResult);
                if (dbResult.containsKey("text")) {
                    context.append("\n可用数据库:\n").append(dbResult.get("text"));
                }

                // 2. 获取表列表
                logger.info("Calling MCP tool db_list_tables on {}", mcp.getServerUrl());
                Map<String, Object> tablesResult = callMcpTool(mcp.getServerUrl(), "db_list_tables", Map.of());
                logger.info("db_list_tables result: {}", tablesResult);
                if (tablesResult.containsKey("text")) {
                    context.append("\n数据库表:\n").append(tablesResult.get("text"));
                }

                // 3. 如果用户问的是具体表的结构，获取表结构
                if (message.contains("结构") || message.contains("schema") || message.contains("字段")) {
                    // 尝试从消息中提取表名
                    String tableName = extractTableName(message, tablesResult);
                    if (tableName != null) {
                        Map<String, Object> descResult = callMcpTool(mcp.getServerUrl(), "db_describe_table", Map.of("table", tableName));
                        if (descResult.containsKey("text")) {
                            context.append("\n表 ").append(tableName).append(" 结构:\n").append(descResult.get("text"));
                        }
                    }
                }

                // 4. 如果用户问的是数据查询，执行SQL
                if (message.contains("查询") || message.contains("数据") || message.contains("select") || message.contains("SELECT")) {
                    String sql = extractSqlFromMessage(message);
                    if (sql != null) {
                        Map<String, Object> queryResult = callMcpTool(mcp.getServerUrl(), "db_query", Map.of("sql", sql));
                        if (queryResult.containsKey("text")) {
                            context.append("\n查询结果:\n").append(queryResult.get("text"));
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("MCP tool call failed for {}", mcp.getName(), e);
            }
        }

        return context.toString();
    }

    /**
     * 调用MCP工具（带超时和SSE响应解析）
     */
    private Map<String, Object> callMcpTool(String serverUrl, String toolName, Map<String, Object> args) throws Exception {
        logger.info("Calling MCP tool {} on {}", toolName, serverUrl);
        URL url = new URL(serverUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setConnectTimeout(3000);
        conn.setReadTimeout(5000);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json, text/event-stream");

        // 构建MCP工具调用请求
        Map<String, Object> request = new HashMap<>();
        request.put("jsonrpc", "2.0");
        request.put("id", System.currentTimeMillis());
        request.put("method", "tools/call");
        Map<String, Object> params = new HashMap<>();
        params.put("name", toolName);
        params.put("arguments", args);
        request.put("params", params);

        String jsonBody = mapper.writeValueAsString(request);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
        }

        int status = conn.getResponseCode();
        if (status >= 200 && status < 300) {
            // 使用BufferedReader逐行读取，避免readAllBytes()因连接不关闭而阻塞
            StringBuilder respBuilder = new StringBuilder();
            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                int emptyLineCount = 0;
                while ((line = reader.readLine()) != null) {
                    respBuilder.append(line).append("\n");
                    if (line.trim().isEmpty()) {
                        emptyLineCount++;
                        if (emptyLineCount >= 2) break; // SSE结束标志
                    } else {
                        emptyLineCount = 0;
                    }
                }
            }
            String resp = respBuilder.toString();
            // 解析SSE响应
            for (String line : resp.split("\n")) {
                if (line.startsWith("data:")) {
                    String json = line.substring(5).trim();
                    JsonNode node = mapper.readTree(json);
                    if (node.has("result") && node.get("result").has("content")) {
                        JsonNode content = node.get("result").get("content");
                        if (content.isArray() && content.size() > 0) {
                            String text = content.get(0).get("text").asText();
                            // 尝试解析为JSON
                            try {
                                JsonNode resultNode = mapper.readTree(text);
                                Map<String, Object> result = new HashMap<>();
                                resultNode.fields().forEachRemaining(e -> result.put(e.getKey(), e.getValue().toString()));
                                return result;
                            } catch (Exception e) {
                                return Map.of("text", text);
                            }
                        }
                    } else if (node.has("error")) {
                        logger.warn("MCP tool {} error: {}", toolName, node.get("error"));
                    }
                    break;
                }
            }
        } else {
            logger.warn("MCP HTTP error: status={}", status);
        }
        return Map.of();
    }

    /**
     * 从消息中提取表名
     */
    private String extractTableName(String message, Map<String, Object> tablesResult) {
        // 简单的表名提取逻辑
        String[] keywords = {"表", "table", "Table"};
        for (String keyword : keywords) {
            int idx = message.indexOf(keyword);
            if (idx >= 0) {
                // 提取关键词后面的内容
                String after = message.substring(idx + keyword.length()).trim();
                // 取第一个空格或标点之前的内容
                int end = after.indexOf(' ');
                if (end < 0) end = after.indexOf('，');
                if (end < 0) end = after.indexOf('。');
                if (end < 0) end = after.length();
                if (end > 0) {
                    return after.substring(0, end).trim();
                }
            }
        }
        return null;
    }

    /**
     * 从消息中提取SQL
     */
    private String extractSqlFromMessage(String message) {
        // 尝试提取SQL语句
        String lower = message.toLowerCase();
        int selectIdx = lower.indexOf("select");
        if (selectIdx >= 0) {
            // 提取SELECT语句
            String sql = message.substring(selectIdx);
            // 去除末尾的标点
            sql = sql.replaceAll("[。？?！!]$", "").trim();
            return sql;
        }
        return null;
    }

    /**
     * 估算token数
     */
    private int estimateTokens(String text) {
        if (text == null || text.isEmpty()) return 0;
        // 粗略估算：中文1字≈2token，英文1词≈1.3token
        int chineseChars = 0;
        int englishWords = 0;
        for (char c : text.toCharArray()) {
            if (Character.toString(c).matches("[\\u4e00-\\u9fa5]")) {
                chineseChars++;
            }
        }
        englishWords = text.split("\\s+").length;
        return (int) (chineseChars * 1.5 + englishWords * 1.3) + 10;
    }

    /**
     * 测试模型连接是否可用
     */
    public Map<String, Object> testModelConnection(ModelConfig model) {
        Map<String, Object> result = new HashMap<>();
        long startTime = System.currentTimeMillis();

        try {
            String apiBase = model.getApiBase();
            if (apiBase == null || apiBase.isEmpty()) {
                result.put("success", false);
                result.put("message", "API地址未配置");
                return result;
            }
            if (model.getApiKey() == null || model.getApiKey().isEmpty()) {
                result.put("success", false);
                result.put("message", "API Key未配置");
                return result;
            }

            if (apiBase.endsWith("/")) apiBase = apiBase.substring(0, apiBase.length() - 1);
            String urlStr = apiBase + "/chat/completions";

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "user", "content", "Say 'OK' in one word."));

            Map<String, Object> body = new HashMap<>();
            body.put("model", model.getModelId());
            body.put("messages", messages);
            body.put("max_tokens", 10);
            body.put("temperature", 0.1);

            String jsonBody = mapper.writeValueAsString(body);

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(15000);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

            if ("claude".equals(model.getProvider())) {
                conn.setRequestProperty("x-api-key", model.getApiKey());
                conn.setRequestProperty("anthropic-version", "2023-06-01");
            } else {
                conn.setRequestProperty("Authorization", "Bearer " + model.getApiKey());
            }

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            }

            long latency = System.currentTimeMillis() - startTime;
            int status = conn.getResponseCode();
            InputStream is = status >= 400 ? conn.getErrorStream() : conn.getInputStream();
            String responseBody = new String(is.readAllBytes(), StandardCharsets.UTF_8);

            result.put("latency", latency);

            if (status >= 200 && status < 300) {
                result.put("success", true);
                result.put("statusCode", status);
                // 尝试解析回复内容
                try {
                    JsonNode node = mapper.readTree(responseBody);
                    if (node.has("choices") && node.get("choices").size() > 0) {
                        String reply = node.get("choices").get(0).get("message").get("content").asText();
                        result.put("reply", reply.trim());
                        result.put("message", "连接成功");
                    }
                } catch (Exception e) {
                    result.put("message", "连接成功 (响应格式异常)");
                }
            } else {
                result.put("success", false);
                result.put("statusCode", status);
                String errMsg = responseBody.length() > 200 ? responseBody.substring(0, 200) + "..." : responseBody;
                result.put("error", errMsg);
                if (status == 401) {
                    result.put("message", "认证失败，请检查API Key");
                } else if (status == 404) {
                    result.put("message", "接口不存在，请检查API地址");
                } else if (status == 429) {
                    result.put("message", "请求频率过高，请稍后重试");
                } else {
                    result.put("message", "请求失败 (HTTP " + status + ")");
                }
            }
        } catch (java.net.ConnectException | java.net.SocketTimeoutException e) {
            result.put("success", false);
            result.put("latency", System.currentTimeMillis() - startTime);
            result.put("message", "连接超时，请检查API地址和网络");
        } catch (java.net.UnknownHostException e) {
            result.put("success", false);
            result.put("latency", System.currentTimeMillis() - startTime);
            result.put("message", "无法解析域名，请检查API地址");
        } catch (Exception e) {
            result.put("success", false);
            result.put("latency", System.currentTimeMillis() - startTime);
            result.put("message", "测试失败: " + e.getMessage());
        }

        return result;
    }
}
