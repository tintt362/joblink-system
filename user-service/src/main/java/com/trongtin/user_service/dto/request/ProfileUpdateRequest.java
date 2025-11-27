package com.trongtin.user_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUpdateRequest {
    @NotBlank(message = "Full name is required")
    private String fullName;

    @Size(max = 15, message = "Phone number must be at most 15 characters")
    private String phoneNumber;

    private String location;
    private String summary;
    private String skills; // JSON String
    private Boolean isPublic;
}