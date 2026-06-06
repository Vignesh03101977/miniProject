package com.onetap.app.database;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.onetap.app.models.OfflineAttendance;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class OfflineAttendanceDaoTest {

    private AppDatabase database;
    private OfflineAttendanceDao dao;

    @Before
    public void setUp() {
        Context context = ApplicationProvider
                .getApplicationContext();
        database = Room.inMemoryDatabaseBuilder(
                        context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        dao = database.offlineAttendanceDao();
    }

    @After
    public void tearDown() {
        database.close();
    }

    private OfflineAttendance createAttendance(
            String code, String uid) {
        return new OfflineAttendance(
                "session001", "Math Class",
                "Mathematics", code,
                "STU001", "Pavan Kumar",
                uid, "teacher001", "CSE",
                System.currentTimeMillis() + 300000,
                true, true, true
        );
    }

    @Test
    public void test_Insert_ReturnsPositiveId() {
        long id = dao.insert(
                createAttendance("ABC123", "uid001")
        );
        assertTrue(id > 0);
    }

    @Test
    public void test_CheckExisting_ReturnsRecord() {
        dao.insert(createAttendance("XYZ789", "uid002"));
        OfflineAttendance result =
                dao.checkExistingAttendance("XYZ789", "uid002");
        assertNotNull(result);
    }

    @Test
    public void test_CheckExisting_ReturnsNull() {
        OfflineAttendance result =
                dao.checkExistingAttendance("NONE", "none");
        assertNull(result);
    }

    @Test
    public void test_UnsyncedCount_IsCorrect() {
        dao.insert(createAttendance("C001", "u001"));
        dao.insert(createAttendance("C002", "u002"));
        dao.insert(createAttendance("C003", "u003"));
        assertEquals(3, dao.getUnsyncedCount());
    }

    @Test
    public void test_MarkSynced_DecreasesCount() {
        long id = dao.insert(
                createAttendance("S001", "u004")
        );
        dao.markAsSynced(id);
        assertEquals(0, dao.getUnsyncedCount());
    }

    @Test
    public void test_GetAll_ReturnsCorrectSize() {
        dao.insert(createAttendance("A001", "u005"));
        dao.insert(createAttendance("A002", "u006"));
        List<OfflineAttendance> all =
                dao.getAllAttendances();
        assertEquals(2, all.size());
    }

    @Test
    public void test_DeleteSynced_RemovesRecords() {
        long id = dao.insert(
                createAttendance("D001", "u007")
        );
        dao.markAsSynced(id);
        dao.deleteSyncedRecords();
        assertEquals(0, dao.getAllAttendances().size());
    }

    @Test
    public void test_UpdateNetworkInfo_ChangesStatus() {
        long id = dao.insert(
                createAttendance("N001", "u008")
        );
        dao.updateNetworkChangeInfo(
                id,
                System.currentTimeMillis(),
                "WIFI",
                true,
                "absent",
                false
        );
        OfflineAttendance updated = dao.getById(id);
        assertEquals("absent", updated.getStatus());
    }

    @Test
    public void test_GetById_ReturnsCorrectRecord() {
        long id = dao.insert(
                createAttendance("G001", "u009")
        );
        OfflineAttendance record = dao.getById(id);
        assertNotNull(record);
        assertEquals("G001", record.getSessionCode());
    }

    @Test
    public void test_GetUnsynced_ReturnsOnlyUnsynced() {
        long id = dao.insert(
                createAttendance("U001", "u010")
        );
        dao.insert(createAttendance("U002", "u011"));
        dao.markAsSynced(id);

        List<OfflineAttendance> unsynced =
                dao.getUnsyncedAttendances();
        assertEquals(1, unsynced.size());
    }
}