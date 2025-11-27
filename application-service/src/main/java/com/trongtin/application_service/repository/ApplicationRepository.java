package com.trongtin.application_service.repository;

import com.trongtin.application_service.entity.Application;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApplicationRepository extends MongoRepository<Application, UUID> {

    // Luồng Xem Danh sách (Recruiter)
    List<Application> findByJobId(UUID jobId);

    // Luồng Xem Danh sách (Candidate)
    List<Application> findByCandidateId(UUID candidateId);

    // Kiểm tra đã nộp đơn chưa (Luồng POST)
    Optional<Application> findByJobIdAndCandidateId(UUID jobId, UUID candidateId);
}