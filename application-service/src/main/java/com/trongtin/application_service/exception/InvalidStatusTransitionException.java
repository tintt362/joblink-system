package com.trongtin.application_service.exception;


import com.trongtin.application_service.entity.ApplicationStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidStatusTransitionException extends RuntimeException {

    public InvalidStatusTransitionException(ApplicationStatus currentStatus, ApplicationStatus newStatus) {
        super(String.format("Invalid status transition: Cannot move from '%s' to '%s'.", currentStatus, newStatus));
    }
}