package com.onetap.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.onetap.app.R;
import com.onetap.app.models.User;

import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.ViewHolder> {

    private List<User> students;

    public StudentAdapter(List<User> students) {
        this.students = students;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User student = students.get(position);
        holder.tvStudentName.setText(student.getFullName());
        holder.tvStudentId.setText("ID: " + student.getStudentId());
        holder.tvDepartment.setText(student.getDepartment());
        holder.tvEmail.setText(student.getEmail());
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    public void updateData(List<User> newStudents) {
        this.students = newStudents;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStudentName, tvStudentId, tvDepartment, tvEmail;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvStudentId = itemView.findViewById(R.id.tvStudentId);
            tvDepartment = itemView.findViewById(R.id.tvDepartment);
            tvEmail = itemView.findViewById(R.id.tvEmail);
        }
    }
}