package com.bookingtour.exception;

public class ResourceNotFoundException extends AppException {

    public ResourceNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ResourceNotFoundException(String resourceName) {
        super(ErrorCode.USER_NOT_FOUND, "Không tìm thấy: " + resourceName);
    }
}