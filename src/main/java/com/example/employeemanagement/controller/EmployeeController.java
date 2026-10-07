package com.example.employeemanagement.controller;

import com.example.employeemanagement.dto.EmployeeRequest;
import com.example.employeemanagement.dto.EmployeeV1Response;
import com.example.employeemanagement.dto.EmployeeV2Response;
import com.example.employeemanagement.entity.Employee;
import com.example.employeemanagement.exception.BadRequestException;
import com.example.employeemanagement.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * Controller chính cho Employee:
 * - CRUD chuẩn RESTful
 * - Phân trang (Paging) & Sắp xếp (Sorting) có Guardrails (giới hạn size, whitelist sort fields)
 * - Minh họa Slice (không dùng count query)
 * - Minh họa 3 chiến lược versioning: Query Param, Custom Header, Media Type
 */
@RestController
@RequestMapping("/api/employees")
@Tag(name = "Employee API", description = "Các API quản lý nhân viên với phân trang, sắp xếp và versioning")
public class EmployeeController {

    private final EmployeeService employeeService;

    // Whitelist danh sách thuộc tính được phép sắp xếp (Slot 15)
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "empId", "name", "designation", "salary", "email", "department", "active"
    );

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    // ==========================================
    // 1. PAGING & SORTING ENDPOINT
    // ==========================================

    @Operation(summary = "Lấy danh sách nhân viên có phân trang và sắp xếp")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<Employee>> getAllEmployees(
            @Parameter(description = "Số trang (bắt đầu từ 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số bản ghi mỗi trang (1..100)")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Trường cần sắp xếp (empId, name, designation, salary...)")
            @RequestParam(defaultValue = "empId") String sortBy,
            @Parameter(description = "Hướng sắp xếp: asc hoặc desc")
            @RequestParam(defaultValue = "asc") String direction
    ) {
        // Page size & index guardrail (Slot 15)
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);

        // Sort field whitelist guardrail (Slot 15)
        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new BadRequestException("Unsupported sort field: '" + sortBy + "'. Allowed fields: " + ALLOWED_SORT_FIELDS);
        }

        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        // Thêm empId làm secondary sort để đảm bảo stable sort
        Sort sort = Sort.by(sortDirection, sortBy).and(Sort.by(Sort.Direction.ASC, "empId"));

        Pageable pageable = PageRequest.of(safePage, safeSize, sort);
        Page<Employee> result = employeeService.getAllEmployees(pageable);

        return ResponseEntity.ok(result);
    }

    // ==========================================
    // 2. SLICE ENDPOINT (Infinite Scroll / Load More)
    // ==========================================

    @Operation(summary = "Lấy danh sách nhân viên bằng Slice (không count query, thích hợp Infinite Scroll)")
    @GetMapping(value = "/slice", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Slice<Employee>> getActiveEmployeesSlice(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "empId") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);

        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new BadRequestException("Unsupported sort field: '" + sortBy + "'");
        }

        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(sortDirection, sortBy));

        Slice<Employee> slice = employeeService.getActiveEmployeesSlice(pageable);
        return ResponseEntity.ok(slice);
    }

    // ==========================================
    // 3. CRUD ENDPOINTS
    // ==========================================

    @Operation(summary = "Xem chi tiết nhân viên theo empId")
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Employee> getEmployeeById(@PathVariable("id") String id) {
        Employee employee = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(employee);
    }

    @Operation(summary = "Thêm mới một nhân viên")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Employee> createEmployee(@Valid @RequestBody EmployeeRequest request) {
        Employee created = employeeService.createEmployee(request.toEntity());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Cập nhật thông tin nhân viên")
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Employee> updateEmployee(@PathVariable("id") String id, @Valid @RequestBody EmployeeRequest request) {
        Employee updated = employeeService.updateEmployee(id, request.toEntity());
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Xóa nhân viên theo empId")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable("id") String id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // 4. VERSIONING STRATEGY: QUERY PARAMETER (?version=1, ?version=2)
    // ==========================================

    @Operation(summary = "Query Parameter Versioning: v1")
    @GetMapping(params = "version=1", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<EmployeeV1Response>> getByQueryV1() {
        List<EmployeeV1Response> list = employeeService.getAllEmployeesList().stream()
                .map(EmployeeV1Response::from)
                .toList();
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Query Parameter Versioning: v2")
    @GetMapping(params = "version=2", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<EmployeeV2Response>> getByQueryV2() {
        List<EmployeeV2Response> list = employeeService.getAllEmployeesList().stream()
                .map(EmployeeV2Response::from)
                .toList();
        return ResponseEntity.ok(list);
    }

    // ==========================================
    // 5. VERSIONING STRATEGY: CUSTOM HEADER (X-API-Version: 1, 2)
    // ==========================================

    @Operation(summary = "Custom Header Versioning: X-API-Version=1")
    @GetMapping(headers = "X-API-Version=1", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<EmployeeV1Response>> getByHeaderV1() {
        List<EmployeeV1Response> list = employeeService.getAllEmployeesList().stream()
                .map(EmployeeV1Response::from)
                .toList();
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Custom Header Versioning: X-API-Version=2")
    @GetMapping(headers = "X-API-Version=2", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<EmployeeV2Response>> getByHeaderV2() {
        List<EmployeeV2Response> list = employeeService.getAllEmployeesList().stream()
                .map(EmployeeV2Response::from)
                .toList();
        return ResponseEntity.ok(list);
    }

    // ==========================================
    // 6. VERSIONING STRATEGY: MEDIA TYPE / ACCEPT HEADER
    // ==========================================

    @Operation(summary = "Media Type Versioning: Accept=application/vnd.company.v1+json")
    @GetMapping(produces = "application/vnd.company.v1+json")
    public ResponseEntity<List<EmployeeV1Response>> getByMediaTypeV1() {
        List<EmployeeV1Response> list = employeeService.getAllEmployeesList().stream()
                .map(EmployeeV1Response::from)
                .toList();
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Media Type Versioning: Accept=application/vnd.company.v2+json")
    @GetMapping(produces = "application/vnd.company.v2+json")
    public ResponseEntity<List<EmployeeV2Response>> getByMediaTypeV2() {
        List<EmployeeV2Response> list = employeeService.getAllEmployeesList().stream()
                .map(EmployeeV2Response::from)
                .toList();
        return ResponseEntity.ok(list);
    }
}
