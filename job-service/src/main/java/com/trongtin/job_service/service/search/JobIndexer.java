package com.trongtin.job_service.service.search;



import com.trongtin.job_service.entity.Job;
import com.trongtin.job_service.entity.JobDocument;
import com.trongtin.job_service.entity.Status;
import com.trongtin.job_service.event.JobIndexedEvent;
import com.trongtin.job_service.repository.JobRepository;
import com.trongtin.job_service.repository.JobSearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class JobIndexer {

    private final JobSearchRepository jobSearchRepository;
    private final JobRepository jobRepository; // Cần truy vấn lại DB

    @Autowired
    public JobIndexer(JobSearchRepository jobSearchRepository, JobRepository jobRepository) {
        this.jobSearchRepository = jobSearchRepository;
        this.jobRepository = jobRepository;
    }

    /**
     * Lắng nghe sự kiện và thực hiện indexing bất đồng bộ.
     */
    @Async
    @EventListener
    public void handleJobIndexing(JobIndexedEvent event) {
        jobRepository.findById(event.jobId())
                .ifPresentOrElse(job -> {
                    if (job.getStatus() == Status.CLOSED) {
                        // Nếu là CLOSED → xóa khỏi Elasticsearch
                        jobSearchRepository.deleteById(job.getId());
                        System.out.println("Deleted CLOSED Job ID from ES: " + job.getId());
                    } else {
                        // Nếu còn active → cập nhật hoặc thêm mới
                        JobDocument document = mapToDocument(job);
                        jobSearchRepository.save(document);
                        System.out.println("Indexed Job ID: " + job.getId());
                    }
                }, () -> {
                    // Nếu không tìm thấy trong DB → xóa khỏi ES
                    jobSearchRepository.deleteById(event.jobId());
                    System.out.println("Deleted Job ID from ES (no longer in DB): " + event.jobId());
                });
    }

    // Mapper từ Entity sang Document
    private JobDocument mapToDocument(Job job) {
        return JobDocument.builder()
                .id(job.getId())
                .title(job.getTitle())
                .description(job.getDescription())
                .location(job.getLocation())
                .skills(job.getSkills())
                .salaryMin(job.getSalaryMin())
                .salaryMax(job.getSalaryMax())
                .build();
    }
}