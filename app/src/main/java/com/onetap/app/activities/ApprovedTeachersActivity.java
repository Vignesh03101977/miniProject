package com.onetap.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.onetap.app.R;
import com.onetap.app.adapters.ApprovedTeacherAdapter;
import com.onetap.app.firebase.FirebaseAdminManager;
import com.onetap.app.models.User;

import java.util.ArrayList;
import java.util.List;

public class ApprovedTeachersActivity extends AppCompatActivity {

    private RecyclerView rvApprovedTeachers;
    private LinearLayout layoutEmpty;
    private ImageView ivBack;
    private ApprovedTeacherAdapter adapter;
    private FirebaseAdminManager adminManager;
    private List<User> teacherList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approved_teachers);

        adminManager = new FirebaseAdminManager();
        teacherList = new ArrayList<>();

        initViews();
        setupRecyclerView();
        loadApprovedTeachers();
    }

    private void initViews() {
        rvApprovedTeachers = findViewById(R.id.rvApprovedTeachers);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        adapter = new ApprovedTeacherAdapter(teacherList);
        rvApprovedTeachers.setLayoutManager(new LinearLayoutManager(this));
        rvApprovedTeachers.setAdapter(adapter);
    }

    private void loadApprovedTeachers() {
        adminManager.getApprovedTeachers(new FirebaseAdminManager.UserListCallback() {
            @Override
            public void onUsersLoaded(List<User> users) {
                teacherList.clear();
                teacherList.addAll(users);
                adapter.updateData(teacherList);

                layoutEmpty.setVisibility(users.isEmpty() ? View.VISIBLE : View.GONE);
                rvApprovedTeachers.setVisibility(users.isEmpty() ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ApprovedTeachersActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}