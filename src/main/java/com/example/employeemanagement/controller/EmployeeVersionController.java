package com.example.employeemanagement.controller;

import com.example.employeemanagement.dto.EmployeeV1Response;
import com.example.employeemanagement.dto.EmployeeV2Response;
import com.example.employeemanagement.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller minh họa chiến lược Path / URI Versioning:
 * - /api/v1/employees: Trả về contract v1 (4 trường: empId, name, designation, salary)
 * - /api/v2/employees: Trả về contract v2 (mở rộng thêm: email, department, active)
 */
@RestController
@RequestMapping("/api")
@Tag(name = "URI Versioning API", description = "Minh họa chiến lược Path/URI Versioning (/api/v1 và /api/v2)")
public class EmployeeVersionController {

    private final EmployeeService employeeService;

    public EmployeeVersionController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @Operation(summary = "Lấy danh sách nhân viên theo API v1 (Path Versioning)")
    @GetMapping("/v1/employees")
    public ResponseEntity<List<EmployeeV1Response>> getEmployeesV1() {
        List<EmployeeV1Response> result = employeeService.getAllEmployeesList().stream()
                .map(EmployeeV1Response::from)
                .toList();
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Lấy danh sách nhân viên theo API v2 (Path Versioning với fields mở rộng)")
    @GetMapping("/v2/employees")
    public ResponseEntity<List<EmployeeV2Response>> getEmployeesV2() {
        List<EmployeeV2Response> result = employeeService.getAllEmployeesList().stream()
                .map(EmployeeV2Response::from)
                .toList();
        return ResponseEntity.ok(result);
    }
}
