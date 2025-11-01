package com.joblink.auth_service.dto.response;

import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccessTokenResponse {

    private String token;
    private String expiryTime;
}
