package com.onetap.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.onetap.app.R;
import com.onetap.app.firebase.FirebaseAdminManager;
import com.onetap.app.firebase.FirebaseManager;
import com.onetap.app.models.User;

import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity {

    private CardView cardPendingApprovals, cardApprovedTeachers, cardAllStudents;
    private TextView tvPendingCount, tvApprovedCount, tvStudentCount;
    private ImageView ivLogout;
    private FirebaseAdminManager adminManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        adminManager = new FirebaseAdminManager();
        initViews();
        loadCounts();
        setupListeners();
    }

    private void initViews() {
        cardPendingApprovals = findViewById(R.id.cardPendingApprovals);
        cardApprovedTeachers = findViewById(R.id.cardApprovedTeachers);
        cardAllStudents = findViewById(R.id.cardAllStudents);
        tvPendingCount = findViewById(R.id.tvPendingCount);
        tvApprovedCount = findViewById(R.id.tvApprovedCount);
        tvStudentCount = findViewById(R.id.tvStudentCount);
        ivLogout = findViewById(R.id.ivLogout);
    }

    private void loadCounts() {
        adminManager.getPendingTeachers(new FirebaseAdminManager.UserListCallback() {
            @Override
            public void onUsersLoaded(List<User> users) {
                tvPendingCount.setText(users.size() + " teachers waiting");
            }

            @Override
            public void onError(String error) {}
        });

        adminManager.getApprovedTeachers(new FirebaseAdminManager.UserListCallback() {
            @Override
            public void onUsersLoaded(List<User> users) {
                tvApprovedCount.setText(users.size() + " active teachers");
            }

            @Override
            public void onError(String error) {}
        });

        adminManager.getAllStudents(new FirebaseAdminManager.UserListCallback() {
            @Override
            public void onUsersLoaded(List<User> users) {
                tvStudentCount.setText(users.size() + " registered students");
            }

            @Override
            public void onError(String error) {}
        });
    }

    private void setupListeners() {
        cardPendingApprovals.setOnClickListener(v ->
                startActivity(new Intent(this, PendingApprovalActivity.class)));

        cardApprovedTeachers.setOnClickListener(v ->
                startActivity(new Intent(this, ApprovedTeachersActivity.class)));

        cardAllStudents.setOnClickListener(v ->
                startActivity(new Intent(this, AllStudentsActivity.class)));

        ivLogout.setOnClickListener(v -> showLogoutDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCounts();
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