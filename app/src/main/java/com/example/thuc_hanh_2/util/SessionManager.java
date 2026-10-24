package com.example.thuc_hanh_2.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.thuc_hanh_2.model.Account;
import com.example.thuc_hanh_2.model.Employee;

public class SessionManager {
    private static final String PREF_NAME = "EmployeeWorkLogPref";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_REMEMBER_ME = "rememberMe";
    private static final String KEY_ACCOUNT_ID = "accountId";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_ROLE = "role";
    private static final String KEY_EMPLOYEE_ID = "employeeId";
    private static final String KEY_FULL_NAME = "fullName";
    private static final String KEY_EMPLOYEE_CODE = "employeeCode";

    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;
    private final Context context;

    public SessionManager(Context context) {
        this.context = context;
        this.pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.editor = pref.edit();
    }

    public void createLoginSession(Account account, Employee employee, boolean rememberMe) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putBoolean(KEY_REMEMBER_ME, rememberMe);
        editor.putInt(KEY_ACCOUNT_ID, account.getId());
        editor.putString(KEY_USERNAME, account.getUsername());
        editor.putString(KEY_ROLE, account.getRole());

        if (employee != null) {
            editor.putInt(KEY_EMPLOYEE_ID, employee.getId());
            editor.putString(KEY_FULL_NAME, employee.getFullname());
            editor.putString(KEY_EMPLOYEE_CODE, employee.getCode());
        } else {
            editor.putInt(KEY_EMPLOYEE_ID, -1);
            editor.putString(KEY_FULL_NAME, account.getUsername());
            editor.putString(KEY_EMPLOYEE_CODE, "");
        }
        editor.apply();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public boolean isRememberMe() {
        return pref.getBoolean(KEY_REMEMBER_ME, false);
    }

    public int getAccountId() {
        return pref.getInt(KEY_ACCOUNT_ID, -1);
    }

    public String getUsername() {
        return pref.getString(KEY_USERNAME, "");
    }

    public String getRole() {
        return pref.getString(KEY_ROLE, "");
    }

    public int getEmployeeId() {
        return pref.getInt(KEY_EMPLOYEE_ID, -1);
    }

    public String getFullName() {
        return pref.getString(KEY_FULL_NAME, "");
    }

    public String getEmployeeCode() {
        return pref.getString(KEY_EMPLOYEE_CODE, "");
    }

    public boolean isAdmin() {
        return Account.ROLE_ADMIN.equalsIgnoreCase(getRole());
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }
}
