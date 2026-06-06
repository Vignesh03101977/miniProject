package com.onetap.app.models;

public class Student extends User {
    private int totalAttendance;
    private int totalSessions;
    private double attendancePercentage;

    public Student() {
        super();
    }

    public Student(String fullName, String studentId) {
        // Set basic info, default role "student", status "approved"
        super(null, fullName, null, null, studentId, null, "student", "approved");
        this.totalAttendance = 0;
        this.totalSessions = 0;
        this.attendancePercentage = 0.0;
    }

    public Student(String uid, String fullName, String email, String phone,
                   String studentId, String department) {
        super(uid, fullName, email, phone, studentId, department, "student", "approved");
        this.totalAttendance = 0;
        this.totalSessions = 0;
        this.attendancePercentage = 0.0;
    }

    public int getTotalAttendance() { return totalAttendance; }
    public void setTotalAttendance(int totalAttendance) { this.totalAttendance = totalAttendance; }
    public int getTotalSessions() { return totalSessions; }
    public void setTotalSessions(int totalSessions) { this.totalSessions = totalSessions; }
    public double getAttendancePercentage() { return attendancePercentage; }
    public void setAttendancePercentage(double attendancePercentage) { this.attendancePercentage = attendancePercentage; }

    // ✅ Helper methods for Excel export & compatibility

    public String getId() {
        // Map "id" to the existing studentId from User
        return getStudentId();
    }

    public String getName() {
        // Map "name" to fullName from User
        return getFullName();
    }

    public String getRollNumber() {
        // Use studentId as roll number
        return getStudentId();
    }
}