package com.onetap.app.models;

public class User {
    private String uid;
    private String fullName;
    private String email;
    private String phone;
    private String studentId;
    private String department;
    private String role; // "student", "teacher", "admin"
    private String status; // "approved", "pending", "rejected"
    private long createdAt;

    public User() {
        // Required for Firebase
    }

    public User(String uid, String fullName, String email, String phone,
                String studentId, String department, String role, String status) {
        this.uid = uid;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.studentId = studentId;
        this.department = department;
        this.role = role;
        this.status = status;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters
    public String getUid() { return uid; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getStudentId() { return studentId; }
    public String getDepartment() { return department; }
    public String getRole() { return role; }
    public String getStatus() { return status; }
    public long getCreatedAt() { return createdAt; }

    // Setters
    public void setUid(String uid) { this.uid = uid; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public void setDepartment(String department) { this.department = department; }
    public void setRole(String role) { this.role = role; }
    public void setStatus(String status) { this.status = status; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public String getName() { return getFullName(); }
    public String getRollNumber() { return getStudentId(); }
}