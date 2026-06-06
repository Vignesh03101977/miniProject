package com.onetap.app.utils;

public class Constants {
    // Firebase Database Paths
    public static final String DB_USERS = "users";
    public static final String DB_SESSIONS = "sessions";
    public static final String DB_ATTENDANCE = "attendance";
    public static final String DB_TEACHERS = "teachers";
    public static final String DB_STUDENTS = "students";

    // User Roles
    public static final String ROLE_STUDENT = "student";
    public static final String ROLE_TEACHER = "teacher";
    public static final String ROLE_ADMIN = "admin";

    // User Status
    public static final String STATUS_APPROVED = "approved";
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_REJECTED = "rejected";

    // Attendance Status
    public static final String ATTENDANCE_PRESENT = "present";
    public static final String ATTENDANCE_ABSENT = "absent";

    // Admin Credentials
    public static final String ADMIN_EMAIL = "admin@onetap.com";
    public static final String ADMIN_PASSWORD = "admin123";

    // SharedPreferences
    public static final String PREF_NAME = "OneTapPrefs";
    public static final String PREF_USER_ROLE = "user_role";
    public static final String PREF_USER_NAME = "user_name";
    public static final String PREF_USER_EMAIL = "user_email";
    public static final String PREF_IS_LOGGED_IN = "is_logged_in";

    // Session Code Length
    public static final int SESSION_CODE_LENGTH = 6;
}