# ⏱️ EMPLOYEE WORKING HOURS AND SALARY MANAGEMENT (Android · Java · SQLite)

Ứng dụng quản lý giờ làm việc, chấm công và tính lương nhân viên với 2 vai trò **Admin** và **Employee**.

---

## Chạy demo
1. Mở thư mục project bằng **Android Studio**.
2. Chạy cấu hình app trên máy ảo hoặc điện thoại thật (**Android 7.0+**, `minSdk 24`).
3. Tài khoản có sẵn:

| Vai trò | Username | Password | Thông tin nhân viên & Mức lương |
| :--- | :--- | :--- | :--- |
| **Admin** | `admin` | `admin123` | Quản trị viên hệ thống (Toàn quyền) |
| **Employee** | `nv01` | `123456` | NV001 - Nguyễn Văn An (50.000 đ/giờ) |
| **Employee** | `nv02` | `123456` | NV002 - Trần Thị Bình (60.000 đ/giờ) |
| **Employee** | `nv03` | `123456` | NV003 - Lê Hoàng Cường (55.000 đ/giờ) |

> **Dữ liệu mẫu:** Đã có sẵn dữ liệu chấm công ngày thường (8.0h) / làm thêm giờ (9.5h) / ca đang làm / bảng lương tháng trước đã thanh toán (`PAID`) và bảng lương tháng này chờ thanh toán (`PENDING`) để demo ngay mà không cần nhập liệu.
>
> **Lưu ý:** Khi chuyển giữa các branch hoặc kiểm thử lại từ đầu, nên gỡ app (hoặc Clear data) trước khi chạy lại để CSDL SQLite được tạo mới theo đúng schema & dữ liệu mẫu.

---

## Kiến trúc (phân lớp theo OOP)

```text
com.example.thuc_hanh_2
├── database
│   └── DatabaseHelper        ← Singleton SQLiteOpenHelper, DDL 4 bảng, seeding dữ liệu mẫu, query & transaction
├── model
│   ├── Account               ← Entity tài khoản (id, username, password, role)
│   ├── Employee              ← Entity hồ sơ nhân viên (id, code, fullname, email, phone, hourlyRate, accountId)
│   ├── WorkLog               ← Entity chấm công (id, employeeId, workDate, checkInTime, checkOutTime, totalHours, status)
│   └── Salary                ← Entity lương tháng (id, employeeId, month, year, totalHours, overtimeHours, totalSalary, status)
├── util
│   ├── SessionManager        ← Quản lý phiên đăng nhập SharedPreferences & Ghi nhớ tài khoản
│   ├── DateTimeUtils         ← Xử lý ngày giờ, tính khoảng cách giờ, định dạng hiển thị
│   └── FormatUtils           ← Định dạng tiền tệ VNĐ (đ) và số giờ làm việc
├── ui
│   ├── auth
│   │   └── LoginActivity     ← Đăng nhập, phân quyền Admin/Employee, nút đăng nhập nhanh 1-chạm
│   ├── admin
│   │   ├── AdminMainActivity ← Dashboard quản trị: 3 Tab (Nhân viên, Chấm công, Tính lương)
│   │   └── AddEditEmployeeActivity ← Form thêm mới & chỉnh sửa nhân sự, validate mã NV / username
│   └── employee
│       └── EmployeeMainActivity ← Cổng nhân viên: 4 Tab (Điểm danh Live, Lịch sử, Bảng lương, Hồ sơ & Đổi MK)
└── adapter
    ├── EmployeeAdapter       ← Adapter danh sách nhân viên, hỗ trợ tìm kiếm thời gian thực, Edit / Delete callback
    ├── AttendanceAdapter     ← Adapter danh sách chấm công, hiển thị badge màu sắc theo trạng thái ca
    └── SalaryAdapter         ← Adapter bảng lương, tính tổng quỹ lương, chuyển đổi trạng thái PAID/PENDING
```

**Luồng gọi:**  
`UI (Activity)` → `SessionManager / Utils` → `DatabaseHelper (Nghiệp vụ, Validate, SQLite Transaction)` → `SQLite Database`.  
Đảm bảo tính đóng gói (Encapsulation), phân tách trách nhiệm rõ ràng (Separation of Concerns).

---

## Cơ sở dữ liệu

| Bảng | Cột | Mô tả & Ràng buộc |
| :--- | :--- | :--- |
| **`Account`** | `id`, `username`, `password`, `role` | Tài khoản đăng nhập hệ thống (`role`: `ADMIN` hoặc `EMPLOYEE`, `username` UNIQUE) |
| **`Employee`** | `id`, `code`, `fullname`, `email`, `phone`, `hourlyRate`, `accountId` | Hồ sơ nhân sự (`code` UNIQUE, `accountId` $\rightarrow$ `Account.id` ON DELETE CASCADE) |
| **`WorkLog`** | `id`, `employeeId`, `workDate`, `checkInTime`, `checkOutTime`, `totalHours`, `status` | Bản ghi chấm công (`employeeId` $\rightarrow$ `Employee.id`, `status`: `CHECKED_IN`, `COMPLETED`, `INCOMPLETE`) |
| **`Salary`** | `id`, `employeeId`, `month`, `year`, `totalHours`, `overtimeHours`, `totalSalary`, `status` | Bảng tổng hợp lương tháng (`employeeId` $\rightarrow$ `Employee.id`, `status`: `PENDING`, `PAID`, `UNIQUE(employeeId, month, year)`) |

---

## Quy tắc nghiệp vụ chính

1. **Chấm công & Điểm danh (Attendance)**:
   - Hiển thị đồng hồ số thời gian thực (`HH:mm:ss`) cập nhật từng giây và ngày hôm nay.
   - **Điểm danh vào (Check-in)**: Ghi nhận ngày làm và giờ vào. Ngăn chặn điểm danh vào trùng lặp nếu ca hiện tại vẫn đang làm (`CHECKED_IN`). Cảnh báo khi đã hoàn thành ca trong ngày.
   - **Điểm danh ra (Check-out)**: Kiểm tra ca làm đang hoạt động, chặn điểm danh ra khi chưa điểm danh vào hoặc giờ ra không hợp lệ (`checkOutTime < checkInTime`).
   - **Xử lý ca dở dang**: Tự động đánh dấu các ca của ngày hôm trước chưa check-out thành `INCOMPLETE` (Chưa hoàn tất).

2. **Tính giờ làm việc & Làm thêm (Working Hours & Overtime Calculation)**:
   - Tự động tính chênh lệch thời gian: `totalHours = (checkOutMillis - checkInMillis) / 3600000.0` (làm tròn 2 chữ số thập phân).
   - Tiêu chuẩn làm việc: **8.0 giờ/ngày**.
   - Thời gian vượt quá 8 giờ trong ngày được tính là giờ làm thêm: `overtimeDay = dailyHours - 8.0`.
   - Hệ số lương làm thêm: **150% (x1.5)** mức lương cơ sở theo giờ.

3. **Tính lương tháng (Monthly Salary Calculation)**:
   - Công thức tính lương tháng:  
     $$\text{Tổng lương} = (\text{Giờ tiêu chuẩn} \times \text{Lương cơ sở}) + (\text{Giờ làm thêm} \times \text{Lương cơ sở} \times 1.5)$$
   - **Admin**: Bấm nút *"Tính Lương"* để duyệt tự động và tổng hợp lương toàn bộ nhân viên trong tháng được chọn; theo dõi tổng quỹ lương chi trả; chuyển đổi trạng thái thanh toán `CHỜ THANH TOÁN` $\leftrightarrow$ `ĐÃ THANH TOÁN`.
   - **Employee**: Xem chi tiết bảng lương từng tháng gồm Lương cơ sở, Giờ chuẩn, Giờ làm thêm (x1.5), Tổng thực lĩnh và Trạng thái thanh toán.

4. **Lưu phiên đăng nhập (SharedPreferences)**:
   - `SessionManager` lưu trạng thái `isLoggedIn`, tài khoản, vai trò và tùy chọn *"Ghi nhớ đăng nhập"*.
   - Tự động điều hướng vào thẳng màn hình Quản trị viên hoặc Cổng nhân viên khi mở lại ứng dụng.

---

## Phân công (2 người – 2 branch)

Các branch được phân chia theo module và xếp chồng theo thứ tự phụ thuộc:
- `main`: Branch gốc tích hợp sản phẩm hoàn chỉnh và bản phát hành.
- `feature/auth-database`: Khởi tạo SQLite `DatabaseHelper`, 4 bảng dữ liệu, Seeding dữ liệu mẫu, `SessionManager` (SharedPreferences), `LoginActivity` và điều hướng phân quyền.
- `feature/employee-management`: Quản lý nhân viên phía Admin (Thêm, Sửa, Xóa cascade, Tìm kiếm thời gian thực, Form `AddEditEmployeeActivity`).
- `feature/attendance-salary`: Nghiệp vụ chấm công Check-in/Check-out, Đồng hồ Live, Tự động tính giờ làm việc, Tính lương tháng & Overtime x1.5 cho cả Admin và Employee.

*Merge lần lượt vào `main` theo đúng thứ tự này (không xung đột).*
