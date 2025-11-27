package com.trongtin.job_service.event;

import com.trongtin.job_service.dto.request.JobCreatedPayload;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JobEventPublisher {

    @Value("${joblink.rabbitmq.exchange}")
    private String exchangeName;

    private final AmqpTemplate rabbitTemplate;

    @Autowired
    public JobEventPublisher(AmqpTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Phát sự kiện Job Created đến RabbitMQ.
     */
    public void publishJobCreated(JobCreatedPayload payload) {
        // Routing Key: job.created
        // Consumer sẽ bind Queue của họ với Routing Key này
        rabbitTemplate.convertAndSend(exchangeName, "job.created", payload);
        System.out.println("Published event: job.created for Job ID: " + payload.jobId());
    }

    // Tương tự, bạn có thể định nghĩa publishJobUpdated(JobUpdatedPayload payload)
}