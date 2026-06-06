package com.onetap.app.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.onetap.app.R;
import com.onetap.app.database.AppDatabase;
import com.onetap.app.database.OfflineAttendanceDao;
import com.onetap.app.firebase.FirebaseAuthManager;
import com.onetap.app.firebase.FirebaseManager;
import com.onetap.app.firebase.FirebaseSessionManager;
import com.onetap.app.models.OfflineAttendance;
import com.onetap.app.models.Session;
import com.onetap.app.models.User;
import com.onetap.app.sync.SyncManager;
import com.onetap.app.utils.LocationUtils;
import com.onetap.app.utils.NetworkUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MarkAttendanceActivity extends AppCompatActivity {

    private static final String TAG = "MarkAttendance";
    private static final int LOCATION_PERMISSION_CODE = 400;

    private TextInputEditText etSessionCode;
    private MaterialButton btnSubmit, btnOpenSettings, btnSyncNow;
    private ProgressBar progressBar, pbSync;
    private CardView cardSuccess, cardAirplaneStatus, cardSyncStatus, cardWarning;
    private ImageView ivBack, ivAirplaneIcon, ivWifiStatus, ivMobileDataStatus, ivAirplaneModeIndicator;
    private ImageView ivSyncIcon;
    private TextView tvAirplaneStatus, tvAirplaneSubtitle;
    private TextView tvWifiStatus, tvMobileDataStatus, tvAirplaneModeIndicator;
    private TextView tvSyncStatus, tvSyncDetail, tvSyncBadge, tvSuccessDetail;
    private TextView tvWarningTitle, tvWarningMessage, tvTimeRemaining;
    private LinearLayout layoutAirplaneStatus;

    private FirebaseSessionManager sessionManager;
    private FirebaseAuthManager authManager;
    private OfflineAttendanceDao attendanceDao;
    private SyncManager syncManager;

    private User currentUser;
    private Session currentSession = null;
    private long currentLocalId = -1;
    private long sessionEndTime = 0;

    private Handler networkCheckHandler;
    private Handler timerHandler;
    private Runnable networkCheckRunnable;
    private Runnable timerRunnable;

    private Animation pulseAnimation, shakeAnimation;

    private boolean hasMarkedAttendance = false;
    private boolean wentOnlineBeforeTimeout = false;
    private boolean sessionFetched = false;

    // ✅ NEW: Location fields
    private double studentLatitude = 0;
    private double studentLongitude = 0;
    private boolean studentLocationCaptured = false;
    private boolean studentInsideBoundary = true;

    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss a", Locale.getDefault());

    private BroadcastReceiver networkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isOnline = NetworkUtils.isNetworkConnected(context);

            if (hasMarkedAttendance && isOnline && currentLocalId > 0 && !wentOnlineBeforeTimeout) {
                long now = System.currentTimeMillis();
                String networkType = NetworkUtils.getNetworkType(context);

                if (now < sessionEndTime) {
                    wentOnlineBeforeTimeout = true;
                    markAsAbsent(now, networkType);
                } else {
                    markAsPresent(now, networkType);
                }
            }

            checkNetworkStatus();
            updateSyncStatusUI();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark_attendance);

        sessionManager = new FirebaseSessionManager();
        authManager = new FirebaseAuthManager();
        AppDatabase db = AppDatabase.getInstance(this);
        attendanceDao = db.offlineAttendanceDao();
        syncManager = SyncManager.getInstance(this);

        currentSession = (Session) getIntent().getSerializableExtra("session");

        if (currentSession != null && currentSession.getEndTime() > 0) {
            sessionFetched = true;
            sessionEndTime = currentSession.getEndTime();
        }

        initViews();
        loadAnimations();
        loadCurrentUser();
        setupListeners();
        registerReceivers();
        startNetworkMonitoring();
        updateSyncStatusUI();
    }

    private void initViews() {
        etSessionCode = findViewById(R.id.etSessionCode);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnOpenSettings = findViewById(R.id.btnOpenSettings);
        btnSyncNow = findViewById(R.id.btnSyncNow);
        progressBar = findViewById(R.id.progressBar);
        pbSync = findViewById(R.id.pbSync);
        cardSuccess = findViewById(R.id.cardSuccess);
        cardAirplaneStatus = findViewById(R.id.cardAirplaneStatus);
        cardSyncStatus = findViewById(R.id.cardSyncStatus);
        cardWarning = findViewById(R.id.cardWarning);
        ivBack = findViewById(R.id.ivBack);
        ivAirplaneIcon = findViewById(R.id.ivAirplaneIcon);
        ivWifiStatus = findViewById(R.id.ivWifiStatus);
        ivMobileDataStatus = findViewById(R.id.ivMobileDataStatus);
        ivAirplaneModeIndicator = findViewById(R.id.ivAirplaneModeIndicator);
        ivSyncIcon = findViewById(R.id.ivSyncIcon);
        tvAirplaneStatus = findViewById(R.id.tvAirplaneStatus);
        tvAirplaneSubtitle = findViewById(R.id.tvAirplaneSubtitle);
        tvWifiStatus = findViewById(R.id.tvWifiStatus);
        tvMobileDataStatus = findViewById(R.id.tvMobileDataStatus);
        tvAirplaneModeIndicator = findViewById(R.id.tvAirplaneModeIndicator);
        tvSyncStatus = findViewById(R.id.tvSyncStatus);
        tvSyncDetail = findViewById(R.id.tvSyncDetail);
        tvSyncBadge = findViewById(R.id.tvSyncBadge);
        tvSuccessDetail = findViewById(R.id.tvSuccessDetail);
        tvWarningTitle = findViewById(R.id.tvWarningTitle);
        tvWarningMessage = findViewById(R.id.tvWarningMessage);
        tvTimeRemaining = findViewById(R.id.tvTimeRemaining);
        layoutAirplaneStatus = findViewById(R.id.layoutAirplaneStatus);
    }

    private void loadAnimations() {
        pulseAnimation = AnimationUtils.loadAnimation(this, R.anim.pulse_animation);
        shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake_animation);
    }

    private void loadCurrentUser() {
        authManager.getCurrentUser(new FirebaseAuthManager.UserCallback() {
            @Override
            public void onUserLoaded(User user) { currentUser = user; }
            @Override
            public void onError(String error) {
                Toast.makeText(MarkAttendanceActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());

        btnSubmit.setOnClickListener(v -> {
            String code = etSessionCode.getText().toString().trim().toUpperCase();
            if (code.isEmpty() || code.length() != 6) {
                etSessionCode.setError("Enter valid 6-character code");
                etSessionCode.startAnimation(shakeAnimation);
                return;
            }

            if (!sessionFetched) {
                if (!NetworkUtils.isNetworkConnected(this)) {
                    Toast.makeText(this,
                            "Connect to internet first to join session.\nThen turn on Airplane Mode to mark attendance.",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                fetchSessionFromFirebase(code);
            } else {
                if (!NetworkUtils.isCompletelyOffline(this)) {
                    cardAirplaneStatus.startAnimation(shakeAnimation);
                    Toast.makeText(this, "Turn ON Airplane Mode to mark attendance!", Toast.LENGTH_LONG).show();
                    return;
                }
                validateAndSaveAttendance(code);
            }
        });

        btnOpenSettings.setOnClickListener(v ->
                startActivity(new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS)));
        btnSyncNow.setOnClickListener(v -> attemptAutoSync());
    }

    // ✅ NEW: Capture student location
    private void captureStudentLocation() {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_CODE);
                return;
            }

            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (loc == null) {
                loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            if (loc != null) {
                studentLatitude = loc.getLatitude();
                studentLongitude = loc.getLongitude();
                studentLocationCaptured = true;

                if (currentSession != null && currentSession.isLocationValidationEnabled()) {
                    studentInsideBoundary = LocationUtils.isInsideRectangle(
                            studentLatitude, studentLongitude,
                            currentSession.getMinLatitude(), currentSession.getMaxLatitude(),
                            currentSession.getMinLongitude(), currentSession.getMaxLongitude());

                    double distance = LocationUtils.distanceBetween(
                            studentLatitude, studentLongitude,
                            currentSession.getTeacherLatitude(), currentSession.getTeacherLongitude());

                    Log.d(TAG, "📍 Student: " + studentLatitude + ", " + studentLongitude);
                    Log.d(TAG, "📍 Distance from teacher: " + String.format("%.1f", distance) + "m");
                    Log.d(TAG, "📍 Inside boundary: " + studentInsideBoundary);
                } else {
                    studentInsideBoundary = true;
                }
            } else {
                Log.d(TAG, "⚠ Could not capture student location");
                studentInsideBoundary = true;
            }
        } catch (Exception e) {
            Log.e(TAG, "Location error: " + e.getMessage());
            studentInsideBoundary = true;
        }
    }

    private void fetchSessionFromFirebase(String code) {
        progressBar.setVisibility(View.VISIBLE);
        btnSubmit.setEnabled(false);

        sessionManager.getSessionByCode(code, new FirebaseSessionManager.SessionDataCallback() {
            @Override
            public void onSessionLoaded(Session session) {
                mainHandler.post(() -> {
                    progressBar.setVisibility(View.GONE);

                    if (!session.isActive()) {
                        btnSubmit.setEnabled(true);
                        Toast.makeText(MarkAttendanceActivity.this, "This session has already ended.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (session.getEndTime() > 0 && System.currentTimeMillis() > session.getEndTime()) {
                        btnSubmit.setEnabled(true);
                        Toast.makeText(MarkAttendanceActivity.this, "This session has expired.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    currentSession = session;
                    sessionFetched = true;

                    if (session.getEndTime() > 0) {
                        sessionEndTime = session.getEndTime();
                    } else if (session.getStartTime() > 0 && session.getDuration() > 0) {
                        sessionEndTime = session.getStartTime() + (session.getDuration() * 60L * 1000L);
                    } else {
                        sessionEndTime = System.currentTimeMillis() + (session.getDuration() * 60L * 1000L);
                    }

                    // ✅ NEW: Capture student location after session fetched
                    captureStudentLocation();

                    long remainingMs = sessionEndTime - System.currentTimeMillis();
                    long remainingMin = (remainingMs / 1000) / 60;
                    long remainingSec = (remainingMs / 1000) % 60;

                    etSessionCode.setEnabled(false);
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("✓ Mark Attendance (Go Offline First)");

                    // ✅ Show location status in session info
                    String locationInfo = "";
                    if (currentSession.isLocationValidationEnabled()) {
                        locationInfo = studentLocationCaptured
                                ? (studentInsideBoundary ? "\n📍 Location: Inside classroom ✓" : "\n📍 Location: Outside classroom ❌")
                                : "\n📍 Location: Not captured";
                    }

                    tvTimeRemaining.setVisibility(View.VISIBLE);
                    tvTimeRemaining.setText(String.format(Locale.getDefault(),
                            "Session: %s | %s\nTime remaining: %02d:%02d%s\n\n⚠️ Now turn ON Airplane Mode, then tap the button again.",
                            session.getSessionTitle(), session.getSubjectName(),
                            remainingMin, remainingSec, locationInfo));
                    tvTimeRemaining.setTextColor(getColor(R.color.accent_green));

                    Toast.makeText(MarkAttendanceActivity.this,
                            "✓ Session found! Now turn ON Airplane Mode and tap Mark Attendance again.",
                            Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onError(String error) {
                mainHandler.post(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnSubmit.setEnabled(true);
                    etSessionCode.setError("Session not found");
                    Toast.makeText(MarkAttendanceActivity.this, "Session not found: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void registerReceivers() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(networkReceiver, filter);
    }

    private void startNetworkMonitoring() {
        networkCheckHandler = new Handler(Looper.getMainLooper());
        networkCheckRunnable = () -> {
            checkNetworkStatus();
            updateSyncStatusUI();
            networkCheckHandler.postDelayed(networkCheckRunnable, 1000);
        };
        networkCheckHandler.post(networkCheckRunnable);
    }

    private void startSessionTimer() {
        if (sessionEndTime <= 0) return;
        timerHandler = new Handler(Looper.getMainLooper());
        timerRunnable = () -> {
            long remaining = sessionEndTime - System.currentTimeMillis();
            if (remaining <= 0) {
                if (hasMarkedAttendance && !wentOnlineBeforeTimeout) {
                    tvTimeRemaining.setText("Session ended! ✓ Your attendance is VALID");
                    tvTimeRemaining.setTextColor(getColor(R.color.accent_green));
                    tvSuccessDetail.setText("✓ Session ended. You can now go online safely.\nYour attendance will be marked as PRESENT.");
                    Toast.makeText(MarkAttendanceActivity.this, "Session ended! You can now turn off Airplane Mode.", Toast.LENGTH_LONG).show();
                }
            } else {
                long minutes = (remaining / 1000) / 60;
                long seconds = (remaining / 1000) % 60;
                tvTimeRemaining.setText(String.format(Locale.getDefault(), "Session ends in: %02d:%02d", minutes, seconds));
                timerHandler.postDelayed(timerRunnable, 1000);
            }
        };
        timerHandler.post(timerRunnable);
    }

    private void checkNetworkStatus() {
        boolean airplaneOn = NetworkUtils.isAirplaneModeOn(this);
        boolean wifiOn = NetworkUtils.isWifiEnabled(this);
        boolean mobileOn = NetworkUtils.isMobileDataEnabled(this);
        boolean connected = NetworkUtils.isNetworkConnected(this);
        boolean completelyOffline = airplaneOn && !connected;
        runOnUiThread(() -> updateNetworkUI(airplaneOn, wifiOn, mobileOn, connected, completelyOffline));
    }

    private void updateNetworkUI(boolean airplaneOn, boolean wifiOn, boolean mobileOn,
                                 boolean connected, boolean completelyOffline) {
        tvAirplaneModeIndicator.setText("Airplane: " + (airplaneOn ? "ON" : "OFF"));
        tvAirplaneModeIndicator.setTextColor(airplaneOn ? getColor(R.color.accent_green) : 0xFFFF5252);
        ivAirplaneModeIndicator.setImageResource(airplaneOn ? android.R.drawable.ic_menu_save : android.R.drawable.ic_menu_close_clear_cancel);

        tvWifiStatus.setText("WiFi: " + (wifiOn ? "ON" : "OFF"));
        tvWifiStatus.setTextColor(wifiOn ? 0xFFFF5252 : getColor(R.color.accent_green));
        ivWifiStatus.setImageResource(wifiOn ? android.R.drawable.ic_menu_close_clear_cancel : android.R.drawable.ic_menu_save);

        tvMobileDataStatus.setText("Data: " + (mobileOn ? "ON" : "OFF"));
        tvMobileDataStatus.setTextColor(mobileOn ? 0xFFFF5252 : getColor(R.color.accent_green));
        ivMobileDataStatus.setImageResource(mobileOn ? android.R.drawable.ic_menu_close_clear_cancel : android.R.drawable.ic_menu_save);

        if (completelyOffline) {
            layoutAirplaneStatus.setBackgroundResource(R.drawable.bg_airplane_mode_on);
            tvAirplaneStatus.setText("✓ Ready to Mark Attendance");
            tvAirplaneSubtitle.setText("Device is completely offline");
            ivAirplaneIcon.clearAnimation();
            btnSubmit.setEnabled(sessionFetched && !hasMarkedAttendance);
            btnOpenSettings.setVisibility(View.GONE);
        } else {
            layoutAirplaneStatus.setBackgroundResource(R.drawable.bg_airplane_mode_off);
            if (!sessionFetched) {
                tvAirplaneStatus.setText("📡 Enter Session Code");
                tvAirplaneSubtitle.setText("Enter code while online, then go offline to mark");
            } else if (!airplaneOn) {
                tvAirplaneStatus.setText("⚠ Turn ON Airplane Mode");
                tvAirplaneSubtitle.setText("Settings → Airplane Mode → ON");
            } else if (connected) {
                tvAirplaneStatus.setText("⚠ Disconnect All Networks");
                tvAirplaneSubtitle.setText("Turn off WiFi and Mobile Data");
            }
            if (ivAirplaneIcon.getAnimation() == null && !hasMarkedAttendance && sessionFetched) {
                ivAirplaneIcon.startAnimation(pulseAnimation);
            }
            btnSubmit.setEnabled(!sessionFetched);
            btnOpenSettings.setVisibility(sessionFetched ? View.VISIBLE : View.GONE);
        }
        cardAirplaneStatus.setVisibility(View.VISIBLE);
    }

    private void updateSyncStatusUI() {
        int pendingCount = syncManager.getPendingSyncCount();
        boolean isOnline = NetworkUtils.isNetworkConnected(this);

        if (wentOnlineBeforeTimeout) {
            cardSyncStatus.setVisibility(View.GONE);
        } else if (pendingCount > 0 || hasMarkedAttendance) {
            cardSyncStatus.setVisibility(View.VISIBLE);
            tvSyncBadge.setVisibility(View.VISIBLE);
            tvSyncBadge.setText("1 pending");
            if (isOnline && !hasMarkedAttendance) {
                tvSyncStatus.setText("⚡ Ready to Sync");
                tvSyncDetail.setText("Tap to sync now");
                btnSyncNow.setEnabled(true);
            } else if (hasMarkedAttendance && !isOnline) {
                long remaining = sessionEndTime - System.currentTimeMillis();
                tvSyncStatus.setText(remaining > 0 ? "⏳ Stay Offline!" : "✓ Safe to go online");
                tvSyncDetail.setText(remaining > 0 ? "Wait until session ends" : "Session ended - you can sync now");
                btnSyncNow.setEnabled(false);
            }
        } else {
            cardSyncStatus.setVisibility(View.GONE);
            tvSyncBadge.setVisibility(View.GONE);
        }
    }

    private void validateAndSaveAttendance(String code) {
        if (currentUser == null) { Toast.makeText(this, "User data not loaded", Toast.LENGTH_SHORT).show(); return; }
        if (!sessionFetched || currentSession == null) { Toast.makeText(this, "Session not loaded.", Toast.LENGTH_LONG).show(); return; }
        if (!NetworkUtils.isCompletelyOffline(this)) { Toast.makeText(this, "Must be completely offline!", Toast.LENGTH_LONG).show(); return; }
        if (System.currentTimeMillis() > sessionEndTime) { Toast.makeText(this, "This session has already ended!", Toast.LENGTH_LONG).show(); return; }

        progressBar.setVisibility(View.VISIBLE);
        btnSubmit.setEnabled(false);
        saveAttendanceOffline(code);
    }

    private void saveAttendanceOffline(String code) {
        executor.execute(() -> {
            try {
                String uid = FirebaseManager.getInstance().getCurrentUserId();

                OfflineAttendance existing = attendanceDao.checkExistingAttendance(code, uid);
                if (existing != null) {
                    mainHandler.post(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Already marked for this session!", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                boolean airplaneOn = NetworkUtils.isAirplaneModeOn(this);
                boolean wifiOff = !NetworkUtils.isWifiEnabled(this);
                boolean mobileOff = !NetworkUtils.isMobileDataEnabled(this);

                long finalEndTime = sessionEndTime;
                if (finalEndTime <= 0) {
                    if (currentSession.getEndTime() > 0) finalEndTime = currentSession.getEndTime();
                    else if (currentSession.getStartTime() > 0 && currentSession.getDuration() > 0)
                        finalEndTime = currentSession.getStartTime() + (currentSession.getDuration() * 60L * 1000L);
                }
                if (finalEndTime <= 0) finalEndTime = System.currentTimeMillis() + (2 * 60 * 1000);
                sessionEndTime = finalEndTime;

                OfflineAttendance attendance = new OfflineAttendance(
                        currentSession.getSessionId(), currentSession.getSessionTitle(),
                        currentSession.getSubjectName(), code, currentUser.getStudentId(),
                        currentUser.getFullName(), uid, currentSession.getTeacherId(),
                        currentUser.getDepartment(), sessionEndTime, airplaneOn, wifiOff, mobileOff
                );

                attendance.setMarkedAt(System.currentTimeMillis());
                attendance.setSynced(false);
                attendance.setStatus("pending");
                attendance.setWentOnlineAt(0);
                attendance.setWentOnlineBeforeTimeout(false);
                attendance.setValidAttendance(true);

                // ✅ NEW: Set location data
                attendance.setStudentLatitude(studentLatitude);
                attendance.setStudentLongitude(studentLongitude);
                attendance.setLocationCaptured(studentLocationCaptured);
                attendance.setInsideLocationBoundary(studentInsideBoundary);
                if (studentLocationCaptured && !studentInsideBoundary) {
                    attendance.setLocationAbsentReason("Outside classroom boundary");
                }

                currentLocalId = attendanceDao.insert(attendance);
                hasMarkedAttendance = true;

                Log.d(TAG, "✅ Attendance saved locally with ID: " + currentLocalId);

                mainHandler.post(() -> {
                    progressBar.setVisibility(View.GONE);
                    showSuccessUI();
                    startSessionTimer();
                });

            } catch (Exception e) {
                Log.e(TAG, "❌ Error: " + e.getMessage(), e);
                mainHandler.post(() -> {
                    progressBar.setVisibility(View.GONE);
                    btnSubmit.setEnabled(true);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showSuccessUI() {
        cardAirplaneStatus.setVisibility(View.GONE);
        cardSuccess.setVisibility(View.VISIBLE);
        Animation slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);
        cardSuccess.startAnimation(slideDown);

        String sessionInfo = "";
        if (currentSession != null) {
            sessionInfo = "\nSession: " + currentSession.getSessionTitle() +
                    "\nSubject: " + currentSession.getSubjectName() +
                    "\nSession ends at: " + timeFormat.format(new Date(sessionEndTime));
        }

        // ✅ NEW: Show location status
        String locationInfo = "";
        if (currentSession != null && currentSession.isLocationValidationEnabled()) {
            if (studentLocationCaptured) {
                locationInfo = studentInsideBoundary
                        ? "\n📍 Location: Inside classroom ✓"
                        : "\n📍 Location: Outside classroom ❌ (will be marked ABSENT)";
            } else {
                locationInfo = "\n📍 Location: Not captured";
            }
        }

        tvSuccessDetail.setText(
                "✓ Saved offline at " + timeFormat.format(new Date()) +
                        sessionInfo + locationInfo +
                        "\n\n⚠️ IMPORTANT: Stay offline until session ends!\n" +
                        "If you go online before timeout, you will be marked ABSENT."
        );

        Toast.makeText(this, "✓ Attendance saved! Stay offline until session ends.", Toast.LENGTH_LONG).show();
        updateSyncStatusUI();
    }

    private void markAsAbsent(long wentOnlineAt, String networkType) {
        executor.execute(() -> {
            attendanceDao.updateNetworkChangeInfo(currentLocalId, wentOnlineAt, networkType, true, "absent", false);
            mainHandler.post(() -> {
                cardSuccess.setVisibility(View.GONE);
                cardWarning.setVisibility(View.VISIBLE);
                Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake_animation);
                cardWarning.startAnimation(shake);
                tvWarningTitle.setText("❌ ATTENDANCE INVALID");
                tvWarningMessage.setText("You went online BEFORE the session ended!\n\n" +
                        "Session ends at: " + timeFormat.format(new Date(sessionEndTime)) + "\n" +
                        "You went online at: " + timeFormat.format(new Date(wentOnlineAt)) + "\n" +
                        "Network: " + networkType + "\n\nYour attendance will be marked as ABSENT.");
                if (timerHandler != null && timerRunnable != null) timerHandler.removeCallbacks(timerRunnable);
                tvTimeRemaining.setText("❌ Marked as ABSENT");
                tvTimeRemaining.setTextColor(getColor(R.color.accent_red));
                Toast.makeText(this, "❌ You went online before timeout! Marked as ABSENT.", Toast.LENGTH_LONG).show();
                mainHandler.postDelayed(() -> attemptAutoSync(), 2000);
            });
        });
    }

    private void markAsPresent(long wentOnlineAt, String networkType) {
        executor.execute(() -> {
            attendanceDao.updateNetworkChangeInfo(currentLocalId, wentOnlineAt, networkType, false, "present", true);
            mainHandler.post(() -> {
                tvSuccessDetail.setText("✓ Attendance VALID!\nYou stayed offline until session ended.\n\nWent online at: " +
                        timeFormat.format(new Date(wentOnlineAt)) + "\nSyncing now...");
                Toast.makeText(this, "✓ Attendance is VALID! Syncing...", Toast.LENGTH_LONG).show();
                attemptAutoSync();
            });
        });
    }

    private void attemptAutoSync() {
        if (!NetworkUtils.isNetworkConnected(this)) { Toast.makeText(this, "No network connection", Toast.LENGTH_SHORT).show(); return; }
        pbSync.setVisibility(View.VISIBLE);
        ivSyncIcon.setVisibility(View.GONE);
        btnSyncNow.setEnabled(false);
        tvSyncStatus.setText("⏳ Syncing...");

        syncManager.syncAll(new SyncManager.SyncCallback() {
            @Override public void onSyncStarted(int t) { mainHandler.post(() -> tvSyncDetail.setText("Uploading " + t + " record(s)...")); }
            @Override public void onSyncProgress(int s, int t) { mainHandler.post(() -> tvSyncDetail.setText("Syncing " + s + "/" + t + "...")); }
            @Override
            public void onSyncCompleted(int presentCount, int absentCount) {
                mainHandler.post(() -> {
                    pbSync.setVisibility(View.GONE);
                    ivSyncIcon.setVisibility(View.VISIBLE);
                    if (presentCount > 0 || absentCount > 0) {
                        String status = wentOnlineBeforeTimeout ? "ABSENT" : "PRESENT";
                        tvSyncStatus.setText("✓ Synced as " + status);
                        tvSyncDetail.setText("Uploaded to server");
                        cardSyncStatus.setCardBackgroundColor(getColor(wentOnlineBeforeTimeout ? R.color.accent_red_light : R.color.accent_green_light));
                        Toast.makeText(MarkAttendanceActivity.this, "Synced as " + status, Toast.LENGTH_LONG).show();
                        mainHandler.postDelayed(() -> finish(), 3000);
                    }
                });
            }
            @Override
            public void onSyncFailed(String error) {
                mainHandler.post(() -> {
                    pbSync.setVisibility(View.GONE);
                    ivSyncIcon.setVisibility(View.VISIBLE);
                    btnSyncNow.setEnabled(true);
                    tvSyncStatus.setText("⚠ Sync Failed");
                    tvSyncDetail.setText(error + " - Tap to retry");
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkNetworkStatus();
        updateSyncStatusUI();
        if (hasMarkedAttendance && !wentOnlineBeforeTimeout && currentLocalId > 0 &&
                NetworkUtils.isNetworkConnected(this) && System.currentTimeMillis() >= sessionEndTime) {
            if (syncManager.getPendingSyncCount() > 0) attemptAutoSync();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (networkCheckHandler != null) networkCheckHandler.removeCallbacks(networkCheckRunnable);
        if (timerHandler != null && timerRunnable != null) timerHandler.removeCallbacks(timerRunnable);
        try { unregisterReceiver(networkReceiver); } catch (Exception ignored) {}
        executor.shutdown();
    }
}