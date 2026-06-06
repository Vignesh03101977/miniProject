package com.onetap.app.firebase;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.onetap.app.models.User;
import com.onetap.app.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class FirebaseAdminManager {
    private FirebaseManager firebaseManager;

    public interface UserListCallback {
        void onUsersLoaded(List<User> users);
        void onError(String error);
    }

    public interface ActionCallback {
        void onSuccess(String message);
        void onFailure(String error);
    }

    public FirebaseAdminManager() {
        firebaseManager = FirebaseManager.getInstance();
    }

    public void getPendingTeachers(UserListCallback callback) {
        firebaseManager.getUsersRef()
                .orderByChild("role")
                .equalTo(Constants.ROLE_TEACHER)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<User> pendingTeachers = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            User user = child.getValue(User.class);
                            if (user != null && Constants.STATUS_PENDING.equals(user.getStatus())) {
                                pendingTeachers.add(user);
                            }
                        }
                        callback.onUsersLoaded(pendingTeachers);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    public void getApprovedTeachers(UserListCallback callback) {
        firebaseManager.getUsersRef()
                .orderByChild("role")
                .equalTo(Constants.ROLE_TEACHER)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<User> approvedTeachers = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            User user = child.getValue(User.class);
                            if (user != null && Constants.STATUS_APPROVED.equals(user.getStatus())) {
                                approvedTeachers.add(user);
                            }
                        }
                        callback.onUsersLoaded(approvedTeachers);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    public void getAllStudents(UserListCallback callback) {
        firebaseManager.getUsersRef()
                .orderByChild("role")
                .equalTo(Constants.ROLE_STUDENT)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<User> students = new ArrayList<>();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            User user = child.getValue(User.class);
                            if (user != null) {
                                students.add(user);
                            }
                        }
                        callback.onUsersLoaded(students);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    public void approveTeacher(String uid, ActionCallback callback) {
        firebaseManager.getUsersRef().child(uid).child("status")
                .setValue(Constants.STATUS_APPROVED)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess("Teacher approved successfully");
                    } else {
                        callback.onFailure("Failed to approve teacher");
                    }
                });
    }

    public void rejectTeacher(String uid, ActionCallback callback) {
        firebaseManager.getUsersRef().child(uid).child("status")
                .setValue(Constants.STATUS_REJECTED)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess("Teacher rejected");
                    } else {
                        callback.onFailure("Failed to reject teacher");
                    }
                });
    }

    public void deleteUser(String uid, ActionCallback callback) {
        firebaseManager.getUsersRef().child(uid).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess("User deleted successfully");
                    } else {
                        callback.onFailure("Failed to delete user");
                    }
                });
    }
}