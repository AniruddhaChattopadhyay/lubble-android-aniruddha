package in.lubble.app;

import android.content.Intent;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.matcher.ViewMatchers.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class GroupsTest {

    @Rule
    public ActivityTestRule<MainActivity> chatActivityActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void clickGroup_opensGroupScreen() throws Exception {
        String groupName = BuildConfig.DEBUG ? "My Lubble" : "My Saraswati Vihar";
        onView(withId(R.id.rv_groups)).perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(groupName)), click()));
        onView(withId(R.id.tv_toolbar_title)).check(matches(ViewMatchers.withText(groupName)));
    }

    @Test
    public void clickNewGroup_opensNewGroupScreen() throws Exception {
        onView(withId(R.id.container_create_group)).perform(click());
        onView(withId(R.id.et_group_title)).check(matches(isDisplayed()));
    }

    @Test
    public void clickSlider_intentSent() throws Exception {
        Intents.init();
        onView(withId(R.id.viewpager)).perform(click());
        intended(hasAction(Intent.ACTION_VIEW));
        Intents.release();
    }

}
