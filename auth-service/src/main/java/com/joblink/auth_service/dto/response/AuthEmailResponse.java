package com.joblink.auth_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

/**
 * DTO trả về email của người dùng.
 * Được sử dụng cho các cuộc gọi nội bộ giữa các microservice (ví dụ: User Service).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthEmailResponse {
    private UUID userId;
    private String email;
}