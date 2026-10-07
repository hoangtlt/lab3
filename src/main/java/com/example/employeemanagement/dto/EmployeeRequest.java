package com.example.employeemanagement.dto;

import com.example.employeemanagement.entity.Employee;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Request body DTO cho các thao tác Create và Update.
 * Tích hợp Bean Validation (@NotBlank, @PositiveOrZero, @Email) theo yêu cầu Slot 16.
 */
public class EmployeeRequest {

    @NotBlank(message = "empId cannot be blank")
    private String empId;

    @NotBlank(message = "name cannot be blank")
    private String name;

    @NotBlank(message = "designation cannot be blank")
    private String designation;

    @NotNull(message = "salary is required")
    @PositiveOrZero(message = "salary must be greater than or equal to 0")
    private Double salary;

    @Email(message = "email format is invalid")
    private String email;

    private String department;

    private Boolean active = true;

    public EmployeeRequest() {
    }

    public EmployeeRequest(String empId, String name, String designation, Double salary) {
        this.empId = empId;
        this.name = name;
        this.designation = designation;
        this.salary = salary;
        this.active = true;
    }

    public EmployeeRequest(String empId, String name, String designation, Double salary, String email, String department, Boolean active) {
        this.empId = empId;
        this.name = name;
        this.designation = designation;
        this.salary = salary;
        this.email = email;
        this.department = department;
        this.active = active != null ? active : true;
    }

    public Employee toEntity() {
        return new Employee(
                this.empId,
                this.name,
                this.designation,
                this.salary,
                this.email,
                this.department,
                this.active != null ? this.active : true
        );
    }

    // Getters and Setters
    public String getEmpId() {
        return empId;
    }

    public void setEmpId(String empId) {
        this.empId = empId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public Double getSalary() {
        return salary;
    }

    public void setSalary(Double salary) {
        this.salary = salary;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
