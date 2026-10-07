package com.example.employeemanagement.dto;

import org.springframework.data.domain.Page;
import java.util.List;

/**
 * DTO chuẩn hóa kết quả phân trang theo khuyến nghị của Spring Data.
 * Giúp contract JSON ổn định, không phụ thuộc vào lớp triển khai PageImpl của framework.
 *
 * @param <T> Kiểu dữ liệu phần tử trong danh sách (content)
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {
    public static <T> PageResponse<T> from(Page<T> p) {
        return new PageResponse<>(
                p.getContent(),
                p.getNumber(),
                p.getSize(),
                p.getTotalElements(),
                p.getTotalPages(),
                p.isLast()
        );
    }
}
