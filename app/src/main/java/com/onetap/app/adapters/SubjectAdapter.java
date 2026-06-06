package com.onetap.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.onetap.app.R;
import com.onetap.app.models.Attendance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SubjectAdapter extends
        RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder> {

    private List<String> subjectList;
    private Map<String, List<Attendance>> subjectMap;
    private OnSubjectActionListener listener;

    public interface OnSubjectActionListener {
        void onDownloadSubjectExcel(String subjectName, List<Attendance> attendanceList);
    }

    public SubjectAdapter(List<String> subjectList,
                          Map<String, List<Attendance>> subjectMap,
                          OnSubjectActionListener listener) {
        this.subjectList = subjectList;
        this.subjectMap = subjectMap;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_subject, parent, false);
        return new SubjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
        String subject = subjectList.get(position);
        List<Attendance> attendanceList = subjectMap.getOrDefault(subject, new ArrayList<>());
        holder.bind(subject, attendanceList);
    }

    @Override
    public int getItemCount() {
        return subjectList.size();
    }

    public void updateData(List<String> subjects, Map<String, List<Attendance>> map) {
        this.subjectList = subjects;
        this.subjectMap = map;
        notifyDataSetChanged();
    }

    class SubjectViewHolder extends RecyclerView.ViewHolder {
        TextView tvSubjectName, tvSessionCount, tvStudentCount, tvFileStatus;
        MaterialButton btnDownload;

        SubjectViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSubjectName = itemView.findViewById(R.id.tvSubjectName);
            tvSessionCount = itemView.findViewById(R.id.tvSessionCount);
            tvStudentCount = itemView.findViewById(R.id.tvStudentCount);
            tvFileStatus = itemView.findViewById(R.id.tvFileStatus);
            btnDownload = itemView.findViewById(R.id.btnDownload);
        }

        void bind(String subject, List<Attendance> attendanceList) {
            tvSubjectName.setText(subject);

            // Count unique sessions
            List<String> uniqueSessions = new ArrayList<>();
            List<String> uniqueStudents = new ArrayList<>();
            for (Attendance att : attendanceList) {
                if (att.getSessionCode() != null &&
                        !uniqueSessions.contains(att.getSessionCode())) {
                    uniqueSessions.add(att.getSessionCode());
                }
                if (att.getStudentUid() != null &&
                        !uniqueStudents.contains(att.getStudentUid())) {
                    uniqueStudents.add(att.getStudentUid());
                }
            }

            tvSessionCount.setText(uniqueSessions.size() + " sessions");
            tvStudentCount.setText(uniqueStudents.size() + " students");

            // ✅ FIXED: Removed subjectExcelExists() since we use MediaStore now
            // MediaStore saves to Downloads via URI, file path check is not possible
            tvFileStatus.setText("📥 Tap to download latest Excel");
            tvFileStatus.setTextColor(
                    itemView.getContext().getColor(R.color.text_secondary));

            btnDownload.setText("📥 Download Excel");
            btnDownload.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDownloadSubjectExcel(subject, attendanceList);
                }
            });
        }
    }
}