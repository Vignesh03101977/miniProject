package com.onetap.app.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.onetap.app.models.OfflineAttendance;

import java.util.List;

@Dao
public interface OfflineAttendanceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(OfflineAttendance attendance);

    @Update
    void update(OfflineAttendance attendance);

    @Delete
    void delete(OfflineAttendance attendance);

    @Query("SELECT * FROM offline_attendance WHERE synced = 0 ORDER BY markedAt DESC")
    List<OfflineAttendance> getUnsyncedAttendances();

    @Query("SELECT * FROM offline_attendance WHERE studentUid = :studentUid ORDER BY markedAt DESC")
    List<OfflineAttendance> getAttendancesByStudent(String studentUid);

    @Query("SELECT * FROM offline_attendance WHERE sessionId = :sessionId ORDER BY markedAt DESC")
    List<OfflineAttendance> getAttendancesBySession(String sessionId);

    @Query("SELECT * FROM offline_attendance ORDER BY markedAt DESC")
    List<OfflineAttendance> getAllAttendances();

    @Query("UPDATE offline_attendance SET synced = 1 WHERE localId = :localId")
    void markAsSynced(long localId);

    @Query("SELECT * FROM offline_attendance WHERE sessionCode = :sessionCode AND studentUid = :studentUid LIMIT 1")
    OfflineAttendance checkExistingAttendance(String sessionCode, String studentUid);

    @Query("SELECT * FROM offline_attendance WHERE localId = :localId LIMIT 1")
    OfflineAttendance getById(long localId);

    @Query("SELECT COUNT(*) FROM offline_attendance WHERE synced = 0")
    int getUnsyncedCount();

    @Query("UPDATE offline_attendance SET " +
            "wentOnlineAt = :timestamp, " +
            "networkTypeWhenOnline = :networkType, " +
            "wentOnlineBeforeTimeout = :beforeTimeout, " +
            "status = :status, " +
            "isValidAttendance = :isValid " +
            "WHERE localId = :localId")
    void updateNetworkChangeInfo(long localId, long timestamp, String networkType,
                                 boolean beforeTimeout, String status, boolean isValid);

    @Query("SELECT * FROM offline_attendance WHERE synced = 0 AND wentOnlineAt = 0")
    List<OfflineAttendance> getPendingAttendances();

    @Query("DELETE FROM offline_attendance WHERE synced = 1")
    void deleteSyncedRecords();
}