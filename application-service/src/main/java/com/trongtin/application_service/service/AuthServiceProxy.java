package com.trongtin.application_service.service;


import com.trongtin.application_service.exception.ResourceNotFoundException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Service Proxy để giao tiếp đồng bộ với Auth Service.
 * Dùng để lấy thông tin cơ bản của người dùng (như Email) phục vụ nghiệp vụ.
 */
@Service
@Slf4j
public class AuthServiceProxy {

    private final WebClient webClient;

    // Cần định nghĩa biến cấu hình 'auth.service.url' trong application.yml
    public AuthServiceProxy(@Value("${auth.service.url}") String authServiceUrl) {
        this.webClient = WebClient.builder().baseUrl(authServiceUrl).build();
    }

    /**
     * Lấy email của người dùng từ Auth Service.
     *
     * @param userId ID của người dùng.
     * @return Email của người dùng.
     * @throws ResourceNotFoundException nếu người dùng không tồn tại.
     */
    public String getEmailByUserId(UUID userId) {
        log.info("Fetching email from Auth Service for User ID: {}", userId);
        try {
            // GIẢ ĐỊNH: Auth Service có endpoint: /api/auth/users/{userId}/email
            AuthEmailDTO response = webClient.get()
                    .uri("/api/auth/users/{userId}/email", userId)
                    .retrieve()
                    // Xử lý lỗi 4xx (ví dụ 404 Not Found)
                    .onStatus(HttpStatusCode::is4xxClientError,
                            resp -> Mono.error(new ResourceNotFoundException("User not found in Auth Service with ID: " + userId)))
                    .bodyToMono(AuthEmailDTO.class)
                    .block(); // Chặn luồng (Synchronous)

            if (response == null || response.getEmail() == null) {
                throw new ResourceNotFoundException("Auth Service returned null response for ID: " + userId);
            }
            return response.getEmail();

        } catch (ResourceNotFoundException e) {
            log.error("Auth Service error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Failed to call Auth Service for user ID {}: {}", userId, e.getMessage());
            throw new RuntimeException("Service Unavailable: Could not fetch email from Auth Service.", e);
        }
    }

    /**
     * DTO nội bộ để ánh xạ response từ Auth Service.
     * Giả định response JSON có dạng: {"email": "user@example.com"}
     */
    @Data
    private static class AuthEmailDTO {
        private String email;
    }
}