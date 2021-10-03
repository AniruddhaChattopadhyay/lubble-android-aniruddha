package in.lubble.app.utils;

import android.net.Uri;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ServerValue;

import java.util.Map;

import in.lubble.app.models.ChatData;

import static in.lubble.app.models.ChatData.REPLY;

public class ChatUtils {

    public static void addAuthorNameandDp(ChatData chatData, String userFlair) {
        chatData.setAuthorName(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        Uri uri = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl();
        if (uri != null) {
            chatData.setAuthorDpUrl(uri.toString());
        }
        chatData.setFlair(userFlair);
    }

    public static String getKeyByValue(Map<String, String> map, String value) {
        if (!map.isEmpty()) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                if (entry.getValue().contains(value)) {
                    return entry.getKey();
                }
            }
        }
        return "";
    }

}
