package com.example.employeemanagement.exception;

/**
 * Ngoại lệ ném ra khi không tìm thấy tài nguyên (trả về mã 404 NOT_FOUND).
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
