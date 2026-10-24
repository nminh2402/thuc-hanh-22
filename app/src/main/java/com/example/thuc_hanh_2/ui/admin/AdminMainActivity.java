package com.example.thuc_hanh_2.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.thuc_hanh_2.R;
import com.example.thuc_hanh_2.adapter.AttendanceAdapter;
import com.example.thuc_hanh_2.adapter.EmployeeAdapter;
import com.example.thuc_hanh_2.adapter.SalaryAdapter;
import com.example.thuc_hanh_2.database.DatabaseHelper;
import com.example.thuc_hanh_2.model.Employee;
import com.example.thuc_hanh_2.model.Salary;
import com.example.thuc_hanh_2.model.WorkLog;
import com.example.thuc_hanh_2.ui.auth.LoginActivity;
import com.example.thuc_hanh_2.util.DateTimeUtils;
import com.example.thuc_hanh_2.util.FormatUtils;
import com.example.thuc_hanh_2.util.SessionManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdminMainActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private DatabaseHelper dbHelper;

    // Header views
    private TextView tvAdminGreeting, tvAdminLiveClock, tvStatTotalEmployees, tvStatTodayLogs, tvStatCurrentMonth;
    private ImageButton btnAdminLogout;
    private android.os.Handler clockHandler;
    private Runnable clockRunnable;

    // Tabs
    private TextView tabEmployees, tabAttendance, tabSalary;
    private LinearLayout layoutEmployeesTab, layoutAttendanceTab, layoutSalaryTab;

    // Tab 1: Employees
    private EditText etSearchEmployee;
    private Button btnAddEmployee;
    private TextView tvEmployeeCount, tvEmptyEmployees;
    private RecyclerView rvEmployees;
    private EmployeeAdapter employeeAdapter;
    private List<Employee> currentEmployees = new ArrayList<>();

    // Tab 2: Attendance
    private Spinner spinnerAttMonth, spinnerAttYear, spinnerAttEmployee;
    private RecyclerView rvAttendanceLogs;
    private TextView tvEmptyAttendance;
    private AttendanceAdapter attendanceAdapter;
    private List<Employee> employeeFilterList = new ArrayList<>();

    // Tab 3: Salary
    private Spinner spinnerSalMonth, spinnerSalYear;
    private Button btnCalculateAllSalaries;
    private TextView tvTotalPayrollSum, tvEmptySalaries;
    private RecyclerView rvSalaries;
    private SalaryAdapter salaryAdapter;

    private int selectedMonth;
    private int selectedYear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        sessionManager = new SessionManager(this);
        dbHelper = DatabaseHelper.getInstance(this);

        // Security check
        if (!sessionManager.isLoggedIn() || !sessionManager.isAdmin()) {
            logout();
            return;
        }

        // Clean up any past unclosed check-ins
        dbHelper.markPastIncompleteLogs(DateTimeUtils.getTodayDate());

        initCalendarDefaults();
        initViews();
        setupTabs();
        setupEmployeeTab();
        setupAttendanceTab();
        setupSalaryTab();
        setupListeners();

        loadAllData();
    }

    private void initCalendarDefaults() {
        Calendar cal = Calendar.getInstance();
        selectedMonth = cal.get(Calendar.MONTH) + 1;
        selectedYear = cal.get(Calendar.YEAR);
    }

    private void initViews() {
        tvAdminGreeting = findViewById(R.id.tvAdminGreeting);
        tvAdminLiveClock = findViewById(R.id.tvAdminLiveClock);
        tvStatTotalEmployees = findViewById(R.id.tvStatTotalEmployees);
        tvStatTodayLogs = findViewById(R.id.tvStatTodayLogs);
        tvStatCurrentMonth = findViewById(R.id.tvStatCurrentMonth);
        btnAdminLogout = findViewById(R.id.btnAdminLogout);

        tabEmployees = findViewById(R.id.tabEmployees);
        tabAttendance = findViewById(R.id.tabAttendance);
        tabSalary = findViewById(R.id.tabSalary);

        layoutEmployeesTab = findViewById(R.id.layoutEmployeesTab);
        layoutAttendanceTab = findViewById(R.id.layoutAttendanceTab);
        layoutSalaryTab = findViewById(R.id.layoutSalaryTab);

        tvAdminGreeting.setText("Xin chào, " + sessionManager.getUsername());
        tvStatCurrentMonth.setText("T" + selectedMonth + "/" + selectedYear);

        startLiveClock();
    }

    private void startLiveClock() {
        clockHandler = new android.os.Handler(android.os.Looper.getMainLooper());
        clockRunnable = new Runnable() {
            @Override
            public void run() {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm:ss • dd/MM/yyyy", Locale.getDefault());
                if (tvAdminLiveClock != null) {
                    tvAdminLiveClock.setText("🕒 " + sdf.format(new java.util.Date()));
                }
                clockHandler.postDelayed(this, 1000);
            }
        };
        clockHandler.post(clockRunnable);
    }

    private void setupTabs() {
        tabEmployees.setOnClickListener(v -> switchTab(0));
        tabAttendance.setOnClickListener(v -> switchTab(1));
        tabSalary.setOnClickListener(v -> switchTab(2));
    }

    private void switchTab(int tabIndex) {
        int primaryColor = ContextCompat.getColor(this, R.color.primary);
        int secondaryColor = ContextCompat.getColor(this, R.color.text_secondary);

        tabEmployees.setTextColor(tabIndex == 0 ? primaryColor : secondaryColor);
        tabAttendance.setTextColor(tabIndex == 1 ? primaryColor : secondaryColor);
        tabSalary.setTextColor(tabIndex == 2 ? primaryColor : secondaryColor);

        layoutEmployeesTab.setVisibility(tabIndex == 0 ? View.VISIBLE : View.GONE);
        layoutAttendanceTab.setVisibility(tabIndex == 1 ? View.VISIBLE : View.GONE);
        layoutSalaryTab.setVisibility(tabIndex == 2 ? View.VISIBLE : View.GONE);

        if (tabIndex == 0) loadEmployees();
        else if (tabIndex == 1) loadAttendanceLogs();
        else if (tabIndex == 2) loadSalaries();
    }

    // =========================================================================
    // TAB 1: EMPLOYEE MANAGEMENT
    // =========================================================================

    private void setupEmployeeTab() {
        etSearchEmployee = findViewById(R.id.etSearchEmployee);
        btnAddEmployee = findViewById(R.id.btnAddEmployee);
        tvEmployeeCount = findViewById(R.id.tvEmployeeCount);
        tvEmptyEmployees = findViewById(R.id.tvEmptyEmployees);
        rvEmployees = findViewById(R.id.rvEmployees);

        rvEmployees.setLayoutManager(new LinearLayoutManager(this));
        employeeAdapter = new EmployeeAdapter(new EmployeeAdapter.OnEmployeeActionListener() {
            @Override
            public void onEdit(Employee employee) {
                Intent intent = new Intent(AdminMainActivity.this, AddEditEmployeeActivity.class);
                intent.putExtra(AddEditEmployeeActivity.EXTRA_EMPLOYEE_ID, employee.getId());
                startActivity(intent);
            }

            @Override
            public void onDelete(Employee employee) {
                showDeleteEmployeeDialog(employee);
            }
        });
        rvEmployees.setAdapter(employeeAdapter);

        btnAddEmployee.setOnClickListener(v -> {
            Intent intent = new Intent(AdminMainActivity.this, AddEditEmployeeActivity.class);
            startActivity(intent);
        });

        etSearchEmployee.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchEmployees(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadEmployees() {
        currentEmployees = dbHelper.getAllEmployees();
        tvEmployeeCount.setText("Danh sách: " + currentEmployees.size() + " nhân viên");
        employeeAdapter.setEmployees(currentEmployees);
        tvEmptyEmployees.setVisibility(currentEmployees.isEmpty() ? View.VISIBLE : View.GONE);
        tvStatTotalEmployees.setText(String.valueOf(currentEmployees.size()));
    }

    private void searchEmployees(String keyword) {
        if (keyword.trim().isEmpty()) {
            employeeAdapter.setEmployees(currentEmployees);
            tvEmptyEmployees.setVisibility(currentEmployees.isEmpty() ? View.VISIBLE : View.GONE);
        } else {
            List<Employee> results = dbHelper.searchEmployees(keyword);
            employeeAdapter.setEmployees(results);
            tvEmptyEmployees.setVisibility(results.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private void showDeleteEmployeeDialog(Employee emp) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa nhân viên")
                .setMessage("Bạn có chắc chắn muốn xóa nhân viên '" + emp.getFullname() + " (" + emp.getCode() + ")' không?\n\nToàn bộ tài khoản đăng nhập, lịch sử chấm công và bảng lương của nhân viên này cũng sẽ bị xóa.")
                .setPositiveButton("Xóa vĩnh viễn", (dialog, which) -> {
                    boolean success = dbHelper.deleteEmployee(emp.getId());
                    if (success) {
                        Toast.makeText(this, "Đã xóa nhân viên " + emp.getFullname(), Toast.LENGTH_SHORT).show();
                        loadEmployees();
                        updateDashboardStats();
                        setupAttendanceEmployeeSpinner();
                    } else {
                        Toast.makeText(this, "Xóa không thành công!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // =========================================================================
    // TAB 2: ATTENDANCE MONITOR
    // =========================================================================

    private void setupAttendanceTab() {
        spinnerAttMonth = findViewById(R.id.spinnerAttMonth);
        spinnerAttYear = findViewById(R.id.spinnerAttYear);
        spinnerAttEmployee = findViewById(R.id.spinnerAttEmployee);
        rvAttendanceLogs = findViewById(R.id.rvAttendanceLogs);
        tvEmptyAttendance = findViewById(R.id.tvEmptyAttendance);

        rvAttendanceLogs.setLayoutManager(new LinearLayoutManager(this));
        attendanceAdapter = new AttendanceAdapter(true); // true = show employee name
        rvAttendanceLogs.setAdapter(attendanceAdapter);

        setupMonthYearSpinners(spinnerAttMonth, spinnerAttYear, (month, year) -> loadAttendanceLogs());
        setupAttendanceEmployeeSpinner();
    }

    private void setupAttendanceEmployeeSpinner() {
        employeeFilterList = dbHelper.getAllEmployees();
        List<String> names = new ArrayList<>();
        names.add("Tất cả nhân viên");
        for (Employee e : employeeFilterList) {
            names.add(e.getCode() + " - " + e.getFullname());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, names);
        spinnerAttEmployee.setAdapter(adapter);

        spinnerAttEmployee.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadAttendanceLogs();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadAttendanceLogs() {
        int month = getSelectedSpinnerMonth(spinnerAttMonth);
        int year = getSelectedSpinnerYear(spinnerAttYear);

        int selectedEmpPos = spinnerAttEmployee.getSelectedItemPosition();
        int filterEmployeeId = -1;
        if (selectedEmpPos > 0 && selectedEmpPos - 1 < employeeFilterList.size()) {
            filterEmployeeId = employeeFilterList.get(selectedEmpPos - 1).getId();
        }

        List<WorkLog> logs = dbHelper.getAllWorkLogs(month, year, filterEmployeeId);
        attendanceAdapter.setLogs(logs);
        tvEmptyAttendance.setVisibility(logs.isEmpty() ? View.VISIBLE : View.GONE);
    }

    // =========================================================================
    // TAB 3: SALARY MANAGEMENT
    // =========================================================================

    private void setupSalaryTab() {
        spinnerSalMonth = findViewById(R.id.spinnerSalMonth);
        spinnerSalYear = findViewById(R.id.spinnerSalYear);
        btnCalculateAllSalaries = findViewById(R.id.btnCalculateAllSalaries);
        tvTotalPayrollSum = findViewById(R.id.tvTotalPayrollSum);
        tvEmptySalaries = findViewById(R.id.tvEmptySalaries);
        rvSalaries = findViewById(R.id.rvSalaries);

        rvSalaries.setLayoutManager(new LinearLayoutManager(this));
        salaryAdapter = new SalaryAdapter(salary -> {
            String newStatus = Salary.STATUS_PAID.equalsIgnoreCase(salary.getStatus()) ? Salary.STATUS_PENDING : Salary.STATUS_PAID;
            dbHelper.updateSalaryStatus(salary.getId(), newStatus);
            loadSalaries();
        });
        rvSalaries.setAdapter(salaryAdapter);

        setupMonthYearSpinners(spinnerSalMonth, spinnerSalYear, (month, year) -> loadSalaries());

        btnCalculateAllSalaries.setOnClickListener(v -> {
            int month = getSelectedSpinnerMonth(spinnerSalMonth);
            int year = getSelectedSpinnerYear(spinnerSalYear);
            int count = dbHelper.calculateSalaryForAllEmployees(month, year);
            Toast.makeText(this, "Đã tính lương cho " + count + " nhân viên!", Toast.LENGTH_SHORT).show();
            loadSalaries();
        });
    }

    private void loadSalaries() {
        int month = getSelectedSpinnerMonth(spinnerSalMonth);
        int year = getSelectedSpinnerYear(spinnerSalYear);

        List<Salary> salaries = dbHelper.getSalariesByMonthYear(month, year);
        salaryAdapter.setSalaries(salaries);
        tvEmptySalaries.setVisibility(salaries.isEmpty() ? View.VISIBLE : View.GONE);

        double totalSum = 0;
        for (Salary s : salaries) {
            totalSum += s.getTotalSalary();
        }
        tvTotalPayrollSum.setText(FormatUtils.formatCurrency(totalSum));
    }

    // =========================================================================
    // COMMON HELPERS & SPINNERS
    // =========================================================================

    private interface OnMonthYearChangedListener {
        void onDateChanged(int month, int year);
    }

    private void setupMonthYearSpinners(Spinner spMonth, Spinner spYear, OnMonthYearChangedListener listener) {
        List<String> months = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            months.add("Tháng " + i);
        }
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, months);
        spMonth.setAdapter(monthAdapter);
        spMonth.setSelection(selectedMonth - 1);

        List<String> years = new ArrayList<>();
        for (int y = selectedYear - 2; y <= selectedYear + 2; y++) {
            years.add(String.valueOf(y));
        }
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, years);
        spYear.setAdapter(yearAdapter);
        spYear.setSelection(2); // selectedYear

        AdapterView.OnItemSelectedListener itemListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (listener != null) {
                    listener.onDateChanged(getSelectedSpinnerMonth(spMonth), getSelectedSpinnerYear(spYear));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        spMonth.setOnItemSelectedListener(itemListener);
        spYear.setOnItemSelectedListener(itemListener);
    }

    private int getSelectedSpinnerMonth(Spinner spMonth) {
        return spMonth.getSelectedItemPosition() + 1;
    }

    private int getSelectedSpinnerYear(Spinner spYear) {
        if (spYear.getSelectedItem() == null) return selectedYear;
        try {
            return Integer.parseInt(spYear.getSelectedItem().toString());
        } catch (NumberFormatException e) {
            return selectedYear;
        }
    }

    private void setupListeners() {
        btnAdminLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có muốn đăng xuất khỏi tài khoản Quản trị viên?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> logout())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void logout() {
        sessionManager.logout();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void loadAllData() {
        loadEmployees();
        loadAttendanceLogs();
        loadSalaries();
        updateDashboardStats();
    }

    private void updateDashboardStats() {
        tvStatTotalEmployees.setText(String.valueOf(dbHelper.getAllEmployees().size()));
        String today = DateTimeUtils.getTodayDate();
        List<WorkLog> todayLogs = dbHelper.getAllWorkLogs(selectedMonth, selectedYear, -1);
        int todayCount = 0;
        for (WorkLog l : todayLogs) {
            if (today.equals(l.getWorkDate())) todayCount++;
        }
        tvStatTodayLogs.setText(String.valueOf(todayCount));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAllData();
        setupAttendanceEmployeeSpinner();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (clockHandler != null && clockRunnable != null) {
            clockHandler.removeCallbacks(clockRunnable);
        }
    }
}
