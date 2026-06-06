package com.onetap.app.activities;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.onetap.app.R;
import com.onetap.app.firebase.FirebaseAuthManager;
import com.onetap.app.firebase.FirebaseManager;
import com.onetap.app.firebase.FirebaseSessionManager;
import com.onetap.app.models.Session;
import com.onetap.app.models.User;
import com.onetap.app.utils.LocationUtils;
import com.onetap.app.utils.ValidationUtils;

public class CreateSessionActivity extends AppCompatActivity {

    private static final String TAG = "CreateSession";
    private static final int LOCATION_PERMISSION_CODE = 300;
    private static final double DEFAULT_BOUNDARY_RANGE_METERS = 100.0;

    private TextInputEditText etSessionTitle, etSubjectName, etDuration;
    private MaterialButton btnGenerateCode, btnStartSession;
    private TextView tvSessionCode;
    private LinearLayout layoutSessionCode;
    private ProgressBar progressBar;
    private ImageView ivBack;
    private FirebaseSessionManager sessionManager;
    private FirebaseAuthManager authManager;
    private String generatedCode = "";
    private User currentUser;

    // ✅ NEW: Location
    private double teacherLatitude = 0;
    private double teacherLongitude = 0;
    private boolean locationCaptured = false;
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_session);

        sessionManager = new FirebaseSessionManager();
        authManager = new FirebaseAuthManager();

        initViews();
        loadCurrentUser();
        setupListeners();
        captureTeacherLocation();
    }

    private void initViews() {
        etSessionTitle = findViewById(R.id.etSessionTitle);
        etSubjectName = findViewById(R.id.etSubjectName);
        etDuration = findViewById(R.id.etDuration);
        btnGenerateCode = findViewById(R.id.btnGenerateCode);
        btnStartSession = findViewById(R.id.btnStartSession);
        tvSessionCode = findViewById(R.id.tvSessionCode);
        layoutSessionCode = findViewById(R.id.layoutSessionCode);
        progressBar = findViewById(R.id.progressBar);
        ivBack = findViewById(R.id.ivBack);
    }

    private void loadCurrentUser() {
        authManager.getCurrentUser(new FirebaseAuthManager.UserCallback() {
            @Override
            public void onUserLoaded(User user) {
                currentUser = user;
            }

            @Override
            public void onError(String error) {
                Toast.makeText(CreateSessionActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ✅ NEW: Capture teacher GPS location
    private void captureTeacherLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    }, LOCATION_PERMISSION_CODE);
            return;
        }
        getLocation();
    }

    private void getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            Location lastKnown = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnown == null) {
                lastKnown = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            if (lastKnown != null) {
                teacherLatitude = lastKnown.getLatitude();
                teacherLongitude = lastKnown.getLongitude();
                locationCaptured = true;
                Log.d(TAG, "✅ Teacher location: " + teacherLatitude + ", " + teacherLongitude);
            }

            LocationListener listener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    teacherLatitude = location.getLatitude();
                    teacherLongitude = location.getLongitude();
                    locationCaptured = true;
                    Log.d(TAG, "✅ Fresh teacher location: " + teacherLatitude + ", " + teacherLongitude);
                    locationManager.removeUpdates(this);
                }
            };

            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 0, 0, listener);
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 0, 0, listener);

        } catch (Exception e) {
            Log.e(TAG, "Location error: " + e.getMessage());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                Toast.makeText(this, "Location permission denied. Location validation disabled.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> onBackPressed());

        btnGenerateCode.setOnClickListener(v -> {
            if (!validateInputs()) return;
            generatedCode = sessionManager.generateSessionCode();
            tvSessionCode.setText(generatedCode);
            layoutSessionCode.setVisibility(View.VISIBLE);
            btnStartSession.setVisibility(View.VISIBLE);
        });

        btnStartSession.setOnClickListener(v -> createSession());
    }

    private boolean validateInputs() {
        String title = etSessionTitle.getText().toString().trim();
        String subject = etSubjectName.getText().toString().trim();
        String duration = etDuration.getText().toString().trim();

        if (!ValidationUtils.isNotEmpty(title)) { etSessionTitle.setError("Enter session title"); return false; }
        if (!ValidationUtils.isNotEmpty(subject)) { etSubjectName.setError("Enter subject name"); return false; }
        if (!ValidationUtils.isNotEmpty(duration)) { etDuration.setError("Enter duration"); return false; }
        try {
            int dur = Integer.parseInt(duration);
            if (dur < 1 || dur > 480) { etDuration.setError("Duration: 1-480 minutes"); return false; }
        } catch (NumberFormatException e) { etDuration.setError("Invalid number"); return false; }
        return true;
    }

    private void createSession() {
        if (currentUser == null) {
            Toast.makeText(this, "User data not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        String title = etSessionTitle.getText().toString().trim();
        String subject = etSubjectName.getText().toString().trim();
        int duration = Integer.parseInt(etDuration.getText().toString().trim());

        long startTime = System.currentTimeMillis();
        long endTime = startTime + (duration * 60L * 1000L);

        Session session = new Session(
                "", title, subject, generatedCode,
                FirebaseManager.getInstance().getCurrentUserId(),
                currentUser.getFullName(), currentUser.getDepartment(), duration
        );

        session.setStartTime(startTime);
        session.setEndTime(endTime);

        // ✅ NEW: Set location data
        if (locationCaptured) {
            session.setTeacherLatitude(teacherLatitude);
            session.setTeacherLongitude(teacherLongitude);
            session.setLocationValidationEnabled(true);
            session.setBoundaryRangeMeters(DEFAULT_BOUNDARY_RANGE_METERS);

            double[] bounds = LocationUtils.calculateRectangleBoundary(
                    teacherLatitude, teacherLongitude, DEFAULT_BOUNDARY_RANGE_METERS);
            session.setMinLatitude(bounds[0]);
            session.setMaxLatitude(bounds[1]);
            session.setMinLongitude(bounds[2]);
            session.setMaxLongitude(bounds[3]);

            Log.d(TAG, "✅ Location boundary set: min(" +
                    bounds[0] + "," + bounds[2] + ") max(" + bounds[1] + "," + bounds[3] + ")");
        } else {
            session.setLocationValidationEnabled(false);
            Log.d(TAG, "⚠ Location not captured, validation disabled");
        }

        sessionManager.createSession(session, new FirebaseSessionManager.SessionCallback() {
            @Override
            public void onSuccess(String message) {
                showLoading(false);
                Toast.makeText(CreateSessionActivity.this, message, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(String error) {
                showLoading(false);
                Toast.makeText(CreateSessionActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnStartSession.setEnabled(!show);
        btnGenerateCode.setEnabled(!show);
    }
}