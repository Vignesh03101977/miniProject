package com.onetap.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.onetap.app.R;
import com.onetap.app.models.User;

import java.util.List;

public class PendingTeacherAdapter extends RecyclerView.Adapter<PendingTeacherAdapter.ViewHolder> {

    private List<User> teachers;
    private OnActionListener listener;

    public interface OnActionListener {
        void onApprove(User teacher);
        void onReject(User teacher);
    }

    public PendingTeacherAdapter(List<User> teachers, OnActionListener listener) {
        this.teachers = teachers;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pending_teacher, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User teacher = teachers.get(position);
        holder.tvTeacherName.setText(teacher.getFullName());
        holder.tvEmail.setText(teacher.getEmail());
        holder.tvDepartment.setText(teacher.getDepartment());

        holder.btnApprove.setOnClickListener(v -> listener.onApprove(teacher));
        holder.btnReject.setOnClickListener(v -> listener.onReject(teacher));
    }

    @Override
    public int getItemCount() {
        return teachers.size();
    }

    public void updateData(List<User> newTeachers) {
        this.teachers = newTeachers;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTeacherName, tvEmail, tvDepartment;
        MaterialButton btnApprove, btnReject;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTeacherName = itemView.findViewById(R.id.tvTeacherName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvDepartment = itemView.findViewById(R.id.tvDepartment);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}