package com.example.thuc_hanh_2.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.thuc_hanh_2.R;
import com.example.thuc_hanh_2.database.DatabaseHelper;
import com.example.thuc_hanh_2.model.Account;
import com.example.thuc_hanh_2.model.Employee;
import com.example.thuc_hanh_2.ui.admin.AdminMainActivity;
import com.example.thuc_hanh_2.ui.employee.EmployeeMainActivity;
import com.example.thuc_hanh_2.util.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etPassword;
    private CheckBox cbRememberMe;
    private Button btnLogin, btnQuickAdmin, btnQuickEmp;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);
        dbHelper = DatabaseHelper.getInstance(this);

        // Check if already logged in and remember me was enabled
        if (sessionManager.isLoggedIn() && sessionManager.isRememberMe()) {
            navigateByRole(sessionManager.getRole());
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        initViews();
        setupListeners();
    }

    private void initViews() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        cbRememberMe = findViewById(R.id.cbRememberMe);
        btnLogin = findViewById(R.id.btnLogin);
        btnQuickAdmin = findViewById(R.id.btnQuickAdmin);
        btnQuickEmp = findViewById(R.id.btnQuickEmp);

        cbRememberMe.setChecked(true); // default true for better UX
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> performLogin());

        btnQuickAdmin.setOnClickListener(v -> {
            etUsername.setText("admin");
            etPassword.setText("admin123");
            performLogin();
        });

        btnQuickEmp.setOnClickListener(v -> {
            etUsername.setText("nv01");
            etPassword.setText("123456");
            performLogin();
        });
    }

    private void performLogin() {
        String username = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        if (username.isEmpty()) {
            etUsername.setError("Vui lòng nhập tên đăng nhập");
            etUsername.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Vui lòng nhập mật khẩu");
            etPassword.requestFocus();
            return;
        }

        Account account = dbHelper.checkLogin(username, password);
        if (account == null) {
            Toast.makeText(this, "Tên đăng nhập hoặc mật khẩu không chính xác!", Toast.LENGTH_LONG).show();
            return;
        }

        Employee employee = null;
        if (Account.ROLE_EMPLOYEE.equalsIgnoreCase(account.getRole())) {
            employee = dbHelper.getEmployeeByAccountId(account.getId());
        }

        boolean rememberMe = cbRememberMe.isChecked();
        sessionManager.createLoginSession(account, employee, rememberMe);

        Toast.makeText(this, "Đăng nhập thành công với vai trò " + account.getRole(), Toast.LENGTH_SHORT).show();
        navigateByRole(account.getRole());
        finish();
    }

    private void navigateByRole(String role) {
        if (Account.ROLE_ADMIN.equalsIgnoreCase(role)) {
            Intent intent = new Intent(this, AdminMainActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, EmployeeMainActivity.class);
            startActivity(intent);
        }
    }
}
