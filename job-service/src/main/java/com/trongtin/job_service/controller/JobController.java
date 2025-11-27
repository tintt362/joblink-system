package com.trongtin.job_service.controller;

import com.trongtin.job_service.dto.request.JobCreateRequest;
import com.trongtin.job_service.dto.request.JobCreatedPayload;
import com.trongtin.job_service.dto.response.ApiResponse;
import com.trongtin.job_service.dto.response.JobResponse;
import com.trongtin.job_service.entity.Job;
import com.trongtin.job_service.entity.JobDocument;
import com.trongtin.job_service.event.JobEventPublisher;
import com.trongtin.job_service.mapper.JobMapper;
import com.trongtin.job_service.service.JobService;
import com.trongtin.job_service.exception.AppException;
import com.trongtin.job_service.exception.ErrorCode;
import com.trongtin.job_service.service.search.JobSearchService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    @Autowired
    private JobService jobService;

    @Autowired
    private JobSearchService jobSearchService;

    @Autowired
    private JobEventPublisher jobEventPublisher;
    private static final String RECRUITER_ID_HEADER = "X-User-Id";
    private static final String ROLE_HEADER = "X-User-Role";

    // POST /api/jobs
    @PostMapping
    public ResponseEntity<ApiResponse<JobResponse>> createJob(
            @Valid @RequestBody JobCreateRequest request,
            @RequestHeader(ROLE_HEADER) String role,
            @RequestHeader(RECRUITER_ID_HEADER) UUID recruiterId) {

        // 1. Validation & Authorization Check
        if (!"RECRUITER".equalsIgnoreCase(role)) {
            throw new AppException(ErrorCode.UNAUTHORIZED, "Only recruiters can create jobs");
        }

        // 2. Service Call & Persistence (PostgreSQL + ES Indexing kích hoạt bên trong Service)
        Job newJob = jobService.createJob(request, recruiterId);

        // 3. 💡 Phát Event cho Microservices khác (Giai đoạn 4)
        // SỬA: Dùng newJob thay vì savedJob
        JobCreatedPayload payload = JobMapper.toPayload(newJob);
        jobEventPublisher.publishJobCreated(payload);

        // 4. Response Mapping
        JobResponse response = JobMapper.toResponse(newJob);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Job created successfully"));
    }
    // GET /api/jobs/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<JobResponse>> getJob(@PathVariable UUID id) {
        Job job = jobService.getJobById(id);
        return ResponseEntity.ok(ApiResponse.success(JobMapper.toResponse(job), "Job fetched successfully"));
    }

    // PUT /api/jobs/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<JobResponse>> updateJob(
            @PathVariable UUID id,
            @Valid @RequestBody JobCreateRequest request,
            @RequestHeader(RECRUITER_ID_HEADER) UUID userId) {

        Job updated = jobService.updateJob(id, request, userId);
        JobResponse response = JobMapper.toResponse(updated);

        return ResponseEntity.ok(ApiResponse.success(response, "Job updated successfully"));
    }

    // DELETE /api/jobs/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteJob(
            @PathVariable UUID id,
            @RequestHeader(RECRUITER_ID_HEADER) UUID userId) {

        jobService.deleteJob(id, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Job closed successfully"));
    }

    @GetMapping("/{id}/ownership")
    public ResponseEntity<Boolean> checkOwnership(
            @PathVariable UUID id,
            @RequestParam UUID recruiterId) {
        return ResponseEntity.ok(jobService.isOwnedByRecruiter(id, recruiterId));
    }
    // GET /api/jobs/search
    @GetMapping("/search")
    public ResponseEntity<Page<JobDocument>> searchJobs(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Integer minSalary,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        // Luồng đọc (Read Flow) mới
        Page<JobDocument> results = jobSearchService.searchJobs(q, location, minSalary, page, size);
        return ResponseEntity.ok(results);
    }
}
