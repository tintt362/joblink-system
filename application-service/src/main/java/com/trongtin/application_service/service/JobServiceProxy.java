package com.trongtin.application_service.service;


import com.trongtin.application_service.exception.JobNotFoundException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.UUID;

@Service
@Slf4j
public class JobServiceProxy {

    private final WebClient webClient;

    public JobServiceProxy(@Value("${job.service.url}") String jobServiceUrl) {
        this.webClient = WebClient.builder().baseUrl(jobServiceUrl).build();
    }

    @Data
    public static class JobDetailsDTO {
        private UUID jobId;
        private String title;
        private String status;
        private String recruiterEmail;
    }

    // 1. Kiểm tra Job tồn tại & lấy thông tin chi tiết
    public JobDetailsDTO getJobDetails(UUID jobId) {
        log.info("Checking job details for ID: {}", jobId);
        try {
            return webClient.get()
                    .uri("/api/jobs/{jobId}", jobId)
                    .retrieve()
                    // Xử lý lỗi 4xx
                    .onStatus(HttpStatusCode::is4xxClientError,
                            response -> Mono.error(new JobNotFoundException("Job not found or closed: " + jobId)))
                    .bodyToMono(JobDetailsDTO.class)
                    .block(); // Đơn giản hóa bằng cách dùng block()
        } catch (Exception e) {
            log.error("Failed to call Job Service for job ID {}: {}", jobId, e.getMessage());
            throw new RuntimeException("Service Unavailable: Could not verify Job details.", e);
        }
    }

    // 2. Kiểm tra quyền sở hữu Job
    public boolean checkRecruiterOwnership(UUID jobId, UUID recruiterId) {
        log.info("Checking ownership for job {} by recruiter {}", jobId, recruiterId);
        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/jobs/{jobId}/ownership")
                            .queryParam("recruiterId", recruiterId)
                            .build(jobId))
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();
        } catch (Exception e) {
            log.error("Failed to check ownership with Job Service: {}", e.getMessage());
            return false; // Mặc định từ chối nếu không thể liên hệ service
        }
    }
}