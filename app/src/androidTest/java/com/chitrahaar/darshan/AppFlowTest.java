package com.chitrahaar.darshan;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.LayoutAssertions.noOverlaps;
import static android.support.test.espresso.assertion.PositionAssertions.isAbove;
import static android.support.test.espresso.assertion.PositionAssertions.isBelow;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.StringContains.containsString;

/**
 * Created by obelix on 29/11/2016.
 */
@RunWith(AndroidJUnit4.class)

public class AppFlowTest {

    /** Launches {@link MainActivity} for every test */
    @Rule
    public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void clickPosterImage() throws Exception {

        //assertThat();
        onData(anything())
                .inAdapterView(withId(R.id.movies_list))
                .atPosition(7)
                .perform(click());

        onView(withId(R.id.movie_favourite)).check(matches(isDisplayed()));

        onView(withId(R.id.movie_favourite)).check(matches(withText(containsString("Remove"))));//Must Pass

        //onView(withId(R.id.movie_favourite)).perform(click());

        onView(withId(R.id.movie_favourite)).check(matches(withText(containsString("Add"))));//Must fail if above is commented

        testRelativePositionOfViews();//Check Layouts of Detail Activity

        testLayout();

        onView(withContentDescription("Navigate up")).perform(click());

        onView(withId(R.id.movies_list)).check(matches(isDisplayed()));

        testMovietags();//Check Type of Movies Shown, Popular, Top Rated, Favourite
    }

    @Test
    private void testLayout() {
        // Test initial layout state
        assertNoLayoutBreakages();
        // Fine-grained test: assert that Wrap button is not wrapped
        // at this point.
        //onView(withId(R.id.movie_title)).check(matches(not(hasMultilineText())));

        // Now Wrap button should be wrapped
        //onView(withId(R.id.movie_overview)).check(matches(hasMultilineText()));
        // Long text should be ellipsized
        //onView(withId(R.id.ellipsized)).check(matches(hasEllipsizedText()));
        // Test final layout state
        assertNoLayoutBreakages();
    }
    @Test
    private void testRelativePositionOfViews() {
        onView(withId(R.id.movie_rating)).check(isBelow(withId(R.id.movie_image)));
        onView(withId(R.id.movie_release))
                .check(isAbove(withId(R.id.movie_overview)));
    }
    private void assertNoLayoutBreakages() {
        // Elements should not overlap
        onView(isRoot()).check(noOverlaps());
        // Buttons should not be ellipsized, though may wrap
        //onView(isRoot()).check(selectedDescendantsMatch(isAssignableFrom(Button.class), not(hasEllipsizedText())));
        // Add other suitable generic checks, e.g. Accessibility, below
    }
    @Test
    private void testMovietags() throws Exception{
        onView(withId(R.id.spinner)).perform(click());

        //openContextualActionModeOverflowMenu();

        onData(allOf(is(instanceOf(String.class)), containsString("Top Rated"))).perform(click());

        onView(withId(R.id.spinner)).check(matches(withSpinnerText(containsString("Top Rated"))));
    }
}
