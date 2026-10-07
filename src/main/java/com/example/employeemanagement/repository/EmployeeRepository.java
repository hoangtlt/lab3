package com.example.employeemanagement.repository;

import com.example.employeemanagement.entity.Employee;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Repository interface cho Employee.
 * Kế thừa JpaRepository để thừa hưởng:
 * - CRUD operations: save, findById, findAll, deleteById,...
 * - Phân trang và sắp xếp: findAll(Pageable), findAll(Sort)
 *
 * Ghi chú giáo trình (Slot 15):
 * JpaRepository đã tích hợp sẵn PagingAndSortingRepository và CrudRepository,
 * rất tiện lợi cho các ứng dụng thực tế.
 */
public interface EmployeeRepository extends JpaRepository<Employee, String> {

    /**
     * Truy vấn sử dụng Slice để minh họa kỹ thuật không dùng count query (thích hợp Infinite Scroll/Load More).
     */
    Slice<Employee> findByActive(boolean active, Pageable pageable);

    /**
     * Truy vấn tìm theo chức vụ (designation) phục vụ test custom query / derived query.
     */
    List<Employee> findByDesignation(String designation);
}
