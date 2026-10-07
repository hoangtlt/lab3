package com.example.employeemanagement.exception;

/**
 * Ngoại lệ ném ra khi yêu cầu không hợp lệ (trả về mã 400 BAD_REQUEST),
 * ví dụ: sort field không nằm trong whitelist, page hoặc size âm không hợp lệ.
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
