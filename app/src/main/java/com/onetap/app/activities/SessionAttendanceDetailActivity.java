package com.onetap.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.onetap.app.R;
import com.onetap.app.firebase.FirebaseAttendanceManager;
import com.onetap.app.firebase.FirebaseManager;
import com.onetap.app.models.Attendance;
import com.onetap.app.utils.ExcelManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SessionAttendanceDetailActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private ImageView ivBack;
    private TextView tvSessionTitle, tvSubjectName, tvSessionCode;
    private TextView tvTotalCount, tvPresentCount, tvAbsentCount;
    private MaterialButton btnDownloadExcel;
    private ScrollView scrollView;
    private TableLayout tableLayout;
    private LinearLayout layoutEmpty;

    private FirebaseAttendanceManager attendanceManager;

    private final List<Attendance> currentSessionAttendance = new ArrayList<>();
    private final List<Attendance> allSubjectAttendance = new ArrayList<>();

    private String sessionCode = "";
    private String sessionTitle = "";
    private String subjectName = "";
    private String teacherId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_attendance_detail);

        attendanceManager = new FirebaseAttendanceManager();
        teacherId = FirebaseManager.getInstance().getCurrentUserId();

        sessionCode = getIntent().getStringExtra("sessionCode");
        sessionTitle = getIntent().getStringExtra("sessionTitle");
        subjectName = getIntent().getStringExtra("subjectName");

        if (sessionCode == null) sessionCode = "";
        if (sessionTitle == null) sessionTitle = "Session";
        if (subjectName == null) subjectName = "Subject";

        initViews();
        loadCurrentSessionAttendance();
    }

    private void initViews() {
        progressBar = findViewById(R.id.progressBar);
        ivBack = findViewById(R.id.ivBack);
        tvSessionTitle = findViewById(R.id.tvSessionTitle);
        tvSubjectName = findViewById(R.id.tvSubjectName);
        tvSessionCode = findViewById(R.id.tvSessionCode);
        tvTotalCount = findViewById(R.id.tvTotalCount);
        tvPresentCount = findViewById(R.id.tvPresentCount);
        tvAbsentCount = findViewById(R.id.tvAbsentCount);
        btnDownloadExcel = findViewById(R.id.btnDownloadExcel);
        scrollView = findViewById(R.id.scrollView);
        tableLayout = findViewById(R.id.tableLayout);
        layoutEmpty = findViewById(R.id.layoutEmpty);

        ivBack.setOnClickListener(v -> onBackPressed());

        tvSessionTitle.setText(sessionTitle);
        tvSubjectName.setText(subjectName);
        tvSessionCode.setText("Code: " + sessionCode);

        btnDownloadExcel.setEnabled(false);
        btnDownloadExcel.setOnClickListener(v -> startDownload());
    }

    private void loadCurrentSessionAttendance() {
        progressBar.setVisibility(View.VISIBLE);
        tableLayout.removeAllViews();

        attendanceManager.getAttendanceBySessionCode(sessionCode,
                new FirebaseAttendanceManager.AttendanceListCallback() {
                    @Override
                    public void onAttendanceLoaded(List<Attendance> list) {
                        progressBar.setVisibility(View.GONE);

                        currentSessionAttendance.clear();
                        currentSessionAttendance.addAll(list);

                        int present = 0, absent = 0;
                        for (Attendance att : list) {
                            if ("present".equalsIgnoreCase(att.getStatus())) {
                                present++;
                            } else {
                                absent++;
                            }
                        }

                        tvTotalCount.setText("Total: " + list.size());
                        tvPresentCount.setText("Present: " + present);
                        tvAbsentCount.setText("Absent: " + absent);

                        if (list.isEmpty()) {
                            layoutEmpty.setVisibility(View.VISIBLE);
                            scrollView.setVisibility(View.GONE);
                        } else {
                            layoutEmpty.setVisibility(View.GONE);
                            scrollView.setVisibility(View.VISIBLE);
                            buildTableUI(list);
                        }

                        btnDownloadExcel.setEnabled(true);
                    }

                    @Override
                    public void onError(String error) {
                        progressBar.setVisibility(View.GONE);
                        btnDownloadExcel.setEnabled(true);
                        Toast.makeText(SessionAttendanceDetailActivity.this,
                                "Error: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void buildTableUI(List<Attendance> list) {
        tableLayout.removeAllViews();
        tableLayout.setStretchAllColumns(true);

        TableRow headerRow = new TableRow(this);
        headerRow.setBackgroundColor(ContextCompat.getColor(this, R.color.primary));
        int padding = dpToPx(8);

        String[] headers = {"S.No", "Student Name", "Roll No", "Dept", "Status", "Time"};
        for (String h : headers) {
            TextView tv = new TextView(this);
            tv.setText(h);
            tv.setTextColor(ContextCompat.getColor(this, R.color.text_white));
            tv.setTypeface(null, android.graphics.Typeface.BOLD);
            tv.setTextSize(12);
            tv.setPadding(padding, padding, padding, padding);
            headerRow.addView(tv);
        }
        tableLayout.addView(headerRow);

        SimpleDateFormat timeFmt = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        int sno = 1;

        for (Attendance att : list) {
            TableRow row = new TableRow(this);
            row.setBackgroundColor(sno % 2 == 0 ? 0xFFF5F5F5 : 0xFFFFFFFF);

            boolean isPresent = "present".equalsIgnoreCase(att.getStatus());

            String[] values = {
                    String.valueOf(sno),
                    att.getStudentName() != null ? att.getStudentName() : "Unknown",
                    att.getStudentId() != null ? att.getStudentId() : "N/A",
                    att.getDepartment() != null ? att.getDepartment() : "N/A",
                    isPresent ? "P" : "A",
                    att.getMarkedAt() > 0 ? timeFmt.format(new Date(att.getMarkedAt())) : "N/A"
            };

            for (int i = 0; i < values.length; i++) {
                TextView tv = new TextView(this);
                tv.setText(values[i]);
                tv.setTextSize(12);
                tv.setPadding(padding, padding, padding, padding);

                if (i == 4) {
                    if (isPresent) {
                        tv.setTextColor(ContextCompat.getColor(this, R.color.accent_green));
                        tv.setTypeface(null, android.graphics.Typeface.BOLD);
                    } else {
                        tv.setTextColor(ContextCompat.getColor(this, R.color.accent_red));
                        tv.setTypeface(null, android.graphics.Typeface.BOLD);
                    }
                } else {
                    tv.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
                }

                row.addView(tv);
            }

            tableLayout.addView(row);

            View divider = new View(this);
            divider.setLayoutParams(new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT, 1));
            divider.setBackgroundColor(0xFFE0E0E0);
            tableLayout.addView(divider);

            sno++;
        }
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private void startDownload() {
        progressBar.setVisibility(View.VISIBLE);
        btnDownloadExcel.setEnabled(false);
        btnDownloadExcel.setText("Fetching all sessions...");

        attendanceManager.getAttendanceBySubjectAndTeacher(
                subjectName,
                teacherId,
                new FirebaseAttendanceManager.AttendanceListCallback() {
                    @Override
                    public void onAttendanceLoaded(List<Attendance> allAttendance) {
                        allSubjectAttendance.clear();

                        if (allAttendance.isEmpty()) {
                            allSubjectAttendance.addAll(currentSessionAttendance);
                        } else {
                            allSubjectAttendance.addAll(allAttendance);
                        }

                        runOnUiThread(() -> btnDownloadExcel.setText("Generating Excel..."));

                        ExcelManager.exportSubjectAttendanceToDownloads(
                                SessionAttendanceDetailActivity.this,
                                subjectName,
                                allSubjectAttendance,
                                new ExcelManager.ExportCallback() {
                                    @Override
                                    public void onSuccess(String savedLocation) {
                                        runOnUiThread(() -> {
                                            progressBar.setVisibility(View.GONE);
                                            btnDownloadExcel.setEnabled(true);
                                            btnDownloadExcel.setText("📥 Download Excel");
                                            Toast.makeText(
                                                    SessionAttendanceDetailActivity.this,
                                                    "✅ Excel saved!\n" + savedLocation +
                                                            "\nOpen Downloads app to find it.",
                                                    Toast.LENGTH_LONG).show();
                                        });
                                    }

                                    @Override
                                    public void onError(String errorMessage) {
                                        runOnUiThread(() -> {
                                            progressBar.setVisibility(View.GONE);
                                            btnDownloadExcel.setEnabled(true);
                                            btnDownloadExcel.setText("📥 Download Excel");
                                            Toast.makeText(
                                                    SessionAttendanceDetailActivity.this,
                                                    "❌ " + errorMessage,
                                                    Toast.LENGTH_LONG).show();
                                        });
                                    }
                                }
                        );
                    }

                    @Override
                    public void onError(String error) {
                        allSubjectAttendance.clear();
                        allSubjectAttendance.addAll(currentSessionAttendance);

                        ExcelManager.exportSubjectAttendanceToDownloads(
                                SessionAttendanceDetailActivity.this,
                                subjectName,
                                allSubjectAttendance,
                                new ExcelManager.ExportCallback() {
                                    @Override
                                    public void onSuccess(String savedLocation) {
                                        runOnUiThread(() -> {
                                            progressBar.setVisibility(View.GONE);
                                            btnDownloadExcel.setEnabled(true);
                                            btnDownloadExcel.setText("📥 Download Excel");
                                            Toast.makeText(
                                                    SessionAttendanceDetailActivity.this,
                                                    "✅ Excel saved!\n" + savedLocation,
                                                    Toast.LENGTH_LONG).show();
                                        });
                                    }

                                    @Override
                                    public void onError(String errorMessage) {
                                        runOnUiThread(() -> {
                                            progressBar.setVisibility(View.GONE);
                                            btnDownloadExcel.setEnabled(true);
                                            btnDownloadExcel.setText("📥 Download Excel");
                                            Toast.makeText(
                                                    SessionAttendanceDetailActivity.this,
                                                    "❌ " + errorMessage,
                                                    Toast.LENGTH_LONG).show();
                                        });
                                    }
                                }
                        );
                    }
                });
    }
}