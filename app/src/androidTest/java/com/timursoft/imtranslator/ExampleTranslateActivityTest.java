package com.timursoft.imtranslator;

import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import android.widget.VideoView;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.RecyclerViewActions.scrollToPosition;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ExampleTranslateActivityTest {

    @Rule
    public ActivityTestRule<ExampleTranslateActivity> activityRule =
            new ActivityTestRule<>(ExampleTranslateActivity.class);

    @Test
    public void play_button() throws Exception {
        onView(withId(R.id.ic_play_pause)).perform(click());

        onView(withId(R.id.ic_play_pause)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }

    @Test
    public void scrollRecyclerAndCheckVideoPosition() throws Exception {
        onView(withId(R.id.recycler_view)).perform(scrollToPosition(10));

        onView(withId(R.id.video_view)).check(matches(hasCurrentTime(is(28000))));
    }

    private static Matcher<View> hasCurrentTime(final Matcher<Integer> integerMatcher) {
        return new BoundedMatcher<View, VideoView>(VideoView.class) {
            int currentPosition = 0;

            @Override
            public boolean matchesSafely(VideoView view) {
                currentPosition = view.getCurrentPosition();
                return integerMatcher.matches(currentPosition);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("with time: ");
                integerMatcher.describeTo(description);
                description.appendText(", but got: " + currentPosition);
            }
        };
    }
}