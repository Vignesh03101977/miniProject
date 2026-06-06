package com.onetap.app.models;

public class Attendance {
    private String attendanceId;
    private String sessionId;
    private String sessionTitle;
    private String subjectName;
    private String sessionCode;
    private String studentId;
    private String studentName;
    private String studentUid;
    private String teacherId;
    private long markedAt;
    private String status;
    private String department;

    // ✅ NEW: Location fields
    private double studentLatitude;
    private double studentLongitude;
    private boolean insideLocationBoundary;
    private boolean locationCaptured;
    private String locationAbsentReason;

    public Attendance() {}

    public Attendance(String attendanceId, String sessionId, String sessionTitle,
                      String subjectName, String studentId, String studentName,
                      String studentUid, String teacherId, String department) {
        this.attendanceId = attendanceId;
        this.sessionId = sessionId;
        this.sessionTitle = sessionTitle;
        this.subjectName = subjectName;
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentUid = studentUid;
        this.teacherId = teacherId;
        this.markedAt = System.currentTimeMillis();
        this.status = "present";
        this.department = department;
    }

    // Existing getters
    public String getAttendanceId() { return attendanceId; }
    public String getSessionId() { return sessionId; }
    public String getSessionTitle() { return sessionTitle; }
    public String getSubjectName() { return subjectName; }
    public String getSessionCode() { return sessionCode; }
    public String getStudentId() { return studentId; }
    public String getStudentName() { return studentName; }
    public String getStudentUid() { return studentUid; }
    public String getTeacherId() { return teacherId; }
    public long getMarkedAt() { return markedAt; }
    public String getStatus() { return status; }
    public String getDepartment() { return department; }

    // ✅ NEW: Location getters
    public double getStudentLatitude() { return studentLatitude; }
    public double getStudentLongitude() { return studentLongitude; }
    public boolean isInsideLocationBoundary() { return insideLocationBoundary; }
    public boolean isLocationCaptured() { return locationCaptured; }
    public String getLocationAbsentReason() { return locationAbsentReason; }

    // Existing setters
    public void setAttendanceId(String v) { this.attendanceId = v; }
    public void setSessionId(String v) { this.sessionId = v; }
    public void setSessionTitle(String v) { this.sessionTitle = v; }
    public void setSubjectName(String v) { this.subjectName = v; }
    public void setSessionCode(String v) { this.sessionCode = v; }
    public void setStudentId(String v) { this.studentId = v; }
    public void setStudentName(String v) { this.studentName = v; }
    public void setStudentUid(String v) { this.studentUid = v; }
    public void setTeacherId(String v) { this.teacherId = v; }
    public void setMarkedAt(long v) { this.markedAt = v; }
    public void setStatus(String v) { this.status = v; }
    public void setDepartment(String v) { this.department = v; }

    // ✅ NEW: Location setters
    public void setStudentLatitude(double v) { this.studentLatitude = v; }
    public void setStudentLongitude(double v) { this.studentLongitude = v; }
    public void setInsideLocationBoundary(boolean v) { this.insideLocationBoundary = v; }
    public void setLocationCaptured(boolean v) { this.locationCaptured = v; }
    public void setLocationAbsentReason(String v) { this.locationAbsentReason = v; }

    public boolean isPresent() { return "present".equalsIgnoreCase(status); }
    public void setPresent(boolean present) { this.status = present ? "present" : "absent"; }
}