package com.joblink.auth_service.controler;

import com.joblink.auth_service.dto.request.LoginRequestDto;
import com.joblink.auth_service.dto.request.RefreshTokenRequest;
import com.joblink.auth_service.dto.request.RegisterRequestDto;
import com.joblink.auth_service.dto.response.*;
import com.joblink.auth_service.service.AuthService;
import com.nimbusds.jose.JOSEException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.text.ParseException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ApiResponse<UserResponse> register(@Valid @RequestBody RegisterRequestDto req) {
        return ApiResponse.<UserResponse>builder()
                .result(authService.register(req))
                .message("Create User Successfully")
                .build();

    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequestDto dto) {
        return ApiResponse.<LoginResponse>builder()
                .result(authService.authenticate(dto))
                .message("Login Success")
                .build();
    }

    @PostMapping("/refresh")
    public ApiResponse<RefreshTokenResponse> refreshToken(@RequestBody RefreshTokenRequest dto) throws ParseException, JOSEException {
        return ApiResponse.<RefreshTokenResponse>builder()
                .result(authService.refreshToken(dto))
                .message("Refresh Token Successfully")
                .build();
    }
    @GetMapping("/me")
    public ApiResponse<MeResponse> getAuthenticatedUser(
            // 💡 Tối ưu: Lấy thông tin đã xác thực từ Security Context
            @AuthenticationPrincipal Jwt jwt
    ) {
        // Spring Security đã xác thực token, chỉ cần lấy claims
        String userId = jwt.getSubject();
        String role = jwt.getClaimAsString("Role"); // Đã fix tên claim

        // Auth Service giờ chỉ cần lấy email từ DB, không cần verify lại token
        return ApiResponse.<MeResponse>builder()
                .result(authService.getMe(userId, role))
                .message("Get Me Successfully").build();
    }
}
