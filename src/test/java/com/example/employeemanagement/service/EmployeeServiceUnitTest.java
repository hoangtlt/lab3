package com.example.employeemanagement.service;

import com.example.employeemanagement.entity.Employee;
import com.example.employeemanagement.exception.ResourceNotFoundException;
import com.example.employeemanagement.repository.EmployeeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Service Plain Unit Test với JUnit 5 + Mockito + AssertJ (Slot 16 - Mục 4 & Demo 2).
 * Đặc điểm:
 * - Chạy trong isolation (cô lập hoàn toàn với Spring Context và Database).
 * - Tốc độ thực thi siêu nhanh (mili-giây).
 * - Tuân thủ mô hình AAA (Arrange - Act - Assert).
 */
@ExtendWith(MockitoExtension.class)
class EmployeeServiceUnitTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    @Test
    @DisplayName("Thêm nhân viên hợp lệ -> ủy quyền gọi repository.save() và trả về kết quả")
    void create_valid_delegatesToRepository() {
        // Arrange
        Employee input = new Employee("E004", "Pham Thi D", "HR", 14_000_000.0);
        when(employeeRepository.save(input)).thenReturn(input);

        // Act
        Employee result = employeeService.createEmployee(input);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isSameAs(input);
        verify(employeeRepository, times(1)).save(input);
    }

    @Test
    @DisplayName("Tìm nhân viên theo ID khi tồn tại -> trả về Employee")
    void getById_existing_returnsEmployee() {
        // Arrange
        Employee e = new Employee("E001", "Nguyen Van A", "Developer", 15_000_000.0);
        when(employeeRepository.findById("E001")).thenReturn(Optional.of(e));

        // Act
        Employee result = employeeService.getEmployeeById("E001");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEmpId()).isEqualTo("E001");
        assertThat(result.getName()).isEqualTo("Nguyen Van A");
        verify(employeeRepository).findById("E001");
    }

    @Test
    @DisplayName("Tìm nhân viên theo ID không tồn tại -> ném ra ResourceNotFoundException")
    void getById_missing_throwsResourceNotFoundException() {
        // Arrange
        when(employeeRepository.findById("E999")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> employeeService.getEmployeeById("E999"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Employee not found with id: E999");

        verify(employeeRepository).findById("E999");
    }

    @Test
    @DisplayName("Lấy danh sách phân trang -> gọi repository.findAll(Pageable) và trả về Page")
    void getAllEmployees_withPageable_returnsPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 2);
        List<Employee> list = List.of(
                new Employee("E001", "An", "Developer", 15_000_000.0),
                new Employee("E002", "Binh", "Tester", 12_000_000.0)
        );
        Page<Employee> expectedPage = new PageImpl<>(list, pageable, 2);
        when(employeeRepository.findAll(pageable)).thenReturn(expectedPage);

        // Act
        Page<Employee> actualPage = employeeService.getAllEmployees(pageable);

        // Assert
        assertThat(actualPage).isNotNull();
        assertThat(actualPage.getContent()).hasSize(2);
        assertThat(actualPage.getTotalElements()).isEqualTo(2);
        verify(employeeRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Lấy danh sách bằng Slice -> gọi repository.findByActive(true, Pageable)")
    void getActiveEmployeesSlice_returnsSlice() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 1);
        List<Employee> list = List.of(new Employee("E001", "An", "Developer", 15_000_000.0));
        Slice<Employee> expectedSlice = new SliceImpl<>(list, pageable, true);
        when(employeeRepository.findByActive(true, pageable)).thenReturn(expectedSlice);

        // Act
        Slice<Employee> actualSlice = employeeService.getActiveEmployeesSlice(pageable);

        // Assert
        assertThat(actualSlice).isNotNull();
        assertThat(actualSlice.getContent()).hasSize(1);
        assertThat(actualSlice.hasNext()).isTrue();
        verify(employeeRepository).findByActive(true, pageable);
    }

    @Test
    @DisplayName("Cập nhật nhân viên tồn tại -> lưu thông tin mới và trả về nhân viên đã update")
    void updateEmployee_existing_updatesAndSaves() {
        // Arrange
        Employee existing = new Employee("E001", "Nguyen Van A", "Developer", 15_000_000.0);
        Employee updateInfo = new Employee("E001", "Nguyen Van A Pro", "Lead Dev", 25_000_000.0);
        when(employeeRepository.findById("E001")).thenReturn(Optional.of(existing));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Employee result = employeeService.updateEmployee("E001", updateInfo);

        // Assert
        assertThat(result.getName()).isEqualTo("Nguyen Van A Pro");
        assertThat(result.getDesignation()).isEqualTo("Lead Dev");
        assertThat(result.getSalary()).isEqualTo(25_000_000.0);
        verify(employeeRepository).save(existing);
    }

    @Test
    @DisplayName("Xóa nhân viên tồn tại -> gọi repository.delete()")
    void deleteEmployee_existing_callsRepositoryDelete() {
        // Arrange
        Employee existing = new Employee("E001", "Nguyen Van A", "Developer", 15_000_000.0);
        when(employeeRepository.findById("E001")).thenReturn(Optional.of(existing));

        // Act
        employeeService.deleteEmployee("E001");

        // Assert
        verify(employeeRepository).delete(existing);
    }
}
