package com.trongtin.job_service.dto.response;

import com.trongtin.job_service.entity.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobResponse {
    private UUID id;
    private UUID recruiterId; // Vẫn cần cho các luồng nghiệp vụ liên quan đến job của Recruiter
    private String title;
    private String description;
    private String location;
    private Integer salaryMin;
    private Integer salaryMax;
    private List<String> skills;
    private Status status;
    private Instant createdAt;
}