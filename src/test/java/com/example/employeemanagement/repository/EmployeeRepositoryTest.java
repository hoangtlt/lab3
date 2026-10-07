package com.example.employeemanagement.repository;

import com.example.employeemanagement.entity.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JPA Repository Slice Test sử dụng @DataJpaTest (Slot 16 - Mục 11, 12 và Demo 5).
 * Đặc điểm:
 * - Chỉ load các JPA components (Entity, Repository, DataSource).
 * - Sử dụng H2 in-memory Database thực sự để kiểm thử SQL queries và mappings.
 * - Tự động rollback sau mỗi test method, đảm bảo tính độc lập (Independent).
 */
@DataJpaTest
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @BeforeEach
    void setUp() {
        // Dọn dẹp dữ liệu để đảm bảo các test case độc lập
        employeeRepository.deleteAll();
    }

    @Test
    @DisplayName("Lưu và tìm nhân viên theo ID (save and findById round trip)")
    void saveAndFindById_roundTrip() {
        // Arrange
        Employee e = new Employee("E010", "Test User", "Tester", 10_000_000.0, "tester@fpt.edu.vn", "QA", true);

        // Act
        employeeRepository.save(e);
        Optional<Employee> found = employeeRepository.findById("E010");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test User");
        assertThat(found.get().getDesignation()).isEqualTo("Tester");
        assertThat(found.get().getSalary()).isEqualTo(10_000_000.0);
    }

    @Test
    @DisplayName("Phân trang và sắp xếp: lấy trang 0 kích thước 2")
    void paging_firstPage_returnsExpectedSize() {
        // Arrange
        employeeRepository.save(new Employee("E001", "An", "Dev", 15_000_000.0));
        employeeRepository.save(new Employee("E002", "Binh", "QA", 12_000_000.0));
        employeeRepository.save(new Employee("E003", "Cuong", "PM", 30_000_000.0));

        // Act
        PageRequest pageRequest = PageRequest.of(0, 2, Sort.by(Sort.Direction.ASC, "salary"));
        Page<Employee> page = employeeRepository.findAll(pageRequest);

        // Assert
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(2);
        // Kiểm tra thứ tự sắp xếp theo lương tăng dần: Binh (12tr) trước An (15tr)
        assertThat(page.getContent().get(0).getName()).isEqualTo("Binh");
        assertThat(page.getContent().get(1).getName()).isEqualTo("An");
    }

    @Test
    @DisplayName("Tìm kiếm bằng Slice: findByActive trả về Slice mà không cần count query")
    void findByActive_returnsSlice() {
        // Arrange
        employeeRepository.save(new Employee("E001", "An", "Dev", 15_000_000.0, "an@fpt.vn", "IT", true));
        employeeRepository.save(new Employee("E002", "Binh", "QA", 12_000_000.0, "binh@fpt.vn", "QA", false));
        employeeRepository.save(new Employee("E003", "Cuong", "Dev", 18_000_000.0, "cuong@fpt.vn", "IT", true));

        // Act
        Slice<Employee> activeSlice = employeeRepository.findByActive(true, PageRequest.of(0, 1));

        // Assert
        assertThat(activeSlice.getContent()).hasSize(1);
        assertThat(activeSlice.hasNext()).isTrue(); // Vì còn E003 ở trang sau
        assertThat(activeSlice.getContent().get(0).isActive()).isTrue();
    }

    @Test
    @DisplayName("Derived Query: findByDesignation trả về đúng các bản ghi khớp")
    void findByDesignation_returnsMatchingRows() {
        // Arrange
        employeeRepository.save(new Employee("E001", "An", "Developer", 15_000_000.0));
        employeeRepository.save(new Employee("E002", "Binh", "QA Engineer", 12_000_000.0));
        employeeRepository.save(new Employee("E003", "Cuong", "Developer", 18_000_000.0));

        // Act
        List<Employee> developers = employeeRepository.findByDesignation("Developer");

        // Assert
        assertThat(developers).hasSize(2);
        assertThat(developers)
                .extracting(Employee::getName)
                .containsExactlyInAnyOrder("An", "Cuong");
    }
}
