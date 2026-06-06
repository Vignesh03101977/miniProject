package com.onetap.app;

import com.onetap.app.models.OfflineAttendance;

import org.junit.Test;
import org.junit.Before;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class SyncManagerLogicTest {

    private OfflineAttendance attendance;

    @Before
    public void setUp() {
        attendance = new OfflineAttendance();
        attendance.setSessionId("session123");
        attendance.setSessionTitle("Math Class");
        attendance.setSubjectName("Mathematics");
        attendance.setSessionCode("ABC123");
        attendance.setStudentId("STU001");
        attendance.setStudentName("Pavan Kumar");
        attendance.setStudentUid("uid123");
        attendance.setTeacherId("teacher001");
        attendance.setDepartment("CSE");
    }

    // ─────────────────────────────
    // TEST 1: Initial State
    // ─────────────────────────────
    @Test
    public void test_InitialState_IsPending() {
        assertEquals(
                "Initial status should be pending",
                "pending",
                attendance.getStatus()
        );
    }

    // ─────────────────────────────
    // TEST 2: Not Synced Initially
    // ─────────────────────────────
    @Test
    public void test_InitialState_NotSynced() {
        assertFalse(
                "Should not be synced initially",
                attendance.isSynced()
        );
    }

    // ─────────────────────────────
    // TEST 3: Valid Attendance Data
    // ─────────────────────────────
    @Test
    public void test_AttendanceData_IsComplete() {
        assertNotNull(attendance.getSessionId());
        assertNotNull(attendance.getStudentId());
        assertNotNull(attendance.getStudentUid());
        assertNotNull(attendance.getTeacherId());
        assertNotNull(attendance.getSessionCode());
    }

    // ─────────────────────────────
    // TEST 4: Mark As Synced
    // ─────────────────────────────
    @Test
    public void test_MarkAsSynced_UpdatesFlag() {
        attendance.setSynced(true);
        assertTrue(
                "Should be synced after update",
                attendance.isSynced()
        );
    }

    // ─────────────────────────────
    // TEST 5: Sync Logic Present
    // ─────────────────────────────
    @Test
    public void test_SyncLogic_PresentAfterSession() {
        long now = System.currentTimeMillis();
        attendance.setSessionEndTime(now - 60000);
        attendance.setWentOnlineAt(now);

        attendance.determineFinalStatus();

        assertEquals(
                "Should be present after session ends",
                "present",
                attendance.getStatus()
        );
    }

    // ─────────────────────────────
    // TEST 6: Sync Logic Absent
    // ─────────────────────────────
    @Test
    public void test_SyncLogic_AbsentBeforeSession() {
        long now = System.currentTimeMillis();
        attendance.setSessionEndTime(now + 60000);
        attendance.setWentOnlineAt(now);

        attendance.determineFinalStatus();

        assertEquals(
                "Should be absent before session ends",
                "absent",
                attendance.getStatus()
        );
    }
}