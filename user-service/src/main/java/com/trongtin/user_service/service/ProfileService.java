package com.trongtin.user_service.service;

import com.trongtin.user_service.dto.request.ProfileUpdateRequest;
import com.trongtin.user_service.dto.response.ProfileResponse;

import java.util.UUID;

public interface ProfileService {

    ProfileResponse updateProfile(UUID userId, ProfileUpdateRequest request);

    ProfileResponse getProfileById(UUID candidateId, UUID viewerId, String viewerRole);

}
