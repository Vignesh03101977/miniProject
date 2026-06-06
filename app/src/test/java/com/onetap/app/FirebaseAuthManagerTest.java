package com.onetap.app;

import com.onetap.app.utils.Constants;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

// ✅ NO Firebase imports
// ✅ NO Mockito annotations
// ✅ ONLY tests Constants values
public class FirebaseAuthManagerTest {

    // ─────────────────────────────
    // TEST 1: Admin Email Format
    // ─────────────────────────────
    @Test
    public void test_AdminEmail_IsCorrectFormat() {
        String adminEmail = Constants.ADMIN_EMAIL;

        assertNotNull(
                "Admin email should not be null",
                adminEmail
        );
        assertTrue(
                "Admin email should contain @",
                adminEmail.contains("@")
        );
        assertTrue(
                "Admin email should contain domain",
                adminEmail.contains(".")
        );
    }

    // ─────────────────────────────
    // TEST 2: Role Constants
    // ─────────────────────────────
    @Test
    public void test_RoleConstants_AreCorrect() {
        assertEquals(
                "Student role should be 'student'",
                "student",
                Constants.ROLE_STUDENT
        );
        assertEquals(
                "Teacher role should be 'teacher'",
                "teacher",
                Constants.ROLE_TEACHER
        );
        assertEquals(
                "Admin role should be 'admin'",
                "admin",
                Constants.ROLE_ADMIN
        );
    }

    // ─────────────────────────────
    // TEST 3: Status Constants
    // ─────────────────────────────
    @Test
    public void test_StatusConstants_AreCorrect() {
        assertEquals(
                "Approved status should be 'approved'",
                "approved",
                Constants.STATUS_APPROVED
        );
        assertEquals(
                "Pending status should be 'pending'",
                "pending",
                Constants.STATUS_PENDING
        );
        assertEquals(
                "Rejected status should be 'rejected'",
                "rejected",
                Constants.STATUS_REJECTED
        );
    }

    // ─────────────────────────────
    // TEST 4: Attendance Constants
    // ─────────────────────────────
    @Test
    public void test_AttendanceConstants_AreCorrect() {
        assertEquals(
                "Present constant should be 'present'",
                "present",
                Constants.ATTENDANCE_PRESENT
        );
        assertEquals(
                "Absent constant should be 'absent'",
                "absent",
                Constants.ATTENDANCE_ABSENT
        );
    }

    // ─────────────────────────────
    // TEST 5: Database Path Constants
    // ─────────────────────────────
    @Test
    public void test_DatabasePaths_AreCorrect() {
        assertEquals(
                "Users path should be 'users'",
                "users",
                Constants.DB_USERS
        );
        assertEquals(
                "Sessions path should be 'sessions'",
                "sessions",
                Constants.DB_SESSIONS
        );
        assertEquals(
                "Attendance path should be 'attendance'",
                "attendance",
                Constants.DB_ATTENDANCE
        );
    }

    // ─────────────────────────────
    // TEST 6: Session Code Length
    // ─────────────────────────────
    @Test
    public void test_SessionCodeLength_IsCorrect() {
        assertEquals(
                "Session code length should be 6",
                6,
                Constants.SESSION_CODE_LENGTH
        );
    }

    // ─────────────────────────────
    // TEST 7: SharedPreferences Keys
    // ─────────────────────────────
    @Test
    public void test_SharedPrefKeys_NotNull() {
        assertNotNull(
                "Pref name should not be null",
                Constants.PREF_NAME
        );
        assertNotNull(
                "User role pref should not be null",
                Constants.PREF_USER_ROLE
        );
        assertNotNull(
                "User name pref should not be null",
                Constants.PREF_USER_NAME
        );
    }
}