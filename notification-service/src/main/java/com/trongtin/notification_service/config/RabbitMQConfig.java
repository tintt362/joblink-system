package com.trongtin.notification_service.config;


import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ErrorHandler;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "joblink.events";
    public static final String QUEUE_NAME = "notification.email.queue";
    public static final String DLQ_NAME = "notification.email.dlq";
    public static final String DLX_NAME = "joblink.dlx"; // Dead Letter Exchange

    // 1. Khai báo Exchange chính (Topic)
    @Bean
    public TopicExchange eventsExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    // 2. Khai báo Dead Letter Exchange
    @Bean
    public TopicExchange dlxExchange() {
        return new TopicExchange(DLX_NAME);
    }

    // 3. Khai báo Dead Letter Queue
    @Bean
    public Queue dlq() {
        return new Queue(DLQ_NAME);
    }

    // 4. Liên kết DLQ với DLX
    @Bean
    public Binding dlqBinding() {
        // DLQ sẽ lắng nghe tất cả message từ DLX (routing key bất kỳ)
        return BindingBuilder.bind(dlq()).to(dlxExchange()).with("#");
    }

    // 5. Khai báo Queue chính với cấu hình DLX
    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable(QUEUE_NAME)
                .withArgument("x-dead-letter-exchange", DLX_NAME) // Chỉ định DLX
                .build();
    }

    // 6. Liên kết Queue chính với Exchange chính
    @Bean
    public Binding emailBinding() {
        // Nghe tất cả event liên quan đến notifications
        return BindingBuilder.bind(emailQueue()).to(eventsExchange()).with("notifications.#");
    }

    // 7. Message Converter: Dùng Jackson để chuyển đổi JSON Payload thành Java Object
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // 8. Cấu hình Listener Container Factory cho Retry/DLQ (Tùy chọn)
    /*
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        // Cấu hình Retry policy: Max 3 attempts, sau đó chuyển sang DLQ
        factory.setAdviceChain(RetryInterceptorBuilder.stateless()
                .maxAttempts(3)
                .backOffOptions(1000, 2.0, 10000) // 1s, nhân 2, max 10s
                .recoverer(rejectAndDontRequeueRecoverer())
                .build());
        return factory;
    }

    // MessageRecoverer: Hành động khi retry thất bại (NACK và chuyển sang DLQ)
    @Bean
    public MessageRecoverer rejectAndDontRequeueRecoverer() {
        return new RejectAndDontRequeueRecoverer();
    }
    */
}