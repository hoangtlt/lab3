# BÁO CÁO VÀ HƯỚNG DẪN THỰC HIỆN LAB 03 - SBA301
## RESTful Web Services với Spring Boot: Versioning, Paging, Sorting và Automated Testing

---

## I. TỔNG QUAN VÀ PHÂN TÍCH YÊU CẦU TỪ 2 FILE GIÁO TRÌNH (SLOT 15 & SLOT 16)

Dự án Lab 03 được xây dựng dựa trên sự kết hợp hoàn chỉnh của hai bài học chuyên sâu trong chương trình SBA301:
1. **Slot 15 (Part A - Chapter 12): API Versioning • Paging • Sorting • Page/Slice**
   - **Mục tiêu:** Nâng cấp RESTful API để hỗ trợ bộ dữ liệu lớn và đáp ứng quá trình phát triển (API evolution) mà không làm gián đoạn các client cũ (Backward compatibility).
   - **4 chiến lược Versioning:**
     - *Path/URI Versioning:* `/api/v1/employees` và `/api/v2/employees` (Rõ ràng, dễ cache).
     - *Query Parameter Versioning:* `/api/employees?version=1` và `?version=2`.
     - *Custom Header Versioning:* `X-API-Version: 1` và `X-API-Version: 2`.
     - *Media Type / Accept Header Versioning:* `Accept: application/vnd.company.v1+json`.
   - **Paging & Sorting có bảo vệ (Guardrails):**
     - Quản lý `page` (0-based) và `size` (mặc định 10, giới hạn tối đa `max = 100` để bảo vệ server/database).
     - Áp dụng **Whitelist sort fields** (`empId`, `name`, `designation`, `salary`, `email`, `department`, `active`) để phòng chống lỗi truy vấn và SQL Injection.
   - **Phân biệt `Page` vs `Slice`:**
     - `Page`: Trả về dữ liệu kèm metadata tổng thể (`totalElements`, `totalPages`), phát sinh câu lệnh `COUNT(*)` trong DB -> Phù hợp cho giao diện Web có phân trang đánh số cụ thể.
     - `Slice`: Chỉ trả về dữ liệu và cờ `hasNext` (còn trang tiếp hay không), **hoàn toàn không chạy `COUNT(*)`** -> Tối ưu hiệu năng, phù hợp Infinite Scroll (cuộn vô tận) hoặc nút "Load More" trên thiết bị di động.

2. **Slot 16 (Part B - Chapter 12): Testing • MockMvc • Lab 03 Completion**
   - **Mục tiêu:** Xây dựng hệ thống kiểm thử tự động toàn diện để bảo vệ các hành vi (behaviors) của API khi refactor mã nguồn.
   - **Tháp kiểm thử (Testing Pyramid):**
     - *Plain Unit Test (JUnit 5 + Mockito):* Kiểm thử độc lập tầng Service (`EmployeeServiceUnitTest`), cô lập hoàn toàn với database và Spring context bằng `@Mock` và `@InjectMocks`.
     - *Web Slice Test (`@WebMvcTest` + `MockMvc`):* Kiểm thử tầng Controller (`EmployeeControllerTest`, `EmployeeVersionControllerTest`) về HTTP routing, data binding, JSON serialization/deserialization, HTTP Status codes (200, 201, 204, 400, 404). Áp dụng annotation mới `@MockitoBean` (chuẩn Spring Boot 3.4+).
     - *JPA Slice Test (`@DataJpaTest`):* Kiểm thử tầng Repository (`EmployeeRepositoryTest`) trên H2 database in-memory thật sự, kiểm tra các câu lệnh query, derived query và tự động rollback.
     - *Full Context Smoke Test (`@SpringBootTest`):* Kiểm thử khởi động toàn bộ ứng dụng và kiểm tra sự kết nối giữa các bean (`Lab3ApplicationTests`).

---

## II. CẤU TRÚC THƯ MỤC DỰ ÁN (3-TIER ARCHITECTURE)

Dự án được tổ chức rõ ràng, đơn giản, chuẩn mực theo mô hình Controller - Service - Repository:

```
lab3/
├── docs/                                          # 2 file PDF đề bài (Slot 15 & Slot 16)
├── pom.xml                                        # Cấu hình Maven (Spring Boot 3.4.3, Java 21)
├── postman_collection.json                        # Bộ test Postman đầy đủ 19 kịch bản
├── README.md                                      # Tài liệu hướng dẫn chi tiết
└── src/
    ├── main/
    │   ├── java/com/example/employeemanagement/
    │   │   ├── EmployeeManagementApplication.java # Lớp chạy chính (@SpringBootApplication)
    │   │   ├── config/
    │   │   │   └── DataSeeder.java                # Tự động nạp 25 nhân viên mẫu khi khởi động
    │   │   ├── controller/
    │   │   │   ├── EmployeeController.java        # CRUD, Paging, Sorting, Slice, Header/Query/Media versioning
    │   │   │   └── EmployeeVersionController.java # Path/URI Versioning (/api/v1 và /api/v2)
    │   │   ├── dto/
    │   │   │   ├── EmployeeRequest.java           # DTO nhận dữ liệu Create/Update có Bean Validation
    │   │   │   ├── EmployeeV1Response.java        # DTO v1 (empId, name, designation, salary)
    │   │   │   ├── EmployeeV2Response.java        # DTO v2 (+ email, department, active)
    │   │   │   └── PageResponse.java              # DTO chuẩn hóa kết quả phân trang (Production-ready)
    │   │   ├── entity/
    │   │   │   └── Employee.java                  # JPA Entity ánh xạ bảng employees
    │   │   ├── exception/
    │   │   │   ├── BadRequestException.java       # Ngoại lệ 400 (lỗi tham số, sai sort field)
    │   │   │   ├── ErrorResponse.java             # Cấu trúc phản hồi lỗi chuẩn
    │   │   │   ├── GlobalExceptionHandler.java    # @RestControllerAdvice bắt lỗi tập trung
    │   │   │   └── ResourceNotFoundException.java # Ngoại lệ 404 (không tìm thấy ID)
    │   │   ├── repository/
    │   │   │   └── EmployeeRepository.java        # JpaRepository hỗ trợ Paging, Sorting & Slice
    │   │   └── service/
    │   │       └── EmployeeService.java           # 1 Class Service duy nhất xử lý nghiệp vụ (không cần Interface)
    │   └── resources/
    │       └── application.properties             # Cấu hình H2 database, cổng 8080 và Swagger UI
    └── test/
        └── java/com/example/employeemanagement/
            ├── Lab3ApplicationTests.java         # @SpringBootTest: Full integration smoke test
            ├── controller/
            │   ├── EmployeeControllerTest.java    # @WebMvcTest: Controller slice test
            │   └── EmployeeVersionControllerTest.java # @WebMvcTest: URI versioning test
            ├── repository/
            │   └── EmployeeRepositoryTest.java    # @DataJpaTest: JPA slice test với H2
            └── service/
                └── EmployeeServiceUnitTest.java   # Plain Unit Test với JUnit 5 + Mockito
```

---

## III. HƯỚNG DẪN KHỞI CHẠY VÀ TRUY CẬP

### 1. Khởi chạy ứng dụng
Mở terminal tại thư mục gốc `lab3` và chạy lệnh:
```bash
mvn spring-boot:run
```

### 2. Truy cập Swagger UI (OpenAPI 3)
Giao diện tài liệu API trực quan để khám phá và thử nghiệm trực tiếp:
- URL: **http://localhost:8080/swagger-ui.html**
- OpenAPI Docs JSON: **http://localhost:8080/v3/api-docs**

### 3. Truy cập H2 Database Console
Xem cơ sở dữ liệu in-memory:
- URL: **http://localhost:8080/h2-console**
- JDBC URL: `jdbc:h2:mem:lab3db`
- User: `sa`
- Password: *(để trống)*

---

## IV. BẢNG DANH MỤC CÁC ENDPOINT VÀ CÁCH GỌI

| Chức năng | Phương thức | Endpoint | Ghi chú / Header / Tham số |
|---|---|---|---|
| **Paging & Sorting** | `GET` | `/api/employees?page=0&size=5&sortBy=salary&direction=desc` | Có guardrail (clamp max 100, whitelist field) |
| **Slice (Load More)** | `GET` | `/api/employees/slice?page=0&size=5` | Không chạy câu lệnh count SQL |
| **Xem chi tiết** | `GET` | `/api/employees/E001` | Trả về 200 OK hoặc 404 nếu không tìm thấy |
| **Thêm mới** | `POST` | `/api/employees` | Body JSON, trả về 201 Created |
| **Cập nhật** | `PUT` | `/api/employees/E001` | Body JSON, trả về 200 OK |
| **Xóa** | `DELETE` | `/api/employees/E001` | Trả về 204 No Content |
| **Path Versioning v1** | `GET` | `/api/v1/employees` | Chỉ trả về: empId, name, designation, salary |
| **Path Versioning v2** | `GET` | `/api/v2/employees` | Trả về thêm: email, department, active |
| **Query Versioning v1**| `GET` | `/api/employees?version=1` | Lọc theo query parameter `version=1` |
| **Query Versioning v2**| `GET` | `/api/employees?version=2` | Lọc theo query parameter `version=2` |
| **Header Versioning v1**| `GET` | `/api/employees` | Header: `X-API-Version: 1` |
| **Header Versioning v2**| `GET` | `/api/employees` | Header: `X-API-Version: 2` |
| **Media Type Version v1**| `GET` | `/api/employees` | Header: `Accept: application/vnd.company.v1+json` |
| **Media Type Version v2**| `GET` | `/api/employees` | Header: `Accept: application/vnd.company.v2+json` |

---

## V. HƯỚNG DẪN KIỂM THỬ TỰ ĐỘNG (AUTOMATED TESTING)

### 1. Chạy toàn bộ test suite
Để thực thi toàn bộ **27 test cases** trong dự án:
```bash
mvn test
```
*Kết quả mong đợi:* **BUILD SUCCESS**, `Tests run: 27, Failures: 0, Errors: 0, Skipped: 0`.

### 2. Chạy từng class test riêng biệt
- Chạy riêng Controller Test:
  ```bash
  mvn -Dtest=EmployeeControllerTest test
  ```
- Chạy riêng Service Unit Test:
  ```bash
  mvn -Dtest=EmployeeServiceUnitTest test
  ```
- Chạy riêng Repository JPA Test:
  ```bash
  mvn -Dtest=EmployeeRepositoryTest test
  ```

---

## VI. BÀI THỰC HÀNH "BREAK-IT" (MINH HỌA RED -> GREEN REGRESSION TEST)
Mục tiêu bài thực hành này là chứng minh giá trị của automated test: **khi code bị lỗi, test sẽ chuyển sang màu đỏ (FAIL), sau khi sửa code, test sẽ chuyển sang màu xanh (PASS)**.

### Kịch bản minh họa 1: Đổi status trả về khi tạo mới nhân viên
1. **Gây lỗi (Red):** Mở file `EmployeeController.java`, tại phương thức `createEmployee`, sửa:
   ```java
   // Ban đầu (đúng chuẩn REST):
   return ResponseEntity.status(HttpStatus.CREATED).body(created); // Trả về 201

   // Cố ý sửa thành sai:
   return ResponseEntity.status(HttpStatus.OK).body(created);      // Trả về 200
   ```
2. **Quan sát thất bại:** Chạy lại `mvn -Dtest=EmployeeControllerTest#create_valid_returns201 test`.
   - Kết quả: **TEST FAIL** (Expected 201 nhưng Actual là 200).
3. **Sửa lỗi (Green):** Hoàn tác lại thành `HttpStatus.CREATED` và chạy lại lệnh trên -> **BUILD SUCCESS** (Test lại XANH).

### Kịch bản minh họa 2: Bỏ qua kiểm tra 404 khi không tìm thấy nhân viên
1. **Gây lỗi (Red):** Tại `EmployeeController.java`, phương thức `getEmployeeById`, sửa thành:
   ```java
   // Cố ý không để exception bung ra mà trả về ok(null):
   return ResponseEntity.ok(null);
   ```
2. **Quan sát thất bại:** Chạy `mvn -Dtest=EmployeeControllerTest#getById_missing_returns404 test`.
   - Kết quả: **TEST FAIL** (Expected 404 NOT_FOUND nhưng Actual nhận được 200 OK).
3. **Sửa lỗi (Green):** Hoàn tác code về ban đầu -> Test PASS hoàn toàn!

---

## VII. HƯỚNG DẪN TRẢ LỜI VẤN ĐÁP CỦA GIẢNG VIÊN (HUMAN VERIFICATION GATE)

Dưới đây là đáp án chuẩn xác và súc tích nhất cho toàn bộ các câu hỏi vấn đáp trong Slot 15 & 16:

### 1. Tại sao cần API Versioning? Khi nào một thay đổi được coi là breaking change?
- **Trả lời:** Cần API Versioning khi API thay đổi contract (cấu trúc dữ liệu hoặc nghiệp vụ) mà vẫn cần hỗ trợ các client cũ (như ứng dụng di động chưa kịp cập nhật) tiếp tục hoạt động bình thường mà không bị sập.
- **Breaking change** xảy ra khi: đổi tên field, xóa field, thay đổi kiểu dữ liệu (vd: từ số sang chuỗi), hoặc thay đổi ngữ nghĩa của API. Ngược lại, thêm một field tùy chọn (optional) thường là non-breaking.

### 2. Ưu và nhược điểm của 4 chiến lược Versioning?
- **Path/URI (`/api/v1`):** Ưu điểm: cực kỳ rõ ràng, dễ nhìn, dễ cấu hình cache và log. Nhược điểm: URL bị thay đổi khi nâng cấp version.
- **Query Parameter (`?version=1`):** Ưu điểm: giữ nguyên đường dẫn gốc (base URI). Nhược điểm: dễ bị nhầm lẫn là tham số lọc dữ liệu (filter).
- **Custom Header (`X-API-Version: 1`):** Ưu điểm: URI sạch sẽ. Nhược điểm: không kiểm tra trực tiếp được qua thanh địa chỉ trình duyệt.
- **Media Type / Accept Header:** Ưu điểm: chuẩn RESTful thuần túy nhất (gắn version vào representation). Nhược điểm: cấu hình phức tạp nhất với client.

### 3. Phân biệt `Page` và `Slice` trong Spring Data? Khi nào dùng loại nào?
- **Page:** Chứa danh sách dữ liệu, kích thước trang, số trang hiện tại, VÀ `totalElements`, `totalPages`. Để có được 2 thông tin tổng số này, Spring Data **phải thực hiện thêm một câu truy vấn `COUNT(*)`**. Phù hợp với UI dạng bảng trên Web có các nút chuyển trang cụ thể (Trang 1, 2, 3... 10).
- **Slice:** Chỉ chứa dữ liệu và cờ `hasNext` (còn trang sau hay không). **Không thực hiện COUNT(*)**, giúp giảm tải rất lớn cho database đối với bảng nhiều triệu dòng. Phù hợp cho Mobile với chức năng cuộn vô tận (Infinite Scroll) hoặc nút "Xem thêm" (Load more).

### 4. Tại sao cần Page Size Guardrail và Sort Whitelist?
- **Page Size Guardrail:** Nếu không giới hạn kích thước trang (ví dụ client gửi `?size=1000000`), server sẽ cố nạp cả triệu bản ghi vào RAM dẫn đến sập hệ thống (OutOfMemoryError) hoặc tấn công DoS. Guardrail giúp giới hạn `size` tối đa (ví dụ 100).
- **Sort Whitelist:** Client có thể truyền bất kỳ tên trường nào vào `?sortBy=...`. Nếu không whitelist, người dùng có thể truyền tên trường không tồn tại (gây lỗi 500) hoặc sắp xếp theo các trường nhạy cảm (như mật khẩu, token).

### 5. Tại sao `@WebMvcTest` + `MockMvc` tốt hơn gọi trực tiếp Controller method trong Unit test?
- Gọi trực tiếp method chỉ kiểm tra logic Java thuần của hàm.
- `MockMvc` mô phỏng toàn bộ luồng xử lý của Spring MVC: kiểm tra URL mapping, HTTP method, binding tham số, chuyển đổi JSON (`ObjectMapper`), validation `@Valid`, và các bộ lọc/bắt lỗi `@ExceptionHandler` mà không cần bật server Tomcat thật.

### 6. Sự khác biệt giữa `@MockitoBean` và `@Mock`?
- `@Mock`: Là annotation thuần của Mockito, dùng trong Plain Unit Test (`@ExtendWith(MockitoExtension.class)`), hoạt động ngoài Spring context, không thể đưa mock vào Spring Bean container.
- `@MockitoBean`: (chuẩn mới từ Spring Boot 3.4+) dùng trong Spring Test Context (`@WebMvcTest`), tự động tạo mock và thay thế Bean tương ứng ngay trong Application Context của Spring.

### 7. Tại sao `@DataJpaTest` không nên dùng cho repository dùng `ArrayList`?
- `@DataJpaTest` là JPA Slice test: nó được thiết kế chuyên biệt để cấu hình DataSource in-memory (như H2), khởi tạo EntityManager và kiểm tra cú pháp Hibernate/SQL mapping. Nếu repository chỉ là một class chứa `ArrayList`, không có JPA hay Database thì dùng `@DataJpaTest` là vô nghĩa, chỉ cần dùng Plain JUnit test.
#   l a b 3  
 