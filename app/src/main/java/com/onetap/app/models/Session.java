package com.onetap.app.models;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Session implements Serializable {
    private String sessionId;
    private String sessionTitle;
    private String subjectName;
    private String sessionCode;
    private String teacherId;
    private String teacherName;
    private String department;
    private int duration;
    private long startTime;
    private long endTime;
    private boolean isActive;
    private int totalStudents;
    private Map<String, Boolean> attendees;

    // ✅ NEW: Location fields
    private double teacherLatitude;
    private double teacherLongitude;
    private double minLatitude;
    private double maxLatitude;
    private double minLongitude;
    private double maxLongitude;
    private double boundaryRangeMeters;
    private boolean locationValidationEnabled;

    public Session() {
        this.attendees = new HashMap<>();
        this.boundaryRangeMeters = 100;
        this.locationValidationEnabled = false;
    }

    public Session(String sessionId, String sessionTitle, String subjectName,
                   String sessionCode, String teacherId, String teacherName,
                   String department, int duration) {
        this.sessionId = sessionId;
        this.sessionTitle = sessionTitle;
        this.subjectName = subjectName;
        this.sessionCode = sessionCode;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.department = department;
        this.duration = duration;
        this.startTime = System.currentTimeMillis();
        this.endTime = this.startTime + (duration * 60 * 1000L);
        this.isActive = true;
        this.totalStudents = 0;
        this.attendees = new HashMap<>();
        this.boundaryRangeMeters = 100;
        this.locationValidationEnabled = false;
    }

    // Existing getters
    public String getSessionId() { return sessionId; }
    public String getSessionTitle() { return sessionTitle; }
    public String getSubjectName() { return subjectName; }
    public String getSessionCode() { return sessionCode; }
    public String getTeacherId() { return teacherId; }
    public String getTeacherName() { return teacherName; }
    public String getDepartment() { return department; }
    public int getDuration() { return duration; }
    public long getStartTime() { return startTime; }
    public long getEndTime() { return endTime; }
    public boolean isActive() { return isActive; }
    public int getTotalStudents() { return totalStudents; }
    public Map<String, Boolean> getAttendees() { return attendees; }

    // ✅ NEW: Location getters
    public double getTeacherLatitude() { return teacherLatitude; }
    public double getTeacherLongitude() { return teacherLongitude; }
    public double getMinLatitude() { return minLatitude; }
    public double getMaxLatitude() { return maxLatitude; }
    public double getMinLongitude() { return minLongitude; }
    public double getMaxLongitude() { return maxLongitude; }
    public double getBoundaryRangeMeters() { return boundaryRangeMeters; }
    public boolean isLocationValidationEnabled() { return locationValidationEnabled; }

    // Existing setters
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public void setSessionTitle(String sessionTitle) { this.sessionTitle = sessionTitle; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public void setSessionCode(String sessionCode) { this.sessionCode = sessionCode; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
    public void setDepartment(String department) { this.department = department; }
    public void setDuration(int duration) { this.duration = duration; }
    public void setStartTime(long startTime) { this.startTime = startTime; }
    public void setEndTime(long endTime) { this.endTime = endTime; }
    public void setActive(boolean active) { isActive = active; }
    public void setTotalStudents(int totalStudents) { this.totalStudents = totalStudents; }
    public void setAttendees(Map<String, Boolean> attendees) { this.attendees = attendees; }

    // ✅ NEW: Location setters
    public void setTeacherLatitude(double v) { this.teacherLatitude = v; }
    public void setTeacherLongitude(double v) { this.teacherLongitude = v; }
    public void setMinLatitude(double v) { this.minLatitude = v; }
    public void setMaxLatitude(double v) { this.maxLatitude = v; }
    public void setMinLongitude(double v) { this.minLongitude = v; }
    public void setMaxLongitude(double v) { this.maxLongitude = v; }
    public void setBoundaryRangeMeters(double v) { this.boundaryRangeMeters = v; }
    public void setLocationValidationEnabled(boolean v) { this.locationValidationEnabled = v; }
}