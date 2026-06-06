package com.onetap.app.firebase;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.onetap.app.models.Attendance;

import java.util.ArrayList;
import java.util.List;

public class FirebaseAttendanceManager {
    private FirebaseManager firebaseManager;

    public interface AttendanceCallback {
        void onSuccess(String message);
        void onFailure(String error);
    }

    public interface AttendanceListCallback {
        void onAttendanceLoaded(List<Attendance> attendanceList);
        void onError(String error);
    }

    public FirebaseAttendanceManager() {
        firebaseManager = FirebaseManager.getInstance();
    }

    public void markAttendance(Attendance attendance, AttendanceCallback callback) {
        firebaseManager.getAttendanceRef()
                .orderByChild("sessionId")
                .equalTo(attendance.getSessionId())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean alreadyMarked = false;
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Attendance existing = child.getValue(Attendance.class);
                            if (existing != null &&
                                    existing.getStudentUid().equals(
                                            attendance.getStudentUid())) {
                                alreadyMarked = true;
                                break;
                            }
                        }

                        if (alreadyMarked) {
                            callback.onFailure("Attendance already marked for this session");
                            return;
                        }

                        String attendanceId =
                                firebaseManager.getAttendanceRef().push().getKey();
                        if (attendanceId == null) {
                            callback.onFailure("Failed to generate ID");
                            return;
                        }

                        attendance.setAttendanceId(attendanceId);
                        firebaseManager.getAttendanceRef()
                                .child(attendanceId).setValue(attendance)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        updateSessionAttendees(
                                                attendance.getSessionId(),
                                                attendance.getStudentUid());
                                        callback.onSuccess(
                                                "Attendance marked successfully!");
                                    } else {
                                        callback.onFailure("Failed to mark attendance");
                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onFailure(error.getMessage());
                    }
                });
    }

    private void updateSessionAttendees(String sessionId, String studentUid) {
        firebaseManager.getSessionsRef().child(sessionId)
                .child("attendees").child(studentUid).setValue(true);

        firebaseManager.getSessionsRef().child(sessionId).child("attendees")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int count = (int) snapshot.getChildrenCount();
                        firebaseManager.getSessionsRef().child(sessionId)
                                .child("totalStudents").setValue(count);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    public void getAttendanceByStudent(String studentUid,
                                       AttendanceListCallback callback) {
        firebaseManager.getAttendanceRef()
                .orderByChild("studentUid")
                .equalTo(studentUid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Attendance> list = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Attendance attendance = child.getValue(Attendance.class);
                            if (attendance != null) list.add(attendance);
                        }
                        callback.onAttendanceLoaded(list);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    public void getAttendanceBySession(String sessionId,
                                       AttendanceListCallback callback) {
        firebaseManager.getAttendanceRef()
                .orderByChild("sessionId")
                .equalTo(sessionId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Attendance> list = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Attendance attendance = child.getValue(Attendance.class);
                            if (attendance != null) list.add(attendance);
                        }
                        callback.onAttendanceLoaded(list);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    // ✅ Get attendance by session CODE
    public void getAttendanceBySessionCode(String sessionCode,
                                           AttendanceListCallback callback) {
        firebaseManager.getAttendanceRef()
                .orderByChild("sessionCode")
                .equalTo(sessionCode)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Attendance> list = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Attendance attendance = child.getValue(Attendance.class);
                            if (attendance != null) list.add(attendance);
                        }
                        callback.onAttendanceLoaded(list);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    public void getAttendanceByTeacher(String teacherId,
                                       AttendanceListCallback callback) {
        firebaseManager.getAttendanceRef()
                .orderByChild("teacherId")
                .equalTo(teacherId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Attendance> list = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Attendance attendance = child.getValue(Attendance.class);
                            if (attendance != null) list.add(attendance);
                        }
                        callback.onAttendanceLoaded(list);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    // ✅ NEW: Get ALL attendance for a subject by teacher (case-insensitive)
    // Used by SessionAttendanceDetailActivity to get all sessions of a subject
    public void getAttendanceBySubjectAndTeacher(String subjectName,
                                                 String teacherId,
                                                 AttendanceListCallback callback) {
        firebaseManager.getAttendanceRef()
                .orderByChild("teacherId")
                .equalTo(teacherId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Attendance> list = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Attendance att = child.getValue(Attendance.class);
                            if (att != null && att.getSubjectName() != null) {
                                // Case-insensitive subject name match
                                if (att.getSubjectName().trim()
                                        .equalsIgnoreCase(subjectName.trim())) {
                                    list.add(att);
                                }
                            }
                        }
                        callback.onAttendanceLoaded(list);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }
}