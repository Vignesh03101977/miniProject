package com.onetap.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.onetap.app.R;
import com.onetap.app.adapters.SubjectAdapter;
import com.onetap.app.firebase.FirebaseAttendanceManager;
import com.onetap.app.firebase.FirebaseManager;
import com.onetap.app.models.Attendance;
import com.onetap.app.utils.ExcelManager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SubjectAttendanceActivity extends AppCompatActivity
        implements SubjectAdapter.OnSubjectActionListener {

    private RecyclerView rvSubjects;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;
    private ImageView ivBack;
    private TextView tvTitle;

    private SubjectAdapter adapter;
    private FirebaseAttendanceManager attendanceManager;

    private final Map<String, List<Attendance>> subjectAttendanceMap = new LinkedHashMap<>();
    private final List<String> subjectList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subject_attendance);

        attendanceManager = new FirebaseAttendanceManager();

        initViews();
        setupRecyclerView();
        loadSubjects();
    }

    private void initViews() {
        rvSubjects = findViewById(R.id.rvSubjects);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        progressBar = findViewById(R.id.progressBar);
        ivBack = findViewById(R.id.ivBack);
        tvTitle = findViewById(R.id.tvTitle);

        ivBack.setOnClickListener(v -> onBackPressed());
        tvTitle.setText("Attendance by Subject");
    }

    private void setupRecyclerView() {
        adapter = new SubjectAdapter(subjectList, subjectAttendanceMap, this);
        rvSubjects.setLayoutManager(new LinearLayoutManager(this));
        rvSubjects.setAdapter(adapter);
    }

    private void loadSubjects() {
        progressBar.setVisibility(View.VISIBLE);
        String teacherId = FirebaseManager.getInstance().getCurrentUserId();

        attendanceManager.getAttendanceByTeacher(teacherId,
                new FirebaseAttendanceManager.AttendanceListCallback() {
                    @Override
                    public void onAttendanceLoaded(List<Attendance> allAttendance) {
                        progressBar.setVisibility(View.GONE);

                        subjectAttendanceMap.clear();
                        subjectList.clear();

                        // Group by subject name (case-insensitive merge)
                        for (Attendance att : allAttendance) {
                            String subject = att.getSubjectName();
                            if (subject == null || subject.trim().isEmpty()) continue;

                            String foundKey = null;
                            for (String key : subjectAttendanceMap.keySet()) {
                                if (key.equalsIgnoreCase(subject.trim())) {
                                    foundKey = key;
                                    break;
                                }
                            }

                            if (foundKey == null) {
                                foundKey = subject.trim();
                                subjectAttendanceMap.put(foundKey, new ArrayList<>());
                                subjectList.add(foundKey);
                            }

                            subjectAttendanceMap.get(foundKey).add(att);
                        }

                        adapter.updateData(subjectList, subjectAttendanceMap);

                        layoutEmpty.setVisibility(subjectList.isEmpty() ? View.VISIBLE : View.GONE);
                        rvSubjects.setVisibility(subjectList.isEmpty() ? View.GONE : View.VISIBLE);
                    }

                    @Override
                    public void onError(String error) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(SubjectAttendanceActivity.this,
                                "Error: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onDownloadSubjectExcel(String subjectName, List<Attendance> attendanceList) {
        if (attendanceList == null || attendanceList.isEmpty()) {
            Toast.makeText(this, "No attendance data for this subject.", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        ExcelManager.exportSubjectAttendanceToDownloads(
                this,
                subjectName,
                attendanceList,
                new ExcelManager.ExportCallback() {
                    @Override
                    public void onSuccess(String savedLocation) {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(SubjectAttendanceActivity.this,
                                    "✅ Excel saved!\n" + savedLocation,
                                    Toast.LENGTH_LONG).show();
                        });
                    }

                    @Override
                    public void onError(String errorMessage) {
                        runOnUiThread(() -> {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(SubjectAttendanceActivity.this,
                                    "❌ " + errorMessage,
                                    Toast.LENGTH_LONG).show();
                        });
                    }
                }
        );
    }
}