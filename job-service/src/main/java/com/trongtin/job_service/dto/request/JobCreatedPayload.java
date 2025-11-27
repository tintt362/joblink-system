package com.trongtin.job_service.dto.request;

import java.io.Serializable;
import java.util.UUID;

// Sử dụng record cho dữ liệu bất biến (immutable data)
public record JobCreatedPayload(
        UUID jobId,
        String title,
        UUID recruiterId
) implements Serializable {
    // Lombok @Data không cần thiết cho record .
}