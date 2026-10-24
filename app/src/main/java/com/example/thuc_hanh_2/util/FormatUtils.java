package com.example.thuc_hanh_2.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class FormatUtils {
    public static String formatCurrency(double amount) {
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        return nf.format(amount) + " đ";
    }

    public static String formatHours(double hours) {
        DecimalFormat df = new DecimalFormat("#,##0.0#");
        return df.format(hours) + " giờ";
    }
}
