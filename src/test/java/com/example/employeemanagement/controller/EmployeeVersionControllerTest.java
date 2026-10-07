package com.example.employeemanagement.controller;

import com.example.employeemanagement.entity.Employee;
import com.example.employeemanagement.service.EmployeeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller Slice Test kiểm tra Path / URI Versioning:
 * - /api/v1/employees: Trả về contract v1
 * - /api/v2/employees: Trả về contract v2 (có thêm email, department, active)
 */
@WebMvcTest(EmployeeVersionController.class)
class EmployeeVersionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployeeService employeeService;

    @Test
    @DisplayName("GET /api/v1/employees -> trả về 200 OK và danh sách contract v1")
    void getEmployeesV1_returnsV1Contract() throws Exception {
        // Arrange
        var employee = new Employee("E001", "Nguyen Van A", "Developer", 15_000_000.0, "vana@fpt.edu.vn", "IT", true);
        when(employeeService.getAllEmployeesList()).thenReturn(List.of(employee));

        // Act & Assert
        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].empId").value("E001"))
                .andExpect(jsonPath("$[0].name").value("Nguyen Van A"))
                .andExpect(jsonPath("$[0].designation").value("Developer"))
                .andExpect(jsonPath("$[0].salary").value(15_000_000.0))
                // Contract v1 KHÔNG chứa email và department
                .andExpect(jsonPath("$[0].email").doesNotExist())
                .andExpect(jsonPath("$[0].department").doesNotExist());
    }

    @Test
    @DisplayName("GET /api/v2/employees -> trả về 200 OK và danh sách contract v2 có email và department")
    void getEmployeesV2_returnsV2Contract() throws Exception {
        // Arrange
        var employee = new Employee("E001", "Nguyen Van A", "Developer", 15_000_000.0, "vana@fpt.edu.vn", "IT", true);
        when(employeeService.getAllEmployeesList()).thenReturn(List.of(employee));

        // Act & Assert
        mockMvc.perform(get("/api/v2/employees"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].empId").value("E001"))
                .andExpect(jsonPath("$[0].email").value("vana@fpt.edu.vn"))
                .andExpect(jsonPath("$[0].department").value("IT"))
                .andExpect(jsonPath("$[0].active").value(true));
    }
}
