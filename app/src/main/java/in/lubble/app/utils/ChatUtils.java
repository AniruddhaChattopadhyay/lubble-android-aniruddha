package in.lubble.app.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ServerValue;
import in.lubble.app.models.ChatData;

import static in.lubble.app.models.ChatData.REPLY;

public class ChatUtils {

    public static ChatData createGroupChatdata(String message) {
        final ChatData chatData = new ChatData();
        chatData.setAuthorUid(FirebaseAuth.getInstance().getUid());
        chatData.setMessage(message);
        chatData.setCreatedTimestamp(System.currentTimeMillis());
        chatData.setServerTimestamp(ServerValue.TIMESTAMP);
        chatData.setIsDm(false);
        return chatData;
    }

    public static ChatData createReplyChatdata(String message, String replyMsgId) {
        final ChatData chatData = new ChatData();
        chatData.setAuthorUid(FirebaseAuth.getInstance().getUid());
        chatData.setMessage(message);
        chatData.setCreatedTimestamp(System.currentTimeMillis());
        chatData.setServerTimestamp(ServerValue.TIMESTAMP);
        chatData.setIsDm(false);
        chatData.setType(REPLY);
        chatData.setReplyMsgId(replyMsgId);
        return chatData;
    }

}
