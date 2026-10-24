package com.example.thuc_hanh_2;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.thuc_hanh_2.model.Account;
import com.example.thuc_hanh_2.ui.admin.AdminMainActivity;
import com.example.thuc_hanh_2.ui.auth.LoginActivity;
import com.example.thuc_hanh_2.ui.employee.EmployeeMainActivity;
import com.example.thuc_hanh_2.util.SessionManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SessionManager sessionManager = new SessionManager(this);

        Intent intent;
        if (sessionManager.isLoggedIn() && sessionManager.isRememberMe()) {
            if (Account.ROLE_ADMIN.equalsIgnoreCase(sessionManager.getRole())) {
                intent = new Intent(this, AdminMainActivity.class);
            } else {
                intent = new Intent(this, EmployeeMainActivity.class);
            }
        } else {
            intent = new Intent(this, LoginActivity.class);
        }

        startActivity(intent);
        finish();
    }
}