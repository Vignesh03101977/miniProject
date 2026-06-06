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

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.ViewHolder> {

    private List<Attendance> attendanceList;

    public AttendanceAdapter(List<Attendance> attendanceList) {
        this.attendanceList = attendanceList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_attendance, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Attendance attendance = attendanceList.get(position);
        holder.tvSessionTitle.setText(attendance.getSessionTitle());
        holder.tvSubjectName.setText(attendance.getSubjectName());

        String status = attendance.getStatus();
        holder.tvStatus.setText(status != null ? status.substring(0, 1).toUpperCase() + status.substring(1) : "Present");

        if ("present".equals(status)) {
            holder.tvStatus.setTextColor(holder.itemView.getContext().getColor(R.color.accent_green));
        } else {
            holder.tvStatus.setTextColor(holder.itemView.getContext().getColor(R.color.accent_red));
        }

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        holder.tvDate.setText(sdf.format(new Date(attendance.getMarkedAt())));
    }

    @Override
    public int getItemCount() {
        return attendanceList.size();
    }

    public void updateData(List<Attendance> newList) {
        this.attendanceList = newList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSessionTitle, tvSubjectName, tvStatus, tvDate;
        View viewStatus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSessionTitle = itemView.findViewById(R.id.tvSessionTitle);
            tvSubjectName = itemView.findViewById(R.id.tvSubjectName);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDate = itemView.findViewById(R.id.tvDate);
            viewStatus = itemView.findViewById(R.id.viewStatus);
        }
    }
}