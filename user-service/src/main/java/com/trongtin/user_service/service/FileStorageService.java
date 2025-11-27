package com.trongtin.user_service.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

public interface FileStorageService {

    String uploadFile(MultipartFile file, UUID userId) throws Exception;

    InputStream downloadFile(String storagePath) throws Exception;

    void deleteFile(String storagePath) throws Exception;
}
