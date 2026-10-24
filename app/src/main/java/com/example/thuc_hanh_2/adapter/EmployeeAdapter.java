package com.example.thuc_hanh_2.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.thuc_hanh_2.R;
import com.example.thuc_hanh_2.model.Employee;
import com.example.thuc_hanh_2.util.FormatUtils;

import java.util.ArrayList;
import java.util.List;

public class EmployeeAdapter extends RecyclerView.Adapter<EmployeeAdapter.EmployeeViewHolder> {

    public interface OnEmployeeActionListener {
        void onEdit(Employee employee);
        void onDelete(Employee employee);
    }

    private List<Employee> employeeList = new ArrayList<>();
    private final OnEmployeeActionListener listener;

    public EmployeeAdapter(OnEmployeeActionListener listener) {
        this.listener = listener;
    }

    public void setEmployees(List<Employee> list) {
        this.employeeList = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EmployeeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_employee, parent, false);
        return new EmployeeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmployeeViewHolder holder, int position) {
        Employee emp = employeeList.get(position);
        holder.bind(emp, listener);
    }

    @Override
    public int getItemCount() {
        return employeeList.size();
    }

    static class EmployeeViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvAvatar;
        private final TextView tvName;
        private final TextView tvCode;
        private final TextView tvContact;
        private final TextView tvRate;
        private final ImageButton btnEdit;
        private final ImageButton btnDelete;

        public EmployeeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatar = itemView.findViewById(R.id.tvItemEmpAvatar);
            tvName = itemView.findViewById(R.id.tvItemEmpName);
            tvCode = itemView.findViewById(R.id.tvItemEmpCode);
            tvContact = itemView.findViewById(R.id.tvItemEmpContact);
            tvRate = itemView.findViewById(R.id.tvItemEmpRate);
            btnEdit = itemView.findViewById(R.id.btnItemEditEmp);
            btnDelete = itemView.findViewById(R.id.btnItemDeleteEmp);
        }

        public void bind(Employee emp, OnEmployeeActionListener listener) {
            tvName.setText(emp.getFullname());
            tvCode.setText(emp.getCode());

            String initials = getInitials(emp.getFullname());
            tvAvatar.setText(initials);

            String contact = "";
            if (emp.getPhone() != null && !emp.getPhone().isEmpty()) {
                contact += emp.getPhone();
            }
            if (emp.getEmail() != null && !emp.getEmail().isEmpty()) {
                if (!contact.isEmpty()) contact += " • ";
                contact += emp.getEmail();
            }
            if (contact.isEmpty()) contact = "Chưa cập nhật thông tin liên hệ";
            tvContact.setText(contact);

            tvRate.setText(FormatUtils.formatCurrency(emp.getHourlyRate()) + "/giờ");

            btnEdit.setOnClickListener(v -> {
                if (listener != null) listener.onEdit(emp);
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) listener.onDelete(emp);
            });
        }

        private String getInitials(String name) {
            if (name == null || name.trim().isEmpty()) return "NV";
            String[] parts = name.trim().split("\\s+");
            if (parts.length == 1) {
                return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
            } else {
                String first = parts[0].substring(0, 1);
                String last = parts[parts.length - 1].substring(0, 1);
                return (first + last).toUpperCase();
            }
        }
    }
}
