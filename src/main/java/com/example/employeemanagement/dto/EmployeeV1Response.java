package com.example.employeemanagement.dto;

import com.example.employeemanagement.entity.Employee;

/**
 * DTO đại diện cho response version 1 (v1).
 * Chỉ bao gồm 4 trường thông tin cơ bản: empId, name, designation, salary.
 */
public record EmployeeV1Response(
        String empId,
        String name,
        String designation,
        Double salary
) {
    public static EmployeeV1Response from(Employee employee) {
        if (employee == null) return null;
        return new EmployeeV1Response(
                employee.getEmpId(),
                employee.getName(),
                employee.getDesignation(),
                employee.getSalary()
        );
    }
}
