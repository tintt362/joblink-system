package com.trongtin.gateway_service.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter implements WebFilter {

    private final JWTService jwtService;

    // Định nghĩa tên Header để truyền xuống Service (theo convention)
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_ROLE_HEADER = "X-User-Role";

    @Autowired
    public JwtAuthenticationFilter(JWTService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String token = getToken(exchange.getRequest());

        if (token != null && jwtService.isTokenValid(token)) {
            // 1. Trích xuất thông tin người dùng từ token
            String userId = jwtService.extractUserId(token);
            String role = jwtService.extractRole(token);
            String email = jwtService.extractEmail(token); // Sử dụng làm principal

            // 2. Cấu hình xác thực cho Security Context (tùy chọn)
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    email, null, List.of());

            // 3. Thêm Internal Headers vào request
            ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                    .header(USER_ID_HEADER, userId)
                    .header(USER_ROLE_HEADER, role)
                    .build();

            // 4. Tạo ServerWebExchange mới với request đã sửa đổi
            ServerWebExchange mutatedExchange = exchange.mutate().request(modifiedRequest).build();

            // 5. Tiếp tục chuỗi lọc với exchange mới và Authentication Context
            return chain.filter(mutatedExchange).contextWrite(
                    ReactiveSecurityContextHolder.withAuthentication(auth));
        }

        // Nếu token không hợp lệ hoặc không tồn tại, chỉ chuyển tiếp request gốc
        return chain.filter(exchange);
    }

    private String getToken(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}