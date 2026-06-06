package com.onetap.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.onetap.app.R;
import com.onetap.app.firebase.FirebaseAuthManager;
import com.onetap.app.models.User;
import com.onetap.app.utils.Constants;
import com.onetap.app.utils.ValidationUtils;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private ProgressBar progressBar;
    private TextView tvForgotPassword, tvSignUp;
    private ImageView ivBack;
    private FirebaseAuthManager authManager;
    private boolean isAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authManager = new FirebaseAuthManager();
        isAdmin = getIntent().getBooleanExtra("isAdmin", false);

        initViews();
        setupListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvSignUp = findViewById(R.id.tvSignUp);
        ivBack = findViewById(R.id.ivBack);

        if (isAdmin) {
            etEmail.setText(Constants.ADMIN_EMAIL);
        }
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> onBackPressed());

        btnLogin.setOnClickListener(v -> attemptLogin());

        tvForgotPassword.setOnClickListener(v -> handleForgotPassword());

        tvSignUp.setOnClickListener(v -> {
            startActivity(new Intent(this, SignupActivity.class));
        });
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!ValidationUtils.isValidEmail(email)) {
            etEmail.setError("Enter a valid email");
            etEmail.requestFocus();
            return;
        }

        if (!ValidationUtils.isValidPassword(password)) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }

        showLoading(true);

        if (isAdmin) {
            authManager.checkAdminLogin(email, password, new FirebaseAuthManager.AuthCallback() {
                @Override
                public void onSuccess(String message) {
                    showLoading(false);
                    navigateToAdmin();
                }

                @Override
                public void onFailure(String error) {
                    showLoading(false);
                    Toast.makeText(LoginActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            authManager.loginUser(email, password, new FirebaseAuthManager.AuthCallback() {
                @Override
                public void onSuccess(String message) {
                    checkUserRoleAndNavigate();
                }

                @Override
                public void onFailure(String error) {
                    showLoading(false);
                    Toast.makeText(LoginActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void checkUserRoleAndNavigate() {
        authManager.getCurrentUser(new FirebaseAuthManager.UserCallback() {
            @Override
            public void onUserLoaded(User user) {
                showLoading(false);
                Intent intent;
                switch (user.getRole()) {
                    case Constants.ROLE_TEACHER:
                        if (Constants.STATUS_APPROVED.equals(user.getStatus())) {
                            intent = new Intent(LoginActivity.this, TeacherDashboardActivity.class);
                        } else {
                            Toast.makeText(LoginActivity.this,
                                    getString(R.string.teacher_pending_msg),
                                    Toast.LENGTH_LONG).show();
                            authManager.signOut();
                            return;
                        }
                        break;
                    case Constants.ROLE_ADMIN:
                        intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
                        break;
                    case Constants.ROLE_STUDENT:
                    default:
                        intent = new Intent(LoginActivity.this, StudentDashboardActivity.class);
                        break;
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                Toast.makeText(LoginActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToAdmin() {
        Intent intent = new Intent(this, AdminDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void handleForgotPassword() {
        String email = etEmail.getText().toString().trim();
        if (!ValidationUtils.isValidEmail(email)) {
            etEmail.setError("Enter your email first");
            etEmail.requestFocus();
            return;
        }

        authManager.resetPassword(email, new FirebaseAuthManager.AuthCallback() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(LoginActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!show);
    }
}