# HỆ THỐNG QUẢN LÝ GIỜ LÀM VIỆC & TÍNH LƯƠNG NHÂN VIÊN
## (Employee Working Hours and Salary Management)

Dự án Android hoàn chỉnh phát triển bằng ngôn ngữ **Java**, sử dụng **SQLite Database** (`SQLiteOpenHelper`) để quản lý dữ liệu bền vững và **SharedPreferences** để lưu trữ phiên đăng nhập.

---

## 1. Cấu Trúc 4 Bảng Dữ Liệu (Database Schema)

### 1.1. Bảng `Account` (Tài khoản người dùng)
| Trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `id` | INTEGER | PRIMARY KEY AUTOINCREMENT | Mã định danh tài khoản |
| `username` | TEXT | UNIQUE, NOT NULL | Tên đăng nhập |
| `password` | TEXT | NOT NULL | Mật khẩu |
| `role` | TEXT | NOT NULL | Vai trò: `ADMIN` hoặc `EMPLOYEE` |

### 1.2. Bảng `Employee` (Hồ sơ nhân viên)
| Trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `id` | INTEGER | PRIMARY KEY AUTOINCREMENT | Mã định danh nhân viên |
| `code` | TEXT | UNIQUE, NOT NULL | Mã nhân viên (VD: `NV001`, `NV002`) |
| `fullname` | TEXT | NOT NULL | Họ và tên nhân viên |
| `email` | TEXT | | Địa chỉ Email |
| `phone` | TEXT | | Số điện thoại |
| `hourlyRate` | REAL | NOT NULL DEFAULT 0 | Mức lương theo giờ (VNĐ/giờ) |
| `accountId` | INTEGER | FOREIGN KEY REFERENCES Account(id) | Khóa ngoại liên kết tài khoản đăng nhập |

### 1.3. Bảng `WorkLog` (Chấm công & Giờ làm việc)
| Trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `id` | INTEGER | PRIMARY KEY AUTOINCREMENT | Mã định danh bản ghi chấm công |
| `employeeId` | INTEGER | FOREIGN KEY REFERENCES Employee(id) | Khóa ngoại liên kết nhân viên |
| `workDate` | TEXT | NOT NULL | Ngày làm việc (`yyyy-MM-dd`) |
| `checkInTime` | TEXT | NOT NULL | Giờ vào ca (`HH:mm:ss`) |
| `checkOutTime` | TEXT | | Giờ ra ca (`HH:mm:ss`) |
| `totalHours` | REAL | DEFAULT 0 | Tổng số giờ làm việc (làm tròn 2 chữ số) |
| `status` | TEXT | NOT NULL | Trạng thái: `CHECKED_IN`, `COMPLETED`, `INCOMPLETE` |

### 1.4. Bảng `Salary` (Bảng lương tháng)
| Trường | Kiểu dữ liệu | Ràng buộc | Mô tả |
| :--- | :--- | :--- | :--- |
| `id` | INTEGER | PRIMARY KEY AUTOINCREMENT | Mã định danh bản ghi lương |
| `employeeId` | INTEGER | FOREIGN KEY REFERENCES Employee(id) | Khóa ngoại liên kết nhân viên |
| `month` | INTEGER | NOT NULL | Tháng tính lương (1 - 12) |
| `year` | INTEGER | NOT NULL | Năm tính lương |
| `totalHours` | REAL | DEFAULT 0 | Tổng số giờ làm trong tháng |
| `overtimeHours`| REAL | DEFAULT 0 | Số giờ làm thêm (OT) trong tháng |
| `totalSalary` | REAL | DEFAULT 0 | Tổng thực lĩnh |
| `status` | TEXT | NOT NULL | Trạng thái thanh toán: `PENDING`, `PAID` |

---

## 2. Các Tính Năng Đã Thực Hiện Theo Yêu Cầu

### 2.1. Đăng nhập & Phân quyền (Login & Role-based Access)
- Hỗ trợ 2 vai trò riêng biệt:
  - **Quản trị viên (Admin)**: Quản lý toàn diện nhân sự, theo dõi chấm công toàn bộ công ty, tính và quản lý bảng lương.
  - **Nhân viên (Employee)**: Điểm danh vào/ra ca, xem lịch sử chấm công cá nhân, theo dõi bảng lương và hồ sơ cá nhân.
- Nút bấm đăng nhập nhanh (Quick Fill) tài khoản mẫu Admin và Nhân viên để giảng viên/người dùng kiểm thử nhanh chóng.

### 2.2. Lưu trữ phiên đăng nhập (SharedPreferences)
- Sử dụng `SessionManager` với `SharedPreferences` để lưu:
  - Trạng thái đăng nhập (`isLoggedIn`).
  - Lựa chọn ghi nhớ đăng nhập (`rememberMe`).
  - Thông tin tài khoản: `accountId`, `username`, `role`, `employeeId`, `fullName`.
- Tự động điều hướng đến đúng giao diện theo vai trò khi mở ứng dụng nếu đã đăng nhập.

### 2.3. Quản lý Nhân viên (Employee Management - Admin)
- **Thêm nhân viên**: Tự động sinh tài khoản đăng nhập tương ứng với vai trò `EMPLOYEE`.
- **Cập nhật thông tin**: Sửa thông tin cá nhân, mức lương theo giờ và đổi mật khẩu cho nhân viên.
- **Xóa nhân viên**: Có hộp thoại xác nhận (AlertDialog) và cơ chế xóa liên hoàn (cascade) dọn dẹp tài khoản, chấm công và bảng lương.
- **Tìm kiếm thời gian thực**: Tìm nhanh theo Họ tên, Mã nhân viên, Số điện thoại hoặc Email.

### 2.4. Quản lý Chấm công (Attendance Management)
- **Đồng hồ số thời gian thực**: Hiển thị ngày và giờ (`HH:mm:ss`) chạy mượt mà từng giây.
- **Điểm danh vào (Check In)**: Ghi nhận ngày làm việc và giờ vào chính xác.
- **Điểm danh ra (Check Out)**: Ghi nhận giờ ra, tự động tính tổng số giờ làm việc.
- **Ràng buộc dữ liệu & Kiểm tra hợp lệ (Data Validation)**:
  - Ngăn chặn điểm danh vào trùng lặp nếu ca hiện tại vẫn đang hoạt động (`CHECKED_IN`).
  - Cảnh báo nếu đã hoàn thành ca trong ngày và hỏi người dùng có muốn tạo ca mới không.
  - Ngăn chặn điểm danh ra khi chưa điểm danh vào.
  - Kiểm tra giờ ra phải sau giờ vào (`checkOutTime > checkInTime`).
  - Tự động chuyển các ca hôm trước quên điểm danh ra thành trạng thái `INCOMPLETE` (Chưa hoàn tất).

### 2.5. Tự Động Tính Giờ & Tính Lương (Salary & Overtime Calculation)
- **Công thức tính giờ**: `totalHours = (checkOutMillis - checkInMillis) / 3600000.0`.
- **Tính làm thêm giờ (Overtime)**:
  - Tiêu chuẩn làm việc: 8.0 giờ/ngày.
  - Số giờ vượt quá 8 giờ trong ngày được tính là giờ làm thêm: `overtimeDay = dailyHours - 8.0`.
  - Hệ số lương làm thêm: **150% (x1.5)** mức lương theo giờ.
- **Công thức lương tháng**:
  $$\text{Tổng lương} = (\text{Giờ tiêu chuẩn} \times \text{Lương cơ sở}) + (\text{Giờ làm thêm} \times \text{Lương cơ sở} \times 1.5)$$
- **Quản trị viên**:
  - Tính lương hàng loạt cho toàn bộ nhân viên theo tháng/năm chỉ với 1 click.
  - Chuyển đổi trạng thái thanh toán: `CHỜ THANH TOÁN` (Pending) $\leftrightarrow$ `ĐÃ THANH TOÁN` (Paid).
- **Nhân viên**:
  - Xem chi tiết bảng lương từng tháng: Mức lương cơ sở, Giờ tiêu chuẩn, Giờ làm thêm (x1.5), Tổng thực lĩnh và Trạng thái thanh toán.

---

## 3. Tài Khoản Mẫu Để Kiểm Thử (Seed Accounts)

Khi ứng dụng chạy lần đầu, cơ sở dữ liệu SQLite tự động khởi tạo dữ liệu mẫu:

| Vai trò | Tên đăng nhập | Mật khẩu | Mã NV | Họ và tên | Lương / giờ |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Admin** | `admin` | `admin123` | - | Quản trị viên | - |
| **Employee** | `nv01` | `123456` | `NV001` | Nguyễn Văn An | 50.000 đ/giờ |
| **Employee** | `nv02` | `123456` | `NV002` | Trần Thị Bình | 60.000 đ/giờ |
| **Employee** | `nv03` | `123456` | `NV003` | Lê Hoàng Cường | 55.000 đ/giờ |

*(Đã có sẵn dữ liệu chấm công và lương mẫu của tháng trước và tháng hiện tại để xem báo cáo ngay khi đăng nhập!)*
