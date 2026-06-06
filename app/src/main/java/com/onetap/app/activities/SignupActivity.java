package com.onetap.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
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

public class SignupActivity extends AppCompatActivity {

    private TextInputEditText etFullName, etEmail, etPhone, etStudentId,
            etDepartment, etPassword, etConfirmPassword;
    private RadioGroup rgRole;
    private MaterialButton btnSignup;
    private ProgressBar progressBar;
    private ImageView ivBack;
    private TextView tvLogin;
    private FirebaseAuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        authManager = new FirebaseAuthManager();
        initViews();
        setupListeners();
    }

    private void initViews() {
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etStudentId = findViewById(R.id.etStudentId);
        etDepartment = findViewById(R.id.etDepartment);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        rgRole = findViewById(R.id.rgRole);
        btnSignup = findViewById(R.id.btnSignup);
        progressBar = findViewById(R.id.progressBar);
        ivBack = findViewById(R.id.ivBack);
        tvLogin = findViewById(R.id.tvLogin);
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> onBackPressed());
        btnSignup.setOnClickListener(v -> attemptSignup());
        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void attemptSignup() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String studentId = etStudentId.getText().toString().trim();
        String department = etDepartment.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validation
        if (!ValidationUtils.isValidName(fullName)) {
            etFullName.setError("Enter your full name");
            etFullName.requestFocus();
            return;
        }
        if (!ValidationUtils.isValidEmail(email)) {
            etEmail.setError("Enter a valid email");
            etEmail.requestFocus();
            return;
        }
        if (!ValidationUtils.isValidPhone(phone)) {
            etPhone.setError("Enter a valid phone number");
            etPhone.requestFocus();
            return;
        }
        if (!ValidationUtils.isValidStudentId(studentId)) {
            etStudentId.setError("Enter your ID");
            etStudentId.requestFocus();
            return;
        }
        if (!ValidationUtils.isNotEmpty(department)) {
            etDepartment.setError("Enter department");
            etDepartment.requestFocus();
            return;
        }
        if (!ValidationUtils.isValidPassword(password)) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }
        if (!ValidationUtils.doPasswordsMatch(password, confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        String role = rgRole.getCheckedRadioButtonId() == R.id.rbTeacher ?
                Constants.ROLE_TEACHER : Constants.ROLE_STUDENT;
        String status = role.equals(Constants.ROLE_TEACHER) ?
                Constants.STATUS_PENDING : Constants.STATUS_APPROVED;

        showLoading(true);

        User user = new User("", fullName, email, phone, studentId, department, role, status);

        authManager.registerUser(user, password, new FirebaseAuthManager.AuthCallback() {
            @Override
            public void onSuccess(String message) {
                showLoading(false);
                if (role.equals(Constants.ROLE_TEACHER)) {
                    Toast.makeText(SignupActivity.this,
                            getString(R.string.teacher_pending_msg),
                            Toast.LENGTH_LONG).show();
                    authManager.signOut();
                    startActivity(new Intent(SignupActivity.this, MainLandingActivity.class));
                    finish();
                } else {
                    Intent intent = new Intent(SignupActivity.this, StudentDashboardActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onFailure(String error) {
                showLoading(false);
                Toast.makeText(SignupActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSignup.setEnabled(!show);
    }
}