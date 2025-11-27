package com.trongtin.application_service.exception;

public class JobAccessDeniedException extends AppException {
    public JobAccessDeniedException(String message) {
        super(ErrorCode.UNAUTHORIZED_JOB_ACCESS, message);
    }

    public JobAccessDeniedException() {
        super(ErrorCode.UNAUTHORIZED_JOB_ACCESS);
    }
}
