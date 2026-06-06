package com.onetap.app.models;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class SessionModelTest {

    private Session session;

    @Before
    public void setUp() {
        session = new Session(
                "session001",
                "Math Class",
                "Mathematics",
                "ABC123",
                "teacher001",
                "Prof. Kumar",
                "CSE",
                60
        );
    }

    @Test
    public void test_SessionCreation_AllFieldsSet() {
        assertNotNull(session);
        assertEquals("session001", session.getSessionId());
        assertEquals("Math Class", session.getSessionTitle());
        assertEquals("Mathematics", session.getSubjectName());
        assertEquals("ABC123", session.getSessionCode());
        assertEquals("teacher001", session.getTeacherId());
        assertEquals("Prof. Kumar", session.getTeacherName());
        assertEquals("CSE", session.getDepartment());
        assertEquals(60, session.getDuration());
    }

    @Test
    public void test_SessionIsActive_OnCreation() {
        assertTrue(
                "New session should be active",
                session.isActive()
        );
    }

    @Test
    public void test_SessionCode_IsCorrectLength() {
        assertEquals(
                "Session code should be 6 chars",
                6,
                session.getSessionCode().length()
        );
    }

    @Test
    public void test_StartTime_IsSet() {
        assertTrue(
                "Start time should be set",
                session.getStartTime() > 0
        );
    }

    @Test
    public void test_EndTime_IsAfterStartTime() {
        assertTrue(
                "End time should be after start time",
                session.getEndTime() > session.getStartTime()
        );
    }

    @Test
    public void test_Duration_CalculatesEndTime() {
        long expectedEnd = session.getStartTime()
                + (60 * 60 * 1000L);
        assertEquals(
                "End time should match duration",
                expectedEnd,
                session.getEndTime()
        );
    }

    @Test
    public void test_TotalStudents_InitiallyZero() {
        assertEquals(
                "Initially no students",
                0,
                session.getTotalStudents()
        );
    }

    @Test
    public void test_LocationValidation_DisabledByDefault() {
        assertFalse(
                "Location validation disabled by default",
                session.isLocationValidationEnabled()
        );
    }

    @Test
    public void test_BoundaryRange_DefaultValue() {
        assertEquals(
                "Default boundary is 100m",
                100.0,
                session.getBoundaryRangeMeters(),
                0.001
        );
    }

    @Test
    public void test_SetActive_False() {
        session.setActive(false);
        assertFalse(
                "Session should be inactive",
                session.isActive()
        );
    }

    @Test
    public void test_LocationFields_CanBeSet() {
        session.setTeacherLatitude(17.3850);
        session.setTeacherLongitude(78.4867);
        session.setLocationValidationEnabled(true);

        assertEquals(17.3850,
                session.getTeacherLatitude(), 0.001);
        assertEquals(78.4867,
                session.getTeacherLongitude(), 0.001);
        assertTrue(session.isLocationValidationEnabled());
    }
}