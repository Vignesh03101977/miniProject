package com.onetap.app.sync;

import com.onetap.app.models.OfflineAttendance;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class SyncManagerLogicTest {

    private OfflineAttendance attendance;

    @Before
    public void setUp() {
        attendance = new OfflineAttendance(
                "session001",
                "Math Class",
                "Mathematics",
                "ABC123",
                "STU001",
                "Pavan Kumar",
                "uid001",
                "teacher001",
                "CSE",
                System.currentTimeMillis() + 300000,
                true, true, true
        );
    }

    @Test
    public void test_InitialStatus_IsPending() {
        assertEquals("pending", attendance.getStatus());
    }

    @Test
    public void test_InitialSynced_IsFalse() {
        assertFalse(attendance.isSynced());
    }

    @Test
    public void test_AirplaneMode_WasOn() {
        assertTrue(attendance.isAirplaneModeWasOn());
    }

    @Test
    public void test_WiFi_WasOff() {
        assertTrue(attendance.isWifiWasOff());
    }

    @Test
    public void test_MobileData_WasOff() {
        assertTrue(attendance.isMobileDataWasOff());
    }

    @Test
    public void test_ValidAttendance_Initially() {
        assertTrue(attendance.isValidAttendance());
    }

    @Test
    public void test_DetermineFinalStatus_Present() {
        attendance.setWentOnlineAt(0);
        attendance.determineFinalStatus();
        assertEquals("present", attendance.getStatus());
    }

    @Test
    public void test_DetermineFinalStatus_Absent() {
        long now = System.currentTimeMillis();
        attendance.setSessionEndTime(now + 60000);
        attendance.setWentOnlineAt(now + 30000);
        attendance.determineFinalStatus();
        assertEquals("absent", attendance.getStatus());
    }

    @Test
    public void test_LocationAbsent_SetsReason() {
        attendance.setLocationCaptured(true);
        attendance.setInsideLocationBoundary(false);
        attendance.setWentOnlineAt(0);
        attendance.determineFinalStatus();
        assertEquals(
                "Outside classroom boundary",
                attendance.getLocationAbsentReason()
        );
    }

    @Test
    public void test_MarkSynced_Works() {
        attendance.setSynced(true);
        assertTrue(attendance.isSynced());
    }

    @Test
    public void test_NetworkInfo_CanBeSet() {
        attendance.setNetworkTypeWhenOnline("WIFI");
        attendance.setWentOnlineBeforeTimeout(true);
        assertEquals("WIFI",
                attendance.getNetworkTypeWhenOnline());
        assertTrue(attendance.isWentOnlineBeforeTimeout());
    }
}