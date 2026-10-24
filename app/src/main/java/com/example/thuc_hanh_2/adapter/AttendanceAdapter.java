package com.example.thuc_hanh_2.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.thuc_hanh_2.R;
import com.example.thuc_hanh_2.model.WorkLog;
import com.example.thuc_hanh_2.util.DateTimeUtils;
import com.example.thuc_hanh_2.util.FormatUtils;

import java.util.ArrayList;
import java.util.List;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder> {

    private List<WorkLog> logList = new ArrayList<>();
    private final boolean showEmployeeInfo;

    public AttendanceAdapter(boolean showEmployeeInfo) {
        this.showEmployeeInfo = showEmployeeInfo;
    }

    public void setLogs(List<WorkLog> logs) {
        this.logList = logs != null ? logs : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AttendanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attendance, parent, false);
        return new AttendanceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendanceViewHolder holder, int position) {
        WorkLog log = logList.get(position);
        holder.bind(log, showEmployeeInfo);
    }

    @Override
    public int getItemCount() {
        return logList.size();
    }

    static class AttendanceViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvDate;
        private final TextView tvEmployee;
        private final TextView tvStatus;
        private final TextView tvCheckIn;
        private final TextView tvCheckOut;
        private final TextView tvTotalHours;

        public AttendanceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvItemAttDate);
            tvEmployee = itemView.findViewById(R.id.tvItemAttEmployee);
            tvStatus = itemView.findViewById(R.id.tvItemAttStatus);
            tvCheckIn = itemView.findViewById(R.id.tvItemAttCheckIn);
            tvCheckOut = itemView.findViewById(R.id.tvItemAttCheckOut);
            tvTotalHours = itemView.findViewById(R.id.tvItemAttTotalHours);
        }

        public void bind(WorkLog log, boolean showEmployeeInfo) {
            Context ctx = itemView.getContext();
            tvDate.setText(DateTimeUtils.formatDisplayDate(log.getWorkDate()));

            if (showEmployeeInfo) {
                tvEmployee.setVisibility(View.VISIBLE);
                String empInfo = (log.getEmployeeName() != null ? log.getEmployeeName() : "Nhân viên")
                        + (log.getEmployeeCode() != null ? " (" + log.getEmployeeCode() + ")" : "");
                tvEmployee.setText(empInfo);
            } else {
                tvEmployee.setVisibility(View.GONE);
            }

            tvCheckIn.setText(log.getCheckInTime() != null && !log.getCheckInTime().isEmpty() ? log.getCheckInTime() : "--:--");
            tvCheckOut.setText(log.getCheckOutTime() != null && !log.getCheckOutTime().isEmpty() ? log.getCheckOutTime() : "--:--");
            tvTotalHours.setText(FormatUtils.formatHours(log.getTotalHours()));

            // Status styling
            if (WorkLog.STATUS_COMPLETED.equalsIgnoreCase(log.getStatus())) {
                tvStatus.setText("Hoàn thành");
                tvStatus.setBackgroundResource(R.drawable.bg_badge_success);
                tvStatus.setTextColor(ContextCompat.getColor(ctx, R.color.success));
            } else if (WorkLog.STATUS_CHECKED_IN.equalsIgnoreCase(log.getStatus())) {
                tvStatus.setText("Đang làm việc");
                tvStatus.setBackgroundResource(R.drawable.bg_badge_warning);
                tvStatus.setTextColor(ContextCompat.getColor(ctx, R.color.warning));
            } else {
                tvStatus.setText("Chưa hoàn tất");
                tvStatus.setBackgroundResource(R.drawable.bg_badge_danger);
                tvStatus.setTextColor(ContextCompat.getColor(ctx, R.color.danger));
            }
        }
    }
}
