package com.aiplatform.oa.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.security.MessageDigest;
import java.util.Arrays;

/**
 * 企业微信回调处理
 * GET  → URL验证（签名校验 + 返回echostr）
 * POST → 接收企微推送消息
 */
@RestController
@RequestMapping("/api/oa/wecom")
public class WecomCallbackController {

    private static final Logger logger = LoggerFactory.getLogger(WecomCallbackController.class);

    // 企业微信后台配置的 Token (在企微后台自定义)
    private static final String WECOM_TOKEN = "oaverifytoken2026";

    /**
     * URL验证 - 企微保存配置时调用
     */
    @GetMapping("/callback")
    public String verifyUrl(
            @RequestParam("msg_signature") String msgSignature,
            @RequestParam("timestamp") String timestamp,
            @RequestParam("nonce") String nonce,
            @RequestParam("echostr") String echostr) {

        logger.info("企微URL验证: signature={}, timestamp={}, nonce={}", msgSignature, timestamp, nonce);

        if (verifySignature(WECOM_TOKEN, timestamp, nonce, msgSignature)) {
            logger.info("✅ 企微URL验证通过, 返回echostr: {}", echostr);
            return echostr;
        }

        logger.warn("❌ 企微URL验证失败");
        return "signature verification failed";
    }

    /**
     * 接收企微推送消息
     */
    @PostMapping("/callback")
    public String receiveMessage(@RequestBody String body,
                                  @RequestParam("msg_signature") String msgSignature,
                                  @RequestParam("timestamp") String timestamp,
                                  @RequestParam("nonce") String nonce) {

        logger.info("收到企微消息推送: signature={}, timestamp={}, nonce={}", msgSignature, timestamp, nonce);
        logger.info("消息体(前500字符): {}", body.length() > 500 ? body.substring(0, 500) : body);

        // 返回success表示处理成功
        return "success";
    }

    /**
     * 验证企业微信签名
     * 1. 将 token, timestamp, nonce 按字典序排序
     * 2. 拼接成字符串
     * 3. SHA1 加密
     * 4. 与 msgSignature 比对
     */
    private boolean verifySignature(String token, String timestamp, String nonce, String msgSignature) {
        try {
            String[] arr = {token, timestamp, nonce};
            Arrays.sort(arr);
            String sortedStr = String.join("", arr);

            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(sortedStr.getBytes("UTF-8"));

            StringBuilder hexStr = new StringBuilder();
            for (byte b : digest) {
                hexStr.append(String.format("%02x", b & 0xff));
            }

            boolean match = hexStr.toString().equals(msgSignature);
            logger.info("签名验证: sorted={}, calc={}, given={}, match={}",
                    Arrays.toString(arr), hexStr, msgSignature, match);
            return match;
        } catch (Exception e) {
            logger.error("签名验证异常", e);
            return false;
        }
    }
}
