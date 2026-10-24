package com.example.thuc_hanh_2.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateTimeUtils {
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String TIME_FORMAT = "HH:mm:ss";
    public static final String DISPLAY_DATE_FORMAT = "dd/MM/yyyy";
    public static final String DISPLAY_TIME_FORMAT = "HH:mm";

    public static String getTodayDate() {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        return sdf.format(new Date());
    }

    public static String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT, Locale.getDefault());
        return sdf.format(new Date());
    }

    public static int getCurrentMonth() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.MONTH) + 1; // 1-indexed (1..12)
    }

    public static int getCurrentYear() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.YEAR);
    }

    public static String formatDisplayDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return "";
        try {
            SimpleDateFormat input = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
            SimpleDateFormat output = new SimpleDateFormat(DISPLAY_DATE_FORMAT, Locale.getDefault());
            Date date = input.parse(dateStr);
            return date != null ? output.format(date) : dateStr;
        } catch (ParseException e) {
            return dateStr;
        }
    }

    public static double calculateHoursBetween(String startTime, String endTime) {
        if (startTime == null || endTime == null || startTime.isEmpty() || endTime.isEmpty()) {
            return 0.0;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT, Locale.getDefault());
            Date dStart = sdf.parse(startTime);
            Date dEnd = sdf.parse(endTime);
            if (dStart != null && dEnd != null) {
                long diffMillis = dEnd.getTime() - dStart.getTime();
                if (diffMillis < 0) {
                    return -1.0; // Invalid, end before start
                }
                double hours = (double) diffMillis / (1000.0 * 60.0 * 60.0);
                return Math.round(hours * 100.0) / 100.0; // round to 2 decimals
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public static boolean isTimeAfter(String endTime, String startTime) {
        if (startTime == null || endTime == null) return false;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT, Locale.getDefault());
            Date dStart = sdf.parse(startTime);
            Date dEnd = sdf.parse(endTime);
            return dStart != null && dEnd != null && dEnd.after(dStart);
        } catch (ParseException e) {
            return false;
        }
    }
}
