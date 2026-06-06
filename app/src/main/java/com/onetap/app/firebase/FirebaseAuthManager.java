package com.onetap.app.firebase;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.onetap.app.models.User;
import com.onetap.app.utils.Constants;

public class FirebaseAuthManager {
    private FirebaseManager firebaseManager;

    public interface AuthCallback {
        void onSuccess(String message);
        void onFailure(String error);
    }

    public interface UserCallback {
        void onUserLoaded(User user);
        void onError(String error);
    }

    public FirebaseAuthManager() {
        firebaseManager = FirebaseManager.getInstance();
    }

    public void loginUser(String email, String password, AuthCallback callback) {
        firebaseManager.getAuth().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess("Login successful");
                    } else {
                        callback.onFailure(task.getException() != null ?
                                task.getException().getMessage() : "Login failed");
                    }
                });
    }

    public void registerUser(User user, String password, AuthCallback callback) {
        firebaseManager.getAuth().createUserWithEmailAndPassword(user.getEmail(), password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = firebaseManager.getAuth().getCurrentUser();
                        if (firebaseUser != null) {
                            user.setUid(firebaseUser.getUid());
                            saveUserToDatabase(user, callback);
                        }
                    } else {
                        callback.onFailure(task.getException() != null ?
                                task.getException().getMessage() : "Registration failed");
                    }
                });
    }

    private void saveUserToDatabase(User user, AuthCallback callback) {
        firebaseManager.getUsersRef().child(user.getUid()).setValue(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess("Registration successful");
                    } else {
                        callback.onFailure("Failed to save user data");
                    }
                });
    }

    public void getCurrentUser(UserCallback callback) {
        String uid = firebaseManager.getCurrentUserId();
        if (uid == null) {
            callback.onError("User not logged in");
            return;
        }

        firebaseManager.getUsersRef().child(uid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            callback.onUserLoaded(user);
                        } else {
                            callback.onError("User data not found");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    public void checkAdminLogin(String email, String password, AuthCallback callback) {
        if (Constants.ADMIN_EMAIL.equals(email) && Constants.ADMIN_PASSWORD.equals(password)) {
            // Sign in with Firebase Auth for admin
            firebaseManager.getAuth().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            callback.onSuccess("Admin login successful");
                        } else {
                            // Create admin account if doesn't exist
                            createAdminAccount(email, password, callback);
                        }
                    });
        } else {
            callback.onFailure("Invalid admin credentials");
        }
    }

    private void createAdminAccount(String email, String password, AuthCallback callback) {
        firebaseManager.getAuth().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = firebaseManager.getAuth().getCurrentUser();
                        if (firebaseUser != null) {
                            User admin = new User(
                                    firebaseUser.getUid(),
                                    "Admin",
                                    email,
                                    "",
                                    "ADMIN001",
                                    "Administration",
                                    Constants.ROLE_ADMIN,
                                    Constants.STATUS_APPROVED
                            );
                            saveUserToDatabase(admin, callback);
                        }
                    } else {
                        callback.onFailure("Failed to create admin account");
                    }
                });
    }

    public void resetPassword(String email, AuthCallback callback) {
        firebaseManager.getAuth().sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess("Password reset email sent");
                    } else {
                        callback.onFailure(task.getException() != null ?
                                task.getException().getMessage() : "Failed to send reset email");
                    }
                });
    }

    public void signOut() {
        firebaseManager.signOut();
    }
}