package com.aiplatform.eclaw.controller;

import com.aiplatform.common.dto.ApiResponse;
import com.aiplatform.eclaw.dto.ChatResponsePayload;
import com.aiplatform.eclaw.entity.*;
import com.aiplatform.eclaw.service.ChatService;
import com.aiplatform.eclaw.service.EclawService;
import com.aiplatform.eclaw.service.PendingActionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/eclaw")
public class EclawController {

    @Autowired
    private EclawService service;

    @Autowired
    private ChatService chatService;

    @Autowired
    private PendingActionService pendingActionService;

    @Autowired
    private com.aiplatform.eclaw.repository.ModelConfigRepository modelRepo;

    // === Dashboard ===
    @GetMapping("/dashboard")
    public ApiResponse<Map<String, Object>> dashboard() {
        return ApiResponse.success(service.getDashboard());
    }

    // === Models ===
    @GetMapping("/models")
    public ApiResponse<List<ModelConfig>> getModels() {
        return ApiResponse.success(service.getModels());
    }

    @GetMapping("/models/enabled")
    public ApiResponse<List<ModelConfig>> getEnabledModels() {
        return ApiResponse.success(service.getEnabledModels());
    }

    @PostMapping("/models")
    public ApiResponse<ModelConfig> createModel(@RequestBody ModelConfig model) {
        return ApiResponse.success(service.saveModel(model));
    }

    @PutMapping("/models/{id}")
    public ApiResponse<ModelConfig> updateModel(@PathVariable Long id, @RequestBody ModelConfig model) {
        model.setId(id);
        return ApiResponse.success(service.saveModel(model));
    }

    @DeleteMapping("/models/{id}")
    public ApiResponse<Void> deleteModel(@PathVariable Long id) {
        service.deleteModel(id);
        return ApiResponse.success(null);
    }

    // === Model Config Test ===
    @PostMapping("/models/{id}/test")
    public ApiResponse<Map<String, Object>> testModel(@PathVariable Long id) {
        ModelConfig model = modelRepo.findById(id).orElse(null);
        if (model == null) return ApiResponse.error(404, "模型不存在");
        return ApiResponse.success(chatService.testModelConnection(model));
    }

    // === MCP Servers ===
    @GetMapping("/mcp-servers")
    public ApiResponse<List<McpServer>> getMcpServers() {
        return ApiResponse.success(service.getMcpServers());
    }

    @PostMapping("/mcp-servers")
    public ApiResponse<McpServer> createMcpServer(@RequestBody McpServer mcp) {
        return ApiResponse.success(service.saveMcpServer(mcp));
    }

    @PutMapping("/mcp-servers/{id}")
    public ApiResponse<McpServer> updateMcpServer(@PathVariable Long id, @RequestBody McpServer mcp) {
        mcp.setId(id);
        return ApiResponse.success(service.saveMcpServer(mcp));
    }

    @DeleteMapping("/mcp-servers/{id}")
    public ApiResponse<Void> deleteMcpServer(@PathVariable Long id) {
        service.deleteMcpServer(id);
        return ApiResponse.success(null);
    }

    /**
     * 测试MCP服务连接
     */
    @PostMapping("/mcp-servers/{id}/test")
    public ApiResponse<Map<String, Object>> testMcpServer(@PathVariable Long id) {
        McpServer mcp = service.getMcpServer(id);
        if (mcp == null) return ApiResponse.error(404, "MCP服务不存在");

        Map<String, Object> result = new java.util.HashMap<>();
        try {
            if ("streamable_http".equals(mcp.getTransport()) && mcp.getServerUrl() != null) {
                String url = mcp.getServerUrl();

                // 先尝试initialize，获取session和工具列表
                Map<String, Object> initResult = mcpInitialize(url);
                if ((Boolean) initResult.get("success")) {
                    result.put("success", true);
                    result.put("message", "连接成功");
                    result.put("tools", initResult.get("tools"));
                } else {
                    // initialize失败，可能已初始化，尝试直接获取工具列表
                    Map<String, Object> listResult = mcpToolsList(url);
                    if ((Boolean) listResult.get("success")) {
                        result.put("success", true);
                        result.put("message", "连接成功");
                        result.put("tools", listResult.get("tools"));
                    } else {
                        result.putAll(initResult); // 返回initialize的错误
                    }
                }
            } else {
                // stdio类型: 只能检查配置是否完整
                if (mcp.getCommand() == null || mcp.getCommand().isEmpty()) {
                    result.put("success", false);
                    result.put("message", "启动命令未配置");
                } else {
                    result.put("success", true);
                    result.put("message", "配置已保存 (stdio类型需运行时测试)");
                }
            }
        } catch (java.net.ConnectException e) {
            result.put("success", false);
            result.put("message", "连接被拒绝，请检查服务是否启动");
        } catch (java.net.SocketTimeoutException e) {
            result.put("success", false);
            result.put("message", "连接超时");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "测试失败: " + e.getMessage());
        }
        return ApiResponse.success(result);
    }

    /**
     * MCP initialize + tools/list
     */
    private Map<String, Object> mcpInitialize(String url) throws Exception {
        Map<String, Object> result = new java.util.HashMap<>();
        java.net.URL u = new java.net.URL(url);
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) u.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(10000);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json, text/event-stream");

        String body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"initialize\",\"params\":{\"protocolVersion\":\"2025-03-26\",\"capabilities\":{},\"clientInfo\":{\"name\":\"eclaw-test\",\"version\":\"1.0\"}}}";
        try (java.io.OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }

        int status = conn.getResponseCode();
        if (status >= 200 && status < 300) {
            String resp = new String(conn.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            String jsonData = extractSseData(resp);
            if (!jsonData.isEmpty()) {
                com.fasterxml.jackson.databind.JsonNode node = new com.fasterxml.jackson.databind.ObjectMapper().readTree(jsonData);
                if (node.has("result")) {
                    result.put("success", true);
                    String sessionId = conn.getHeaderField("Mcp-Session-Id");
                    if (sessionId != null) {
                        result.put("tools", listMcpTools(url, sessionId));
                    }
                    return result;
                } else if (node.has("error")) {
                    result.put("success", false);
                    result.put("message", node.get("error").get("message").asText());
                    return result;
                }
            }
            result.put("success", true);
            result.put("tools", new java.util.ArrayList<>());
        } else {
            result.put("success", false);
            result.put("message", "连接失败 (HTTP " + status + ")");
        }
        return result;
    }

    /**
     * 直接获取MCP工具列表（不initialize）
     */
    private Map<String, Object> mcpToolsList(String url) throws Exception {
        Map<String, Object> result = new java.util.HashMap<>();
        java.net.URL u = new java.net.URL(url);
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) u.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(10000);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json, text/event-stream");

        String body = "{\"jsonrpc\":\"2.0\",\"id\":2,\"method\":\"tools/list\",\"params\":{}}";
        try (java.io.OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }

        int status = conn.getResponseCode();
        if (status >= 200 && status < 300) {
            String resp = new String(conn.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            String jsonData = extractSseData(resp);
            if (!jsonData.isEmpty()) {
                com.fasterxml.jackson.databind.JsonNode node = new com.fasterxml.jackson.databind.ObjectMapper().readTree(jsonData);
                if (node.has("result") && node.get("result").has("tools")) {
                    result.put("success", true);
                    List<Map<String, String>> tools = new java.util.ArrayList<>();
                    for (com.fasterxml.jackson.databind.JsonNode t : node.get("result").get("tools")) {
                        Map<String, String> tool = new java.util.HashMap<>();
                        tool.put("name", t.get("name").asText());
                        tool.put("description", t.has("description") ? t.get("description").asText() : "");
                        tools.add(tool);
                    }
                    result.put("tools", tools);
                    return result;
                }
            }
            result.put("success", false);
            result.put("message", "无法获取工具列表");
        } else {
            result.put("success", false);
            result.put("message", "请求失败 (HTTP " + status + ")");
        }
        return result;
    }

    /**
     * 从SSE响应中提取data内容
     */
    private String extractSseData(String resp) {
        for (String line : resp.split("\n")) {
            if (line.startsWith("data:")) {
                return line.substring(5).trim();
            }
        }
        return "";
    }

    /**
     * 获取MCP服务的工具列表
     */
    private List<Map<String, String>> listMcpTools(String url, String sessionId) throws Exception {
        List<Map<String, String>> tools = new java.util.ArrayList<>();
        java.net.URL u = new java.net.URL(url);
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) u.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(10000);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json, text/event-stream");
        conn.setRequestProperty("Mcp-Session-Id", sessionId);

        String body = "{\"jsonrpc\":\"2.0\",\"id\":2,\"method\":\"tools/list\",\"params\":{}}";
        try (java.io.OutputStream os = conn.getOutputStream()) {
            os.write(body.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }

        int status = conn.getResponseCode();
        if (status >= 200 && status < 300) {
            String resp = new String(conn.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            for (String line : resp.split("\n")) {
                if (line.startsWith("data:")) {
                    String json = line.substring(5).trim();
                    com.fasterxml.jackson.databind.JsonNode node = new com.fasterxml.jackson.databind.ObjectMapper().readTree(json);
                    if (node.has("result") && node.get("result").has("tools")) {
                        for (com.fasterxml.jackson.databind.JsonNode t : node.get("result").get("tools")) {
                            Map<String, String> tool = new java.util.HashMap<>();
                            tool.put("name", t.get("name").asText());
                            tool.put("description", t.has("description") ? t.get("description").asText() : "");
                            tools.add(tool);
                        }
                    }
                    break;
                }
            }
        }
        return tools;
    }

    // === Skills ===
    @GetMapping("/skills")
    public ApiResponse<List<Skill>> getSkills() {
        return ApiResponse.success(service.getSkills());
    }

    @PostMapping("/skills")
    public ApiResponse<Skill> createSkill(@RequestBody Skill skill) {
        return ApiResponse.success(service.saveSkill(skill));
    }

    @PutMapping("/skills/{id}")
    public ApiResponse<Skill> updateSkill(@PathVariable Long id, @RequestBody Skill skill) {
        skill.setId(id);
        return ApiResponse.success(service.saveSkill(skill));
    }

    @DeleteMapping("/skills/{id}")
    public ApiResponse<Void> deleteSkill(@PathVariable Long id) {
        service.deleteSkill(id);
        return ApiResponse.success(null);
    }

    // === Agents ===
    @GetMapping("/agents")
    public ApiResponse<List<Agent>> getAgents(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return ApiResponse.success(service.getAgents(userId));
    }

    @GetMapping("/agents/{id}")
    public ApiResponse<Agent> getAgent(@PathVariable Long id) {
        Agent a = service.getAgent(id);
        return a != null ? ApiResponse.success(a) : ApiResponse.error(404, "Agent不存在");
    }

    @PostMapping("/agents")
    public ApiResponse<Agent> createAgent(@RequestBody Agent agent,
                                           @RequestHeader(value = "X-User-Id", required = false) Long userId,
                                           @RequestHeader(value = "X-User-Name", required = false) String userName) {
        agent.setCreatorId(userId);
        agent.setCreatorName(userName);
        return ApiResponse.success(service.saveAgent(agent));
    }

    @PutMapping("/agents/{id}")
    public ApiResponse<Agent> updateAgent(@PathVariable Long id, @RequestBody Agent agent) {
        agent.setId(id);
        return ApiResponse.success(service.saveAgent(agent));
    }

    @PutMapping("/agents/{id}/status")
    public ApiResponse<Agent> updateAgentStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Agent a = service.updateAgentStatus(id, body.get("status"));
        return a != null ? ApiResponse.success(a) : ApiResponse.error(404, "Agent不存在");
    }

    @DeleteMapping("/agents/{id}")
    public ApiResponse<Void> deleteAgent(@PathVariable Long id) {
        service.deleteAgent(id);
        return ApiResponse.success(null);
    }

    // === Workflows ===
    @GetMapping("/workflows")
    public ApiResponse<List<Workflow>> getWorkflows(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return ApiResponse.success(service.getWorkflows(userId));
    }

    @GetMapping("/workflows/{id}")
    public ApiResponse<Workflow> getWorkflow(@PathVariable Long id) {
        Workflow w = service.getWorkflow(id);
        return w != null ? ApiResponse.success(w) : ApiResponse.error(404, "编排不存在");
    }

    @PostMapping("/workflows")
    public ApiResponse<Workflow> createWorkflow(@RequestBody Workflow workflow,
                                                  @RequestHeader(value = "X-User-Id", required = false) Long userId,
                                                  @RequestHeader(value = "X-User-Name", required = false) String userName) {
        workflow.setCreatorId(userId);
        workflow.setCreatorName(userName);
        return ApiResponse.success(service.saveWorkflow(workflow));
    }

    @PutMapping("/workflows/{id}")
    public ApiResponse<Workflow> updateWorkflow(@PathVariable Long id, @RequestBody Workflow workflow) {
        workflow.setId(id);
        return ApiResponse.success(service.saveWorkflow(workflow));
    }

    @DeleteMapping("/workflows/{id}")
    public ApiResponse<Void> deleteWorkflow(@PathVariable Long id) {
        service.deleteWorkflow(id);
        return ApiResponse.success(null);
    }

    // === Sessions ===
    @GetMapping("/sessions")
    public ApiResponse<List<Session>> getSessions(@RequestParam(required = false) Long agentId) {
        return ApiResponse.success(service.getSessions(agentId));
    }

    @PostMapping("/sessions")
    public ApiResponse<Session> createSession(@RequestBody Map<String, Object> body,
                                               @RequestHeader(value = "X-User-Id", required = false) Long userId,
                                               @RequestHeader(value = "X-User-Name", required = false) String userName) {
        Long agentId = Long.valueOf(body.get("agentId").toString());
        return ApiResponse.success(service.createSession(agentId, userId, userName));
    }

    @PutMapping("/sessions/{id}")
    public ApiResponse<Session> updateSession(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String title = body.containsKey("title") ? body.get("title").toString() : null;
        String messages = body.containsKey("messages") ? body.get("messages").toString() : null;
        Integer tokens = body.containsKey("totalTokens") ? Integer.valueOf(body.get("totalTokens").toString()) : null;
        String sessionMode = body.containsKey("sessionMode") && body.get("sessionMode") != null ? body.get("sessionMode").toString() : null;
        String pendingAction = body.containsKey("pendingAction") && body.get("pendingAction") != null ? body.get("pendingAction").toString() : null;
        String pendingActionStatus = body.containsKey("pendingActionStatus") && body.get("pendingActionStatus") != null ? body.get("pendingActionStatus").toString() : null;
        return ApiResponse.success(service.updateSession(id, title, messages, tokens, sessionMode, pendingAction, pendingActionStatus));
    }

    @DeleteMapping("/sessions/{id}")
    public ApiResponse<Void> deleteSession(@PathVariable Long id) {
        service.deleteSession(id);
        return ApiResponse.success(null);
    }

    // === Logs ===
    @GetMapping("/logs")
    public ApiResponse<List<EclawLog>> getLogs(@RequestParam(required = false) Long agentId,
                                                @RequestParam(required = false) Long workflowId) {
        return ApiResponse.success(service.getLogs(agentId, workflowId));
    }

    // === Chat (supports real LLM via model config) ===
    @PostMapping("/chat")
    public ApiResponse<Map<String, Object>> chat(@RequestBody Map<String, Object> body) {
        Long agentId = body.containsKey("agentId") ? Long.valueOf(body.get("agentId").toString()) : null;
        Long sessionId = body.containsKey("sessionId") ? Long.valueOf(body.get("sessionId").toString()) : null;
        Long modelId = body.containsKey("modelId") && body.get("modelId") != null ? Long.valueOf(body.get("modelId").toString()) : null;
        String message = body.get("message").toString();

        List<Long> skillIds = null;
        if (body.containsKey("skillIds") && body.get("skillIds") != null) {
            skillIds = new java.util.ArrayList<>();
            for (Object id : (List<?>) body.get("skillIds")) {
                skillIds.add(Long.valueOf(id.toString()));
            }
        }

        List<Long> mcpIds = null;
        if (body.containsKey("mcpIds") && body.get("mcpIds") != null) {
            mcpIds = new java.util.ArrayList<>();
            for (Object id : (List<?>) body.get("mcpIds")) {
                mcpIds.add(Long.valueOf(id.toString()));
            }
        }

        Agent agent = agentId != null ? service.getAgent(agentId) : null;
        if (agent == null) {
            agent = new Agent();
            agent.setName("助手");
        }

        Session session = sessionId != null ? service.getSession(sessionId) : null;
        if (session == null) {
            return ApiResponse.error(404, "会话不存在");
        }

        ChatResponsePayload response = chatService.chat(agent, session, modelId, message, skillIds, mcpIds);
        response.setSessionId(session.getId());
        response.setSessionMode(session.getSessionMode());

        if ("approval_required".equals(response.getResponseType()) && response.getPendingAction() != null) {
            Session updated = pendingActionService.storeWaiting(session, response.getPendingAction());
            response.setSessionMode(updated.getSessionMode());
        }

        return ApiResponse.success(response.toMap());
    }

    @PostMapping("/sessions/{id}/pending-actions/{actionId}/confirm")
    public ApiResponse<Map<String, Object>> confirmPendingAction(@PathVariable Long id, @PathVariable String actionId) {
        return ApiResponse.success(pendingActionService.confirmAndExecute(id, actionId));
    }

    @PostMapping("/sessions/{id}/pending-actions/{actionId}/cancel")
    public ApiResponse<Map<String, Object>> cancelPendingAction(@PathVariable Long id, @PathVariable String actionId) {
        return ApiResponse.success(pendingActionService.cancelAsMessage(id, actionId));
    }
}
