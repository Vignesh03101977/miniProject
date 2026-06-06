package com.onetap.app.models;

public class Teacher extends User {
    private int totalSessions;
    private boolean isApproved;

    public Teacher() {
        super();
    }

    public Teacher(String uid, String fullName, String email, String phone,
                   String studentId, String department, String status) {
        super(uid, fullName, email, phone, studentId, department, "teacher", status);
        this.totalSessions = 0;
        this.isApproved = "approved".equals(status);
    }

    public int getTotalSessions() { return totalSessions; }
    public void setTotalSessions(int totalSessions) { this.totalSessions = totalSessions; }
    public boolean isApproved() { return isApproved; }
    public void setApproved(boolean approved) { isApproved = approved; }
}