package com.chitrahaar.darshan;

import android.content.pm.ActivityInfo;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.AllOf.allOf;

/**
 * Created by obelix on 29/11/2016.
 */

@RunWith(AndroidJUnit4.class)
public class DeviceRotationTest {


    /** Launches {@link MainActivity} for every test */
    @Rule
    public final ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(MainActivity.class);

    /** Checks that what has been typed into EditText is retained after screen rotation */
    @Test
    public void whenDeviceRotates_sameTextInputIsRetained() {
        //GIVEN
        onView(withId(R.id.spinner)).perform(click());
        //WHEN

        onData(allOf(is(instanceOf(String.class)), containsString("Top Rated"))).perform(click());

        rotateDevice();
        //THEN
        onView(withId(R.id.spinner)).check(matches(withSpinnerText(containsString("Top Rated"))));
    }

    private void rotateDevice() {
        activityRule.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

}
