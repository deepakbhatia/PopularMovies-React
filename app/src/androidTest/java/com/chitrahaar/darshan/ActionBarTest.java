package com.chitrahaar.darshan;

import android.content.res.Resources;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by obelix on 29/11/2016.
 */
@RunWith(AndroidJUnit4.class)

public class ActionBarTest {

    /** Launches {@link MainActivity} for every test */
    @Rule
    public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(MainActivity.class);

    @SuppressWarnings("unchecked")
    @Test
    public void testActionBarOverflow() throws Exception {

        Resources resources = getInstrumentation().getTargetContext().getResources();
        int actionBarId = resources.getIdentifier("toolbar", "id", "com.chitrahaar.darshan");
        onView(withId(actionBarId)).check(matches(isDisplayed()));

        // Open the overflow menu OR open the options menu,
        // depending on if the device has a hardware or software overflow menu button.
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        // Click the item.
        onView(withText("Refresh")).perform(click());

    }

}
