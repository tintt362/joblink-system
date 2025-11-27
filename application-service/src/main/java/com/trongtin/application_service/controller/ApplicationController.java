package com.trongtin.application_service.controller;

import com.trongtin.application_service.dto.request.SubmitApplicationRequest;
import com.trongtin.application_service.dto.response.ApplicationResponse;
import com.trongtin.application_service.dto.request.ApplicationStatusUpdateRequest;
import com.trongtin.application_service.entity.Application;
import com.trongtin.application_service.mapper.ApplicationMapper;
import com.trongtin.application_service.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;
    private final ApplicationMapper mapper; // Dùng để chuyển đổi Entity <-> DTO

    // POST /api/applications
    // Quyền: CANDIDATE
    @PostMapping
    public ResponseEntity<ApplicationResponse> submit(
            @Valid @RequestBody SubmitApplicationRequest request,
            @RequestHeader("X-User-Id") UUID candidateId) {

        Application application = applicationService.submitApplication(request, candidateId);
        return new ResponseEntity<>(mapper.toResponse(application), HttpStatus.CREATED);
    }

    // PUT /api/applications/{id}/status
    // Quyền: RECRUITER
    @PutMapping("/{id}/status")
    public ResponseEntity<ApplicationResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody ApplicationStatusUpdateRequest request,
            @RequestHeader("X-User-Id") UUID recruiterId) throws AccessDeniedException {

        Application application = applicationService.updateApplicationStatus(id, request, recruiterId);
        return ResponseEntity.ok(mapper.toResponse(application));
    }

    // GET /api/applications?jobId=... (Recruiter) hoặc ?candidateId=... (Candidate)
    @GetMapping
    public ResponseEntity<List<ApplicationResponse>> getApplications(
            @RequestParam(required = false) UUID jobId,
            @RequestParam(required = false) UUID candidateId,
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Id") UUID userId) {

        List<Application> applications;

        if ("RECRUITER".equals(role) && jobId != null) {
            // Service sẽ kiểm tra quyền sở hữu Job (Nếu cần)
            applications = applicationService.findByJobId(jobId);
        } else if ("CANDIDATE".equals(role) && candidateId != null && candidateId.equals(userId)) {
            // Đảm bảo Candidate chỉ xem đơn của chính họ
            applications = applicationService.findByCandidateId(candidateId);
        } else {
            return ResponseEntity.badRequest().build(); // Yêu cầu tham số không hợp lệ/thiếu quyền
        }

        return ResponseEntity.ok(mapper.toResponseList(applications));
    }

    // GET /api/applications/{id}
    // Quyền: CANDIDATE (nếu là đơn của họ) HOẶC RECRUITER (nếu là Job của họ)
    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponse> getApplicationDetails(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-User-Role") String role) {

        Application application = applicationService.findById(id);

        // Phân quyền chi tiết (Business Logic)
        boolean isCandidateOwner = application.getCandidateId().equals(userId) && "CANDIDATE".equals(role);
        boolean isRecruiterOwner = "RECRUITER".equals(role) && applicationService.checkRecruiterOwnership(application.getJobId(), userId);

        if (!isCandidateOwner && !isRecruiterOwner) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(mapper.toResponse(application));
    }
}