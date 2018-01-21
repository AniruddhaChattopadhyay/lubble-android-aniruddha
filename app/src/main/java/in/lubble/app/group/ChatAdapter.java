package in.lubble.app.group;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import in.lubble.app.R;
import in.lubble.app.models.ChatData;

/**
 * Created by ishaan on 21/1/18.
 */

public class ChatAdapter extends RecyclerView.Adapter {

    private Context context;
    private ArrayList<ChatData> chatDataList;

    public ChatAdapter(Context context, ArrayList<ChatData> chatDataList) {
        this.context = context;
        this.chatDataList = chatDataList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ChatViewHolder(LayoutInflater.from(context).inflate(R.layout.item_chat, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        ChatViewHolder chatViewHolder = (ChatViewHolder) holder;
        ChatData chatData = chatDataList.get(position);

        chatViewHolder.authorNameTv.setText(chatData.getAuthorName());
        chatViewHolder.messageTv.setText(chatData.getMessage());
    }

    @Override
    public int getItemCount() {
        return chatDataList.size();
    }

    public class ChatViewHolder extends RecyclerView.ViewHolder {

        private TextView authorNameTv;
        private TextView messageTv;

        public ChatViewHolder(View itemView) {
            super(itemView);
            authorNameTv = itemView.findViewById(R.id.tv_author);
            messageTv = itemView.findViewById(R.id.tv_message);
        }

    }

}
