package com.example.employeemanagement.controller;

import com.example.employeemanagement.dto.EmployeeRequest;
import com.example.employeemanagement.entity.Employee;
import com.example.employeemanagement.exception.ResourceNotFoundException;
import com.example.employeemanagement.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller Slice Test sử dụng @WebMvcTest và MockMvc.
 * Mục tiêu: Kiểm tra routing, binding, JSON serialization/deserialization, HTTP status codes,
 * và Exception handling của EmployeeController mà không cần khởi động toàn bộ server hay database.
 *
 * Ghi chú Spring Boot 3.4+:
 * Sử dụng @MockitoBean thay thế cho @MockBean đã deprecated.
 */
@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EmployeeService employeeService;

    // ==========================================
    // 1. TEST PAGING & SORTING (Slot 16 - Mục 9)
    // ==========================================

    @Test
    @DisplayName("GET /api/employees có phân trang -> trả về 200 OK và cấu trúc Page")
    void getAll_withPaging_returnsPageContent() throws Exception {
        // Arrange
        var content = List.of(
                new Employee("E001", "An", "Developer", 15_000_000.0),
                new Employee("E002", "Binh", "Tester", 12_000_000.0)
        );
        Page<Employee> page = new PageImpl<>(content, PageRequest.of(0, 2), 5);
        when(employeeService.getAllEmployees(any(Pageable.class))).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/employees")
                        .param("page", "0")
                        .param("size", "2")
                        .param("sortBy", "name")
                        .param("direction", "asc"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.content[0].empId").value("E001"));
    }

    @Test
    @DisplayName("GET /api/employees với sort field không hợp lệ -> trả về 400 Bad Request")
    void getAll_invalidSortField_returns400() throws Exception {
        // Act & Assert (Guardrail test)
        mockMvc.perform(get("/api/employees")
                        .param("sortBy", "unsupported_column"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    // ==========================================
    // 2. TEST SLICE (Slot 15 & 16)
    // ==========================================

    @Test
    @DisplayName("GET /api/employees/slice -> trả về 200 OK và Slice (có hasNext, không totalElements)")
    void getActiveSlice_returnsSliceContent() throws Exception {
        // Arrange
        var content = List.of(new Employee("E001", "An", "Developer", 15_000_000.0));
        Slice<Employee> slice = new SliceImpl<>(content, PageRequest.of(0, 1), true);
        when(employeeService.getActiveEmployeesSlice(any(Pageable.class))).thenReturn(slice);

        // Act & Assert
        mockMvc.perform(get("/api/employees/slice")
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.last").value(false))
                .andExpect(jsonPath("$.totalElements").doesNotExist())
                .andExpect(jsonPath("$.totalPages").doesNotExist());
    }

    // ==========================================
    // 3. TEST GET BY ID (Happy path & Error path - Slot 16 Mục 7)
    // ==========================================

    @Test
    @DisplayName("GET /api/employees/{id} khi tồn tại -> trả về 200 OK và JSON employee")
    void getById_existing_returns200AndJson() throws Exception {
        // Arrange
        var employee = new Employee("E001", "Nguyen Van A", "Developer", 15_000_000.0);
        when(employeeService.getEmployeeById("E001")).thenReturn(employee);

        // Act & Assert
        mockMvc.perform(get("/api/employees/E001"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.empId").value("E001"))
                .andExpect(jsonPath("$.name").value("Nguyen Van A"))
                .andExpect(jsonPath("$.designation").value("Developer"));
    }

    @Test
    @DisplayName("GET /api/employees/{id} khi không tồn tại -> trả về 404 NOT FOUND")
    void getById_missing_returns404() throws Exception {
        // Arrange
        when(employeeService.getEmployeeById("E999"))
                .thenThrow(new ResourceNotFoundException("Employee not found with id: E999"));

        // Act & Assert
        mockMvc.perform(get("/api/employees/E999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Employee not found with id: E999"));
    }

    // ==========================================
    // 4. TEST POST CREATE (JSON request + response 201 - Slot 16 Mục 8)
    // ==========================================

    @Test
    @DisplayName("POST /api/employees với payload hợp lệ -> trả về 201 CREATED")
    void create_valid_returns201() throws Exception {
        // Arrange
        var request = new EmployeeRequest("E004", "Pham Thi D", "HR", 14_000_000.0);
        var created = new Employee("E004", "Pham Thi D", "HR", 14_000_000.0);
        when(employeeService.createEmployee(any(Employee.class))).thenReturn(created);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.empId").value("E004"))
                .andExpect(jsonPath("$.name").value("Pham Thi D"));
    }

    @Test
    @DisplayName("POST /api/employees thiếu trường bắt buộc -> trả về 400 Bad Request")
    void create_missingRequiredField_returns400() throws Exception {
        // Arrange: empId rỗng, name rỗng
        var invalidRequest = new EmployeeRequest("", "", "HR", 14_000_000.0);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // ==========================================
    // 5. TEST PUT UPDATE & DELETE (CRUD completeness)
    // ==========================================

    @Test
    @DisplayName("PUT /api/employees/{id} hợp lệ -> trả về 200 OK")
    void update_valid_returns200() throws Exception {
        // Arrange
        var request = new EmployeeRequest("E001", "Nguyen Van A Updated", "Lead Dev", 20_000_000.0);
        var updated = new Employee("E001", "Nguyen Van A Updated", "Lead Dev", 20_000_000.0);
        when(employeeService.updateEmployee(eq("E001"), any(Employee.class))).thenReturn(updated);

        // Act & Assert
        mockMvc.perform(put("/api/employees/E001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.empId").value("E001"))
                .andExpect(jsonPath("$.name").value("Nguyen Van A Updated"));
    }

    @Test
    @DisplayName("DELETE /api/employees/{id} thành công -> trả về 204 No Content")
    void delete_existing_returns204() throws Exception {
        // Arrange
        doNothing().when(employeeService).deleteEmployee("E001");

        // Act & Assert
        mockMvc.perform(delete("/api/employees/E001"))
                .andExpect(status().isNoContent());
    }

    // ==========================================
    // 6. TEST VERSIONING STRATEGIES (Slot 15)
    // ==========================================

    @Test
    @DisplayName("Query Versioning: GET /api/employees?version=1 -> trả về 200 OK và contract v1")
    void getByQueryVersion1_returns200() throws Exception {
        // Arrange
        when(employeeService.getAllEmployeesList()).thenReturn(List.of(
                new Employee("E001", "Nguyen Van A", "Developer", 15_000_000.0)
        ));

        // Act & Assert
        mockMvc.perform(get("/api/employees").param("version", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].empId").value("E001"))
                .andExpect(jsonPath("$[0].name").value("Nguyen Van A"));
    }

    @Test
    @DisplayName("Header Versioning: GET /api/employees với X-API-Version=1 -> trả về 200 OK")
    void getByHeaderVersion1_returns200() throws Exception {
        // Arrange
        when(employeeService.getAllEmployeesList()).thenReturn(List.of(
                new Employee("E001", "Nguyen Van A", "Developer", 15_000_000.0)
        ));

        // Act & Assert
        mockMvc.perform(get("/api/employees").header("X-API-Version", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].empId").value("E001"));
    }

    @Test
    @DisplayName("Media Type Versioning: GET /api/employees với Accept v1 -> trả về 200 OK")
    void getByMediaTypeVersion1_returns200() throws Exception {
        // Arrange
        when(employeeService.getAllEmployeesList()).thenReturn(List.of(
                new Employee("E001", "Nguyen Van A", "Developer", 15_000_000.0)
        ));

        // Act & Assert
        mockMvc.perform(get("/api/employees").accept("application/vnd.company.v1+json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].empId").value("E001"));
    }
}
