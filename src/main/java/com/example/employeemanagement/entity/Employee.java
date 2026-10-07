package com.example.employeemanagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;

/**
 * Entity đại diện cho nhân viên (Employee).
 * Thiết kế đơn giản, dễ hiểu theo chuẩn JPA cho người mới bắt đầu.
 */
@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @Column(name = "emp_id", length = 20, nullable = false)
    private String empId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "designation", nullable = false)
    private String designation;

    @Column(name = "salary")
    private Double salary;

    @Column(name = "email")
    private String email;

    @Column(name = "department")
    private String department;

    @Column(name = "active")
    private boolean active = true;

    // Constructor mặc định (bắt buộc bởi JPA)
    public Employee() {
    }

    // Constructor 4 tham số (dành cho API v1 và bài tập cơ bản)
    public Employee(String empId, String name, String designation, Double salary) {
        this.empId = empId;
        this.name = name;
        this.designation = designation;
        this.salary = salary;
        this.active = true;
    }

    // Constructor đầy đủ tham số (dành cho API v2 và test mở rộng)
    public Employee(String empId, String name, String designation, Double salary, String email, String department, boolean active) {
        this.empId = empId;
        this.name = name;
        this.designation = designation;
        this.salary = salary;
        this.email = email;
        this.department = department;
        this.active = active;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Employee employee = (Employee) o;
        return Objects.equals(empId, employee.empId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(empId);
    }

    @Override
    public String toString() {
        return "Employee{" +
                "empId='" + empId + '\'' +
                ", name='" + name + '\'' +
                ", designation='" + designation + '\'' +
                ", salary=" + salary +
                ", email='" + email + '\'' +
                ", department='" + department + '\'' +
                ", active=" + active +
                '}';
    }
}
