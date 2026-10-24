package com.example.thuc_hanh_2.ui.employee;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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
import com.example.thuc_hanh_2.database.DatabaseHelper;
import com.example.thuc_hanh_2.model.Employee;
import com.example.thuc_hanh_2.model.Salary;
import com.example.thuc_hanh_2.model.WorkLog;
import com.example.thuc_hanh_2.ui.auth.LoginActivity;
import com.example.thuc_hanh_2.util.DateTimeUtils;
import com.example.thuc_hanh_2.util.FormatUtils;
import com.example.thuc_hanh_2.util.SessionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EmployeeMainActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private DatabaseHelper dbHelper;
    private Employee currentEmployee;

    // Header views
    private TextView tvEmpHeaderName, tvEmpHeaderCodeRate;
    private ImageButton btnEmpLogout;

    // Tab buttons & layouts
    private TextView tabEmpCheckInOut, tabEmpHistory, tabEmpSalary, tabEmpProfile;
    private ScrollView layoutEmpCheckInOutTab, layoutEmpSalaryTab, layoutEmpProfileTab;
    private LinearLayout layoutEmpHistoryTab;

    // Tab 1: Check in / Check out
    private TextView tvLiveDate, tvLiveClock, tvTodayStatusBadge;
    private TextView tvTodayCheckInTime, tvTodayCheckOutTime, tvTodayTotalHours;
    private Button btnDoCheckIn, btnDoCheckOut;
    private Handler clockHandler;
    private Runnable clockRunnable;

    // Tab 2: Attendance History
    private Spinner spinnerEmpHistMonth, spinnerEmpHistYear;
    private TextView tvEmpHistTotalDays, tvEmpHistTotalHours, tvEmpHistOtHours, tvEmptyEmpHistory;
    private RecyclerView rvEmpHistoryLogs;
    private AttendanceAdapter historyAdapter;

    // Tab 3: Salary View
    private Spinner spinnerEmpSalMonth, spinnerEmpSalYear;
    private TextView tvEmpSalaryStatusBadge, tvEmpSalHourlyRate, tvEmpSalRegularHours;
    private TextView tvEmpSalOtHours, tvEmpSalTotalHours, tvEmpSalTotalAmount;

    // Tab 4: Profile
    private TextView tvProfileCode, tvProfileFullName, tvProfileEmail, tvProfilePhone;
    private TextView tvProfileHourlyRate, tvProfileUsername;
    private Button btnChangePassword, btnProfileLogout;

    private int selectedMonth;
    private int selectedYear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_main);

        sessionManager = new SessionManager(this);
        dbHelper = DatabaseHelper.getInstance(this);

        if (!sessionManager.isLoggedIn() || sessionManager.getEmployeeId() <= 0) {
            logout();
            return;
        }

        currentEmployee = dbHelper.getEmployeeById(sessionManager.getEmployeeId());
        if (currentEmployee == null) {
            Toast.makeText(this, "Không tìm thấy hồ sơ nhân viên!", Toast.LENGTH_SHORT).show();
            logout();
            return;
        }

        // Clean up any past unclosed check-ins
        dbHelper.markPastIncompleteLogs(DateTimeUtils.getTodayDate());

        initCalendarDefaults();
        initViews();
        setupTabs();
        setupCheckInOutTab();
        setupHistoryTab();
        setupSalaryTab();
        setupProfileTab();
        startLiveClock();

        loadTodayAttendance();
    }

    private void initCalendarDefaults() {
        Calendar cal = Calendar.getInstance();
        selectedMonth = cal.get(Calendar.MONTH) + 1;
        selectedYear = cal.get(Calendar.YEAR);
    }

    private void initViews() {
        tvEmpHeaderName = findViewById(R.id.tvEmpHeaderName);
        tvEmpHeaderCodeRate = findViewById(R.id.tvEmpHeaderCodeRate);
        btnEmpLogout = findViewById(R.id.btnEmpLogout);

        tabEmpCheckInOut = findViewById(R.id.tabEmpCheckInOut);
        tabEmpHistory = findViewById(R.id.tabEmpHistory);
        tabEmpSalary = findViewById(R.id.tabEmpSalary);
        tabEmpProfile = findViewById(R.id.tabEmpProfile);

        layoutEmpCheckInOutTab = findViewById(R.id.layoutEmpCheckInOutTab);
        layoutEmpHistoryTab = findViewById(R.id.layoutEmpHistoryTab);
        layoutEmpSalaryTab = findViewById(R.id.layoutEmpSalaryTab);
        layoutEmpProfileTab = findViewById(R.id.layoutEmpProfileTab);

        updateHeaderInfo();

        btnEmpLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void updateHeaderInfo() {
        if (currentEmployee != null) {
            tvEmpHeaderName.setText(currentEmployee.getFullname());
            String info = "Mã NV: " + currentEmployee.getCode() + " | " + FormatUtils.formatCurrency(currentEmployee.getHourlyRate()) + "/giờ";
            tvEmpHeaderCodeRate.setText(info);
        }
    }

    private void setupTabs() {
        tabEmpCheckInOut.setOnClickListener(v -> switchTab(0));
        tabEmpHistory.setOnClickListener(v -> switchTab(1));
        tabEmpSalary.setOnClickListener(v -> switchTab(2));
        tabEmpProfile.setOnClickListener(v -> switchTab(3));
    }

    private void switchTab(int tabIndex) {
        int primaryColor = ContextCompat.getColor(this, R.color.primary);
        int secondaryColor = ContextCompat.getColor(this, R.color.text_secondary);

        tabEmpCheckInOut.setTextColor(tabIndex == 0 ? primaryColor : secondaryColor);
        tabEmpHistory.setTextColor(tabIndex == 1 ? primaryColor : secondaryColor);
        tabEmpSalary.setTextColor(tabIndex == 2 ? primaryColor : secondaryColor);
        tabEmpProfile.setTextColor(tabIndex == 3 ? primaryColor : secondaryColor);

        layoutEmpCheckInOutTab.setVisibility(tabIndex == 0 ? View.VISIBLE : View.GONE);
        layoutEmpHistoryTab.setVisibility(tabIndex == 1 ? View.VISIBLE : View.GONE);
        layoutEmpSalaryTab.setVisibility(tabIndex == 2 ? View.VISIBLE : View.GONE);
        layoutEmpProfileTab.setVisibility(tabIndex == 3 ? View.VISIBLE : View.GONE);

        if (tabIndex == 0) loadTodayAttendance();
        else if (tabIndex == 1) loadAttendanceHistory();
        else if (tabIndex == 2) loadPersonalSalary();
        else if (tabIndex == 3) loadProfileInfo();
    }

    // =========================================================================
    // TAB 1: ATTENDANCE (CHECK IN / CHECK OUT)
    // =========================================================================

    private void setupCheckInOutTab() {
        tvLiveDate = findViewById(R.id.tvLiveDate);
        tvLiveClock = findViewById(R.id.tvLiveClock);
        tvTodayStatusBadge = findViewById(R.id.tvTodayStatusBadge);
        tvTodayCheckInTime = findViewById(R.id.tvTodayCheckInTime);
        tvTodayCheckOutTime = findViewById(R.id.tvTodayCheckOutTime);
        tvTodayTotalHours = findViewById(R.id.tvTodayTotalHours);
        btnDoCheckIn = findViewById(R.id.btnDoCheckIn);
        btnDoCheckOut = findViewById(R.id.btnDoCheckOut);

        SimpleDateFormat dateFmt = new SimpleDateFormat("EEEE, dd/MM/yyyy", new Locale("vi", "VN"));
        tvLiveDate.setText(dateFmt.format(new Date()));

        btnDoCheckIn.setOnClickListener(v -> performCheckIn());
        btnDoCheckOut.setOnClickListener(v -> performCheckOut());
    }

    private void startLiveClock() {
        clockHandler = new Handler(Looper.getMainLooper());
        clockRunnable = new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                tvLiveClock.setText(timeFmt.format(new Date()));
                clockHandler.postDelayed(this, 1000);
            }
        };
        clockHandler.post(clockRunnable);
    }

    private void loadTodayAttendance() {
        String todayDate = DateTimeUtils.getTodayDate();
        WorkLog latestLog = dbHelper.getTodayLatestWorkLog(currentEmployee.getId(), todayDate);

        if (latestLog == null) {
            // Not checked in yet
            tvTodayStatusBadge.setText("Chưa điểm danh vào");
            tvTodayStatusBadge.setBackgroundResource(R.drawable.bg_badge_warning);
            tvTodayStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.warning));

            tvTodayCheckInTime.setText("--:--:--");
            tvTodayCheckOutTime.setText("--:--:--");
            tvTodayTotalHours.setText("0.0 giờ");

            btnDoCheckIn.setEnabled(true);
            btnDoCheckIn.setAlpha(1.0f);
            btnDoCheckOut.setEnabled(false);
            btnDoCheckOut.setAlpha(0.5f);
        } else if (WorkLog.STATUS_CHECKED_IN.equalsIgnoreCase(latestLog.getStatus())) {
            // Currently working
            tvTodayStatusBadge.setText("Đang làm việc (Vào: " + latestLog.getCheckInTime() + ")");
            tvTodayStatusBadge.setBackgroundResource(R.drawable.bg_badge_primary);
            tvTodayStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.primary));

            tvTodayCheckInTime.setText(latestLog.getCheckInTime());
            tvTodayCheckOutTime.setText("Đang làm việc...");
            tvTodayTotalHours.setText("Đang tính...");

            btnDoCheckIn.setEnabled(false);
            btnDoCheckIn.setAlpha(0.5f);
            btnDoCheckOut.setEnabled(true);
            btnDoCheckOut.setAlpha(1.0f);
        } else {
            // Completed
            tvTodayStatusBadge.setText("Đã hoàn thành ca làm việc");
            tvTodayStatusBadge.setBackgroundResource(R.drawable.bg_badge_success);
            tvTodayStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.success));

            tvTodayCheckInTime.setText(latestLog.getCheckInTime());
            tvTodayCheckOutTime.setText(latestLog.getCheckOutTime());
            tvTodayTotalHours.setText(FormatUtils.formatHours(latestLog.getTotalHours()));

            btnDoCheckIn.setEnabled(false);
            btnDoCheckIn.setAlpha(0.5f);
            btnDoCheckOut.setEnabled(false);
            btnDoCheckOut.setAlpha(0.5f);
        }
    }

    private void performCheckIn() {
        String todayDate = DateTimeUtils.getTodayDate();
        String currentTime = DateTimeUtils.getCurrentTime();

        // Validation: Prevent duplicate check-in
        WorkLog activeLog = dbHelper.getTodayActiveWorkLog(currentEmployee.getId(), todayDate);
        if (activeLog != null) {
            Toast.makeText(this, "Bạn đã điểm danh vào ca lúc " + activeLog.getCheckInTime() + " rồi!", Toast.LENGTH_LONG).show();
            return;
        }

        WorkLog latestLog = dbHelper.getTodayLatestWorkLog(currentEmployee.getId(), todayDate);
        if (latestLog != null && WorkLog.STATUS_COMPLETED.equalsIgnoreCase(latestLog.getStatus())) {
            new AlertDialog.Builder(this)
                    .setTitle("Thông báo")
                    .setMessage("Bạn đã hoàn thành ca làm việc hôm nay (" + latestLog.getCheckInTime() + " - " + latestLog.getCheckOutTime() + "). Bạn có muốn điểm danh thêm ca mới không?")
                    .setPositiveButton("Điểm danh tiếp", (dialog, which) -> doActualCheckIn(todayDate, currentTime))
                    .setNegativeButton("Hủy", null)
                    .show();
            return;
        }

        doActualCheckIn(todayDate, currentTime);
    }

    private void doActualCheckIn(String todayDate, String currentTime) {
        long result = dbHelper.checkIn(currentEmployee.getId(), todayDate, currentTime);
        if (result > 0) {
            Toast.makeText(this, "Điểm danh vào thành công lúc " + currentTime, Toast.LENGTH_SHORT).show();
            loadTodayAttendance();
        } else if (result == -2) {
            Toast.makeText(this, "Đã có ca làm việc đang hoạt động!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Không thể điểm danh vào, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
        }
    }

    private void performCheckOut() {
        String todayDate = DateTimeUtils.getTodayDate();
        String currentTime = DateTimeUtils.getCurrentTime();

        // Validation: Must have active check-in
        WorkLog activeLog = dbHelper.getTodayActiveWorkLog(currentEmployee.getId(), todayDate);
        if (activeLog == null) {
            Toast.makeText(this, "Không tìm thấy lượt điểm danh vào cho hôm nay. Vui lòng điểm danh vào trước!", Toast.LENGTH_LONG).show();
            return;
        }

        // Validation: Checkout time must be after checkin time
        double hours = DateTimeUtils.calculateHoursBetween(activeLog.getCheckInTime(), currentTime);
        if (hours < 0) {
            Toast.makeText(this, "Thời gian điểm danh ra không hợp lệ (trước giờ vào)!", Toast.LENGTH_LONG).show();
            return;
        }

        int result = dbHelper.checkOut(currentEmployee.getId(), todayDate, currentTime);
        if (result > 0) {
            Toast.makeText(this, "Điểm danh ra thành công! Tổng giờ: " + FormatUtils.formatHours(hours), Toast.LENGTH_LONG).show();
            // Automatically update salary calculation for current month
            dbHelper.calculateAndSaveSalary(currentEmployee.getId(), selectedMonth, selectedYear);
            loadTodayAttendance();
        } else {
            Toast.makeText(this, "Không thể điểm danh ra, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
        }
    }

    // =========================================================================
    // TAB 2: ATTENDANCE HISTORY
    // =========================================================================

    private void setupHistoryTab() {
        spinnerEmpHistMonth = findViewById(R.id.spinnerEmpHistMonth);
        spinnerEmpHistYear = findViewById(R.id.spinnerEmpHistYear);
        tvEmpHistTotalDays = findViewById(R.id.tvEmpHistTotalDays);
        tvEmpHistTotalHours = findViewById(R.id.tvEmpHistTotalHours);
        tvEmpHistOtHours = findViewById(R.id.tvEmpHistOtHours);
        tvEmptyEmpHistory = findViewById(R.id.tvEmptyEmpHistory);
        rvEmpHistoryLogs = findViewById(R.id.rvEmpHistoryLogs);

        rvEmpHistoryLogs.setLayoutManager(new LinearLayoutManager(this));
        historyAdapter = new AttendanceAdapter(false); // false = hide employee name in personal history
        rvEmpHistoryLogs.setAdapter(historyAdapter);

        setupMonthYearSpinners(spinnerEmpHistMonth, spinnerEmpHistYear, (month, year) -> loadAttendanceHistory());
    }

    private void loadAttendanceHistory() {
        int month = getSelectedSpinnerMonth(spinnerEmpHistMonth);
        int year = getSelectedSpinnerYear(spinnerEmpHistYear);

        List<WorkLog> logs = dbHelper.getWorkLogsByEmployee(currentEmployee.getId(), month, year);
        historyAdapter.setLogs(logs);
        tvEmptyEmpHistory.setVisibility(logs.isEmpty() ? View.VISIBLE : View.GONE);

        // Summaries
        int completedDays = 0;
        double totalHours = 0.0;
        double otHours = 0.0;

        for (WorkLog l : logs) {
            if (WorkLog.STATUS_COMPLETED.equalsIgnoreCase(l.getStatus())) {
                completedDays++;
                double h = l.getTotalHours();
                totalHours += h;
                if (h > 8.0) {
                    otHours += (h - 8.0);
                }
            }
        }

        tvEmpHistTotalDays.setText(String.valueOf(completedDays));
        tvEmpHistTotalHours.setText(FormatUtils.formatHours(totalHours));
        tvEmpHistOtHours.setText(FormatUtils.formatHours(otHours));
    }

    // =========================================================================
    // TAB 3: SALARY VIEW
    // =========================================================================

    private void setupSalaryTab() {
        spinnerEmpSalMonth = findViewById(R.id.spinnerEmpSalMonth);
        spinnerEmpSalYear = findViewById(R.id.spinnerEmpSalYear);
        tvEmpSalaryStatusBadge = findViewById(R.id.tvEmpSalaryStatusBadge);
        tvEmpSalHourlyRate = findViewById(R.id.tvEmpSalHourlyRate);
        tvEmpSalRegularHours = findViewById(R.id.tvEmpSalRegularHours);
        tvEmpSalOtHours = findViewById(R.id.tvEmpSalOtHours);
        tvEmpSalTotalHours = findViewById(R.id.tvEmpSalTotalHours);
        tvEmpSalTotalAmount = findViewById(R.id.tvEmpSalTotalAmount);

        setupMonthYearSpinners(spinnerEmpSalMonth, spinnerEmpSalYear, (month, year) -> loadPersonalSalary());
    }

    private void loadPersonalSalary() {
        int month = getSelectedSpinnerMonth(spinnerEmpSalMonth);
        int year = getSelectedSpinnerYear(spinnerEmpSalYear);

        // Ensure fresh calculation
        Salary salary = dbHelper.calculateAndSaveSalary(currentEmployee.getId(), month, year);

        if (salary != null) {
            tvEmpSalHourlyRate.setText(FormatUtils.formatCurrency(currentEmployee.getHourlyRate()) + "/giờ");

            double regularHours = Math.max(0, salary.getTotalHours() - salary.getOvertimeHours());
            tvEmpSalRegularHours.setText(FormatUtils.formatHours(regularHours));
            tvEmpSalOtHours.setText(FormatUtils.formatHours(salary.getOvertimeHours()));
            tvEmpSalTotalHours.setText(FormatUtils.formatHours(salary.getTotalHours()));
            tvEmpSalTotalAmount.setText(FormatUtils.formatCurrency(salary.getTotalSalary()));

            boolean isPaid = Salary.STATUS_PAID.equalsIgnoreCase(salary.getStatus());
            if (isPaid) {
                tvEmpSalaryStatusBadge.setText("ĐÃ THANH TOÁN");
                tvEmpSalaryStatusBadge.setBackgroundResource(R.drawable.bg_badge_success);
                tvEmpSalaryStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.success));
            } else {
                tvEmpSalaryStatusBadge.setText("CHỜ THANH TOÁN");
                tvEmpSalaryStatusBadge.setBackgroundResource(R.drawable.bg_badge_warning);
                tvEmpSalaryStatusBadge.setTextColor(ContextCompat.getColor(this, R.color.warning));
            }
        }
    }

    // =========================================================================
    // TAB 4: PROFILE & SETTINGS
    // =========================================================================

    private void setupProfileTab() {
        tvProfileCode = findViewById(R.id.tvProfileCode);
        tvProfileFullName = findViewById(R.id.tvProfileFullName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        tvProfilePhone = findViewById(R.id.tvProfilePhone);
        tvProfileHourlyRate = findViewById(R.id.tvProfileHourlyRate);
        tvProfileUsername = findViewById(R.id.tvProfileUsername);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnProfileLogout = findViewById(R.id.btnProfileLogout);

        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        btnProfileLogout.setOnClickListener(v -> showLogoutDialog());

        loadProfileInfo();
    }

    private void loadProfileInfo() {
        if (currentEmployee != null) {
            tvProfileCode.setText(currentEmployee.getCode());
            tvProfileFullName.setText(currentEmployee.getFullname());
            tvProfileEmail.setText(currentEmployee.getEmail() != null && !currentEmployee.getEmail().isEmpty() ? currentEmployee.getEmail() : "Chưa cập nhật");
            tvProfilePhone.setText(currentEmployee.getPhone() != null && !currentEmployee.getPhone().isEmpty() ? currentEmployee.getPhone() : "Chưa cập nhật");
            tvProfileHourlyRate.setText(FormatUtils.formatCurrency(currentEmployee.getHourlyRate()) + "/giờ");
            tvProfileUsername.setText(sessionManager.getUsername());
        }
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Đổi Mật Khẩu");

        View dialogView = LayoutInflater.from(this).inflate(android.R.layout.select_dialog_item, null);
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(40, 20, 40, 10);

        EditText etOld = new EditText(this);
        etOld.setHint("Mật khẩu hiện tại");
        etOld.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        container.addView(etOld);

        EditText etNew = new EditText(this);
        etNew.setHint("Mật khẩu mới (tối thiểu 4 ký tự)");
        etNew.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        container.addView(etNew);

        EditText etConfirm = new EditText(this);
        etConfirm.setHint("Xác nhận mật khẩu mới");
        etConfirm.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        container.addView(etConfirm);

        builder.setView(container);
        builder.setPositiveButton("Đổi mật khẩu", (dialog, which) -> {
            String oldPass = etOld.getText().toString();
            String newPass = etNew.getText().toString();
            String confirmPass = etConfirm.getText().toString();

            if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ các trường!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPass.equals(confirmPass)) {
                Toast.makeText(this, "Mật khẩu xác nhận không trùng khớp!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPass.length() < 4) {
                Toast.makeText(this, "Mật khẩu mới tối thiểu 4 ký tự!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Verify old password
            if (dbHelper.checkLogin(sessionManager.getUsername(), oldPass) == null) {
                Toast.makeText(this, "Mật khẩu hiện tại không đúng!", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean ok = dbHelper.updatePassword(sessionManager.getAccountId(), newPass);
            if (ok) {
                Toast.makeText(this, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Đổi mật khẩu thất bại!", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    // =========================================================================
    // SPINNERS & COMMON
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

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có muốn đăng xuất khỏi tài khoản nhân viên?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> logout())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void logout() {
        if (clockHandler != null && clockRunnable != null) {
            clockHandler.removeCallbacks(clockRunnable);
        }
        sessionManager.logout();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (clockHandler != null && clockRunnable != null) {
            clockHandler.removeCallbacks(clockRunnable);
        }
    }
}
