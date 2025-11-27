package com.trongtin.job_service.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Getter
@Setter
public class JobCreateRequest {

    @NotBlank(message = "Title must not be empty")
    @Size(min = 10, max = 255, message = "Title must be between 10 and 255 characters")
    private String title;

    @NotBlank(message = "Description must not be empty")
    private String description;

    @NotBlank(message = "Location must not be empty")
    private String location;

    @NotNull(message = "Minimum salary is required")
    @Min(value = 1000, message = "Minimum salary must be at least 1000")
    private Integer salaryMin;

    @NotNull(message = "Maximum salary is required")
    @Min(value = 1000, message = "Maximum salary must be at least 1000")
    private Integer salaryMax;

    @NotNull(message = "Skills list must not be null")
    @Size(min = 1, message = "At least one skill is required")
    private List<String> skills;

    // Getters, Setters, Constructors
}