package com.aiplatform.oa.service;

import com.aiplatform.oa.entity.*;
import com.aiplatform.oa.repository.*;
import com.aiplatform.oa.service.WecomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Optional;

@Service
public class OAService {

    @Autowired
    private LeaveTypeRepository leaveTypeRepo;
    @Autowired
    private ApplicationRepository applicationRepo;
    @Autowired
    private ApprovalRecordRepository recordRepo;
    @Autowired
    private NotificationRepository notificationRepo;
    @Autowired
    private ApproverRepository approverRepo;
    @Autowired
    private ApprovalFlowRepository flowRepo;
    @Autowired
    private WechatNotifyService wechatNotifyService;

    @Autowired
    private WecomService wecomService;

    @Autowired
    private WecomContactRepository wecomContactRepo;

    // === Leave Types ===
    public List<LeaveType> getLeaveTypes() {
        return leaveTypeRepo.findByStatus("active");
    }

    // === Applications ===
    public List<Application> getMyApplications(Long userId, String status) {
        if (status != null && !status.isEmpty() && !"all".equals(status)) {
            return applicationRepo.findByApplicantIdAndStatus(userId, status);
        }
        return applicationRepo.findByApplicantIdOrderByCreatedAtDesc(userId);
    }

    public Application getApplication(Long id) {
        return applicationRepo.findById(id).orElse(null);
    }

    @Transactional
    public Application createApplication(Long userId, String userName, Long typeId, String typeName,
                                          String reason, LocalDate startDate, LocalDate endDate,
                                          BigDecimal days, String attachmentUrl) {
        Application app = new Application();
        app.setAppNo(generateAppNo());
        app.setApplicantId(userId);
        app.setApplicantName(userName);
        app.setTypeId(typeId);
        app.setTypeName(typeName);
        app.setFlowId(1L); // default flow
        app.setReason(reason);
        app.setStartDate(startDate);
        app.setEndDate(endDate);
        app.setDays(days);
        app.setAttachmentUrl(attachmentUrl);
        app.setStatus("pending");
        app.setCurrentStep(1);
        app.setTotalSteps(1);
        app = applicationRepo.save(app);

        // Find approver for this user
        Approver approver = approverRepo.findByUserIdAndIsDefaultTrue(userId);
        if (approver != null) {
            // Create approval record (pending)
            ApprovalRecord record = new ApprovalRecord();
            record.setApplicationId(app.getId());
            record.setApproverId(approver.getApproverId());
            record.setApproverName(approver.getApproverName());
            record.setStepOrder(1);
            recordRepo.save(record);

            // Create notification for approver
            Notification notify = new Notification();
            notify.setApplicationId(app.getId());
            notify.setUserId(approver.getApproverId());
            notify.setTitle("新审批待处理");
            notify.setContent(userName + "提交了" + typeName + "申请（" + days + "天），请尽快审批");
            notify.setType("apply");
            notify.setIsRead(false);
            notificationRepo.save(notify);

            // Send WeChat notification
            wechatNotifyService.sendApprovalNotify(
                approver.getApproverName(), userName, typeName, 
                days.doubleValue(), reason, app.getAppNo()
            );

            // Send WeCom notification
            try {
                // 查找审批人的企业微信userid
                Optional<WecomContact> approverWecom = wecomContactRepo.findByUserId(approver.getApproverId());
                String wecomUserid = approverWecom.map(WecomContact::getWecomUserid).orElse(null);
                wecomService.sendApprovalNotification(
                    wecomUserid, approver.getApproverName(), userName,
                    typeName, days.doubleValue(), reason, app.getAppNo()
                );
            } catch (Exception e) {
                // 企业微信通知失败不影响主流程
            }
        }

        return app;
    }

    @Transactional
    public Application approveApplication(Long appId, Long approverId, String approverName, String comment) {
        Application app = applicationRepo.findById(appId).orElse(null);
        if (app == null || !"pending".equals(app.getStatus())) {
            return null;
        }

        // Update approval record
        List<ApprovalRecord> records = recordRepo.findByApplicationIdOrderByStepOrderAsc(appId);
        for (ApprovalRecord r : records) {
            if (r.getApproverId().equals(approverId) && r.getAction() == null) {
                r.setAction("approve");
                r.setComment(comment);
                r.setApprovedAt(LocalDateTime.now());
                recordRepo.save(r);
                break;
            }
        }

        // Approve the application
        app.setStatus("approved");
        applicationRepo.save(app);

        // Notify applicant
        Notification notify = new Notification();
        notify.setApplicationId(appId);
        notify.setUserId(app.getApplicantId());
        notify.setTitle("审批已通过");
        notify.setContent("您的" + app.getTypeName() + "申请已通过审批");
        notify.setType("approve");
        notify.setIsRead(false);
        notificationRepo.save(notify);

        // Send WeChat result notification
        wechatNotifyService.sendResultNotify(
            app.getApplicantName(), app.getTypeName(), app.getDays().doubleValue(),
            true, approverName, comment
        );

        // Send WeCom result notification
        try {
            Optional<WecomContact> applicantWecom = wecomContactRepo.findByUserId(app.getApplicantId());
            String applicantWecomUserid = applicantWecom.map(WecomContact::getWecomUserid).orElse(null);
            wecomService.sendResultNotification(
                applicantWecomUserid, app.getApplicantName(), app.getTypeName(),
                app.getDays().doubleValue(), true, approverName, comment
            );
        } catch (Exception e) {
            // 企业微信通知失败不影响主流程
        }

        return app;
    }

    @Transactional
    public Application rejectApplication(Long appId, Long approverId, String approverName, String comment) {
        Application app = applicationRepo.findById(appId).orElse(null);
        if (app == null || !"pending".equals(app.getStatus())) {
            return null;
        }

        // Update approval record
        List<ApprovalRecord> records = recordRepo.findByApplicationIdOrderByStepOrderAsc(appId);
        for (ApprovalRecord r : records) {
            if (r.getApproverId().equals(approverId) && r.getAction() == null) {
                r.setAction("reject");
                r.setComment(comment);
                r.setApprovedAt(LocalDateTime.now());
                recordRepo.save(r);
                break;
            }
        }

        app.setStatus("rejected");
        applicationRepo.save(app);

        // Notify applicant
        Notification notify = new Notification();
        notify.setApplicationId(appId);
        notify.setUserId(app.getApplicantId());
        notify.setTitle("审批被驳回");
        notify.setContent("您的" + app.getTypeName() + "申请被驳回" + (comment != null ? "：" + comment : ""));
        notify.setType("reject");
        notify.setIsRead(false);
        notificationRepo.save(notify);

        // Send WeChat result notification
        wechatNotifyService.sendResultNotify(
            app.getApplicantName(), app.getTypeName(), app.getDays().doubleValue(),
            false, approverName, comment
        );

        // Send WeCom result notification
        try {
            Optional<WecomContact> applicantWecom = wecomContactRepo.findByUserId(app.getApplicantId());
            String applicantWecomUserid = applicantWecom.map(WecomContact::getWecomUserid).orElse(null);
            wecomService.sendResultNotification(
                applicantWecomUserid, app.getApplicantName(), app.getTypeName(),
                app.getDays().doubleValue(), false, approverName, comment
            );
        } catch (Exception e) {
            // 企业微信通知失败不影响主流程
        }

        return app;
    }

    @Transactional
    public Application cancelApplication(Long appId, Long userId) {
        Application app = applicationRepo.findById(appId).orElse(null);
        if (app == null || !app.getApplicantId().equals(userId) || !"pending".equals(app.getStatus())) {
            return null;
        }
        app.setStatus("cancelled");
        return applicationRepo.save(app);
    }

    public List<Application> getPendingApprovals(Long approverId) {
        List<ApprovalRecord> records = recordRepo.findByApproverIdOrderByCreatedAtDesc(approverId);
        List<Application> pending = new ArrayList<>();
        for (ApprovalRecord r : records) {
            if (r.getAction() == null) {
                Application app = applicationRepo.findById(r.getApplicationId()).orElse(null);
                if (app != null && "pending".equals(app.getStatus())) {
                    pending.add(app);
                }
            }
        }
        return pending;
    }

    // === Approval Records ===
    public List<ApprovalRecord> getApprovalRecords(Long applicationId) {
        return recordRepo.findByApplicationIdOrderByStepOrderAsc(applicationId);
    }

    // === Notifications ===
    public List<Notification> getNotifications(Long userId, String type) {
        if (type != null && !type.isEmpty()) {
            return notificationRepo.findByUserIdAndType(userId, type);
        }
        return notificationRepo.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public long getUnreadCount(Long userId) {
        return notificationRepo.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(Long notifyId) {
        Notification n = notificationRepo.findById(notifyId).orElse(null);
        if (n != null) {
            n.setIsRead(true);
            n.setReadAt(LocalDateTime.now());
            notificationRepo.save(n);
        }
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepo.findByUserIdAndType(userId, null);
        for (Notification n : unread) {
            if (!n.getIsRead()) {
                n.setIsRead(true);
                n.setReadAt(LocalDateTime.now());
                notificationRepo.save(n);
            }
        }
    }

    // === Approvers ===
    public List<Approver> getApprovers(Long userId) {
        return approverRepo.findByUserId(userId);
    }

    // === Dashboard ===
    public Map<String, Object> getDashboard(Long userId) {
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("pendingCount", applicationRepo.countPendingByUserId(userId));
        dashboard.put("unreadNotifyCount", notificationRepo.countByUserIdAndIsReadFalse(userId));
        
        BigDecimal yearDays = applicationRepo.sumDaysByUserAndTypeThisYear(userId, "年假");
        dashboard.put("yearLeaveUsed", yearDays);
        
        List<Notification> recentNotifies = notificationRepo.findByUserIdOrderByCreatedAtDesc(userId);
        if (recentNotifies.size() > 5) recentNotifies = recentNotifies.subList(0, 5);
        dashboard.put("recentNotifications", recentNotifies);
        
        List<Application> recentApps = applicationRepo.findByApplicantIdOrderByCreatedAtDesc(userId);
        if (recentApps.size() > 5) recentApps = recentApps.subList(0, 5);
        dashboard.put("recentApplications", recentApps);

        return dashboard;
    }

    // === Stats ===
    public Map<String, Object> getStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("pendingCount", applicationRepo.countPendingByUserId(userId));
        stats.put("unreadNotifyCount", notificationRepo.countByUserIdAndIsReadFalse(userId));
        BigDecimal yearDays = applicationRepo.sumDaysByUserAndTypeThisYear(userId, "年假");
        stats.put("yearLeaveUsed", yearDays);
        return stats;
    }

    // === All Applications (admin) ===
    public List<Application> getAllApplications() {
        return applicationRepo.findAllByOrderByCreatedAtDesc();
    }

    private String generateAppNo() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = applicationRepo.count() + 1;
        return String.format("OA-%s-%03d", date, count);
    }
}
