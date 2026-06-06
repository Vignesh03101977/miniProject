package com.onetap.app.models;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class AttendanceModelTest {

    private Attendance attendance;

    @Before
    public void setUp() {
        attendance = new Attendance(
                "att001",
                "session001",
                "Math Class",
                "Mathematics",
                "STU001",
                "Pavan Kumar",
                "uid001",
                "teacher001",
                "CSE"
        );
    }

    @Test
    public void test_AttendanceCreation_AllFieldsSet() {
        assertNotNull(attendance);
        assertEquals("att001", attendance.getAttendanceId());
        assertEquals("session001", attendance.getSessionId());
        assertEquals("Math Class", attendance.getSessionTitle());
        assertEquals("Mathematics", attendance.getSubjectName());
        assertEquals("STU001", attendance.getStudentId());
        assertEquals("Pavan Kumar", attendance.getStudentName());
        assertEquals("uid001", attendance.getStudentUid());
        assertEquals("teacher001", attendance.getTeacherId());
        assertEquals("CSE", attendance.getDepartment());
    }

    @Test
    public void test_DefaultStatus_IsPresent() {
        assertEquals(
                "Default status should be present",
                "present",
                attendance.getStatus()
        );
    }

    @Test
    public void test_IsPresent_ReturnsTrue() {
        assertTrue(
                "Should be present initially",
                attendance.isPresent()
        );
    }

    @Test
    public void test_SetAbsent_Works() {
        attendance.setStatus("absent");
        assertFalse(
                "Should not be present after setting absent",
                attendance.isPresent()
        );
    }

    @Test
    public void test_MarkedAt_IsSet() {
        assertTrue(
                "MarkedAt should be set",
                attendance.getMarkedAt() > 0
        );
    }

    @Test
    public void test_LocationFields_DefaultValues() {
        assertFalse(
                "Location not captured by default",
                attendance.isLocationCaptured()
        );
    }

    @Test
    public void test_SetPresent_True() {
        attendance.setPresent(true);
        assertEquals("present", attendance.getStatus());
    }

    @Test
    public void test_SetPresent_False() {
        attendance.setPresent(false);
        assertEquals("absent", attendance.getStatus());
    }

    @Test
    public void test_LocationFields_CanBeSet() {
        attendance.setStudentLatitude(17.3850);
        attendance.setStudentLongitude(78.4867);
        attendance.setLocationCaptured(true);
        attendance.setInsideLocationBoundary(true);

        assertEquals(17.3850,
                attendance.getStudentLatitude(), 0.001);
        assertEquals(78.4867,
                attendance.getStudentLongitude(), 0.001);
        assertTrue(attendance.isLocationCaptured());
        assertTrue(attendance.isInsideLocationBoundary());
    }
}