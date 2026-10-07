package com.example.employeemanagement.service;

import com.example.employeemanagement.entity.Employee;
import com.example.employeemanagement.exception.ResourceNotFoundException;
import com.example.employeemanagement.repository.EmployeeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * Service xử lý nghiệp vụ cho Employee.
 * Thiết kế đơn giản chỉ với 1 class duy nhất (không cần interface IEmployeeService),
 * giống hệt cách giáo trình viết NewsService ở Slot 15.
 * Rất dễ đọc, dễ hiểu và dễ bảo trì cho người mới bắt đầu.
 */
@Service
@Transactional
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    // Constructor Injection (chuẩn Spring Boot tốt nhất, dễ test với Mockito)
    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Transactional(readOnly = true)
    public Page<Employee> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Slice<Employee> getActiveEmployeesSlice(Pageable pageable) {
        return employeeRepository.findByActive(true, pageable);
    }

    @Transactional(readOnly = true)
    public List<Employee> getAllEmployeesList() {
        return employeeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Employee getEmployeeById(String empId) {
        return employeeRepository.findById(empId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + empId));
    }

    public Employee createEmployee(Employee employee) {
        return employeeRepository.save(employee);
    }

    public Employee updateEmployee(String empId, Employee employee) {
        Employee existing = getEmployeeById(empId);
        existing.setName(employee.getName());
        existing.setDesignation(employee.getDesignation());
        existing.setSalary(employee.getSalary());
        if (employee.getEmail() != null) {
            existing.setEmail(employee.getEmail());
        }
        if (employee.getDepartment() != null) {
            existing.setDepartment(employee.getDepartment());
        }
        existing.setActive(employee.isActive());
        return employeeRepository.save(existing);
    }

    public void deleteEmployee(String empId) {
        Employee existing = getEmployeeById(empId);
        employeeRepository.delete(existing);
    }

    @Transactional(readOnly = true)
    public List<Employee> getEmployeesByDesignation(String designation) {
        return employeeRepository.findByDesignation(designation);
    }
}
