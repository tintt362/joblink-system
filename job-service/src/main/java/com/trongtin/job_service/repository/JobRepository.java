package com.trongtin.job_service.repository;

import com.trongtin.job_service.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JobRepository extends JpaRepository<Job, UUID> {
    // Thêm các phương thức tìm kiếm custom nếu cần, ví dụ:
    // List<Job> findByRecruiterId(UUID recruiterId);
}