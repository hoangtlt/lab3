package com.example.employeemanagement.dto;

import com.example.employeemanagement.entity.Employee;

/**
 * DTO đại diện cho response version 2 (v2).
 * Mở rộng thêm các trường: email, department, active để phục vụ nhu cầu tiến hóa API (API evolution).
 */
public record EmployeeV2Response(
        String empId,
        String name,
        String designation,
        Double salary,
        String email,
        String department,
        boolean active
) {
    public static EmployeeV2Response from(Employee employee) {
        if (employee == null) return null;
        return new EmployeeV2Response(
                employee.getEmpId(),
                employee.getName(),
                employee.getDesignation(),
                employee.getSalary(),
                employee.getEmail(),
                employee.getDepartment(),
                employee.isActive()
        );
    }
}
