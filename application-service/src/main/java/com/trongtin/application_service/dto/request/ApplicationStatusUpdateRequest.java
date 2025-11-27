package com.trongtin.application_service.dto.request;

import com.trongtin.application_service.entity.ApplicationStatus;
import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class ApplicationStatusUpdateRequest {
    @NotNull
    private ApplicationStatus status;
}