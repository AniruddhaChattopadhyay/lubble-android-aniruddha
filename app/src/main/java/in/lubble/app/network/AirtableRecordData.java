package in.lubble.app.network;

import com.google.gson.annotations.SerializedName;

import in.lubble.app.models.ChatMoreData;

public class AirtableRecordData {

    @SerializedName("fields")
    private ChatMoreData chatMoreData;

    public ChatMoreData getChatMoreData() {
        return chatMoreData;
    }

    public void setChatMoreData(ChatMoreData chatMoreData) {
        this.chatMoreData = chatMoreData;
    }

}
