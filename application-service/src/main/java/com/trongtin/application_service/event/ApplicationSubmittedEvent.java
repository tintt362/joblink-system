package com.trongtin.application_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ApplicationSubmittedEvent {
    private UUID applicationId;
    private UUID jobId;
    private UUID candidateId;
    private String recruiterEmail; // Đã fetch từ Job Service
    private String jobTitle;      // Đã fetch từ Job Service
}