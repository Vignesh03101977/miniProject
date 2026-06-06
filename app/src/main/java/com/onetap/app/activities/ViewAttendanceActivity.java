package com.onetap.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.onetap.app.R;
import com.onetap.app.adapters.AttendanceAdapter;
import com.onetap.app.firebase.FirebaseAttendanceManager;
import com.onetap.app.firebase.FirebaseAuthManager;
import com.onetap.app.firebase.FirebaseManager;
import com.onetap.app.models.Attendance;
import com.onetap.app.models.User;
import com.onetap.app.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class ViewAttendanceActivity extends AppCompatActivity {

    private RecyclerView rvAttendance;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;
    private ImageView ivBack;
    private AttendanceAdapter adapter;
    private FirebaseAttendanceManager attendanceManager;
    private FirebaseAuthManager authManager;
    private List<Attendance> attendanceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_attendance);

        attendanceManager = new FirebaseAttendanceManager();
        authManager = new FirebaseAuthManager();
        attendanceList = new ArrayList<>();

        initViews();
        setupRecyclerView();
        loadAttendance();
    }

    private void initViews() {
        rvAttendance = findViewById(R.id.rvAttendance);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        progressBar = findViewById(R.id.progressBar);
        ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        adapter = new AttendanceAdapter(attendanceList);
        rvAttendance.setLayoutManager(new LinearLayoutManager(this));
        rvAttendance.setAdapter(adapter);
    }

    private void loadAttendance() {
        progressBar.setVisibility(View.VISIBLE);

        authManager.getCurrentUser(new FirebaseAuthManager.UserCallback() {
            @Override
            public void onUserLoaded(User user) {
                String uid = FirebaseManager.getInstance().getCurrentUserId();

                if (Constants.ROLE_STUDENT.equals(user.getRole())) {
                    // ✅ Student sees their own attendance records
                    attendanceManager.getAttendanceByStudent(uid, attendanceCallback);

                } else if (Constants.ROLE_TEACHER.equals(user.getRole())) {
                    // ✅ Teacher goes to subject-wise attendance screen
                    progressBar.setVisibility(View.GONE);
                    startActivity(new Intent(ViewAttendanceActivity.this,
                            SubjectAttendanceActivity.class));
                    finish();
                }
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ViewAttendanceActivity.this, error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private final FirebaseAttendanceManager.AttendanceListCallback attendanceCallback =
            new FirebaseAttendanceManager.AttendanceListCallback() {
                @Override
                public void onAttendanceLoaded(List<Attendance> list) {
                    progressBar.setVisibility(View.GONE);
                    attendanceList.clear();
                    attendanceList.addAll(list);
                    adapter.updateData(attendanceList);

                    layoutEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                    rvAttendance.setVisibility(list.isEmpty() ? View.GONE : View.VISIBLE);
                }

                @Override
                public void onError(String error) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ViewAttendanceActivity.this, error,
                            Toast.LENGTH_SHORT).show();
                }
            };
}