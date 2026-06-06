package com.onetap.app;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.onetap.app.activities.LoginActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class LoginScreenTest {

    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule =
            new ActivityScenarioRule<>(LoginActivity.class);

    // ─────────────────────────────
    // TEST 1: Login Button Visible
    // ─────────────────────────────
    @Test
    public void test_LoginButton_IsVisible() {
        Espresso.onView(ViewMatchers.withId(R.id.btnLogin))
                .check(ViewAssertions.matches(
                        ViewMatchers.isDisplayed()
                ));
    }

    // ─────────────────────────────
    // TEST 2: Empty Fields Error
    // ─────────────────────────────
    @Test
    public void test_EmptyFields_ShowsError() {
        Espresso.onView(ViewMatchers.withId(R.id.btnLogin))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.etEmail))
                .check(ViewAssertions.matches(
                        ViewMatchers.hasErrorText("Enter a valid email")
                ));
    }

    // ─────────────────────────────
    // TEST 3: Invalid Email Error
    // ─────────────────────────────
    @Test
    public void test_InvalidEmail_ShowsError() {
        Espresso.onView(ViewMatchers.withId(R.id.etEmail))
                .perform(ViewActions.typeText("invalidemail"));

        Espresso.onView(ViewMatchers.withId(R.id.btnLogin))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.etEmail))
                .check(ViewAssertions.matches(
                        ViewMatchers.hasErrorText("Enter a valid email")
                ));
    }

    // ─────────────────────────────
    // TEST 4: Short Password Error
    // ─────────────────────────────
    @Test
    public void test_ShortPassword_ShowsError() {
        Espresso.onView(ViewMatchers.withId(R.id.etEmail))
                .perform(ViewActions.typeText("test@test.com"));

        Espresso.onView(ViewMatchers.withId(R.id.etPassword))
                .perform(ViewActions.typeText("123"));

        Espresso.onView(ViewMatchers.withId(R.id.btnLogin))
                .perform(ViewActions.click());

        Espresso.onView(ViewMatchers.withId(R.id.etPassword))
                .check(ViewAssertions.matches(
                        ViewMatchers.hasErrorText(
                                "Password must be at least 6 characters"
                        )
                ));
    }
}