package io.michaelrocks.libphonenumber.android;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import io.michaelrocks.libphonenumber.android.sample.MainActivity;
import io.michaelrocks.libphonenumber.android.sample.R;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityTest {

  @Rule
  public ActivityScenarioRule<MainActivity> rule = new ActivityScenarioRule<>(MainActivity.class);

  @Test
  public void checkTextViewContainsEnabledFeatures() {
    onView(withId(R.id.button))
        .perform(click());
    onView(withId(R.id.textView))
        .check(matches(withText("+1 800-555-1212")));
  }
}
