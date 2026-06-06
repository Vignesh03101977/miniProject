package com.onetap.app.firebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseManager {
    private static FirebaseManager instance;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference usersRef;
    private DatabaseReference sessionsRef;
    private DatabaseReference attendanceRef;

    private FirebaseManager() {
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("users");
        sessionsRef = database.getReference("sessions");
        attendanceRef = database.getReference("attendance");
    }

    public static synchronized FirebaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseManager();
        }
        return instance;
    }

    public FirebaseAuth getAuth() { return auth; }
    public FirebaseDatabase getDatabase() { return database; }
    public DatabaseReference getUsersRef() { return usersRef; }
    public DatabaseReference getSessionsRef() { return sessionsRef; }
    public DatabaseReference getAttendanceRef() { return attendanceRef; }

    public String getCurrentUserId() {
        if (auth.getCurrentUser() != null) {
            return auth.getCurrentUser().getUid();
        }
        return null;
    }

    public boolean isLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    public void signOut() {
        auth.signOut();
    }
}