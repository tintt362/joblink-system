package com.trongtin.user_service.controller;

import com.trongtin.user_service.dto.request.ProfileUpdateRequest;
import com.trongtin.user_service.dto.response.ProfileResponse;
import com.trongtin.user_service.dto.response.UserEmailResponse;
import com.trongtin.user_service.service.CvServiceImpl;
import com.trongtin.user_service.service.ProfileServiceImpl;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final ProfileServiceImpl profileServiceImpl;
    private final CvServiceImpl cvServiceImpl;
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USER_ROLE_HEADER = "X-User-Role";

    public UserController(ProfileServiceImpl profileServiceImpl, CvServiceImpl cvServiceImpl) {
        this.profileServiceImpl = profileServiceImpl;
        this.cvServiceImpl = cvServiceImpl;
    }

    // 1. Lấy hồ sơ (GET /api/users/{id}/profile)
    @GetMapping("/{id}/profile")
    public ResponseEntity<ProfileResponse> getProfile(
            @PathVariable("id") UUID candidateId,
            @RequestHeader(USER_ID_HEADER) UUID viewerId,
            @RequestHeader(USER_ROLE_HEADER) String viewerRole
    ) {
        ProfileResponse response = profileServiceImpl.getProfileById(candidateId, viewerId, viewerRole);
        return ResponseEntity.ok(response);
    }

    // 2. Cập nhật hồ sơ (PUT /api/users/me/profile)
    @PutMapping("/me/profile")
    public ResponseEntity<ProfileResponse> updateMyProfile(
            @RequestHeader(USER_ID_HEADER) UUID userId,
            @Valid @RequestBody ProfileUpdateRequest request
    ) {
        ProfileResponse response = profileServiceImpl.updateProfile(userId, request);
        return ResponseEntity.ok(response);
    }

    // 3. Tải lên CV (POST /api/users/me/cv)
    @PostMapping("/me/cv")
    public ResponseEntity<String> uploadCv(
            @RequestHeader(USER_ID_HEADER) UUID userId,
            @RequestParam("file") MultipartFile file
    ) throws Exception {
        UUID cvId = cvServiceImpl.uploadCv(userId, file);
        return new ResponseEntity<>("CV uploaded successfully with ID: " + cvId, HttpStatus.CREATED); // 201 Created
    }

    // 4. Tải về CV (GET /api/users/me/cv/{id})
    @GetMapping("/me/cv/{id}")
    public ResponseEntity<byte[]> downloadCv(
            @PathVariable("id") UUID cvId,
            @RequestHeader(USER_ID_HEADER) UUID viewerId,
            @RequestHeader(USER_ROLE_HEADER) String viewerRole
    ) throws Exception {
        // Lấy InputStream từ service
        InputStream is = cvServiceImpl.downloadCv(cvId, viewerId, viewerRole);

        // Cần lấy metadata file (mime type) từ UserCv entity để set Content-Type
        // Giả sử logic này được xử lý trong service và trả về một đối tượng chứa cả InputStream và Metadata

        // Xử lý response header (Tạm thời):
        HttpHeaders headers = new HttpHeaders();
        // headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + cv.getFileName() + "\"");
        // headers.add(HttpHeaders.CONTENT_TYPE, cv.getMimeType());

        // Chuyển InputStream sang byte[] và trả về
        byte[] data = is.readAllBytes();
        return ResponseEntity.ok()
                .headers(headers)
                .body(data);
    }

    // 5. Xóa CV (DELETE /api/users/me/cv/{id})
    @DeleteMapping("/me/cv/{id}")
    public ResponseEntity<Void> deleteCv(
            @PathVariable("id") UUID cvId,
            @RequestHeader(USER_ID_HEADER) UUID userId
    ) throws Exception {
        cvServiceImpl.deleteCv(cvId, userId);
        return ResponseEntity.noContent().build(); // 204 No Content
    }



}