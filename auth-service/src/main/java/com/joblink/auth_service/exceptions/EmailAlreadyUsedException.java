package com.joblink.auth_service.exceptions;

public class EmailAlreadyUsedException extends Exception{
    public EmailAlreadyUsedException(String message) {
        super(message);
    }
}
