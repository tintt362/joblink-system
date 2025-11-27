package com.joblink.auth_service.service;

import com.joblink.auth_service.dto.request.LoginRequestDto;
import com.joblink.auth_service.dto.request.RefreshTokenRequest;
import com.joblink.auth_service.dto.request.RegisterRequestDto;
import com.joblink.auth_service.dto.response.*;
import com.joblink.auth_service.entity.User;
import com.nimbusds.jose.JOSEException;

import java.text.ParseException;
import java.util.UUID;

public interface AuthService {

    UserResponse register(RegisterRequestDto request);
    /**
     * Authenticate with email+password; returns User on success.
     */
    LoginResponse authenticate(LoginRequestDto request);


    /**
     * Provision an OAuth user from provider attributes and return User entity.
     */
    User provisionOAuthUser(String email, String provider, Object attributes);

    String generateAccessToken(User user);
    /**
     * Consume refresh token and rotate -> returns pair(accessToken, refreshToken)
     * Implementation returns map with keys: accessToken, refreshToken
     */


    RefreshTokenResponse refreshToken(RefreshTokenRequest request) throws ParseException, JOSEException;

    MeResponse getMe(String userId, String role);
    AuthEmailResponse getEmailByUserId(UUID userId);
}
