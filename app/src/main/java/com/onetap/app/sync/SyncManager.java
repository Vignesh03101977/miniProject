package com.onetap.app.sync;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.onetap.app.database.AppDatabase;
import com.onetap.app.database.OfflineAttendanceDao;
import com.onetap.app.firebase.FirebaseManager;
import com.onetap.app.models.Attendance;
import com.onetap.app.models.OfflineAttendance;
import com.onetap.app.utils.NetworkUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SyncManager {

    private static final String TAG = "SyncManager";
    private static SyncManager instance;
    private Context context;
    private OfflineAttendanceDao attendanceDao;
    private FirebaseManager firebaseManager;
    private boolean isSyncing = false;

    public interface SyncCallback {
        void onSyncStarted(int totalPending);
        void onSyncProgress(int synced, int total);
        void onSyncCompleted(int presentCount, int absentCount);
        void onSyncFailed(String error);
    }

    private SyncManager(Context context) {
        this.context = context.getApplicationContext();
        AppDatabase db = AppDatabase.getInstance(this.context);
        this.attendanceDao = db.offlineAttendanceDao();
        this.firebaseManager = FirebaseManager.getInstance();
    }

    public static synchronized SyncManager getInstance(Context context) {
        if (instance == null) instance = new SyncManager(context);
        return instance;
    }

    public void syncAll(SyncCallback callback) {
        if (!NetworkUtils.isNetworkConnected(context)) { if (callback != null) callback.onSyncFailed("No network connection"); return; }
        if (isSyncing) { if (callback != null) callback.onSyncFailed("Sync already in progress"); return; }

        isSyncing = true;
        List<OfflineAttendance> unsyncedList = attendanceDao.getUnsyncedAttendances();
        if (unsyncedList.isEmpty()) { isSyncing = false; if (callback != null) callback.onSyncCompleted(0, 0); return; }
        if (callback != null) callback.onSyncStarted(unsyncedList.size());
        syncAttendanceAtIndex(unsyncedList, 0, 0, 0, callback);
    }

    private void syncAttendanceAtIndex(List<OfflineAttendance> list, int index,
                                       int presentCount, int absentCount, SyncCallback callback) {
        if (index >= list.size()) {
            isSyncing = false;
            if (callback != null) callback.onSyncCompleted(presentCount, absentCount);
            return;
        }

        OfflineAttendance offline = list.get(index);
        if (callback != null) callback.onSyncProgress(index + 1, list.size());

        offline.determineFinalStatus();
        String finalStatus = offline.getStatus();
        boolean isValid = offline.isValidAttendance();

        DatabaseReference attendanceRef = firebaseManager.getAttendanceRef();
        attendanceRef.orderByChild("sessionCode").equalTo(offline.getSessionCode())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean alreadyExists = false;
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Attendance existing = child.getValue(Attendance.class);
                            if (existing != null && existing.getStudentUid() != null &&
                                    existing.getStudentUid().equals(offline.getStudentUid())) {
                                alreadyExists = true;
                                break;
                            }
                        }

                        if (!alreadyExists) {
                            uploadAttendance(offline, finalStatus, isValid, list, index, presentCount, absentCount, callback);
                        } else {
                            attendanceDao.markAsSynced(offline.getLocalId());
                            int np = presentCount + (isValid ? 1 : 0);
                            int na = absentCount + (isValid ? 0 : 1);
                            syncAttendanceAtIndex(list, index + 1, np, na, callback);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        syncAttendanceAtIndex(list, index + 1, presentCount, absentCount, callback);
                    }
                });
    }

    private void uploadAttendance(OfflineAttendance offline, String finalStatus, boolean isValid,
                                  List<OfflineAttendance> list, int index,
                                  int presentCount, int absentCount, SyncCallback callback) {
        String key = firebaseManager.getAttendanceRef().push().getKey();
        if (key == null) { syncAttendanceAtIndex(list, index + 1, presentCount, absentCount, callback); return; }

        Map<String, Object> data = new HashMap<>();
        data.put("attendanceId", key);
        data.put("sessionId", offline.getSessionId());
        data.put("sessionTitle", offline.getSessionTitle());
        data.put("subjectName", offline.getSubjectName());
        data.put("sessionCode", offline.getSessionCode());
        data.put("studentId", offline.getStudentId());
        data.put("studentName", offline.getStudentName());
        data.put("studentUid", offline.getStudentUid());
        data.put("teacherId", offline.getTeacherId());
        data.put("department", offline.getDepartment());
        data.put("markedAt", offline.getMarkedAt());
        data.put("sessionEndTime", offline.getSessionEndTime());
        data.put("wentOnlineAt", offline.getWentOnlineAt());
        data.put("syncedAt", System.currentTimeMillis());
        data.put("status", finalStatus);
        data.put("isValidAttendance", isValid);
        data.put("wentOnlineBeforeTimeout", offline.isWentOnlineBeforeTimeout());
        data.put("networkTypeWhenOnline", offline.getNetworkTypeWhenOnline());
        data.put("airplaneModeWasOn", offline.isAirplaneModeWasOn());
        data.put("wifiWasOff", offline.isWifiWasOff());
        data.put("mobileDataWasOff", offline.isMobileDataWasOff());

        // ✅ NEW: Location fields
        data.put("studentLatitude", offline.getStudentLatitude());
        data.put("studentLongitude", offline.getStudentLongitude());
        data.put("insideLocationBoundary", offline.isInsideLocationBoundary());
        data.put("locationCaptured", offline.isLocationCaptured());
        data.put("locationAbsentReason", offline.getLocationAbsentReason() != null ? offline.getLocationAbsentReason() : "");

        if (!isValid) {
            String reason = "Went online before session ended";
            if (offline.getLocationAbsentReason() != null && !offline.getLocationAbsentReason().isEmpty()) {
                reason = offline.getLocationAbsentReason();
            }
            data.put("absentReason", reason);
        }

        firebaseManager.getAttendanceRef().child(key).setValue(data)
                .addOnSuccessListener(aVoid -> {
                    attendanceDao.markAsSynced(offline.getLocalId());
                    if (isValid) updateSessionAttendeeCount(offline.getSessionId(), offline.getStudentUid());
                    int np = presentCount + (isValid ? 1 : 0);
                    int na = absentCount + (isValid ? 0 : 1);
                    syncAttendanceAtIndex(list, index + 1, np, na, callback);
                })
                .addOnFailureListener(e -> syncAttendanceAtIndex(list, index + 1, presentCount, absentCount, callback));
    }

    private void updateSessionAttendeeCount(String sessionId, String studentUid) {
        if (sessionId == null || sessionId.startsWith("pending_")) return;
        DatabaseReference sessionRef = firebaseManager.getSessionsRef().child(sessionId);
        sessionRef.child("attendees").child(studentUid).setValue(true);
        sessionRef.child("attendees").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                sessionRef.child("totalStudents").setValue((int) snapshot.getChildrenCount());
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    public int getPendingSyncCount() { return attendanceDao.getUnsyncedCount(); }
    public List<OfflineAttendance> getUnsyncedAttendances() { return attendanceDao.getUnsyncedAttendances(); }

    public void onNetworkAvailable(String networkType) {
        List<OfflineAttendance> pending = attendanceDao.getPendingAttendances();
        long now = System.currentTimeMillis();
        for (OfflineAttendance attendance : pending) {
            if (attendance.getWentOnlineAt() == 0) {
                boolean beforeTimeout = now < attendance.getSessionEndTime();
                attendanceDao.updateNetworkChangeInfo(attendance.getLocalId(), now, networkType,
                        beforeTimeout, beforeTimeout ? "absent" : "present", !beforeTimeout);
            }
        }
    }
}