package com.trongtin.application_service.dto.request;

import lombok.Data;
import java.util.UUID;
import jakarta.validation.constraints.NotNull;

@Data
public class SubmitApplicationRequest {
    @NotNull
    private UUID jobId;
    private String coverLetter;
    @NotNull
    private UUID cvId;
}