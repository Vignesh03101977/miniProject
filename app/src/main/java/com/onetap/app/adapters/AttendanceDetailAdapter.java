package com.onetap.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.onetap.app.R;
import com.onetap.app.models.Attendance;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AttendanceDetailAdapter extends
        RecyclerView.Adapter<AttendanceDetailAdapter.ViewHolder> {

    private List<Attendance> list;

    public AttendanceDetailAdapter(List<Attendance> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_attendance_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Attendance att = list.get(position);

        // Serial number
        holder.tvSno.setText(String.valueOf(position + 1));

        // Student info
        holder.tvStudentName.setText(att.getStudentName() != null ?
                att.getStudentName() : "Unknown");

        holder.tvStudentId.setText(att.getStudentId() != null ?
                att.getStudentId() : "N/A");

        holder.tvDepartment.setText(att.getDepartment() != null ?
                att.getDepartment() : "N/A");

        // Status P or A
        String status = att.getStatus() != null ? att.getStatus() : "absent";
        boolean isPresent = "present".equalsIgnoreCase(status);

        holder.tvStatus.setText(isPresent ? "P" : "A");

        if (isPresent) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_active);
            holder.tvStatus.setTextColor(
                    holder.itemView.getContext().getColor(R.color.accent_green));
        } else {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_inactive);
            holder.tvStatus.setTextColor(
                    holder.itemView.getContext().getColor(R.color.accent_red));
        }

        // Marked at time
        if (att.getMarkedAt() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            holder.tvTime.setText(sdf.format(new Date(att.getMarkedAt())));
        } else {
            holder.tvTime.setText("N/A");
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void updateData(List<Attendance> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSno, tvStudentName, tvStudentId,
                tvDepartment, tvStatus, tvTime;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSno = itemView.findViewById(R.id.tvSno);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvStudentId = itemView.findViewById(R.id.tvStudentId);
            tvDepartment = itemView.findViewById(R.id.tvDepartment);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}