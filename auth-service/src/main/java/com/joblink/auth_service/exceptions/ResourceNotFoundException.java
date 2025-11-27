package com.joblink.auth_service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception signaling that a requested resource (User, Job, etc.) could not be found.
 * Thrown in service layer and automatically mapped to HTTP Status 404 NOT FOUND
 * by Spring Boot when used with @ResponseStatus annotation.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}