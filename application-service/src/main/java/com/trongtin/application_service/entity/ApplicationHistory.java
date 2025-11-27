package com.trongtin.application_service.entity;

import lombok.*;

import java.time.Instant;

@Data // Lombok
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ApplicationHistory {
    private ApplicationStatus status;
    private Instant date;
    private String updatedBy; // ID của Recruiter
}