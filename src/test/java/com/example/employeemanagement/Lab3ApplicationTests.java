package com.example.employeemanagement;

import com.example.employeemanagement.service.EmployeeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Full Context Integration Smoke Test (@SpringBootTest) theo Slot 16 - Demo 3.
 * Mục tiêu: Xác nhận Spring Application Context khởi động hoàn hảo,
 * các Bean (Controller, Service, Repository, Database) được nối (wire) thông suốt.
 */
@SpringBootTest
class Lab3ApplicationTests {

    @Autowired
    private EmployeeService employeeService;

    @Test
    @DisplayName("Application Context tải thành công và Service đã được inject")
    void contextLoads() {
        assertThat(employeeService).isNotNull();
    }

    @Test
    @DisplayName("Service và Repository wiring hoạt động và DataSeeder đã nạp dữ liệu mẫu")
    void applicationContext_wiresServiceAndRepository() {
        var employees = employeeService.getAllEmployeesList();
        assertThat(employees).isNotEmpty();
        assertThat(employees.size()).isGreaterThanOrEqualTo(20);
    }
}
