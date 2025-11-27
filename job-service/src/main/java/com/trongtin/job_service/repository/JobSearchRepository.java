package com.trongtin.job_service.repository;
import com.trongtin.job_service.entity.JobDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JobSearchRepository extends ElasticsearchRepository<JobDocument, UUID> {
    // Spring Data ES sẽ tự động cung cấp các phương thức CRUD cho JobDocument
    // Các phương thức tìm kiếm phức tạp sẽ được triển khai bằng Native Query (trong Service)
}