package com.joblink.auth_service.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Uncategorized error", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "User existed", HttpStatus.BAD_REQUEST),
    ROLE_NOT_FOUND(1002, "Role not found", HttpStatus.BAD_REQUEST),
    POST_NOT_FOUND(1002, "Role not found", HttpStatus.BAD_REQUEST),
    COMMENT_NOT_FOUND(1002, "Comment not found", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003, "Username must be at least {min} characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "Password must be at least {min} characters", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "User not existed", HttpStatus.NOT_FOUND),
    POST_NOT_EXISTED(1005, "Post not existed", HttpStatus.NOT_FOUND),
    POST_TITLE_NOT_EXISTED(1005, "Post not contains title", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),
    INVALID_DOB(1008, "Your age must be at least {min}", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL(1009, "Invalid email address", HttpStatus.BAD_REQUEST),
    EMAIL_IS_REQUIRED(1009, "Email is required", HttpStatus.BAD_REQUEST),
    EMAIL_EXISTS(1010, "Email already exists", HttpStatus.BAD_REQUEST),
    CREDENTIALS_NOT_VALID(1010, "Invalid login information", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED_ACTION(1011,  "You can only update your own posts.", HttpStatus.FORBIDDEN),
    HEADER_INVALID(1011,  "Token xác thực bị thiếu hoặc sai định dạng.", HttpStatus.UNAUTHORIZED),

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
