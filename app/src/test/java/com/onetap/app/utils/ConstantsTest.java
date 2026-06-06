package com.onetap.app.utils;

import com.onetap.app.utils.Constants;

import org.junit.Test;
import static org.junit.Assert.*;

public class ConstantsTest {

    @Test
    public void test_AllRoles_Defined() {
        assertNotNull(Constants.ROLE_STUDENT);
        assertNotNull(Constants.ROLE_TEACHER);
        assertNotNull(Constants.ROLE_ADMIN);
    }

    @Test
    public void test_AllStatuses_Defined() {
        assertNotNull(Constants.STATUS_APPROVED);
        assertNotNull(Constants.STATUS_PENDING);
        assertNotNull(Constants.STATUS_REJECTED);
    }

    @Test
    public void test_DatabasePaths_Defined() {
        assertNotNull(Constants.DB_USERS);
        assertNotNull(Constants.DB_SESSIONS);
        assertNotNull(Constants.DB_ATTENDANCE);
    }

    @Test
    public void test_SessionCodeLength_IsSix() {
        assertEquals(6, Constants.SESSION_CODE_LENGTH);
    }

    @Test
    public void test_AdminEmail_IsValid() {
        assertTrue(Constants.ADMIN_EMAIL.contains("@"));
    }

    @Test
    public void test_PrefKeys_AllDefined() {
        assertNotNull(Constants.PREF_NAME);
        assertNotNull(Constants.PREF_USER_ROLE);
        assertNotNull(Constants.PREF_USER_NAME);
        assertNotNull(Constants.PREF_USER_EMAIL);
        assertNotNull(Constants.PREF_IS_LOGGED_IN);
    }
}