package in.lubble.app;

import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

@RunWith(AndroidJUnit4.class)
public class ChatMessagesTest {

    @Rule
    public ActivityTestRule<MainActivity> chatActivityActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void openGroup_typeMessage_msgShown() throws Exception {
        String groupName = BuildConfig.DEBUG ? "My Lubble" : "My Saraswati Vihar";
        onView(withId(R.id.rv_groups)).perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(groupName)), click()));
        onView(withId(R.id.et_new_message)).perform(typeText("hello m typing"));
        onView(withId(R.id.iv_send_btn)).perform(click());
        onView(withId(R.id.rv_chat)).check(matches(hasDescendant(withText("hello m typing"))));
    }

}
