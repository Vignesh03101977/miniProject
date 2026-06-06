package com.onetap.app.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;


@Entity(tableName = "offline_attendance")
public class OfflineAttendance {

    @PrimaryKey(autoGenerate = true)
    private long localId;

    @NonNull private String sessionId;
    @NonNull private String sessionTitle;
    @NonNull private String subjectName;
    @NonNull private String sessionCode;
    @NonNull private String studentId;
    @NonNull private String studentName;
    @NonNull private String studentUid;
    @NonNull private String teacherId;
    @NonNull private String department;

    private long markedAt;
    private long sessionEndTime;
    private long wentOnlineAt;
    private String status;
    private boolean synced;
    private boolean wentOnlineBeforeTimeout;
    private String networkTypeWhenOnline;
    private boolean airplaneModeWasOn;
    private boolean wifiWasOff;
    private boolean mobileDataWasOff;
    private boolean isValidAttendance;

    // ✅ NEW: Location fields
    private double studentLatitude;
    private double studentLongitude;
    private boolean insideLocationBoundary;
    private boolean locationCaptured;
    private String locationAbsentReason;

    public OfflineAttendance() {
        this.markedAt = System.currentTimeMillis();
        this.synced = false;
        this.status = "pending";
        this.wentOnlineAt = 0;
        this.wentOnlineBeforeTimeout = false;
        this.networkTypeWhenOnline = "";
        this.airplaneModeWasOn = true;
        this.wifiWasOff = true;
        this.mobileDataWasOff = true;
        this.isValidAttendance = true;
        this.studentLatitude = 0;
        this.studentLongitude = 0;
        this.insideLocationBoundary = true;
        this.locationCaptured = false;
        this.locationAbsentReason = "";
    }

    @Ignore
    public OfflineAttendance(@NonNull String sessionId, @NonNull String sessionTitle,
                             @NonNull String subjectName, @NonNull String sessionCode,
                             @NonNull String studentId, @NonNull String studentName,
                             @NonNull String studentUid, @NonNull String teacherId,
                             @NonNull String department, long sessionEndTime,
                             boolean airplaneModeWasOn, boolean wifiWasOff,
                             boolean mobileDataWasOff) {
        this.sessionId = sessionId;
        this.sessionTitle = sessionTitle;
        this.subjectName = subjectName;
        this.sessionCode = sessionCode;
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentUid = studentUid;
        this.teacherId = teacherId;
        this.department = department;
        this.sessionEndTime = sessionEndTime;
        this.markedAt = System.currentTimeMillis();
        this.synced = false;
        this.status = "pending";
        this.wentOnlineAt = 0;
        this.wentOnlineBeforeTimeout = false;
        this.networkTypeWhenOnline = "";
        this.airplaneModeWasOn = airplaneModeWasOn;
        this.wifiWasOff = wifiWasOff;
        this.mobileDataWasOff = mobileDataWasOff;
        this.isValidAttendance = true;
        this.studentLatitude = 0;
        this.studentLongitude = 0;
        this.insideLocationBoundary = true;
        this.locationCaptured = false;
        this.locationAbsentReason = "";
    }

    // Existing getters/setters
    public long getLocalId() { return localId; }
    public void setLocalId(long localId) { this.localId = localId; }
    @NonNull public String getSessionId() { return sessionId; }
    public void setSessionId(@NonNull String v) { this.sessionId = v; }
    @NonNull public String getSessionTitle() { return sessionTitle; }
    public void setSessionTitle(@NonNull String v) { this.sessionTitle = v; }
    @NonNull public String getSubjectName() { return subjectName; }
    public void setSubjectName(@NonNull String v) { this.subjectName = v; }
    @NonNull public String getSessionCode() { return sessionCode; }
    public void setSessionCode(@NonNull String v) { this.sessionCode = v; }
    @NonNull public String getStudentId() { return studentId; }
    public void setStudentId(@NonNull String v) { this.studentId = v; }
    @NonNull public String getStudentName() { return studentName; }
    public void setStudentName(@NonNull String v) { this.studentName = v; }
    @NonNull public String getStudentUid() { return studentUid; }
    public void setStudentUid(@NonNull String v) { this.studentUid = v; }
    @NonNull public String getTeacherId() { return teacherId; }
    public void setTeacherId(@NonNull String v) { this.teacherId = v; }
    @NonNull public String getDepartment() { return department; }
    public void setDepartment(@NonNull String v) { this.department = v; }
    public long getMarkedAt() { return markedAt; }
    public void setMarkedAt(long v) { this.markedAt = v; }
    public long getSessionEndTime() { return sessionEndTime; }
    public void setSessionEndTime(long v) { this.sessionEndTime = v; }
    public long getWentOnlineAt() { return wentOnlineAt; }
    public void setWentOnlineAt(long v) { this.wentOnlineAt = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }
    public boolean isSynced() { return synced; }
    public void setSynced(boolean v) { this.synced = v; }
    public boolean isWentOnlineBeforeTimeout() { return wentOnlineBeforeTimeout; }
    public void setWentOnlineBeforeTimeout(boolean v) { this.wentOnlineBeforeTimeout = v; }
    public String getNetworkTypeWhenOnline() { return networkTypeWhenOnline; }
    public void setNetworkTypeWhenOnline(String v) { this.networkTypeWhenOnline = v; }
    public boolean isAirplaneModeWasOn() { return airplaneModeWasOn; }
    public void setAirplaneModeWasOn(boolean v) { this.airplaneModeWasOn = v; }
    public boolean isWifiWasOff() { return wifiWasOff; }
    public void setWifiWasOff(boolean v) { this.wifiWasOff = v; }
    public boolean isMobileDataWasOff() { return mobileDataWasOff; }
    public void setMobileDataWasOff(boolean v) { this.mobileDataWasOff = v; }
    public boolean isValidAttendance() { return isValidAttendance; }
    public void setValidAttendance(boolean v) { isValidAttendance = v; }

    // ✅ NEW: Location getters/setters
    public double getStudentLatitude() { return studentLatitude; }
    public void setStudentLatitude(double v) { this.studentLatitude = v; }
    public double getStudentLongitude() { return studentLongitude; }
    public void setStudentLongitude(double v) { this.studentLongitude = v; }
    public boolean isInsideLocationBoundary() { return insideLocationBoundary; }
    public void setInsideLocationBoundary(boolean v) { this.insideLocationBoundary = v; }
    public boolean isLocationCaptured() { return locationCaptured; }
    public void setLocationCaptured(boolean v) { this.locationCaptured = v; }
    public String getLocationAbsentReason() { return locationAbsentReason; }
    public void setLocationAbsentReason(String v) { this.locationAbsentReason = v; }

    public void determineFinalStatus() {
        // Existing timing check
        if (wentOnlineAt > 0 && wentOnlineAt < sessionEndTime) {
            this.status = "absent";
            this.wentOnlineBeforeTimeout = true;
            this.isValidAttendance = false;
        } else {
            this.status = "present";
            this.wentOnlineBeforeTimeout = false;
            this.isValidAttendance = true;
        }

        // ✅ NEW: Additional location check (only if timing passed)
        if (this.isValidAttendance && this.locationCaptured && !this.insideLocationBoundary) {
            this.status = "absent";
            this.isValidAttendance = false;
            this.locationAbsentReason = "Outside classroom boundary";
        }
    }
}