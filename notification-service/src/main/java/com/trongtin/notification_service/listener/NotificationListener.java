package com.trongtin.notification_service.listener;


import com.trongtin.notification_service.config.RabbitMQConfig;
import com.trongtin.notification_service.model.ApplicationSubmittedEvent;
import com.trongtin.notification_service.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NotificationListener {

    @Autowired
    private NotificationService notificationService;

    /**
     * Lắng nghe queue chính. Routing Key sẽ được xử lý bởi cấu hình Binding.
     * messageConverter sẽ tự động deserialize JSON thành ApplicationSubmittedEvent.
     */
    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void handleApplicationSubmitted(ApplicationSubmittedEvent event) {
        // **Tư duy:** Listener chỉ là cầu nối, ủy quyền xử lý nặng cho Service Layer
        try {
            notificationService.processApplicationSubmitted(event);
        } catch (Exception e) {
            // **Tư duy:** Nếu service throw Exception, Spring AMQP sẽ tự động NACK.
            // Cơ chế Retry sẽ được kích hoạt, hoặc chuyển sang DLQ nếu vượt quá giới hạn retry.
            log.error("Failed to process message for application ID {}. Will rely on RabbitMQ Retry/DLQ.",
                    event.getApplicationId(), e);
            throw new RuntimeException("Processing failed, requesting re-delivery or DLQ move.", e);
        }
    }

    /**
     * Listener giám sát DLQ (Được sử dụng cho mục đích cảnh báo và xử lý thủ công)
     */
    @RabbitListener(queues = RabbitMQConfig.DLQ_NAME)
    public void handleDeadLetter(String failedMessage) {
        log.error("=================================================");
        log.error("DEAD LETTER MESSAGE RECEIVED: Needs Attention!");
        log.error("Payload: {}", failedMessage);
        log.error("=================================================");
        // **Tư duy:** Không ném ngoại lệ ở đây để message bị xóa khỏi DLQ.
        // Alert hệ thống vận hành (Prometheus/Grafana) về sự kiện này.
    }
}