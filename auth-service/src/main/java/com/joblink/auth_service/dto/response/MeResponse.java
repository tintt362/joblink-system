package com.joblink.auth_service.dto.response;

import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MeResponse {
    private String id;
    private String email;
    private String role;
}