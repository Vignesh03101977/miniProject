package com.onetap.app.models;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class StudentModelTest {

    private Student student;

    @Before
    public void setUp() {
        student = new Student(
                "uid001",
                "Pavan Kumar",
                "pavan@test.com",
                "9876543210",
                "STU001",
                "CSE"
        );
    }

    @Test
    public void test_StudentCreation_Works() {
        assertNotNull(student);
        assertEquals("Pavan Kumar", student.getFullName());
        assertEquals("STU001", student.getStudentId());
        assertEquals("CSE", student.getDepartment());
    }

    @Test
    public void test_StudentRole_IsStudent() {
        assertEquals("student", student.getRole());
    }

    @Test
    public void test_StudentStatus_IsApproved() {
        assertEquals("approved", student.getStatus());
    }

    @Test
    public void test_InitialAttendance_IsZero() {
        assertEquals(0, student.getTotalAttendance());
        assertEquals(0, student.getTotalSessions());
        assertEquals(0.0,
                student.getAttendancePercentage(), 0.001);
    }

    @Test
    public void test_SetAttendance_Works() {
        student.setTotalAttendance(8);
        student.setTotalSessions(10);
        student.setAttendancePercentage(80.0);

        assertEquals(8, student.getTotalAttendance());
        assertEquals(10, student.getTotalSessions());
        assertEquals(80.0,
                student.getAttendancePercentage(), 0.001);
    }

    @Test
    public void test_GetId_ReturnsStudentId() {
        assertEquals("STU001", student.getId());
    }

    @Test
    public void test_GetName_ReturnsFullName() {
        assertEquals("Pavan Kumar", student.getName());
    }

    @Test
    public void test_GetRollNumber_ReturnsStudentId() {
        assertEquals("STU001", student.getRollNumber());
    }
}