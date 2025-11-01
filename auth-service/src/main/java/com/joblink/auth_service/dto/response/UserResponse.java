package com.joblink.auth_service.dto.response;

import lombok.*;

import java.time.Instant;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {

    String id;
    String email;
    String enabled;
    Instant createdAt;
    Instant updatedAt;
}
