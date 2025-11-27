package com.trongtin.application_service.service;


import com.trongtin.application_service.dto.request.SubmitApplicationRequest;
import com.trongtin.application_service.dto.request.ApplicationStatusUpdateRequest;
import com.trongtin.application_service.entity.Application;
import com.trongtin.application_service.entity.ApplicationHistory;
import com.trongtin.application_service.entity.ApplicationStatus;
import com.trongtin.application_service.event.ApplicationSubmittedEvent;
import com.trongtin.application_service.event.StatusChangedEvent;
import com.trongtin.application_service.exception.ApplicationException;
import com.trongtin.application_service.exception.ApplicationNotFoundException;
import com.trongtin.application_service.exception.BusinessException;
import com.trongtin.application_service.exception.InvalidStatusTransitionException;
import com.trongtin.application_service.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobServiceProxy jobServiceProxy;
    private final RabbitTemplate rabbitTemplate;
    private final AuthServiceProxy authServiceProxy;
    // private final UserServiceProxy userServiceProxy; // Cần thiết cho luồng PUT status

    private static final String     EVENT_EXCHANGE = "joblink.events";

    // --- Luồng 1: Nộp Đơn Ứng Tuyển (POST /api/applications) ---
    @Transactional
    public Application submitApplication(SubmitApplicationRequest request, UUID candidateId) {
        // 1. Kiểm tra Job tồn tại và trạng thái (Đồng bộ)
        JobServiceProxy.JobDetailsDTO jobDetails = jobServiceProxy.getJobDetails(request.getJobId());

        if (!"OPEN".equals(jobDetails.getStatus())) {
            throw new BusinessException("Job is not open for application.");
        }

        // 2. Kiểm tra đã nộp đơn chưa
        if (applicationRepository.findByJobIdAndCandidateId(request.getJobId(), candidateId).isPresent()) {
            throw new ApplicationException("You have already applied for this job.");
        }

        // 3. Persistence (Lưu vào MongoDB)
        Application application = new Application();
        application.setId(UUID.randomUUID());
        application.setJobId(request.getJobId());
        application.setCandidateId(candidateId);
        application.setCoverLetter(request.getCoverLetter());
        application.setCvId(request.getCvId());

        application.setStatus(ApplicationStatus.APPLIED);
        application.setCreatedAt(Instant.now());
        application.setUpdatedAt(Instant.now());
        application.setHistory(List.of(new ApplicationHistory(ApplicationStatus.APPLIED, Instant.now(), candidateId.toString())));

        Application savedApp = applicationRepository.save(application);

        // 4. Phát Event (Bất đồng bộ)
        ApplicationSubmittedEvent event = new ApplicationSubmittedEvent(
                savedApp.getId(),
                savedApp.getJobId(),
                savedApp.getCandidateId(),
                jobDetails.getRecruiterEmail(),
                jobDetails.getTitle()
        );
        rabbitTemplate.convertAndSend(EVENT_EXCHANGE, "application.submitted", event);
        log.info("Application submitted event published for ID: {}", savedApp.getId());

        return savedApp;
    }

    // --- Luồng 2: Cập Nhật Trạng Thái (PUT /api/applications/{id}/status) ---
    @Transactional
    public Application updateApplicationStatus(UUID applicationId, ApplicationStatusUpdateRequest request, UUID recruiterId) throws AccessDeniedException {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ApplicationNotFoundException(applicationId));

        // 1. Kiểm tra Quyền sở hữu (Đồng bộ)
        if (!jobServiceProxy.checkRecruiterOwnership(application.getJobId(), recruiterId)) {
            throw new AccessDeniedException("Recruiter does not own this job.");
        }

        // 2. Validation Trạng thái (Nghiệp vụ)
        ApplicationStatus newStatus = request.getStatus();
        if (!application.getStatus().getValidNextStatuses().contains(newStatus)) {
            throw new InvalidStatusTransitionException(application.getStatus(), newStatus);
        }

        // 3. Persistence (Cập nhật MongoDB)
        ApplicationStatus oldStatus = application.getStatus();
        application.setStatus(newStatus);
        application.setUpdatedAt(Instant.now());
        application.getHistory().add(new ApplicationHistory(newStatus, Instant.now(), recruiterId.toString()));

        Application updatedApp = applicationRepository.save(application);

        // 4. Phát Event (Bất đồng bộ)
        // NOTE: Cần fetch candidateEmail từ User Service trước khi phát Event!
        // Giả sử logic fetch đã được thực hiện
        // StatusChangedEvent event = new StatusChangedEvent(updatedApp.getId(), newStatus, candidateEmail);
        // rabbitTemplate.convertAndSend(EVENT_EXCHANGE, "application.status_changed", event);
        String candidateEmail = authServiceProxy.getEmailByUserId(updatedApp.getCandidateId());

        StatusChangedEvent event = StatusChangedEvent.builder()
                .applicationId(updatedApp.getId())
                .jobId(updatedApp.getJobId())
                .candidateId(updatedApp.getCandidateId())
                .oldStatus(oldStatus.name())
                .newStatus(newStatus.name())
                .candidateEmail(candidateEmail)
                .recruiterActionBy(recruiterId.toString())
                .build();

        rabbitTemplate.convertAndSend(EVENT_EXCHANGE, "application.status_changed", event);
        log.info("Application status changed from {} to {} for ID: {}", oldStatus, newStatus, updatedApp.getId());

        return updatedApp;
    }
    // Phương thức mới được thêm vào để sử dụng trong Controller
    // Kiểm tra xem RecruiterId có phải là chủ sở hữu của JobId này không.
    public boolean checkRecruiterOwnership(UUID jobId, UUID recruiterId) {
        // Ủy quyền việc kiểm tra cho JobServiceProxy
        return jobServiceProxy.checkRecruiterOwnership(jobId, recruiterId);
    }
    // --- Luồng 3: Xem Danh Sách/Chi Tiết ---
    public List<Application> findByJobId(UUID jobId) {
        // NOTE: Quyền kiểm tra Job Ownership nên được lồng ở đây hoặc trong Controller
        return applicationRepository.findByJobId(jobId);
    }

    public List<Application> findByCandidateId(UUID candidateId) {
        return applicationRepository.findByCandidateId(candidateId);
    }

    public Application findById(UUID id) {
        return applicationRepository.findById(id).orElseThrow(() -> new ApplicationNotFoundException(id));
    }


}