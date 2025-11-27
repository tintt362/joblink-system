package com.trongtin.user_service.repository;

import com.trongtin.user_service.entity.UserCv;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface UserCvRepository extends JpaRepository<UserCv, UUID> {
    // Có thể thêm phương thức tìm kiếm theo user_id nếu cần
    // List<UserCv> findByUserId(UUID userId);
}