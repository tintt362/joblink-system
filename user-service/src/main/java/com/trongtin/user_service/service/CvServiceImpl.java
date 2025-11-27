package com.trongtin.user_service.service;

import com.trongtin.user_service.entity.UserCv;
import com.trongtin.user_service.exception.AccessDeniedException;
import com.trongtin.user_service.exception.ResourceNotFoundException;
import com.trongtin.user_service.repository.UserCvRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.util.UUID;

@Service
public class CvServiceImpl implements CvService{

    @Autowired
    private  UserCvRepository cvRepository;

    @Autowired
    private  FileStorageService fileStorageService; // MinIO/S3 Service

    public CvServiceImpl(UserCvRepository cvRepository, FileStorageService fileStorageService) {
        this.cvRepository = cvRepository;
        this.fileStorageService = fileStorageService;
    }

    // Luồng Tải Lên CV (POST /api/users/me/cv)
    @Override
    public UUID uploadCv(UUID userId, MultipartFile file) {
        // 1. Lưu trữ File Vật lý
        String storagePath = null;
        try {
            // Tên file và đường dẫn được tạo bên trong FileStorageService
            storagePath = fileStorageService.uploadFile(file, userId);

            // 2. Lưu Metadata vào CSDL (PostgreSQL)
            UserCv userCv = new UserCv();
            userCv.setUserId(userId);
            userCv.setFileName(file.getOriginalFilename());
            userCv.setStoragePath(storagePath);
            userCv.setMimeType(file.getContentType());

            UserCv savedCv = cvRepository.save(userCv);
            return savedCv.getId(); // Trả về ID CV vừa tạo (201 Created)

        } catch (Exception e) {
            // 3. Xử lý Rollback (quan trọng!)
            if (storagePath != null) {
                try {
                    fileStorageService.deleteFile(storagePath);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
            throw new RuntimeException("Failed to upload CV: " + e.getMessage());
        }
    }

    // Xóa CV (DELETE /api/users/me/cv/{id})
    @Override
    public void deleteCv(UUID cvId, UUID userId) {
        UserCv cv = cvRepository.findById(cvId)
                .orElseThrow(() -> new RuntimeException("CV not found"));

        // Kiểm tra ủy quyền: Đảm bảo người dùng xóa CV của chính họ
        if (!cv.getUserId().equals(userId)) {
            throw new AccessDeniedException("Cannot delete CV belonging to another user.");
        }

        // 1. Xóa file vật lý khỏi MinIO/S3
        try {
            fileStorageService.deleteFile(cv.getStoragePath());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // 2. Xóa metadata khỏi PostgreSQL
        cvRepository.delete(cv);
    }

    // Tải về CV (GET /api/users/me/cv/{id})
    @Override
    public InputStream downloadCv(UUID cvId, UUID viewerId, String viewerRole) {
        UserCv cv = cvRepository.findById(cvId)
                .orElseThrow(() ->  new ResourceNotFoundException("CV metadata not found for ID: " + cvId));

        // 1. Kiểm tra quyền truy cập (Giống luồng Get Profile)
        boolean canAccess = cv.getUserId().equals(viewerId) || "RECRUITER".equals(viewerRole) /* && TODO: Thêm logic nghiệp vụ */;

        if (!canAccess) {
            throw new RuntimeException("403 Forbidden: Access denied");
        }

        // 2. Tải file từ MinIO/S3
        try {
            return fileStorageService.downloadFile(cv.getStoragePath());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
