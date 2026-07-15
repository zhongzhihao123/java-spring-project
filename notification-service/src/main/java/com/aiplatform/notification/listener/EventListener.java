package com.aiplatform.notification.listener;

import com.aiplatform.notification.service.NotificationService;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * RabbitMQ 事件监听器
 *
 * 从 ai.events Exchange 消费通知类消息，触发站内信。
 * 与 Python AI 系统共用同一 Exchange，实现跨语言事件通信。
 */
@Component
public class EventListener {

    private static final Logger log = LoggerFactory.getLogger(EventListener.class);
    private final NotificationService notificationService;

    public EventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = "java.notification")
    public void handleEvent(Message message, Channel channel) {
        var body = new String(message.getBody(), StandardCharsets.UTF_8);
        log.info("收到事件消息: {}", body);

        try {
            notificationService.sendInternal(0L, "系统通知", body);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("事件处理失败", e);
        }
    }
}
