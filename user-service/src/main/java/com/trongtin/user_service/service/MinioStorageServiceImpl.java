package com.trongtin.user_service.service;


import io.minio.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Service
public class MinioStorageServiceImpl implements FileStorageService {

    private final MinioClient minioClient;
    private final String bucketName; // Lưu ý: Loại bỏ @Value

    // Tiêm MinioClient (từ MinioConfig) và bucketName (từ @Value)
    // Spring sẽ tự động tiêm giá trị string cho bucketName
    public MinioStorageServiceImpl(MinioClient minioClient, @Value("${minio.bucketName}") String bucketName) {
        this.minioClient = minioClient;
        this.bucketName = bucketName; // Gán giá trị bucketName đã được tiêm
        // Thực hiện kiểm tra và tạo bucket khi service khởi tạo
        ensureBucketExists();
    }

    // Đảm bảo bucket tồn tại khi ứng dụng khởi động
    private void ensureBucketExists() {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                System.out.println("MinIO: Bucket '" + bucketName + "' created successfully.");
            } else {
                System.out.println("MinIO: Bucket '" + bucketName + "' already exists.");
            }
        } catch (Exception e) {
            // Lỗi này xảy ra khi không thể kết nối hoặc xác thực
            System.err.println("MinIO: Failed to check or create bucket: " + e.getMessage());
            // Tùy chọn: Throw RuntimeException nếu không có bucket thì service không chạy được
        }
    }

    /**
     * Tải lên file CV và trả về đường dẫn lưu trữ (storage_path)
     * Đường dẫn sẽ có dạng: {userId}/{uuid}-{originalFileName}
     */
    @Override
    public String uploadFile(MultipartFile file, UUID userId) throws Exception {
        // 1. Tạo tên file duy nhất trong bucket (Object Name/storagePath)
        // Loại bỏ các ký tự đặc biệt trong tên file gốc
        String cleanFileName = file.getOriginalFilename().replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
        String objectName = String.format("%s/%s-%s", userId.toString(), UUID.randomUUID(), cleanFileName);

        // 2. Thực hiện tải lên (PutObject)
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());
            System.out.println("✅ Uploaded: " + objectName);
            return objectName; // Trả về objectName làm storage_path
        } catch (Exception e) {
            System.err.println("❌ MinIO Upload Error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }



    }

    /**
     * Lấy file theo đường dẫn (storage_path)
     */
    @Override
    public InputStream downloadFile(String storagePath) throws Exception {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(storagePath)
                        .build());
    }

    /**
     * Xóa file
     */
    @Override
    public void deleteFile(String storagePath) throws Exception {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(storagePath)
                        .build());
    }
}