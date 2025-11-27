package com.trongtin.job_service.service;

import com.trongtin.job_service.dto.request.JobCreateRequest;
import com.trongtin.job_service.dto.request.JobCreatedPayload;
import com.trongtin.job_service.entity.Job;
import com.trongtin.job_service.entity.Status;
import com.trongtin.job_service.event.JobEventPublisher;
import com.trongtin.job_service.event.JobIndexedEvent;
import com.trongtin.job_service.exception.AppException;
import com.trongtin.job_service.exception.ErrorCode;
import com.trongtin.job_service.exception.JobAccessDeniedException;
import com.trongtin.job_service.exception.JobNotFoundException;
import com.trongtin.job_service.mapper.JobMapper;
import com.trongtin.job_service.repository.JobRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Transactional
public class JobService {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobEventPublisher jobEventPublisher;

    @Autowired
    private  ApplicationEventPublisher eventPublisher; // Thêm publisher
    // Validate lương hợp lệ
    private void validateSalary(JobCreateRequest request) {
        if (request.getSalaryMin() != null && request.getSalaryMax() != null
                && request.getSalaryMin() > request.getSalaryMax()) {
            throw new AppException(ErrorCode.INVALID_SALARY_RANGE);
        }
    }

    // CREATE Job
    public Job createJob(JobCreateRequest request, UUID recruiterId) {
        validateSalary(request);
        Job job = JobMapper.toEntity(request, recruiterId);
        Job savedJob = jobRepository.save(job);
        eventPublisher.publishEvent(new JobIndexedEvent(job.getId()));

        // 3. 💡 Phát Event cho Microservices khác (RabbitMQ - Bất đồng bộ)
        JobCreatedPayload payload = new JobCreatedPayload(
                savedJob.getId(),
                savedJob.getTitle(),
                savedJob.getRecruiterId()
        );
        jobEventPublisher.publishJobCreated(payload); // Gọi RabbitMQ Publisher
        return savedJob;
    }

    // GET Job by ID
    public Job getJobById(UUID jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new JobNotFoundException(jobId.toString()));
    }

    // DELETE Job (chuyển trạng thái sang CLOSED)
    public void deleteJob(UUID jobId, UUID currentUserId) {
        Job job = getJobById(jobId);

        // 💡 1. Kiểm tra ủy quyền: Chỉ xóa/đóng job của mình
        if (!job.getRecruiterId().equals(currentUserId)) {
            throw new JobAccessDeniedException("You are not allowed to delete this job.");
        }

        // 💡 2. Soft Delete: Chuyển trạng thái sang CLOSED
        job.setStatus(Status.CLOSED);

        // Lưu thay đổi vào database
        Job updatedJob = jobRepository.save(job); // Lưu kết quả trả về vào biến nếu cần

        // 💡 3. Phát sự kiện để cập nhật trạng thái trong ES (Elasticsearch)
        // SỬA LỖI: Thay 'deletedJob' bằng 'updatedJob' hoặc 'job'
        eventPublisher.publishEvent(new JobIndexedEvent(updatedJob.getId()));
    }

    // UPDATE Job
    public Job updateJob(UUID jobId, JobCreateRequest request, UUID currentUserId) {
        validateSalary(request);

        Job job = getJobById(jobId);

        if (!job.getRecruiterId().equals(currentUserId)) {
            throw new JobAccessDeniedException("You are not allowed to update this job.");
        }

        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setLocation(request.getLocation());
        job.setSalaryMin(request.getSalaryMin());
        job.setSalaryMax(request.getSalaryMax());
        job.setSkills(request.getSkills());

        Job updatedJob = jobRepository.save(job);

        // 💡 Phát sự kiện để ES cập nhật document
        eventPublisher.publishEvent(new JobIndexedEvent(updatedJob.getId()));

        return updatedJob;
    }

    public boolean isOwnedByRecruiter(UUID jobId, UUID recruiterId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new JobNotFoundException("Job not found with ID: " + jobId));

        return job.getRecruiterId().equals(recruiterId);
    }
}
