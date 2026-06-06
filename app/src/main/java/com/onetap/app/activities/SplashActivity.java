package com.onetap.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.onetap.app.R;
import com.onetap.app.UpdateManager;
import com.onetap.app.firebase.FirebaseAuthManager;
import com.onetap.app.firebase.FirebaseManager;
import com.onetap.app.models.User;
import com.onetap.app.utils.Constants;

public class SplashActivity extends AppCompatActivity {

    private UpdateManager updateManager;
    private boolean hasNavigated = false;
    private boolean isWaitingForUpdateChoice = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        updateManager = new UpdateManager(this);

        // Animate logo
        ImageView ivLogo = findViewById(R.id.ivLogo);
        TextView tvAppName = findViewById(R.id.tvAppName);
        TextView tvTagline = findViewById(R.id.tvTagline);

        AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(1000);
        fadeIn.setFillAfter(true);
        ivLogo.startAnimation(fadeIn);
        tvAppName.startAnimation(fadeIn);

        AlphaAnimation fadeInDelay = new AlphaAnimation(0f, 1f);
        fadeInDelay.setDuration(1000);
        fadeInDelay.setStartOffset(500);
        fadeInDelay.setFillAfter(true);
        tvTagline.startAnimation(fadeInDelay);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            checkForUpdates();
        }, 2500);
    }

    private void checkForUpdates() {
        updateManager.checkForUpdate(new UpdateManager.UpdateCheckCallback() {
            @Override
            public void onUpdateAvailable(String downloadUrl) {
                isWaitingForUpdateChoice = true;
                updateManager.showUpdateDialog(downloadUrl, new UpdateManager.UpdateCheckCallback() {
                    @Override public void onUpdateAvailable(String url) {}
                    @Override public void onNoUpdate() {}
                    @Override public void onError() {}
                    @Override
                    public void onUserMadeChoice(boolean shouldNavigate) {
                        isWaitingForUpdateChoice = false;
                        if (shouldNavigate) {
                            proceedWithNavigation();
                        } else {
                            // User clicked Update: Wait for installation
                            // Do not navigate to dashboard
                        }
                    }
                });
            }

            @Override
            public void onNoUpdate() {
                proceedWithNavigation();
            }

            @Override
            public void onError() {
                proceedWithNavigation();
            }

            @Override
            public void onUserMadeChoice(boolean shouldNavigate) {
                if (shouldNavigate) proceedWithNavigation();
            }
        });
    }

    private void proceedWithNavigation() {
        if (hasNavigated || isWaitingForUpdateChoice) return;
        hasNavigated = true;

        if (FirebaseManager.getInstance().isLoggedIn()) {
            navigateBasedOnRole();
        } else {
            startActivity(new Intent(SplashActivity.this, MainLandingActivity.class));
            finish();
        }
    }

    private void navigateBasedOnRole() {
        FirebaseAuthManager authManager = new FirebaseAuthManager();
        authManager.getCurrentUser(new FirebaseAuthManager.UserCallback() {
            @Override
            public void onUserLoaded(User user) {
                Intent intent;
                switch (user.getRole()) {
                    case Constants.ROLE_TEACHER:
                        if (Constants.STATUS_APPROVED.equals(user.getStatus())) {
                            intent = new Intent(SplashActivity.this, TeacherDashboardActivity.class);
                        } else {
                            FirebaseManager.getInstance().signOut();
                            intent = new Intent(SplashActivity.this, MainLandingActivity.class);
                        }
                        break;
                    case Constants.ROLE_ADMIN:
                        intent = new Intent(SplashActivity.this, AdminDashboardActivity.class);
                        break;
                    case Constants.ROLE_STUDENT:
                    default:
                        intent = new Intent(SplashActivity.this, StudentDashboardActivity.class);
                        break;
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String error) {
                startActivity(new Intent(SplashActivity.this, MainLandingActivity.class));
                finish();
            }
        });
    }

    // ✅ Clean up receiver when activity is destroyed
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (updateManager != null) {
            updateManager.cleanup();
        }
    }
}