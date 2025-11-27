package com.trongtin.application_service.mapper;


import com.trongtin.application_service.dto.request.SubmitApplicationRequest;
import com.trongtin.application_service.dto.response.ApplicationResponse;
import com.trongtin.application_service.entity.Application;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Component quản lý việc ánh xạ thủ công (manual mapping)
 * giữa DTOs và Application Entity.
 */
@Component
public class ApplicationMapper {

    /**
     * Chuyển đổi Application Entity sang ApplicationResponse DTO.
     * Dùng để trả về thông tin đơn ứng tuyển cho Recruiter/Candidate.
     *
     * @param application Application Entity
     * @return ApplicationResponse DTO
     */
    public ApplicationResponse toResponse(Application application) {
        if (application == null) {
            return null;
        }

        ApplicationResponse response = new ApplicationResponse();

        // Ánh xạ các trường
        response.setId(application.getId());
        response.setJobId(application.getJobId());
        response.setCandidateId(application.getCandidateId());
        response.setStatus(application.getStatus());
        response.setCoverLetter(application.getCoverLetter());
        response.setCvId(application.getCvId());
        response.setHistory(application.getHistory());
        response.setCreatedAt(application.getCreatedAt());
        response.setUpdatedAt(application.getUpdatedAt());

        return response;
    }
    public List<ApplicationResponse> toResponseList(List<Application> applications) {
        if (applications == null) {
            return List.of(); // Trả về danh sách rỗng nếu input là null
        }

        return applications.stream()
                .map(this::toResponse) // Gọi lại phương thức ánh xạ đơn lẻ
                .collect(java.util.stream.Collectors.toList());
    }
    // --- Phương thức tạo mới (Creation) ---

    /**
     * Chuyển đổi SubmitApplicationRequest DTO sang Application Entity mới.
     * Lưu ý: Phương thức này chỉ ánh xạ các trường từ Request.
     * Các trường metadata (id, status, timestamps) sẽ được thiết lập
     * trong Application Service (lớp nghiệp vụ).
     *
     * @param request DTO nộp đơn ứng tuyển
     * @param candidateId ID của ứng viên (lấy từ Header)
     * @return Application Entity mới
     */
    public Application toNewEntity(SubmitApplicationRequest request, java.util.UUID candidateId) {
        if (request == null) {
            return null;
        }

        Application application = new Application();

        // Ánh xạ các trường từ Request
        application.setJobId(request.getJobId());
        application.setCoverLetter(request.getCoverLetter());
        application.setCvId(request.getCvId());

        // Thiết lập trường lấy từ Header/Context
        application.setCandidateId(candidateId);

        // Các trường khác (id, status, history, timestamps)
        // sẽ được thiết lập trong ApplicationService.

        return application;
    }
}