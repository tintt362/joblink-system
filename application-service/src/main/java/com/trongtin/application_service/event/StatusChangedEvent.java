package com.trongtin.application_service.event;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusChangedEvent {

    // 1. Dữ liệu định danh chính
    private UUID applicationId;
    private UUID jobId;          // ID của công việc liên quan
    private UUID candidateId;    // ID của ứng viên (để lấy thông tin chi tiết)

    // 2. Thông tin về thay đổi trạng thái
    private String oldStatus;    // Trạng thái cũ (String representation of ApplicationStatus)
    private String newStatus;    // Trạng thái mới (String representation of ApplicationStatus)

    // 3. Dữ liệu đã làm giàu (Rich Data) cho Notification Service
    // Thông tin này phải được Application Service fetch từ User/Auth Service trước khi publish.
    private String candidateEmail;

    // (Tùy chọn) Thông tin người thực hiện
    private String recruiterActionBy; // UUID của Recruiter đã thực hiện hành động
}