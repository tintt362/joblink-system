package com.joblink.auth_service.dto.response;

import lombok.*;

import java.time.Instant;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginResponse {

    private String accessToken;
    private String refreshToken;

    // Thay đổi từ Instant sang Long: Số giây hoặc millisecond còn lại
    private Long expiresIn;

    // Đổi tên trường từ 'typeToken' thành 'tokenType' theo convention
    private String tokenType;
}
