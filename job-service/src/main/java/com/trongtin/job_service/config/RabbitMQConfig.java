package com.trongtin.job_service.config;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${joblink.rabbitmq.exchange}")
    private String exchangeName;

    // 1. Định nghĩa Exchange (Topic Exchange)
    @Bean
    public TopicExchange jobEventsExchange() {
        // durable=true: đảm bảo Exchange không bị mất khi broker restart
        return new TopicExchange(exchangeName, true, false);
    }

    // 2. Định nghĩa Message Converter (Sử dụng Jackson để gửi/nhận JSON)
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // 3. Cấu hình RabbitTemplate để sử dụng Converter
    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        // Tùy chọn: Có thể thêm logic xác nhận/retry tại đây
        return rabbitTemplate;
    }
}