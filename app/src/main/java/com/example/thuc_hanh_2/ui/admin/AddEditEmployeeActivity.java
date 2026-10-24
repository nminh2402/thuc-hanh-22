package com.example.thuc_hanh_2.ui.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.thuc_hanh_2.R;
import com.example.thuc_hanh_2.database.DatabaseHelper;
import com.example.thuc_hanh_2.model.Employee;
import com.google.android.material.textfield.TextInputEditText;

public class AddEditEmployeeActivity extends AppCompatActivity {

    public static final String EXTRA_EMPLOYEE_ID = "extra_employee_id";

    private TextView tvTitle, tvPasswordHint;
    private ImageButton btnBack;
    private TextInputEditText etCode, etFullName, etEmail, etPhone, etRate, etUsername, etPassword;
    private Button btnSave, btnCancel;

    private DatabaseHelper dbHelper;
    private int employeeId = -1;
    private Employee existingEmployee;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_employee);

        dbHelper = DatabaseHelper.getInstance(this);

        initViews();

        if (getIntent().hasExtra(EXTRA_EMPLOYEE_ID)) {
            employeeId = getIntent().getIntExtra(EXTRA_EMPLOYEE_ID, -1);
            existingEmployee = dbHelper.getEmployeeById(employeeId);
        }

        setupForm();
        setupListeners();
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tvAddEditTitle);
        tvPasswordHint = findViewById(R.id.tvPasswordHint);
        btnBack = findViewById(R.id.btnBackAddEdit);

        etCode = findViewById(R.id.etEmpCode);
        etFullName = findViewById(R.id.etEmpFullName);
        etEmail = findViewById(R.id.etEmpEmail);
        etPhone = findViewById(R.id.etEmpPhone);
        etRate = findViewById(R.id.etEmpRate);
        etUsername = findViewById(R.id.etEmpUsername);
        etPassword = findViewById(R.id.etEmpPassword);

        btnSave = findViewById(R.id.btnSaveEmployee);
        btnCancel = findViewById(R.id.btnCancelEmployee);
    }

    private void setupForm() {
        if (existingEmployee != null) {
            tvTitle.setText("Chỉnh Sửa Nhân Viên");
            tvPasswordHint.setVisibility(View.VISIBLE);

            etCode.setText(existingEmployee.getCode());
            etFullName.setText(existingEmployee.getFullname());
            etEmail.setText(existingEmployee.getEmail());
            etPhone.setText(existingEmployee.getPhone());
            etRate.setText(String.valueOf(existingEmployee.getHourlyRate()));
            etUsername.setText(existingEmployee.getUsername());
            etUsername.setEnabled(false); // Username is identity, don't allow modifying username directly
            etPassword.setHint("Mật khẩu mới (tùy chọn)");
        } else {
            tvTitle.setText("Thêm Nhân Viên Mới");
            tvPasswordHint.setVisibility(View.GONE);
            etUsername.setEnabled(true);
            etRate.setText("50000"); // sensible default
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnCancel.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveEmployee());
    }

    private void saveEmployee() {
        String code = etCode.getText() != null ? etCode.getText().toString().trim() : "";
        String fullname = etFullName.getText() != null ? etFullName.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
        String rateStr = etRate.getText() != null ? etRate.getText().toString().trim() : "";
        String username = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        // Validations
        if (code.isEmpty()) {
            etCode.setError("Vui lòng nhập mã nhân viên");
            etCode.requestFocus();
            return;
        }

        if (dbHelper.isEmployeeCodeTaken(code, employeeId)) {
            etCode.setError("Mã nhân viên này đã tồn tại!");
            etCode.requestFocus();
            return;
        }

        if (fullname.isEmpty()) {
            etFullName.setError("Vui lòng nhập họ tên nhân viên");
            etFullName.requestFocus();
            return;
        }

        double hourlyRate;
        try {
            hourlyRate = Double.parseDouble(rateStr);
            if (hourlyRate <= 0) {
                etRate.setError("Mức lương theo giờ phải lớn hơn 0");
                etRate.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etRate.setError("Mức lương theo giờ không hợp lệ");
            etRate.requestFocus();
            return;
        }

        if (existingEmployee == null) {
            // New Employee Validations
            if (username.isEmpty()) {
                etUsername.setError("Vui lòng nhập tên đăng nhập");
                etUsername.requestFocus();
                return;
            }

            if (dbHelper.isUsernameTaken(username, -1)) {
                etUsername.setError("Tên đăng nhập này đã có người sử dụng!");
                etUsername.requestFocus();
                return;
            }

            if (password.isEmpty() || password.length() < 4) {
                etPassword.setError("Mật khẩu tối thiểu 4 ký tự");
                etPassword.requestFocus();
                return;
            }

            Employee newEmp = new Employee(code, fullname, email, phone, hourlyRate, 0);
            long res = dbHelper.addEmployeeWithAccount(newEmp, username, password);
            if (res != -1) {
                Toast.makeText(this, "Thêm nhân viên thành công!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Có lỗi xảy ra khi thêm nhân viên", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Edit Employee
            existingEmployee.setCode(code);
            existingEmployee.setFullname(fullname);
            existingEmployee.setEmail(email);
            existingEmployee.setPhone(phone);
            existingEmployee.setHourlyRate(hourlyRate);

            boolean res = dbHelper.updateEmployee(existingEmployee, password);
            if (res) {
                Toast.makeText(this, "Cập nhật thông tin nhân viên thành công!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Có lỗi xảy ra khi cập nhật nhân viên", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
