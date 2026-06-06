package com.onetap.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.onetap.app.R;
import com.onetap.app.firebase.FirebaseAuthManager;
import com.onetap.app.firebase.FirebaseManager;
import com.onetap.app.models.User;

public class StudentDashboardActivity extends AppCompatActivity {

    private TextView tvStudentName, tvStudentId, tvDepartment;
    private CardView cardMarkAttendance, cardAttendanceHistory;
    private ImageView ivLogout;
    private FirebaseAuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        authManager = new FirebaseAuthManager();
        initViews();
        loadUserData();
        setupListeners();
    }

    private void initViews() {
        tvStudentName = findViewById(R.id.tvStudentName);
        tvStudentId = findViewById(R.id.tvStudentId);
        tvDepartment = findViewById(R.id.tvDepartment);
        cardMarkAttendance = findViewById(R.id.cardMarkAttendance);
        cardAttendanceHistory = findViewById(R.id.cardAttendanceHistory);
        ivLogout = findViewById(R.id.ivLogout);
    }

    private void loadUserData() {
        authManager.getCurrentUser(new FirebaseAuthManager.UserCallback() {
            @Override
            public void onUserLoaded(User user) {
                tvStudentName.setText(user.getFullName());
                tvStudentId.setText(user.getStudentId());
                tvDepartment.setText(user.getDepartment());
            }

            @Override
            public void onError(String error) {
                tvStudentName.setText("Student");
            }
        });
    }

    private void setupListeners() {
        // ✅ Opens MarkAttendanceActivity
        // Session will be fetched inside MarkAttendanceActivity using session code
        cardMarkAttendance.setOnClickListener(v ->
                startActivity(new Intent(this, MarkAttendanceActivity.class)));

        cardAttendanceHistory.setOnClickListener(v ->
                startActivity(new Intent(this, ViewAttendanceActivity.class)));

        ivLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage(getString(R.string.confirm_logout))
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    FirebaseManager.getInstance().signOut();
                    Intent intent = new Intent(this, MainLandingActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }
}