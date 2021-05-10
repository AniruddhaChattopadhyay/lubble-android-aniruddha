package in.lubble.app.feed_user;

import io.getstream.core.models.Reaction;

public interface ReplyListener {
    void onReplied(String activityId, Reaction reaction);
    void onDismissed();
}
