package com.trongtin.application_service.entity;

import java.util.Set;

public enum ApplicationStatus {
    APPLIED,
    REVIEWING,
    INTERVIEW,
    HIRED,
    REJECTED;

    // Định nghĩa chuyển đổi trạng thái hợp lệ (Dùng cho Service)
    public Set<ApplicationStatus> getValidNextStatuses() {
        return switch (this) {
            case APPLIED -> Set.of(REVIEWING, INTERVIEW, REJECTED);
            case REVIEWING -> Set.of(INTERVIEW, REJECTED);
            case INTERVIEW -> Set.of(HIRED, REJECTED);
            case HIRED, REJECTED -> Set.of(); // Trạng thái cuối cùng
        };
    }
}