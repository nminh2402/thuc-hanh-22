package com.example.thuc_hanh_2.model;

import java.io.Serializable;

public class WorkLog implements Serializable {
    public static final String STATUS_CHECKED_IN = "CHECKED_IN";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_INCOMPLETE = "INCOMPLETE";

    private int id;
    private int employeeId;
    private String workDate;      // YYYY-MM-DD
    private String checkInTime;   // HH:mm:ss
    private String checkOutTime;  // HH:mm:ss
    private double totalHours;
    private String status;

    // Joined / helper fields for display
    private String employeeName;
    private String employeeCode;

    public WorkLog() {}

    public WorkLog(int id, int employeeId, String workDate, String checkInTime, String checkOutTime, double totalHours, String status) {
        this.id = id;
        this.employeeId = employeeId;
        this.workDate = workDate;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.totalHours = totalHours;
        this.status = status;
    }

    public WorkLog(int employeeId, String workDate, String checkInTime, String checkOutTime, double totalHours, String status) {
        this.employeeId = employeeId;
        this.workDate = workDate;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.totalHours = totalHours;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public String getWorkDate() {
        return workDate;
    }

    public void setWorkDate(String workDate) {
        this.workDate = workDate;
    }

    public String getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(String checkInTime) {
        this.checkInTime = checkInTime;
    }

    public String getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(String checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public double getTotalHours() {
        return totalHours;
    }

    public void setTotalHours(double totalHours) {
        this.totalHours = totalHours;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }
}
