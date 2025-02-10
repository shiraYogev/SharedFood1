package com.example.sharedfood;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UITests {
    @Rule
    public ActivityScenarioRule<LoginActivity> loginActivityRule =
            new ActivityScenarioRule<>(LoginActivity.class);

    @Rule
    public ActivityScenarioRule<SignUpActivity> signUpActivityRule =
            new ActivityScenarioRule<>(SignUpActivity.class);

    // מבחן למסך ההרשמה
    @Test
    public void test1_SignUp() {
        // Start at MainActivity
        ActivityScenario.launch(MainActivity.class);

        // Ensure the activity is started
        onView(withId(R.id.main)).check(matches(isDisplayed()));

        // Attempt to click on the sign-up button
        onView(withId(R.id.signUpButton)).perform(click());


        // הזנת כתובת אימייל
        onView(withId(R.id.emailEditText))
                .perform(typeText("newuser@example.com"), closeSoftKeyboard());

        // הזנת סיסמה
        onView(withId(R.id.passwordEditText))
                .perform(typeText("password123"), closeSoftKeyboard());

        // הזנת סיסמה לאישור
        onView(withId(R.id.confirmPasswordEditText))
                .perform(typeText("password123"), closeSoftKeyboard());

        // סימון תיבת הסימון של תנאי השימוש
        onView(withId(R.id.termsCheckBox)).perform(click());

        // לחיצה על כפתור ההרשמה
        onView(withId(R.id.signUpButton)).perform(click());

        onView(withText("Registration successful"))
                .inRoot(new ToastMatcher())
                .check(matches(withText("Registration successful")));

    }

    // מבחן למסך ההתחברות
    @Test
    public void test2_Login() {
        // הזנת כתובת אימייל
        onView(withId(R.id.emailEditText))
                .perform(typeText("daniel@gmail.com"), closeSoftKeyboard());

        // הזנת סיסמה
        onView(withId(R.id.passwordEditText))
                .perform(typeText("123456"), closeSoftKeyboard());

        // לחיצה על כפתור ההתחברות
        onView(withId(R.id.loginButton)).perform(click());

        // בדיקה שהמסך הראשי מוצג לאחר התחברות מוצלחת
        onView(withId(R.id.main)).check(matches(isDisplayed()));
    }


}
