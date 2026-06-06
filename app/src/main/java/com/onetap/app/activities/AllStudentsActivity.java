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
import com.onetap.app.adapters.StudentAdapter;
import com.onetap.app.firebase.FirebaseAdminManager;
import com.onetap.app.models.User;

import java.util.ArrayList;
import java.util.List;

public class AllStudentsActivity extends AppCompatActivity {

    private RecyclerView rvStudents;
    private LinearLayout layoutEmpty;
    private ImageView ivBack;
    private StudentAdapter adapter;
    private FirebaseAdminManager adminManager;
    private List<User> studentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_students);

        adminManager = new FirebaseAdminManager();
        studentList = new ArrayList<>();

        initViews();
        setupRecyclerView();
        loadStudents();
    }

    private void initViews() {
        rvStudents = findViewById(R.id.rvStudents);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        adapter = new StudentAdapter(studentList);
        rvStudents.setLayoutManager(new LinearLayoutManager(this));
        rvStudents.setAdapter(adapter);
    }

    private void loadStudents() {
        adminManager.getAllStudents(new FirebaseAdminManager.UserListCallback() {
            @Override
            public void onUsersLoaded(List<User> users) {
                studentList.clear();
                studentList.addAll(users);
                adapter.updateData(studentList);

                layoutEmpty.setVisibility(users.isEmpty() ? View.VISIBLE : View.GONE);
                rvStudents.setVisibility(users.isEmpty() ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(AllStudentsActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}