package com.onetap.app.models;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class TeacherModelTest {

    private Teacher teacher;

    @Before
    public void setUp() {
        teacher = new Teacher(
                "uid002",
                "Prof. Kumar",
                "kumar@test.com",
                "9876543211",
                "TCH001",
                "CSE",
                "approved"
        );
    }

    @Test
    public void test_TeacherCreation_Works() {
        assertNotNull(teacher);
        assertEquals("Prof. Kumar", teacher.getFullName());
        assertEquals("TCH001", teacher.getStudentId());
        assertEquals("CSE", teacher.getDepartment());
    }

    @Test
    public void test_TeacherRole_IsTeacher() {
        assertEquals("teacher", teacher.getRole());
    }

    @Test
    public void test_TeacherApproved_IsTrue() {
        assertTrue(
                "Approved teacher should be approved",
                teacher.isApproved()
        );
    }

    @Test
    public void test_PendingTeacher_IsNotApproved() {
        Teacher pending = new Teacher(
                "uid003",
                "New Teacher",
                "new@test.com",
                "9876543212",
                "TCH002",
                "ECE",
                "pending"
        );
        assertFalse(
                "Pending teacher should not be approved",
                pending.isApproved()
        );
    }

    @Test
    public void test_InitialSessions_IsZero() {
        assertEquals(0, teacher.getTotalSessions());
    }

    @Test
    public void test_SetSessions_Works() {
        teacher.setTotalSessions(15);
        assertEquals(15, teacher.getTotalSessions());
    }

    @Test
    public void test_SetApproved_Works() {
        teacher.setApproved(false);
        assertFalse(teacher.isApproved());
    }
}