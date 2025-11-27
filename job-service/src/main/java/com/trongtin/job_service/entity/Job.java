package com.trongtin.job_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "jobs")
@Setter
@Getter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    private UUID recruiterId; // recruiter_id (UUID)

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String location;

    private Integer salaryMin;

    private Integer salaryMax;

    // Sử dụng @JdbcTypeCode(SqlTypes.JSON) cho kiểu dữ liệu JSONB trong PostgreSQL
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> skills;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Status status = Status.DRAFT;

    private Instant createdAt = Instant.now();

    private Instant expiresAt;

    // Constructors, Getters, Setters, etc.
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}