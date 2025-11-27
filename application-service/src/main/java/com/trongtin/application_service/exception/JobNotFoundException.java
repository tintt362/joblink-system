package com.trongtin.application_service.exception;

public class JobNotFoundException extends AppException {
    public JobNotFoundException(String jobIdOrMessage) {
        super(ErrorCode.JOB_NOT_FOUND, "Job not found: " + jobIdOrMessage);
    }
}
