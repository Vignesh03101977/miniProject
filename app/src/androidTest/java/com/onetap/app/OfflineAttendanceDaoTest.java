package com.onetap.app;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.onetap.app.database.AppDatabase;
import com.onetap.app.database.OfflineAttendanceDao;
import com.onetap.app.models.OfflineAttendance;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class OfflineAttendanceDaoTest {

    private AppDatabase database;
    private OfflineAttendanceDao dao;

    @Before
    public void setUp() {
        // Create in-memory database (no real data affected)
        Context context = ApplicationProvider
                .getApplicationContext();
        database = Room.inMemoryDatabaseBuilder(
                context,
                AppDatabase.class
        ).allowMainThreadQueries().build();
        dao = database.offlineAttendanceDao();
    }

    @After
    public void tearDown() {
        // Close database after each test
        database.close();
    }

    // Helper method to create test attendance
    private OfflineAttendance createTestAttendance(
            String code, String uid) {
        OfflineAttendance att = new OfflineAttendance(
                "session123",
                "Math Class",
                "Mathematics",
                code,
                "STU001",
                "Pavan Kumar",
                uid,
                "teacher001",
                "CSE",
                System.currentTimeMillis() + 300000,
                true, true, true
        );
        return att;
    }

    // ─────────────────────────────
    // TEST 1: Insert and Retrieve
    // ─────────────────────────────
    @Test
    public void test_Insert_RecordSaved() {
        OfflineAttendance att = createTestAttendance(
                "ABC123", "uid001"
        );
        long id = dao.insert(att);
        assertTrue("Insert should return positive ID", id > 0);
    }

    // ─────────────────────────────
    // TEST 2: Check Existing Attendance
    // ─────────────────────────────
    @Test
    public void test_CheckExisting_Found() {
        OfflineAttendance att = createTestAttendance(
                "XYZ789", "uid002"
        );
        dao.insert(att);

        OfflineAttendance existing =
                dao.checkExistingAttendance("XYZ789", "uid002");
        assertNotNull(
                "Should find existing attendance",
                existing
        );
    }

    // ─────────────────────────────
    // TEST 3: No Duplicate Attendance
    // ─────────────────────────────
    @Test
    public void test_NoDuplicate_SameCodeSameStudent() {
        OfflineAttendance att = createTestAttendance(
                "DUP123", "uid003"
        );
        dao.insert(att);

        OfflineAttendance duplicate =
                dao.checkExistingAttendance("DUP123", "uid003");
        assertNotNull(
                "Duplicate check should return record",
                duplicate
        );
    }

    // ─────────────────────────────
    // TEST 4: Get Unsynced Count
    // ─────────────────────────────
    @Test
    public void test_UnsyncedCount_CorrectNumber() {
        dao.insert(createTestAttendance("CODE1", "uid004"));
        dao.insert(createTestAttendance("CODE2", "uid005"));

        int count = dao.getUnsyncedCount();
        assertEquals(
                "Should have 2 unsynced records",
                2, count
        );
    }

    // ─────────────────────────────
    // TEST 5: Mark As Synced
    // ─────────────────────────────
    @Test
    public void test_MarkSynced_UpdatesRecord() {
        OfflineAttendance att = createTestAttendance(
                "SYNC1", "uid006"
        );
        long id = dao.insert(att);
        dao.markAsSynced(id);

        int count = dao.getUnsyncedCount();
        assertEquals(
                "Synced count should be 0",
                0, count
        );
    }

    // ─────────────────────────────
    // TEST 6: Get All Attendances
    // ─────────────────────────────
    @Test
    public void test_GetAll_ReturnsAllRecords() {
        dao.insert(createTestAttendance("ALL1", "uid007"));
        dao.insert(createTestAttendance("ALL2", "uid008"));
        dao.insert(createTestAttendance("ALL3", "uid009"));

        List<OfflineAttendance> all = dao.getAllAttendances();
        assertEquals(
                "Should return 3 records",
                3, all.size()
        );
    }

    // ─────────────────────────────
    // TEST 7: Delete Synced Records
    // ─────────────────────────────
    @Test
    public void test_DeleteSynced_ClearsRecords() {
        OfflineAttendance att = createTestAttendance(
                "DEL1", "uid010"
        );
        long id = dao.insert(att);
        dao.markAsSynced(id);
        dao.deleteSyncedRecords();

        List<OfflineAttendance> all = dao.getAllAttendances();
        assertEquals(
                "All synced records should be deleted",
                0, all.size()
        );
    }

    // ─────────────────────────────
    // TEST 8: Update Network Info
    // ─────────────────────────────
    @Test
    public void test_UpdateNetworkInfo_ChangesStatus() {
        OfflineAttendance att = createTestAttendance(
                "NET1", "uid011"
        );
        long id = dao.insert(att);

        long now = System.currentTimeMillis();
        dao.updateNetworkChangeInfo(
                id, now, "WIFI", true, "absent", false
        );

        OfflineAttendance updated = dao.getById(id);
        assertEquals(
                "Status should be absent",
                "absent",
                updated.getStatus()
        );
    }
}