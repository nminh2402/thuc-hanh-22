package com.example.thuc_hanh_2.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.thuc_hanh_2.model.Account;
import com.example.thuc_hanh_2.model.Employee;
import com.example.thuc_hanh_2.model.Salary;
import com.example.thuc_hanh_2.model.WorkLog;
import com.example.thuc_hanh_2.util.DateTimeUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "worklog_salary.db";
    private static final int DATABASE_VERSION = 1;

    // Table names
    public static final String TABLE_ACCOUNT = "Account";
    public static final String TABLE_EMPLOYEE = "Employee";
    public static final String TABLE_WORKLOG = "WorkLog";
    public static final String TABLE_SALARY = "Salary";

    // Common columns
    public static final String COL_ID = "id";

    // Table Account
    public static final String COL_ACC_USERNAME = "username";
    public static final String COL_ACC_PASSWORD = "password";
    public static final String COL_ACC_ROLE = "role";

    // Table Employee
    public static final String COL_EMP_CODE = "code";
    public static final String COL_EMP_FULLNAME = "fullname";
    public static final String COL_EMP_EMAIL = "email";
    public static final String COL_EMP_PHONE = "phone";
    public static final String COL_EMP_HOURLY_RATE = "hourlyRate";
    public static final String COL_EMP_ACCOUNT_ID = "accountId";

    // Table WorkLog
    public static final String COL_LOG_EMPLOYEE_ID = "employeeId";
    public static final String COL_LOG_WORK_DATE = "workDate";
    public static final String COL_LOG_CHECK_IN_TIME = "checkInTime";
    public static final String COL_LOG_CHECK_OUT_TIME = "checkOutTime";
    public static final String COL_LOG_TOTAL_HOURS = "totalHours";
    public static final String COL_LOG_STATUS = "status";

    // Table Salary
    public static final String COL_SAL_EMPLOYEE_ID = "employeeId";
    public static final String COL_SAL_MONTH = "month";
    public static final String COL_SAL_YEAR = "year";
    public static final String COL_SAL_TOTAL_HOURS = "totalHours";
    public static final String COL_SAL_OVERTIME_HOURS = "overtimeHours";
    public static final String COL_SAL_TOTAL_SALARY = "totalSalary";
    public static final String COL_SAL_STATUS = "status";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Table Account(id, username, password, role)
        String createTableAccount = "CREATE TABLE " + TABLE_ACCOUNT + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_ACC_USERNAME + " TEXT UNIQUE NOT NULL, "
                + COL_ACC_PASSWORD + " TEXT NOT NULL, "
                + COL_ACC_ROLE + " TEXT NOT NULL"
                + ");";

        // Table Employee(id, code, fullname, email, phone, hourlyRate, accountId)
        String createTableEmployee = "CREATE TABLE " + TABLE_EMPLOYEE + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_EMP_CODE + " TEXT UNIQUE NOT NULL, "
                + COL_EMP_FULLNAME + " TEXT NOT NULL, "
                + COL_EMP_EMAIL + " TEXT, "
                + COL_EMP_PHONE + " TEXT, "
                + COL_EMP_HOURLY_RATE + " REAL NOT NULL DEFAULT 0, "
                + COL_EMP_ACCOUNT_ID + " INTEGER, "
                + "FOREIGN KEY(" + COL_EMP_ACCOUNT_ID + ") REFERENCES " + TABLE_ACCOUNT + "(" + COL_ID + ") ON DELETE CASCADE"
                + ");";

        // Table WorkLog(id, employeeId, workDate, checkInTime, checkOutTime, totalHours, status)
        String createTableWorkLog = "CREATE TABLE " + TABLE_WORKLOG + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_LOG_EMPLOYEE_ID + " INTEGER NOT NULL, "
                + COL_LOG_WORK_DATE + " TEXT NOT NULL, "
                + COL_LOG_CHECK_IN_TIME + " TEXT NOT NULL, "
                + COL_LOG_CHECK_OUT_TIME + " TEXT, "
                + COL_LOG_TOTAL_HOURS + " REAL DEFAULT 0, "
                + COL_LOG_STATUS + " TEXT NOT NULL, "
                + "FOREIGN KEY(" + COL_LOG_EMPLOYEE_ID + ") REFERENCES " + TABLE_EMPLOYEE + "(" + COL_ID + ") ON DELETE CASCADE"
                + ");";

        // Table Salary(id, employeeId, month, year, totalHours, overtimeHours, totalSalary, status)
        String createTableSalary = "CREATE TABLE " + TABLE_SALARY + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_SAL_EMPLOYEE_ID + " INTEGER NOT NULL, "
                + COL_SAL_MONTH + " INTEGER NOT NULL, "
                + COL_SAL_YEAR + " INTEGER NOT NULL, "
                + COL_SAL_TOTAL_HOURS + " REAL DEFAULT 0, "
                + COL_SAL_OVERTIME_HOURS + " REAL DEFAULT 0, "
                + COL_SAL_TOTAL_SALARY + " REAL DEFAULT 0, "
                + COL_SAL_STATUS + " TEXT NOT NULL, "
                + "UNIQUE(" + COL_SAL_EMPLOYEE_ID + ", " + COL_SAL_MONTH + ", " + COL_SAL_YEAR + "), "
                + "FOREIGN KEY(" + COL_SAL_EMPLOYEE_ID + ") REFERENCES " + TABLE_EMPLOYEE + "(" + COL_ID + ") ON DELETE CASCADE"
                + ");";

        db.execSQL(createTableAccount);
        db.execSQL(createTableEmployee);
        db.execSQL(createTableWorkLog);
        db.execSQL(createTableSalary);

        // Seed initial data
        seedInitialData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SALARY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WORKLOG);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EMPLOYEE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACCOUNT);
        onCreate(db);
    }

    private void seedInitialData(SQLiteDatabase db) {
        // 1. Admin Account
        ContentValues adminAcc = new ContentValues();
        adminAcc.put(COL_ACC_USERNAME, "admin");
        adminAcc.put(COL_ACC_PASSWORD, "admin123");
        adminAcc.put(COL_ACC_ROLE, Account.ROLE_ADMIN);
        db.insert(TABLE_ACCOUNT, null, adminAcc);

        // 2. Employee 1
        ContentValues emp1Acc = new ContentValues();
        emp1Acc.put(COL_ACC_USERNAME, "nv01");
        emp1Acc.put(COL_ACC_PASSWORD, "123456");
        emp1Acc.put(COL_ACC_ROLE, Account.ROLE_EMPLOYEE);
        long acc1Id = db.insert(TABLE_ACCOUNT, null, emp1Acc);

        ContentValues emp1 = new ContentValues();
        emp1.put(COL_EMP_CODE, "NV001");
        emp1.put(COL_EMP_FULLNAME, "Nguyễn Văn An");
        emp1.put(COL_EMP_EMAIL, "an.nguyen@company.com");
        emp1.put(COL_EMP_PHONE, "0901234567");
        emp1.put(COL_EMP_HOURLY_RATE, 50000.0);
        emp1.put(COL_EMP_ACCOUNT_ID, acc1Id);
        long emp1Id = db.insert(TABLE_EMPLOYEE, null, emp1);

        // 3. Employee 2
        ContentValues emp2Acc = new ContentValues();
        emp2Acc.put(COL_ACC_USERNAME, "nv02");
        emp2Acc.put(COL_ACC_PASSWORD, "123456");
        emp2Acc.put(COL_ACC_ROLE, Account.ROLE_EMPLOYEE);
        long acc2Id = db.insert(TABLE_ACCOUNT, null, emp2Acc);

        ContentValues emp2 = new ContentValues();
        emp2.put(COL_EMP_CODE, "NV002");
        emp2.put(COL_EMP_FULLNAME, "Trần Thị Bình");
        emp2.put(COL_EMP_EMAIL, "binh.tran@company.com");
        emp2.put(COL_EMP_PHONE, "0912345678");
        emp2.put(COL_EMP_HOURLY_RATE, 60000.0);
        emp2.put(COL_EMP_ACCOUNT_ID, acc2Id);
        long emp2Id = db.insert(TABLE_EMPLOYEE, null, emp2);

        // 4. Employee 3
        ContentValues emp3Acc = new ContentValues();
        emp3Acc.put(COL_ACC_USERNAME, "nv03");
        emp3Acc.put(COL_ACC_PASSWORD, "123456");
        emp3Acc.put(COL_ACC_ROLE, Account.ROLE_EMPLOYEE);
        long acc3Id = db.insert(TABLE_ACCOUNT, null, emp3Acc);

        ContentValues emp3 = new ContentValues();
        emp3.put(COL_EMP_CODE, "NV003");
        emp3.put(COL_EMP_FULLNAME, "Lê Hoàng Cường");
        emp3.put(COL_EMP_EMAIL, "cuong.le@company.com");
        emp3.put(COL_EMP_PHONE, "0987654321");
        emp3.put(COL_EMP_HOURLY_RATE, 55000.0);
        emp3.put(COL_EMP_ACCOUNT_ID, acc3Id);
        long emp3Id = db.insert(TABLE_EMPLOYEE, null, emp3);

        // Seed Sample Attendance (WorkLogs) for Current Month
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1; // 1..12

        // Insert sample past days for emp1
        for (int day = 1; day <= Math.min(10, cal.get(Calendar.DAY_OF_MONTH)); day++) {
            String dateStr = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, day);
            ContentValues log = new ContentValues();
            log.put(COL_LOG_EMPLOYEE_ID, emp1Id);
            log.put(COL_LOG_WORK_DATE, dateStr);
            log.put(COL_LOG_CHECK_IN_TIME, "08:00:00");
            if (day % 3 == 0) {
                // Overtime day: 9.5 hours
                log.put(COL_LOG_CHECK_OUT_TIME, "17:30:00");
                log.put(COL_LOG_TOTAL_HOURS, 9.5);
            } else {
                // Regular day: 8.0 hours
                log.put(COL_LOG_CHECK_OUT_TIME, "16:00:00");
                log.put(COL_LOG_TOTAL_HOURS, 8.0);
            }
            log.put(COL_LOG_STATUS, WorkLog.STATUS_COMPLETED);
            db.insert(TABLE_WORKLOG, null, log);
        }

        // Insert sample past days for emp2
        for (int day = 1; day <= Math.min(8, cal.get(Calendar.DAY_OF_MONTH)); day++) {
            String dateStr = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month, day);
            ContentValues log = new ContentValues();
            log.put(COL_LOG_EMPLOYEE_ID, emp2Id);
            log.put(COL_LOG_WORK_DATE, dateStr);
            log.put(COL_LOG_CHECK_IN_TIME, "08:30:00");
            log.put(COL_LOG_CHECK_OUT_TIME, "17:00:00");
            log.put(COL_LOG_TOTAL_HOURS, 8.5);
            log.put(COL_LOG_STATUS, WorkLog.STATUS_COMPLETED);
            db.insert(TABLE_WORKLOG, null, log);
        }

        // Insert sample previous month salary for emp1
        int prevMonth = month - 1;
        int prevYear = year;
        if (prevMonth < 1) {
            prevMonth = 12;
            prevYear--;
        }
        ContentValues sal1 = new ContentValues();
        sal1.put(COL_SAL_EMPLOYEE_ID, emp1Id);
        sal1.put(COL_SAL_MONTH, prevMonth);
        sal1.put(COL_SAL_YEAR, prevYear);
        sal1.put(COL_SAL_TOTAL_HOURS, 176.0);
        sal1.put(COL_SAL_OVERTIME_HOURS, 16.0);
        // (160 * 50000) + (16 * 50000 * 1.5) = 8,000,000 + 1,200,000 = 9,200,000
        sal1.put(COL_SAL_TOTAL_SALARY, 9200000.0);
        sal1.put(COL_SAL_STATUS, Salary.STATUS_PAID);
        db.insert(TABLE_SALARY, null, sal1);
    }

    // =========================================================================
    // ACCOUNT OPERATIONS
    // =========================================================================

    public Account checkLogin(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ACCOUNT,
                new String[]{COL_ID, COL_ACC_USERNAME, COL_ACC_PASSWORD, COL_ACC_ROLE},
                COL_ACC_USERNAME + " = ? AND " + COL_ACC_PASSWORD + " = ?",
                new String[]{username.trim(), password},
                null, null, null);

        Account account = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                account = new Account(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_ACC_USERNAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_ACC_PASSWORD)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_ACC_ROLE))
                );
            }
            cursor.close();
        }
        return account;
    }

    public Account getAccountById(int accountId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ACCOUNT,
                new String[]{COL_ID, COL_ACC_USERNAME, COL_ACC_PASSWORD, COL_ACC_ROLE},
                COL_ID + " = ?",
                new String[]{String.valueOf(accountId)},
                null, null, null);

        Account account = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                account = new Account(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_ACC_USERNAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_ACC_PASSWORD)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_ACC_ROLE))
                );
            }
            cursor.close();
        }
        return account;
    }

    public boolean updatePassword(int accountId, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_ACC_PASSWORD, newPassword);
        return db.update(TABLE_ACCOUNT, values, COL_ID + " = ?", new String[]{String.valueOf(accountId)}) > 0;
    }

    public boolean isUsernameTaken(String username, int excludeAccountId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COL_ACC_USERNAME + " = ?";
        String[] selectionArgs;
        if (excludeAccountId > 0) {
            selection += " AND " + COL_ID + " != ?";
            selectionArgs = new String[]{username.trim(), String.valueOf(excludeAccountId)};
        } else {
            selectionArgs = new String[]{username.trim()};
        }
        Cursor cursor = db.query(TABLE_ACCOUNT, new String[]{COL_ID}, selection, selectionArgs, null, null, null);
        boolean exists = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) cursor.close();
        return exists;
    }

    // =========================================================================
    // EMPLOYEE OPERATIONS
    // =========================================================================

    public long addEmployeeWithAccount(Employee employee, String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        long empId = -1;
        try {
            // 1. Create Account
            ContentValues accValues = new ContentValues();
            accValues.put(COL_ACC_USERNAME, username.trim());
            accValues.put(COL_ACC_PASSWORD, password);
            accValues.put(COL_ACC_ROLE, Account.ROLE_EMPLOYEE);
            long accountId = db.insert(TABLE_ACCOUNT, null, accValues);

            if (accountId != -1) {
                // 2. Create Employee
                ContentValues empValues = new ContentValues();
                empValues.put(COL_EMP_CODE, employee.getCode().trim().toUpperCase());
                empValues.put(COL_EMP_FULLNAME, employee.getFullname().trim());
                empValues.put(COL_EMP_EMAIL, employee.getEmail() != null ? employee.getEmail().trim() : "");
                empValues.put(COL_EMP_PHONE, employee.getPhone() != null ? employee.getPhone().trim() : "");
                empValues.put(COL_EMP_HOURLY_RATE, employee.getHourlyRate());
                empValues.put(COL_EMP_ACCOUNT_ID, accountId);

                empId = db.insert(TABLE_EMPLOYEE, null, empValues);
                if (empId != -1) {
                    db.setTransactionSuccessful();
                }
            }
        } finally {
            db.endTransaction();
        }
        return empId;
    }

    public boolean updateEmployee(Employee employee, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        boolean success = false;
        try {
            ContentValues empValues = new ContentValues();
            empValues.put(COL_EMP_CODE, employee.getCode().trim().toUpperCase());
            empValues.put(COL_EMP_FULLNAME, employee.getFullname().trim());
            empValues.put(COL_EMP_EMAIL, employee.getEmail() != null ? employee.getEmail().trim() : "");
            empValues.put(COL_EMP_PHONE, employee.getPhone() != null ? employee.getPhone().trim() : "");
            empValues.put(COL_EMP_HOURLY_RATE, employee.getHourlyRate());

            int rows = db.update(TABLE_EMPLOYEE, empValues, COL_ID + " = ?", new String[]{String.valueOf(employee.getId())});

            if (rows > 0) {
                if (newPassword != null && !newPassword.trim().isEmpty()) {
                    ContentValues accValues = new ContentValues();
                    accValues.put(COL_ACC_PASSWORD, newPassword);
                    db.update(TABLE_ACCOUNT, accValues, COL_ID + " = ?", new String[]{String.valueOf(employee.getAccountId())});
                }
                db.setTransactionSuccessful();
                success = true;
            }
        } finally {
            db.endTransaction();
        }
        return success;
    }

    public boolean deleteEmployee(int employeeId) {
        SQLiteDatabase db = this.getWritableDatabase();
        Employee emp = getEmployeeById(employeeId);
        if (emp == null) return false;

        db.beginTransaction();
        boolean success = false;
        try {
            // Delete associated work logs and salaries
            db.delete(TABLE_WORKLOG, COL_LOG_EMPLOYEE_ID + " = ?", new String[]{String.valueOf(employeeId)});
            db.delete(TABLE_SALARY, COL_SAL_EMPLOYEE_ID + " = ?", new String[]{String.valueOf(employeeId)});
            // Delete employee
            db.delete(TABLE_EMPLOYEE, COL_ID + " = ?", new String[]{String.valueOf(employeeId)});
            // Delete account
            if (emp.getAccountId() > 0) {
                db.delete(TABLE_ACCOUNT, COL_ID + " = ?", new String[]{String.valueOf(emp.getAccountId())});
            }
            db.setTransactionSuccessful();
            success = true;
        } finally {
            db.endTransaction();
        }
        return success;
    }

    public Employee getEmployeeById(int employeeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT e.*, a.username FROM " + TABLE_EMPLOYEE + " e "
                + "LEFT JOIN " + TABLE_ACCOUNT + " a ON e." + COL_EMP_ACCOUNT_ID + " = a." + COL_ID + " "
                + "WHERE e." + COL_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(employeeId)});
        Employee emp = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                emp = mapCursorToEmployee(cursor);
            }
            cursor.close();
        }
        return emp;
    }

    public Employee getEmployeeByAccountId(int accountId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT e.*, a.username FROM " + TABLE_EMPLOYEE + " e "
                + "LEFT JOIN " + TABLE_ACCOUNT + " a ON e." + COL_EMP_ACCOUNT_ID + " = a." + COL_ID + " "
                + "WHERE e." + COL_EMP_ACCOUNT_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(accountId)});
        Employee emp = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                emp = mapCursorToEmployee(cursor);
            }
            cursor.close();
        }
        return emp;
    }

    public List<Employee> getAllEmployees() {
        List<Employee> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT e.*, a.username FROM " + TABLE_EMPLOYEE + " e "
                + "LEFT JOIN " + TABLE_ACCOUNT + " a ON e." + COL_EMP_ACCOUNT_ID + " = a." + COL_ID + " "
                + "ORDER BY e." + COL_ID + " DESC";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(mapCursorToEmployee(cursor));
            }
            cursor.close();
        }
        return list;
    }

    public List<Employee> searchEmployees(String keyword) {
        List<Employee> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String pattern = "%" + keyword.trim() + "%";
        String query = "SELECT e.*, a.username FROM " + TABLE_EMPLOYEE + " e "
                + "LEFT JOIN " + TABLE_ACCOUNT + " a ON e." + COL_EMP_ACCOUNT_ID + " = a." + COL_ID + " "
                + "WHERE e." + COL_EMP_CODE + " LIKE ? OR e." + COL_EMP_FULLNAME + " LIKE ? OR e." + COL_EMP_PHONE + " LIKE ? OR e." + COL_EMP_EMAIL + " LIKE ? "
                + "ORDER BY e." + COL_ID + " DESC";
        Cursor cursor = db.rawQuery(query, new String[]{pattern, pattern, pattern, pattern});
        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(mapCursorToEmployee(cursor));
            }
            cursor.close();
        }
        return list;
    }

    public boolean isEmployeeCodeTaken(String code, int excludeEmployeeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COL_EMP_CODE + " = ?";
        String[] selectionArgs;
        if (excludeEmployeeId > 0) {
            selection += " AND " + COL_ID + " != ?";
            selectionArgs = new String[]{code.trim().toUpperCase(), String.valueOf(excludeEmployeeId)};
        } else {
            selectionArgs = new String[]{code.trim().toUpperCase()};
        }
        Cursor cursor = db.query(TABLE_EMPLOYEE, new String[]{COL_ID}, selection, selectionArgs, null, null, null);
        boolean exists = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) cursor.close();
        return exists;
    }

    private Employee mapCursorToEmployee(Cursor cursor) {
        Employee emp = new Employee();
        emp.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)));
        emp.setCode(cursor.getString(cursor.getColumnIndexOrThrow(COL_EMP_CODE)));
        emp.setFullname(cursor.getString(cursor.getColumnIndexOrThrow(COL_EMP_FULLNAME)));
        emp.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COL_EMP_EMAIL)));
        emp.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(COL_EMP_PHONE)));
        emp.setHourlyRate(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_EMP_HOURLY_RATE)));
        emp.setAccountId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_EMP_ACCOUNT_ID)));
        int userCol = cursor.getColumnIndex("username");
        if (userCol != -1) {
            emp.setUsername(cursor.getString(userCol));
        }
        return emp;
    }

    // =========================================================================
    // WORKLOG & ATTENDANCE OPERATIONS
    // =========================================================================

    public WorkLog getTodayActiveWorkLog(int employeeId, String workDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_WORKLOG, null,
                COL_LOG_EMPLOYEE_ID + " = ? AND " + COL_LOG_WORK_DATE + " = ? AND " + COL_LOG_STATUS + " = ?",
                new String[]{String.valueOf(employeeId), workDate, WorkLog.STATUS_CHECKED_IN},
                null, null, COL_ID + " DESC", "1");
        WorkLog log = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                log = mapCursorToWorkLog(cursor);
            }
            cursor.close();
        }
        return log;
    }

    public WorkLog getTodayLatestWorkLog(int employeeId, String workDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_WORKLOG, null,
                COL_LOG_EMPLOYEE_ID + " = ? AND " + COL_LOG_WORK_DATE + " = ?",
                new String[]{String.valueOf(employeeId), workDate},
                null, null, COL_ID + " DESC", "1");
        WorkLog log = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                log = mapCursorToWorkLog(cursor);
            }
            cursor.close();
        }
        return log;
    }

    /**
     * Check-in function.
     * Prevents duplicate check-in if there's already an active CHECKED_IN log today.
     */
    public long checkIn(int employeeId, String workDate, String checkInTime) {
        // Validation: Check if already checked in and hasn't checked out
        WorkLog activeLog = getTodayActiveWorkLog(employeeId, workDate);
        if (activeLog != null) {
            return -2; // Error: Already checked in and active
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_LOG_EMPLOYEE_ID, employeeId);
        values.put(COL_LOG_WORK_DATE, workDate);
        values.put(COL_LOG_CHECK_IN_TIME, checkInTime);
        values.put(COL_LOG_CHECK_OUT_TIME, "");
        values.put(COL_LOG_TOTAL_HOURS, 0.0);
        values.put(COL_LOG_STATUS, WorkLog.STATUS_CHECKED_IN);

        return db.insert(TABLE_WORKLOG, null, values);
    }

    /**
     * Check-out function.
     * Validates active check-in exists, calculates total working hours.
     */
    public int checkOut(int employeeId, String workDate, String checkOutTime) {
        WorkLog activeLog = getTodayActiveWorkLog(employeeId, workDate);
        if (activeLog == null) {
            return -1; // No active check-in found
        }

        double totalHours = DateTimeUtils.calculateHoursBetween(activeLog.getCheckInTime(), checkOutTime);
        if (totalHours < 0) {
            return -2; // Invalid checkout time (checkout before checkin)
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_LOG_CHECK_OUT_TIME, checkOutTime);
        values.put(COL_LOG_TOTAL_HOURS, totalHours);
        values.put(COL_LOG_STATUS, WorkLog.STATUS_COMPLETED);

        return db.update(TABLE_WORKLOG, values, COL_ID + " = ?", new String[]{String.valueOf(activeLog.getId())});
    }

    /**
     * Mark past unclosed check-ins (before today) as INCOMPLETE.
     */
    public void markPastIncompleteLogs(String todayDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_LOG_STATUS, WorkLog.STATUS_INCOMPLETE);
        db.update(TABLE_WORKLOG, values,
                COL_LOG_WORK_DATE + " < ? AND " + COL_LOG_STATUS + " = ?",
                new String[]{todayDate, WorkLog.STATUS_CHECKED_IN});
    }

    public List<WorkLog> getWorkLogsByEmployee(int employeeId, int month, int year) {
        List<WorkLog> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String monthStr = String.format(Locale.getDefault(), "%02d", month);
        String yearStr = String.valueOf(year);
        String prefix = yearStr + "-" + monthStr + "%";

        String query = "SELECT w.*, e.fullname, e.code FROM " + TABLE_WORKLOG + " w "
                + "JOIN " + TABLE_EMPLOYEE + " e ON w." + COL_LOG_EMPLOYEE_ID + " = e." + COL_ID + " "
                + "WHERE w." + COL_LOG_EMPLOYEE_ID + " = ? AND w." + COL_LOG_WORK_DATE + " LIKE ? "
                + "ORDER BY w." + COL_LOG_WORK_DATE + " DESC, w." + COL_ID + " DESC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(employeeId), prefix});
        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(mapCursorToWorkLog(cursor));
            }
            cursor.close();
        }
        return list;
    }

    public List<WorkLog> getAllWorkLogs(int month, int year, int employeeId) {
        List<WorkLog> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String monthStr = String.format(Locale.getDefault(), "%02d", month);
        String yearStr = String.valueOf(year);
        String prefix = yearStr + "-" + monthStr + "%";

        String query = "SELECT w.*, e.fullname, e.code FROM " + TABLE_WORKLOG + " w "
                + "JOIN " + TABLE_EMPLOYEE + " e ON w." + COL_LOG_EMPLOYEE_ID + " = e." + COL_ID + " "
                + "WHERE w." + COL_LOG_WORK_DATE + " LIKE ? ";

        List<String> args = new ArrayList<>();
        args.add(prefix);

        if (employeeId > 0) {
            query += "AND w." + COL_LOG_EMPLOYEE_ID + " = ? ";
            args.add(String.valueOf(employeeId));
        }

        query += "ORDER BY w." + COL_LOG_WORK_DATE + " DESC, w." + COL_ID + " DESC";

        Cursor cursor = db.rawQuery(query, args.toArray(new String[0]));
        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(mapCursorToWorkLog(cursor));
            }
            cursor.close();
        }
        return list;
    }

    public boolean deleteWorkLog(int logId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_WORKLOG, COL_ID + " = ?", new String[]{String.valueOf(logId)}) > 0;
    }

    private WorkLog mapCursorToWorkLog(Cursor cursor) {
        WorkLog log = new WorkLog();
        log.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)));
        log.setEmployeeId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_LOG_EMPLOYEE_ID)));
        log.setWorkDate(cursor.getString(cursor.getColumnIndexOrThrow(COL_LOG_WORK_DATE)));
        log.setCheckInTime(cursor.getString(cursor.getColumnIndexOrThrow(COL_LOG_CHECK_IN_TIME)));
        log.setCheckOutTime(cursor.getString(cursor.getColumnIndexOrThrow(COL_LOG_CHECK_OUT_TIME)));
        log.setTotalHours(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_LOG_TOTAL_HOURS)));
        log.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(COL_LOG_STATUS)));

        int nameCol = cursor.getColumnIndex("fullname");
        if (nameCol != -1) log.setEmployeeName(cursor.getString(nameCol));
        int codeCol = cursor.getColumnIndex("code");
        if (codeCol != -1) log.setEmployeeCode(cursor.getString(codeCol));

        return log;
    }

    // =========================================================================
    // SALARY OPERATIONS & CALCULATION
    // =========================================================================

    /**
     * Calculates salary for an employee for a specific month and year.
     * Logic:
     * - Standard daily threshold = 8.0 hours.
     * - Daily hours <= 8.0 -> regular hours.
     * - Daily hours > 8.0 -> 8.0 regular hours + (daily - 8.0) overtime hours.
     * - Overtime pay rate = hourlyRate * 1.5.
     * - Total salary = (totalRegularHours * hourlyRate) + (totalOvertimeHours * hourlyRate * 1.5).
     * - Inserts or updates into Salary table.
     */
    public Salary calculateAndSaveSalary(int employeeId, int month, int year) {
        Employee emp = getEmployeeById(employeeId);
        if (emp == null) return null;

        List<WorkLog> logs = getWorkLogsByEmployee(employeeId, month, year);

        double totalHours = 0.0;
        double regularHours = 0.0;
        double overtimeHours = 0.0;

        for (WorkLog log : logs) {
            if (WorkLog.STATUS_COMPLETED.equalsIgnoreCase(log.getStatus())) {
                double hours = log.getTotalHours();
                totalHours += hours;
                if (hours > 8.0) {
                    regularHours += 8.0;
                    overtimeHours += (hours - 8.0);
                } else {
                    regularHours += hours;
                }
            }
        }

        totalHours = Math.round(totalHours * 100.0) / 100.0;
        overtimeHours = Math.round(overtimeHours * 100.0) / 100.0;
        regularHours = Math.round(regularHours * 100.0) / 100.0;

        double hourlyRate = emp.getHourlyRate();
        double totalSalary = (regularHours * hourlyRate) + (overtimeHours * hourlyRate * 1.5);
        totalSalary = Math.round(totalSalary);

        SQLiteDatabase db = this.getWritableDatabase();

        // Check if salary record already exists
        Salary existing = getSalaryByEmployeeAndMonth(employeeId, month, year);
        String currentStatus = (existing != null) ? existing.getStatus() : Salary.STATUS_PENDING;

        ContentValues values = new ContentValues();
        values.put(COL_SAL_EMPLOYEE_ID, employeeId);
        values.put(COL_SAL_MONTH, month);
        values.put(COL_SAL_YEAR, year);
        values.put(COL_SAL_TOTAL_HOURS, totalHours);
        values.put(COL_SAL_OVERTIME_HOURS, overtimeHours);
        values.put(COL_SAL_TOTAL_SALARY, totalSalary);
        values.put(COL_SAL_STATUS, currentStatus);

        if (existing != null) {
            db.update(TABLE_SALARY, values, COL_ID + " = ?", new String[]{String.valueOf(existing.getId())});
        } else {
            db.insert(TABLE_SALARY, null, values);
        }

        Salary sal = getSalaryByEmployeeAndMonth(employeeId, month, year);
        return sal;
    }

    /**
     * Calculates salary for all employees for a given month and year.
     */
    public int calculateSalaryForAllEmployees(int month, int year) {
        List<Employee> allEmployees = getAllEmployees();
        int count = 0;
        for (Employee emp : allEmployees) {
            calculateAndSaveSalary(emp.getId(), month, year);
            count++;
        }
        return count;
    }

    public Salary getSalaryByEmployeeAndMonth(int employeeId, int month, int year) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT s.*, e.fullname, e.code, e.hourlyRate FROM " + TABLE_SALARY + " s "
                + "JOIN " + TABLE_EMPLOYEE + " e ON s." + COL_SAL_EMPLOYEE_ID + " = e." + COL_ID + " "
                + "WHERE s." + COL_SAL_EMPLOYEE_ID + " = ? AND s." + COL_SAL_MONTH + " = ? AND s." + COL_SAL_YEAR + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(employeeId), String.valueOf(month), String.valueOf(year)});
        Salary salary = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                salary = mapCursorToSalary(cursor);
            }
            cursor.close();
        }
        return salary;
    }

    public List<Salary> getSalariesByMonthYear(int month, int year) {
        List<Salary> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT s.*, e.fullname, e.code, e.hourlyRate FROM " + TABLE_SALARY + " s "
                + "JOIN " + TABLE_EMPLOYEE + " e ON s." + COL_SAL_EMPLOYEE_ID + " = e." + COL_ID + " "
                + "WHERE s." + COL_SAL_MONTH + " = ? AND s." + COL_SAL_YEAR + " = ? "
                + "ORDER BY e." + COL_EMP_CODE + " ASC";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(month), String.valueOf(year)});
        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(mapCursorToSalary(cursor));
            }
            cursor.close();
        }
        return list;
    }

    public boolean updateSalaryStatus(int salaryId, String newStatus) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_SAL_STATUS, newStatus);
        return db.update(TABLE_SALARY, values, COL_ID + " = ?", new String[]{String.valueOf(salaryId)}) > 0;
    }

    private Salary mapCursorToSalary(Cursor cursor) {
        Salary salary = new Salary();
        salary.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)));
        salary.setEmployeeId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_SAL_EMPLOYEE_ID)));
        salary.setMonth(cursor.getInt(cursor.getColumnIndexOrThrow(COL_SAL_MONTH)));
        salary.setYear(cursor.getInt(cursor.getColumnIndexOrThrow(COL_SAL_YEAR)));
        salary.setTotalHours(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_SAL_TOTAL_HOURS)));
        salary.setOvertimeHours(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_SAL_OVERTIME_HOURS)));
        salary.setTotalSalary(cursor.getDouble(cursor.getColumnIndexOrThrow(COL_SAL_TOTAL_SALARY)));
        salary.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(COL_SAL_STATUS)));

        int nameCol = cursor.getColumnIndex("fullname");
        if (nameCol != -1) salary.setEmployeeName(cursor.getString(nameCol));
        int codeCol = cursor.getColumnIndex("code");
        if (codeCol != -1) salary.setEmployeeCode(cursor.getString(codeCol));
        int rateCol = cursor.getColumnIndex("hourlyRate");
        if (rateCol != -1) salary.setHourlyRate(cursor.getDouble(rateCol));

        return salary;
    }
}
