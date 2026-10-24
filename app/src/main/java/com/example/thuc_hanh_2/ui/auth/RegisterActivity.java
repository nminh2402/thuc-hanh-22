package com.example.thuc_hanh_2.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.thuc_hanh_2.R;
import com.example.thuc_hanh_2.database.DatabaseHelper;
import com.example.thuc_hanh_2.model.Employee;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {

    public static final String EXTRA_REGISTERED_USERNAME = "extra_registered_username";

    private ImageButton btnBack;
    private TextInputEditText etFullName, etCode, etPhone, etEmail, etRate;
    private TextInputEditText etUsername, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvGoToLogin;

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = DatabaseHelper.getInstance(this);

        initViews();
        setupListeners();
        suggestEmployeeCode();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBackFromRegister);
        etFullName = findViewById(R.id.etRegFullName);
        etCode = findViewById(R.id.etRegCode);
        etPhone = findViewById(R.id.etRegPhone);
        etEmail = findViewById(R.id.etRegEmail);
        etRate = findViewById(R.id.etRegRate);
        etUsername = findViewById(R.id.etRegUsername);
        etPassword = findViewById(R.id.etRegPassword);
        etConfirmPassword = findViewById(R.id.etRegConfirmPassword);
        btnRegister = findViewById(R.id.btnDoRegister);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);
    }

    private void suggestEmployeeCode() {
        String nextCode = dbHelper.getNextEmployeeCode();
        if (nextCode != null && !nextCode.isEmpty()) {
            etCode.setText(nextCode);
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        tvGoToLogin.setOnClickListener(v -> finish());
        btnRegister.setOnClickListener(v -> performRegister());
    }

    private void performRegister() {
        String fullname = etFullName.getText() != null ? etFullName.getText().toString().trim() : "";
        String code = etCode.getText() != null ? etCode.getText().toString().trim().toUpperCase() : "";
        String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String rateStr = etRate.getText() != null ? etRate.getText().toString().trim() : "";
        String username = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim() : "";

        // 1. Validate Full Name
        if (fullname.isEmpty()) {
            etFullName.setError("Vui lòng nhập họ và tên");
            etFullName.requestFocus();
            return;
        }

        // 2. Validate Code
        if (code.isEmpty()) {
            etCode.setError("Vui lòng nhập mã nhân viên");
            etCode.requestFocus();
            return;
        }

        if (dbHelper.isEmployeeCodeTaken(code, -1)) {
            etCode.setError("Mã nhân viên này đã tồn tại trong hệ thống");
            etCode.requestFocus();
            return;
        }

        // 3. Validate Hourly Rate
        double hourlyRate = 50000.0;
        if (!rateStr.isEmpty()) {
            try {
                hourlyRate = Double.parseDouble(rateStr);
                if (hourlyRate <= 0) {
                    etRate.setError("Mức lương phải lớn hơn 0");
                    etRate.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                etRate.setError("Mức lương không hợp lệ");
                etRate.requestFocus();
                return;
            }
        }

        // 4. Validate Username
        if (username.isEmpty()) {
            etUsername.setError("Vui lòng nhập tên đăng nhập");
            etUsername.requestFocus();
            return;
        }

        if (dbHelper.isUsernameTaken(username, -1)) {
            etUsername.setError("Tên đăng nhập này đã được sử dụng");
            etUsername.requestFocus();
            return;
        }

        // 5. Validate Password
        if (password.isEmpty() || password.length() < 4) {
            etPassword.setError("Mật khẩu tối thiểu 4 ký tự");
            etPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Mật khẩu xác nhận không khớp");
            etConfirmPassword.requestFocus();
            return;
        }

        // Save to SQLite Database
        Employee employee = new Employee(code, fullname, email, phone, hourlyRate, 0);
        long newEmpId = dbHelper.addEmployeeWithAccount(employee, username, password);

        if (newEmpId > 0) {
            Toast.makeText(this, "Đăng ký thành công! Vui lòng đăng nhập với tài khoản mới.", Toast.LENGTH_LONG).show();

            Intent resultIntent = new Intent();
            resultIntent.putExtra(EXTRA_REGISTERED_USERNAME, username);
            setResult(RESULT_OK, resultIntent);
            finish();
        } else {
            Toast.makeText(this, "Đăng ký thất bại, vui lòng kiểm tra lại thông tin!", Toast.LENGTH_SHORT).show();
        }
    }
}
