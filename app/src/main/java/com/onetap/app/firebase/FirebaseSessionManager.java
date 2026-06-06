package com.onetap.app.firebase;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.onetap.app.models.Session;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FirebaseSessionManager {
    private FirebaseManager firebaseManager;

    public interface SessionCallback {
        void onSuccess(String message);
        void onFailure(String error);
    }

    public interface SessionDataCallback {
        void onSessionLoaded(Session session);
        void onError(String error);
    }

    public interface SessionListCallback {
        void onSessionsLoaded(List<Session> sessions);
        void onError(String error);
    }

    public FirebaseSessionManager() {
        firebaseManager = FirebaseManager.getInstance();
    }

    public String generateSessionCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }

    public void createSession(Session session, SessionCallback callback) {
        String sessionId = firebaseManager.getSessionsRef().push().getKey();
        if (sessionId == null) {
            callback.onFailure("Failed to generate session ID");
            return;
        }
        session.setSessionId(sessionId);

        firebaseManager.getSessionsRef().child(sessionId).setValue(session)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess("Session created successfully");
                    } else {
                        callback.onFailure("Failed to create session");
                    }
                });
    }

    public void getSessionByCode(String code, SessionDataCallback callback) {
        firebaseManager.getSessionsRef()
                .orderByChild("sessionCode")
                .equalTo(code)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot child : snapshot.getChildren()) {
                                Session session = child.getValue(Session.class);
                                if (session != null) {
                                    callback.onSessionLoaded(session);
                                    return;
                                }
                            }
                        }
                        callback.onError("Session not found");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    public void getActiveSessions(String teacherId, SessionListCallback callback) {
        firebaseManager.getSessionsRef()
                .orderByChild("teacherId")
                .equalTo(teacherId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Session> sessions = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Session session = child.getValue(Session.class);
                            if (session != null && session.isActive()) {
                                // Check if session has expired
                                if (System.currentTimeMillis() > session.getEndTime()) {
                                    // Auto-deactivate expired session
                                    child.getRef().child("active").setValue(false);
                                } else {
                                    sessions.add(session);
                                }
                            }
                        }
                        callback.onSessionsLoaded(sessions);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    public void getAllSessionsByTeacher(String teacherId, SessionListCallback callback) {
        firebaseManager.getSessionsRef()
                .orderByChild("teacherId")
                .equalTo(teacherId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Session> sessions = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Session session = child.getValue(Session.class);
                            if (session != null) {
                                sessions.add(session);
                            }
                        }
                        callback.onSessionsLoaded(sessions);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    public void endSession(String sessionId, SessionCallback callback) {
        firebaseManager.getSessionsRef().child(sessionId).child("active").setValue(false)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess("Session ended");
                    } else {
                        callback.onFailure("Failed to end session");
                    }
                });
    }

    public void getAllSessions(SessionListCallback callback) {
        firebaseManager.getSessionsRef()
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Session> sessions = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Session session = child.getValue(Session.class);
                            if (session != null) {
                                sessions.add(session);
                            }
                        }
                        callback.onSessionsLoaded(sessions);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }
}