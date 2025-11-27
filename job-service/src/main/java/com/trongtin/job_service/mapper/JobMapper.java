package com.trongtin.job_service.mapper;

import com.trongtin.job_service.dto.request.JobCreateRequest;
import com.trongtin.job_service.dto.request.JobCreatedPayload;
import com.trongtin.job_service.dto.response.JobResponse;
import com.trongtin.job_service.entity.Job;
import com.trongtin.job_service.entity.Status;

import java.util.UUID;

public class JobMapper {

    // Ánh xạ từ request sang entity
    public static Job toEntity(JobCreateRequest request, UUID recruiterId) {
        return Job.builder()
                .recruiterId(recruiterId)
                .title(request.getTitle())
                .description(request.getDescription())
                .location(request.getLocation())
                .salaryMin(request.getSalaryMin())
                .salaryMax(request.getSalaryMax())
                .skills(request.getSkills())
                .status(Status.OPEN) // Khi đăng job mới, mặc định OPEN
                .build();
    }

    // Ánh xạ từ entity sang DTO response
    public static JobResponse toResponse(Job job) {
        return JobResponse.builder()
                .id(job.getId())
                .recruiterId(job.getRecruiterId())
                .title(job.getTitle())
                .description(job.getDescription())
                .location(job.getLocation())
                .salaryMin(job.getSalaryMin())
                .salaryMax(job.getSalaryMax())
                .skills(job.getSkills())
                .status(job.getStatus())
                .createdAt(job.getCreatedAt())
                .build();
    }

    public static JobCreatedPayload toPayload(Job job) {
        return new JobCreatedPayload(
                job.getId(),
                job.getTitle(),
                job.getRecruiterId() // Giả sử recruiterId đã có trong Entity
        );
    }
}
