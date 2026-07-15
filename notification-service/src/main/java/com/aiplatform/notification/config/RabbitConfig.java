package com.aiplatform.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 队列配置
 *
 * 与 AI 系统 Python 端共用 Exchange，实现跨语言事件通信。
 * Python 端发送 AI 任务完成事件，Java 端消费后发送通知。
 */
@Configuration
public class RabbitConfig {

    public static final String EXCHANGE_EVENTS = "ai.events";
    public static final String QUEUE_NOTIFICATION = "java.notification";
    public static final String ROUTING_KEY = "notification.*";

    /** 声明 Topic Exchange（与 Python 端一致） */
    @Bean
    public TopicExchange eventsExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE_EVENTS).durable(true).build();
    }

    /** Java 端通知队列 */
    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(QUEUE_NOTIFICATION).build();
    }

    /** 绑定队列到 Exchange */
    @Bean
    public Binding notificationBinding(Queue notificationQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(notificationQueue)
                .to(eventsExchange)
                .with(ROUTING_KEY);
    }
}
