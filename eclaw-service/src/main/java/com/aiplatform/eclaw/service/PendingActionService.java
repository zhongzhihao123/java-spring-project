package com.aiplatform.eclaw.service;

import com.aiplatform.eclaw.dto.PendingActionPayload;
import com.aiplatform.eclaw.entity.Session;
import com.aiplatform.eclaw.repository.SessionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class PendingActionService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final SessionRepository sessionRepo;
    private final ActionExecutionService actionExecutionService;

    public PendingActionService(SessionRepository sessionRepo, ActionExecutionService actionExecutionService) {
        this.sessionRepo = sessionRepo;
        this.actionExecutionService = actionExecutionService;
    }

    public Session storeWaiting(Session session, PendingActionPayload payload) {
        if ("waiting".equals(session.getPendingActionStatus())) {
            throw new IllegalStateException("当前会话已有待确认动作");
        }
        try {
            session.setPendingAction(MAPPER.writeValueAsString(payload));
        } catch (Exception e) {
            throw new IllegalStateException("待确认动作保存失败", e);
        }
        session.setPendingActionStatus("waiting");
        return sessionRepo.save(session);
    }

    public Map<String, Object> confirmAndExecute(Long sessionId, String actionId) {
        Session session = loadWaiting(sessionId, actionId);
        PendingActionPayload payload = readPendingAction(session);
        session.setPendingActionStatus("approved");
        sessionRepo.save(session);

        try {
            Map<String, Object> result = actionExecutionService.execute(payload);
            session.setPendingAction(null);
            session.setPendingActionStatus("executed");
            sessionRepo.save(session);
            return result;
        } catch (RuntimeException e) {
            session.setPendingActionStatus("failed");
            sessionRepo.save(session);
            throw e;
        }
    }

    public Map<String, Object> cancelAsMessage(Long sessionId, String actionId) {
        Session session = loadWaiting(sessionId, actionId);
        session.setPendingActionStatus("cancelled");
        session.setPendingAction(null);
        sessionRepo.save(session);

        Map<String, Object> result = new HashMap<>();
        result.put("status", "cancelled");
        result.put("content", "已取消待确认动作。");
        return result;
    }

    private Session loadWaiting(Long sessionId, String actionId) {
        Session session = sessionRepo.findById(sessionId).orElseThrow(() -> new IllegalStateException("会话不存在"));
        if (!"waiting".equals(session.getPendingActionStatus())) {
            throw new IllegalStateException("当前会话没有可确认的动作");
        }
        PendingActionPayload payload = readPendingAction(session);
        if (payload.getId() == null || !payload.getId().equals(actionId)) {
            throw new IllegalStateException("待确认动作不匹配");
        }
        return session;
    }

    private PendingActionPayload readPendingAction(Session session) {
        try {
            return MAPPER.readValue(session.getPendingAction(), PendingActionPayload.class);
        } catch (Exception e) {
            throw new IllegalStateException("待确认动作解析失败", e);
        }
    }
}
