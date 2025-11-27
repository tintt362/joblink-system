package com.trongtin.user_service.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO trả về email của người dùng.
 * Được sử dụng bởi các Service khác (ví dụ: Application Service) để làm giàu dữ liệu Event.
 * Định dạng này khớp với DTO mà UserServiceProxy trong Application Service mong đợi.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEmailResponse {
    private UUID userId;
    private String email;
}