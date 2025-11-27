package com.trongtin.user_service.service;


import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

public interface CvService {

    UUID uploadCv(UUID userId, MultipartFile file);

    void deleteCv(UUID cvId, UUID userId);

    InputStream downloadCv(UUID cvId, UUID viewerId, String viewerRole);
}
