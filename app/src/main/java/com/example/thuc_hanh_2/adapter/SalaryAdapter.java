package com.example.thuc_hanh_2.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.thuc_hanh_2.R;
import com.example.thuc_hanh_2.model.Salary;
import com.example.thuc_hanh_2.util.FormatUtils;

import java.util.ArrayList;
import java.util.List;

public class SalaryAdapter extends RecyclerView.Adapter<SalaryAdapter.SalaryViewHolder> {

    public interface OnSalaryActionListener {
        void onToggleStatus(Salary salary);
    }

    private List<Salary> salaryList = new ArrayList<>();
    private final OnSalaryActionListener listener;

    public SalaryAdapter(OnSalaryActionListener listener) {
        this.listener = listener;
    }

    public void setSalaries(List<Salary> salaries) {
        this.salaryList = salaries != null ? salaries : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SalaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_salary, parent, false);
        return new SalaryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SalaryViewHolder holder, int position) {
        Salary salary = salaryList.get(position);
        holder.bind(salary, listener);
    }

    @Override
    public int getItemCount() {
        return salaryList.size();
    }

    static class SalaryViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvEmpName;
        private final TextView tvPeriodRate;
        private final TextView tvStatusBadge;
        private final TextView tvTotalHours;
        private final TextView tvOtHours;
        private final TextView tvTotalAmount;
        private final Button btnToggleStatus;

        public SalaryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEmpName = itemView.findViewById(R.id.tvItemSalEmpName);
            tvPeriodRate = itemView.findViewById(R.id.tvItemSalPeriodRate);
            tvStatusBadge = itemView.findViewById(R.id.tvItemSalStatusBadge);
            tvTotalHours = itemView.findViewById(R.id.tvItemSalTotalHours);
            tvOtHours = itemView.findViewById(R.id.tvItemSalOtHours);
            tvTotalAmount = itemView.findViewById(R.id.tvItemSalTotalAmount);
            btnToggleStatus = itemView.findViewById(R.id.btnTogglePaidStatus);
        }

        public void bind(Salary salary, OnSalaryActionListener listener) {
            Context ctx = itemView.getContext();

            tvEmpName.setText(salary.getEmployeeName() != null ? salary.getEmployeeName() : "Nhân viên");

            String subInfo = "Mã NV: " + (salary.getEmployeeCode() != null ? salary.getEmployeeCode() : "---")
                    + " • " + FormatUtils.formatCurrency(salary.getHourlyRate()) + "/giờ";
            tvPeriodRate.setText(subInfo);

            tvTotalHours.setText(FormatUtils.formatHours(salary.getTotalHours()));
            tvOtHours.setText(FormatUtils.formatHours(salary.getOvertimeHours()));
            tvTotalAmount.setText(FormatUtils.formatCurrency(salary.getTotalSalary()));

            boolean isPaid = Salary.STATUS_PAID.equalsIgnoreCase(salary.getStatus());
            if (isPaid) {
                tvStatusBadge.setText("ĐÃ THANH TOÁN");
                tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_success);
                tvStatusBadge.setTextColor(ContextCompat.getColor(ctx, R.color.success));
                btnToggleStatus.setText("Đổi sang Chưa trả");
            } else {
                tvStatusBadge.setText("CHỜ THANH TOÁN");
                tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_warning);
                tvStatusBadge.setTextColor(ContextCompat.getColor(ctx, R.color.warning));
                btnToggleStatus.setText("Đánh dấu Đã trả");
            }

            btnToggleStatus.setOnClickListener(v -> {
                if (listener != null) listener.onToggleStatus(salary);
            });
        }
    }
}
