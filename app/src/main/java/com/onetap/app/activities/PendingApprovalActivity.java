package com.onetap.app.activities;

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
import com.onetap.app.adapters.PendingTeacherAdapter;
import com.onetap.app.firebase.FirebaseAdminManager;
import com.onetap.app.models.User;

import java.util.ArrayList;
import java.util.List;

public class PendingApprovalActivity extends AppCompatActivity implements PendingTeacherAdapter.OnActionListener {

    private RecyclerView rvPendingTeachers;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;
    private ImageView ivBack;
    private PendingTeacherAdapter adapter;
    private FirebaseAdminManager adminManager;
    private List<User> teacherList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_approval);

        adminManager = new FirebaseAdminManager();
        teacherList = new ArrayList<>();

        initViews();
        setupRecyclerView();
        loadPendingTeachers();
    }

    private void initViews() {
        rvPendingTeachers = findViewById(R.id.rvPendingTeachers);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        progressBar = findViewById(R.id.progressBar);
        ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        adapter = new PendingTeacherAdapter(teacherList, this);
        rvPendingTeachers.setLayoutManager(new LinearLayoutManager(this));
        rvPendingTeachers.setAdapter(adapter);
    }

    private void loadPendingTeachers() {
        progressBar.setVisibility(View.VISIBLE);

        adminManager.getPendingTeachers(new FirebaseAdminManager.UserListCallback() {
            @Override
            public void onUsersLoaded(List<User> users) {
                progressBar.setVisibility(View.GONE);
                teacherList.clear();
                teacherList.addAll(users);
                adapter.updateData(teacherList);

                layoutEmpty.setVisibility(users.isEmpty() ? View.VISIBLE : View.GONE);
                rvPendingTeachers.setVisibility(users.isEmpty() ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(PendingApprovalActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onApprove(User teacher) {
        adminManager.approveTeacher(teacher.getUid(), new FirebaseAdminManager.ActionCallback() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(PendingApprovalActivity.this, message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(PendingApprovalActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onReject(User teacher) {
        adminManager.rejectTeacher(teacher.getUid(), new FirebaseAdminManager.ActionCallback() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(PendingApprovalActivity.this, message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(PendingApprovalActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}