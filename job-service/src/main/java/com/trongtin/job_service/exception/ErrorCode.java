package com.trongtin.job_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(2000, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),

    // Job domain (2001 - 2999)
    JOB_NOT_FOUND(2001, "Job not found", HttpStatus.NOT_FOUND),
    INVALID_SALARY_RANGE(2002, "Minimum salary cannot exceed maximum salary", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED_JOB_ACCESS(2003, "You are not authorized to perform this action on the job", HttpStatus.FORBIDDEN),
    INVALID_REQUEST(2004, "Invalid request", HttpStatus.BAD_REQUEST),

    // Auth / header
    HEADER_INVALID(3001, "Authentication header is missing or invalid", HttpStatus.UNAUTHORIZED),
    UNAUTHENTICATED(3002, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(3003, "You do not have permission", HttpStatus.FORBIDDEN);


    ;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}
