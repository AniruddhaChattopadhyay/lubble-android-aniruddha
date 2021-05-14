package in.lubble.app.feed_user;

import io.getstream.core.models.Reaction;

public interface ReplyListener {
    void onReplied(String activityId, String foreignId, Reaction reaction);
    void onDismissed();
}
