package com.trongtin.job_service.event;

import java.util.UUID;

public record JobIndexedEvent(UUID jobId) {
    // Record đơn giản chỉ chứa ID của job vừa được CRUD.
}