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

public class TeacherDashboardActivity extends AppCompatActivity {

    private TextView tvTeacherName, tvDepartment;
    private CardView cardCreateSession, cardActiveSessions, cardSessionHistory, cardViewAttendance;
    private ImageView ivLogout;
    private FirebaseAuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_dashboard);

        authManager = new FirebaseAuthManager();
        initViews();
        loadUserData();
        setupListeners();
    }

    private void initViews() {
        tvTeacherName = findViewById(R.id.tvTeacherName);
        tvDepartment = findViewById(R.id.tvDepartment);
        cardCreateSession = findViewById(R.id.cardCreateSession);
        cardActiveSessions = findViewById(R.id.cardActiveSessions);
        cardSessionHistory = findViewById(R.id.cardSessionHistory);
        cardViewAttendance = findViewById(R.id.cardViewAttendance);
        ivLogout = findViewById(R.id.ivLogout);
    }

    private void loadUserData() {
        authManager.getCurrentUser(new FirebaseAuthManager.UserCallback() {
            @Override
            public void onUserLoaded(User user) {
                tvTeacherName.setText(user.getFullName());
                tvDepartment.setText(user.getDepartment());
            }

            @Override
            public void onError(String error) {
                tvTeacherName.setText("Teacher");
            }
        });
    }

    private void setupListeners() {
        cardCreateSession.setOnClickListener(v ->
                startActivity(new Intent(this, CreateSessionActivity.class)));

        cardActiveSessions.setOnClickListener(v ->
                startActivity(new Intent(this, ActiveSessionsActivity.class)));

        cardSessionHistory.setOnClickListener(v ->
                startActivity(new Intent(this, SessionHistoryActivity.class)));

        cardViewAttendance.setOnClickListener(v ->
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