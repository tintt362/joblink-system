package com.trongtin.notification_service.service;


import com.trongtin.notification_service.model.ApplicationSubmittedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class NotificationService {

    @Autowired
    private ThymeleafService templateService;

    @Autowired
    private EmailSender emailSender;

    public void processApplicationSubmitted(ApplicationSubmittedEvent event) {
        // Bắt đầu luồng xử lý chính
        log.info("Processing application.submitted event. Trace ID: {}", event.getTraceId());
        long startTime = System.currentTimeMillis();

        // A. Validation
        if (event.getRecruiterEmail() == null || event.getJobTitle() == null) {
            log.error("Validation failed: Missing recruiterEmail or jobTitle for application ID {}", event.getApplicationId());
            // **Tư duy:** Không throw Exception ở đây. Log lỗi và ACK (vì đây là lỗi data vĩnh viễn), hoặc ném ngoại lệ nếu muốn đẩy lỗi vào DLQ.
            // Để đơn giản, ta sẽ ném Exception để kích hoạt DLQ/Retry.
            throw new IllegalArgumentException("Invalid application submitted event payload.");
        }

        // B. Transformation (Tạo Template)
        Map<String, Object> templateVariables = new HashMap<>();
        templateVariables.put("recruiterName", "Recruiter"); // Giả sử tên là "Recruiter"
        templateVariables.put("candidateName", event.getCandidateName());
        templateVariables.put("jobTitle", event.getJobTitle());
        templateVariables.put("applicationId", event.getApplicationId());

        String htmlContent = templateService.buildEmailContent("application-submitted", templateVariables);

        // C. Delivery (Gửi Email)
        String subject = "[Joblink] Đơn ứng tuyển mới cho vị trí: " + event.getJobTitle();
        emailSender.sendHtmlEmail(event.getRecruiterEmail(), subject, htmlContent);

        // D. Post-Processing & Logging
        long duration = System.currentTimeMillis() - startTime;
        log.info("application.submitted.processed status=SUCCESS, target_email={}, duration_ms={}, trace_id={}",
                event.getRecruiterEmail(), duration, event.getTraceId());
    }
}
