package com.onetap.app.models;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class UserModelTest {

    private User user;

    @Before
    public void setUp() {
        user = new User(
                "uid001",
                "Pavan Kumar",
                "pavan@test.com",
                "9876543210",
                "STU001",
                "CSE",
                "student",
                "approved"
        );
    }

    @Test
    public void test_UserCreation_AllFieldsSet() {
        assertNotNull(user);
        assertEquals("uid001", user.getUid());
        assertEquals("Pavan Kumar", user.getFullName());
        assertEquals("pavan@test.com", user.getEmail());
        assertEquals("9876543210", user.getPhone());
        assertEquals("STU001", user.getStudentId());
        assertEquals("CSE", user.getDepartment());
        assertEquals("student", user.getRole());
        assertEquals("approved", user.getStatus());
    }

    @Test
    public void test_UserRole_IsStudent() {
        assertEquals("student", user.getRole());
    }

    @Test
    public void test_UserStatus_IsApproved() {
        assertEquals("approved", user.getStatus());
    }

    @Test
    public void test_UserSetters_WorkCorrectly() {
        user.setFullName("Updated Name");
        assertEquals("Updated Name", user.getFullName());
    }

    @Test
    public void test_UserEmail_IsValid() {
        assertTrue(user.getEmail().contains("@"));
    }

    @Test
    public void test_GetName_ReturnsFullName() {
        assertEquals("Pavan Kumar", user.getName());
    }

    @Test
    public void test_GetRollNumber_ReturnsStudentId() {
        assertEquals("STU001", user.getRollNumber());
    }

    @Test
    public void test_TeacherUser_PendingStatus() {
        User teacher = new User(
                "uid002",
                "Teacher Name",
                "teacher@test.com",
                "9876543211",
                "TCH001",
                "CSE",
                "teacher",
                "pending"
        );
        assertEquals("teacher", teacher.getRole());
        assertEquals("pending", teacher.getStatus());
    }

    @Test
    public void test_AdminUser_ApprovedStatus() {
        User admin = new User(
                "uid003",
                "Admin",
                "admin@onetap.com",
                "",
                "ADMIN001",
                "Administration",
                "admin",
                "approved"
        );
        assertEquals("admin", admin.getRole());
        assertEquals("approved", admin.getStatus());
    }

    @Test
    public void test_CreatedAt_IsSet() {
        assertTrue(user.getCreatedAt() > 0);
    }
}