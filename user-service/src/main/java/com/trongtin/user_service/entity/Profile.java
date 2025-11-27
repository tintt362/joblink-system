package com.trongtin.user_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Type;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "profiles")
@Data
public class Profile {
    // Primary Key & Foreign Key (từ Auth Service)
    @Id
    @Column(name = "user_id", updatable = false, nullable = false)
    private UUID userId;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "phone_number", length = 15)
    private String phoneNumber;

    @Column(name = "location")
    private String location;

    @Column(name = "summary")
    private String summary;

    // Sử dụng org.hibernate.annotations.Type cho JSONB
    @Type(io.hypersistence.utils.hibernate.type.json.JsonType.class)
    @Column(name = "skills", columnDefinition = "jsonb")
    private String skills; // Lưu trữ dưới dạng JSON String

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}