package com.example.employeemanagement.config;

import com.example.employeemanagement.entity.Employee;
import com.example.employeemanagement.repository.EmployeeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

/**
 * Tự động tạo dữ liệu mẫu khi khởi động ứng dụng.
 * Tạo 25 bản ghi để phục vụ việc kiểm thử phân trang (paging), sắp xếp (sorting) và Slice (Slot 15 readiness gate).
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final EmployeeRepository employeeRepository;

    public DataSeeder(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public void run(String... args) {
        if (employeeRepository.count() > 0) {
            return;
        }

        String[] departments = {"Engineering", "QA", "Human Resources", "Finance", "Product"};
        String[] designations = {"Software Engineer", "Senior Engineer", "QA Engineer", "HR Specialist", "Product Manager"};

        List<Employee> sampleList = new ArrayList<>();
        for (int i = 1; i <= 25; i++) {
            String empId = String.format("E%03d", i);
            String name = "Employee " + i;
            String designation = designations[(i - 1) % designations.length];
            String department = departments[(i - 1) % departments.length];
            Double salary = 12_000_000.0 + (i * 1_500_000.0);
            String email = "employee" + i + "@fpt.edu.vn";
            boolean active = (i % 5 != 0); // Đa số active=true, một vài record active=false để test Slice filter

            sampleList.add(new Employee(empId, name, designation, salary, email, department, active));
        }

        employeeRepository.saveAll(sampleList);
        System.out.println(">>> DataSeeder: Đã khởi tạo thành công " + sampleList.size() + " nhân viên mẫu vào H2 Database!");
    }
}
