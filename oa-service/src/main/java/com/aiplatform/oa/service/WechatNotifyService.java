package com.aiplatform.oa.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class WechatNotifyService {

    private static final Logger logger = LoggerFactory.getLogger(WechatNotifyService.class);

    @Value("${wechat.webhook.url:}")
    private String webhookUrl;

    @Value("${wechat.webhook.enabled:false}")
    private boolean enabled;

    /**
     * Send approval notification to WeChat Work robot
     * In production, this would POST to the webhook URL
     * Currently simulated with logging
     */
    public void sendApprovalNotify(String approverName, String applicantName, String typeName, 
                                     double days, String reason, String appNo) {
        String markdown = buildMarkdown(approverName, applicantName, typeName, days, reason, appNo);
        logger.info("=== WeChat Work Notification (Simulated) ===");
        logger.info("To: {} (via WeChat Work Robot)", approverName);
        logger.info("Content:\n{}", markdown);
        logger.info("=============================================");

        if (enabled && webhookUrl != null && !webhookUrl.isEmpty()) {
            // In production: POST to webhookUrl with markdown content
            logger.info("Would POST to: {}", webhookUrl);
        }
    }

    /**
     * Send result notification (approved/rejected) to applicant
     */
    public void sendResultNotify(String applicantName, String typeName, double days, 
                                  boolean approved, String approverName, String comment) {
        String status = approved ? "已通过" : "已驳回";
        String color = approved ? "#52c41a" : "#ff4d4f";
        
        logger.info("=== WeChat Work Result Notification (Simulated) ===");
        logger.info("To: {} (via WeChat Work Robot)", applicantName);
        logger.info("Status: {} | Type: {} | Days: {} | Approver: {} | Comment: {}", 
                    status, typeName, days, approverName, comment);
        logger.info("==================================================");
    }

    private String buildMarkdown(String approverName, String applicantName, String typeName,
                                  double days, String reason, String appNo) {
        return String.format(
            "## 📋 新审批待处理\n\n" +
            "> **申请人**: %s\n\n" +
            "> **假期类型**: %s\n\n" +
            "> **请假天数**: %.1f 天\n\n" +
            "> **申请编号**: %s\n\n" +
            "> **请假原因**: %s\n\n" +
            "> 请尽快审批处理",
            applicantName, typeName, days, appNo, reason
        );
    }
}
