package com.aiplatform.eclaw.service;

import com.aiplatform.eclaw.entity.*;
import com.aiplatform.eclaw.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class EclawService {

    @Autowired private ModelConfigRepository modelRepo;
    @Autowired private McpServerRepository mcpRepo;
    @Autowired private SkillRepository skillRepo;
    @Autowired private AgentRepository agentRepo;
    @Autowired private WorkflowRepository workflowRepo;
    @Autowired private SessionRepository sessionRepo;
    @Autowired private EclawLogRepository logRepo;

    // === Models ===
    public List<ModelConfig> getModels() { return modelRepo.findAll(); }
    public List<ModelConfig> getEnabledModels() { return modelRepo.findByIsEnabledTrue(); }
    public ModelConfig saveModel(ModelConfig m) { return modelRepo.save(m); }
    public void deleteModel(Long id) { modelRepo.deleteById(id); }

    // === MCP Servers ===
    public List<McpServer> getMcpServers() { return mcpRepo.findAll(); }
    public McpServer getMcpServer(Long id) { return mcpRepo.findById(id).orElse(null); }
    public McpServer saveMcpServer(McpServer m) { return mcpRepo.save(m); }
    public void deleteMcpServer(Long id) { mcpRepo.deleteById(id); }

    // === Skills ===
    public List<Skill> getSkills() { return skillRepo.findAll(); }
    public Skill saveSkill(Skill s) { return skillRepo.save(s); }
    public void deleteSkill(Long id) { skillRepo.deleteById(id); }

    // === Agents ===
    public List<Agent> getAgents(Long userId) {
        return userId != null ? agentRepo.findByCreatorIdOrderByCreatedAtDesc(userId)
                              : agentRepo.findAll();
    }
    public Agent getAgent(Long id) { return agentRepo.findById(id).orElse(null); }
    public Agent saveAgent(Agent a) { return agentRepo.save(a); }
    public Agent updateAgentStatus(Long id, String status) {
        Agent a = agentRepo.findById(id).orElse(null);
        if (a != null) { a.setStatus(status); return agentRepo.save(a); }
        return null;
    }
    public void deleteAgent(Long id) { agentRepo.deleteById(id); }

    // === Workflows ===
    public List<Workflow> getWorkflows(Long userId) {
        return userId != null ? workflowRepo.findByCreatorIdOrderByCreatedAtDesc(userId)
                              : workflowRepo.findAll();
    }
    public Workflow getWorkflow(Long id) { return workflowRepo.findById(id).orElse(null); }
    public Workflow saveWorkflow(Workflow w) { return workflowRepo.save(w); }
    public void deleteWorkflow(Long id) { workflowRepo.deleteById(id); }

    // === Sessions ===
    public List<Session> getSessions(Long agentId) {
        return agentId != null ? sessionRepo.findByAgentIdOrderByCreatedAtDesc(agentId)
                               : sessionRepo.findAll();
    }
    public Session getSession(Long id) { return sessionRepo.findById(id).orElse(null); }
    public Session createSession(Long agentId, Long userId, String userName) {
        Session s = new Session();
        s.setAgentId(agentId);
        s.setUserId(userId);
        s.setUserName(userName);
        s.setTitle("新对话");
        s.setMessages("[]");
        Agent agent = agentId != null ? agentRepo.findById(agentId).orElse(null) : null;
        s.setSessionMode(agent != null && agent.getDefaultChatMode() != null ? agent.getDefaultChatMode() : "standard");
        s.setPendingActionStatus("none");
        return sessionRepo.save(s);
    }
    public Session updateSession(Long id, String title, String messages, Integer tokens,
                                 String sessionMode, String pendingAction, String pendingActionStatus) {
        Session s = sessionRepo.findById(id).orElse(null);
        if (s != null) {
            if (title != null) s.setTitle(title);
            if (messages != null) s.setMessages(messages);
            if (tokens != null) s.setTotalTokens(tokens);
            if (sessionMode != null) s.setSessionMode(sessionMode);
            if (pendingAction != null) s.setPendingAction(pendingAction);
            if (pendingActionStatus != null) s.setPendingActionStatus(pendingActionStatus);
            return sessionRepo.save(s);
        }
        return null;
    }
    public void deleteSession(Long id) { sessionRepo.deleteById(id); }

    // === Logs ===
    public List<EclawLog> getLogs(Long agentId, Long workflowId) {
        if (agentId != null) return logRepo.findByAgentIdOrderByCreatedAtDesc(agentId);
        if (workflowId != null) return logRepo.findByWorkflowIdOrderByCreatedAtDesc(workflowId);
        return logRepo.findByAgentIdOrderByCreatedAtDesc(null); // all
    }
    public EclawLog saveLog(EclawLog l) { return logRepo.save(l); }
    public long getErrorCount() { return logRepo.countByStatus("error"); }

    // === Dashboard ===
    public Map<String, Object> getDashboard() {
        Map<String, Object> d = new HashMap<>();
        d.put("agentCount", agentRepo.count());
        d.put("activeAgents", agentRepo.findByStatus("running").size());
        d.put("workflowCount", workflowRepo.count());
        d.put("sessionCount", sessionRepo.count());
        d.put("skillCount", skillRepo.count());
        d.put("mcpCount", mcpRepo.count());
        d.put("errorLogCount", getErrorCount());
        return d;
    }
}
