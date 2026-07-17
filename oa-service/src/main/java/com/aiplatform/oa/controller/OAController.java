package com.aiplatform.oa.controller;

import com.aiplatform.common.dto.ApiResponse;
import com.aiplatform.oa.entity.*;
import com.aiplatform.oa.service.OAService;
import com.aiplatform.oa.service.WechatNotifyService;
import com.aiplatform.oa.service.WecomService;
import com.aiplatform.oa.repository.WecomContactRepository;
import com.aiplatform.oa.entity.WecomContact;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/oa")
public class OAController {

    @Autowired
    private OAService oaService;

    @Autowired
    private WechatNotifyService wechatNotifyService;

    @Autowired
    private WecomService wecomService;

    @Autowired
    private WecomContactRepository wecomContactRepo;

    // === Leave Types ===
    @GetMapping("/leave-types")
    public ApiResponse<List<LeaveType>> getLeaveTypes() {
        return ApiResponse.success(oaService.getLeaveTypes());
    }

    // === Applications ===
    @PostMapping("/applications")
    public ApiResponse<Application> createApplication(@RequestBody Map<String, Object> body,
                                                       @RequestHeader("X-User-Id") Long userId,
                                                       @RequestHeader("X-User-Name") String userName) {
        Long typeId = Long.valueOf(body.get("typeId").toString());
        String typeName = body.get("typeName").toString();
        String reason = body.get("reason").toString();
        LocalDate startDate = LocalDate.parse(body.get("startDate").toString());
        LocalDate endDate = LocalDate.parse(body.get("endDate").toString());
        BigDecimal days = new BigDecimal(body.get("days").toString());
        String attachmentUrl = body.get("attachmentUrl") != null ? body.get("attachmentUrl").toString() : null;

        Application app = oaService.createApplication(userId, userName, typeId, typeName, reason, startDate, endDate, days, attachmentUrl);
        return ApiResponse.success(app);
    }

    @GetMapping("/applications")
    public ApiResponse<List<Application>> getMyApplications(@RequestHeader("X-User-Id") Long userId,
                                                             @RequestParam(required = false) String status) {
        return ApiResponse.success(oaService.getMyApplications(userId, status));
    }

    @GetMapping("/applications/{id}")
    public ApiResponse<Application> getApplication(@PathVariable Long id) {
        Application app = oaService.getApplication(id);
        return app != null ? ApiResponse.success(app) : ApiResponse.error(404, "申请不存在");
    }

    @PostMapping("/applications/{id}/approve")
    public ApiResponse<Application> approveApplication(@PathVariable Long id,
                                                        @RequestBody Map<String, String> body,
                                                        @RequestHeader("X-User-Id") Long approverId,
                                                        @RequestHeader("X-User-Name") String approverName) {
        String comment = body.getOrDefault("comment", "同意");
        Application app = oaService.approveApplication(id, approverId, approverName, comment);
        return app != null ? ApiResponse.success(app) : ApiResponse.error(400, "审批失败");
    }

    @PostMapping("/applications/{id}/reject")
    public ApiResponse<Application> rejectApplication(@PathVariable Long id,
                                                       @RequestBody Map<String, String> body,
                                                       @RequestHeader("X-User-Id") Long approverId,
                                                       @RequestHeader("X-User-Name") String approverName) {
        String comment = body.getOrDefault("comment", "驳回");
        Application app = oaService.rejectApplication(id, approverId, approverName, comment);
        return app != null ? ApiResponse.success(app) : ApiResponse.error(400, "驳回失败");
    }

    @PostMapping("/applications/{id}/cancel")
    public ApiResponse<Application> cancelApplication(@PathVariable Long id,
                                                       @RequestHeader("X-User-Id") Long userId) {
        Application app = oaService.cancelApplication(id, userId);
        return app != null ? ApiResponse.success(app) : ApiResponse.error(400, "撤回失败");
    }

    @GetMapping("/applications/pending")
    public ApiResponse<List<Application>> getPendingApprovals(@RequestHeader("X-User-Id") Long approverId) {
        return ApiResponse.success(oaService.getPendingApprovals(approverId));
    }

    @GetMapping("/applications/stats")
    public ApiResponse<Map<String, Object>> getStats(@RequestHeader("X-User-Id") Long userId) {
        return ApiResponse.success(oaService.getStats(userId));
    }

    // === Approval Records ===
    @GetMapping("/approval-records/{applicationId}")
    public ApiResponse<List<ApprovalRecord>> getApprovalRecords(@PathVariable Long applicationId) {
        return ApiResponse.success(oaService.getApprovalRecords(applicationId));
    }

    // === Notifications ===
    @GetMapping("/notifications")
    public ApiResponse<List<Notification>> getNotifications(@RequestHeader("X-User-Id") Long userId,
                                                             @RequestParam(required = false) String type) {
        return ApiResponse.success(oaService.getNotifications(userId, type));
    }

    @GetMapping("/notifications/unread-count")
    public ApiResponse<Long> getUnreadCount(@RequestHeader("X-User-Id") Long userId) {
        return ApiResponse.success(oaService.getUnreadCount(userId));
    }

    @PutMapping("/notifications/{id}/read")
    public ApiResponse<Void> markAsRead(@PathVariable Long id) {
        oaService.markAsRead(id);
        return ApiResponse.success(null);
    }

    @PutMapping("/notifications/read-all")
    public ApiResponse<Void> markAllAsRead(@RequestHeader("X-User-Id") Long userId) {
        oaService.markAllAsRead(userId);
        return ApiResponse.success(null);
    }

    // === Approvers ===
    @GetMapping("/approvers")
    public ApiResponse<List<Approver>> getApprovers(@RequestHeader("X-User-Id") Long userId) {
        return ApiResponse.success(oaService.getApprovers(userId));
    }

    // === WeCom Integration ===
    @GetMapping("/wecom/contacts")
    public ApiResponse<List<WecomContact>> getWecomContacts() {
        return ApiResponse.success(wecomContactRepo.findByIsActiveTrue());
    }

    @GetMapping("/wecom/approver")
    public ApiResponse<Map<String, Object>> getMyApprover(@RequestHeader("X-User-Id") Long userId) {
        Map<String, Object> result = new HashMap<>();
        // 查找当前用户的审批人
        Approver approver = null;
        List<Approver> approvers = oaService.getApprovers(userId);
        if (!approvers.isEmpty()) {
            approver = approvers.get(0);
        }
        
        if (approver != null) {
            result.put("approverId", approver.getApproverId());
            result.put("approverName", approver.getApproverName());
            // 查找审批人的企业微信信息
            Optional<WecomContact> wecomContact = wecomContactRepo.findByUserId(approver.getApproverId());
            if (wecomContact.isPresent()) {
                result.put("wecomName", wecomContact.get().getWecomName());
                result.put("wecomDepartment", wecomContact.get().getWecomDepartment());
                result.put("wecomPosition", wecomContact.get().getWecomPosition());
                result.put("wecomAvatar", wecomContact.get().getWecomAvatar());
            }
        }
        return ApiResponse.success(result);
    }

    @GetMapping("/wecom/departments")
    public ApiResponse<List<Map<String, Object>>> getWecomDepartments() {
        return ApiResponse.success(wecomService.getDepartments());
    }

    @GetMapping("/wecom/members/{departmentId}")
    public ApiResponse<List<Map<String, Object>>> getWecomMembers(@PathVariable int departmentId) {
        return ApiResponse.success(wecomService.getDepartmentMembers(departmentId));
    }

    // === Dashboard ===
    @GetMapping("/dashboard")
    public ApiResponse<Map<String, Object>> getDashboard(@RequestHeader("X-User-Id") Long userId) {
        return ApiResponse.success(oaService.getDashboard(userId));
    }

    // === All Applications (for admin/testing) ===
    @GetMapping("/applications/all")
    public ApiResponse<List<Application>> getAllApplications() {
        return ApiResponse.success(oaService.getAllApplications());
    }

    // === Manual WeChat Notify ===
    @PostMapping("/notify/wechat")
    public ApiResponse<Void> manualNotify(@RequestBody Map<String, String> body) {
        wechatNotifyService.sendApprovalNotify(
            body.get("approverName"), body.get("applicantName"), body.get("typeName"),
            Double.parseDouble(body.get("days")), body.get("reason"), body.get("appNo")
        );
        return ApiResponse.success(null);
    }

    // === WeCom Test Notify ===
    @PostMapping("/wecom/test-notify")
    public ApiResponse<String> testWecomNotify(@RequestBody Map<String, String> body) {
        String userId = body.get("userId");
        String title = body.getOrDefault("title", "测试通知");
        String content = body.getOrDefault("content", "这是一条测试消息");
        boolean result = wecomService.sendApplicationMessage(userId, title, content);
        return ApiResponse.success(result ? "发送成功" : "发送失败");
    }
}
