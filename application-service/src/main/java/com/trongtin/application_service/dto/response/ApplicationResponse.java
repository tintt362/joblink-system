package com.trongtin.application_service.dto.response;

import com.trongtin.application_service.entity.ApplicationHistory;
import com.trongtin.application_service.entity.ApplicationStatus;
import lombok.Data;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
public class ApplicationResponse {
    private UUID id;
    private UUID jobId;
    private UUID candidateId;
    private ApplicationStatus status;
    private String coverLetter;
    private UUID cvId;
    private List<ApplicationHistory> history; // Dùng để hiển thị lịch sử
    private Instant createdAt;
    private Instant updatedAt;
}