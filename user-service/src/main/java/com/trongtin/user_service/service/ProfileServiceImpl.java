package com.trongtin.user_service.service;

import com.trongtin.user_service.dto.request.ProfileUpdateRequest;
import com.trongtin.user_service.dto.response.ProfileResponse;
import com.trongtin.user_service.entity.Profile;
import com.trongtin.user_service.exception.AccessDeniedException;
import com.trongtin.user_service.exception.ResourceNotFoundException;
import com.trongtin.user_service.repository.ProfileRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProfileServiceImpl implements  ProfileService{

    @Autowired
    private ProfileRepository profileRepository;
    // ... Khác (ObjectMapper, v.v.)

    public ProfileServiceImpl(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    // Luồng Cập Nhật Hồ Sơ Cá Nhân (PUT /api/users/me/profile)
    @Override
    public ProfileResponse updateProfile(UUID userId, ProfileUpdateRequest request) {
        Optional<Profile> existingProfile = profileRepository.findByUserId(userId);
        Profile profile;

        if (existingProfile.isPresent()) {
            profile = existingProfile.get();
        } else {
            // Logic Upsert: Nếu hồ sơ chưa tồn tại, tạo mới
            profile = new Profile();
            profile.setUserId(userId);
        }

        // Cập nhật các trường dữ liệu từ request
        BeanUtils.copyProperties(request, profile);

        // Lưu vào CSDL (PostgreSQL)
        Profile savedProfile = profileRepository.save(profile);

        ProfileResponse response = new ProfileResponse();
        BeanUtils.copyProperties(savedProfile, response);
        response.setUserId(savedProfile.getUserId().toString());
        return response;
    }

    // Luồng Lấy Hồ Sơ Công Khai (GET /api/users/{id}/profile)
    @Override
    public ProfileResponse getProfileById(UUID candidateId, UUID viewerId, String viewerRole) {
        Profile profile = profileRepository.findByUserId(candidateId)
                .orElseThrow(() ->  new ResourceNotFoundException("Profile not found for ID: " + candidateId)); // Xử lý 404

        // 1. Kiểm tra quyền xem của chính người dùng
        if (candidateId.equals(viewerId)) {
            return mapToResponse(profile);
        }

        // 2. Kiểm tra quyền xem của Recruiter/Public
        if (profile.getIsPublic()) {
            return mapToResponse(profile);
        }

        // 3. Logic phức tạp cho Recruiter (Nếu không Public, Recruiter vẫn có thể xem
        // nếu có mối quan hệ nghiệp vụ, ví dụ: đã nộp đơn)
        if ("RECRUITER".equals(viewerRole)) {
            // TODO: Triển khai kiểm tra nghiệp vụ phức tạp
            // Gọi REST API tới Application Service để xác minh (ví dụ: isCandidateApplied(viewerId, candidateId))
            // Nếu hợp lệ, return mapToResponse(profile);
        }

        // Nếu không thỏa mãn bất kỳ điều kiện nào
        throw new AccessDeniedException("Access denied to view this profile.");
    }


    // Helper method
    private ProfileResponse mapToResponse(Profile profile) {
        ProfileResponse response = new ProfileResponse();
        BeanUtils.copyProperties(profile, response);
        response.setUserId(profile.getUserId().toString());
        return response;
    }
}