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
import com.onetap.app.adapters.SessionAdapter;
import com.onetap.app.firebase.FirebaseAttendanceManager;
import com.onetap.app.firebase.FirebaseManager;
import com.onetap.app.firebase.FirebaseSessionManager;
import com.onetap.app.models.Attendance;
import com.onetap.app.models.Session;

import java.util.ArrayList;
import java.util.List;

public class SessionHistoryActivity extends AppCompatActivity implements
        SessionAdapter.OnSessionActionListener {

    private RecyclerView rvSessions;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;
    private ImageView ivBack;
    private SessionAdapter adapter;
    private FirebaseSessionManager sessionManager;
    private FirebaseAttendanceManager attendanceManager;
    private List<Session> sessionList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_history);

        sessionManager = new FirebaseSessionManager();
        attendanceManager = new FirebaseAttendanceManager();
        sessionList = new ArrayList<>();

        initViews();
        setupRecyclerView();
        loadSessions();
    }

    private void initViews() {
        rvSessions = findViewById(R.id.rvSessions);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        progressBar = findViewById(R.id.progressBar);
        ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        adapter = new SessionAdapter(sessionList, this, false);
        rvSessions.setLayoutManager(new LinearLayoutManager(this));
        rvSessions.setAdapter(adapter);
    }

    private void loadSessions() {
        progressBar.setVisibility(View.VISIBLE);
        String teacherId = FirebaseManager.getInstance().getCurrentUserId();

        sessionManager.getAllSessionsByTeacher(teacherId,
                new FirebaseSessionManager.SessionListCallback() {
                    @Override
                    public void onSessionsLoaded(List<Session> sessions) {
                        progressBar.setVisibility(View.GONE);
                        sessionList.clear();
                        sessionList.addAll(sessions);
                        adapter.updateData(sessionList);

                        layoutEmpty.setVisibility(sessions.isEmpty() ?
                                View.VISIBLE : View.GONE);
                        rvSessions.setVisibility(sessions.isEmpty() ?
                                View.GONE : View.VISIBLE);
                    }

                    @Override
                    public void onError(String error) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(SessionHistoryActivity.this,
                                error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onEndSession(Session session) {
        // Not used in history
    }

    // ✅ Click on session card → open SessionAttendanceDetailActivity
    @Override
    public void onSessionClick(Session session) {
        Intent intent = new Intent(this, SessionAttendanceDetailActivity.class);
        intent.putExtra("sessionCode", session.getSessionCode());
        intent.putExtra("sessionTitle", session.getSessionTitle());
        intent.putExtra("subjectName", session.getSubjectName());
        startActivity(intent);
    }

    // ✅ Download button on session card → open SessionAttendanceDetailActivity
    @Override
    public void onDownloadAttendance(Session session) {
        Intent intent = new Intent(this, SessionAttendanceDetailActivity.class);
        intent.putExtra("sessionCode", session.getSessionCode());
        intent.putExtra("sessionTitle", session.getSessionTitle());
        intent.putExtra("subjectName", session.getSubjectName());
        startActivity(intent);
    }
}