package in.lubble.app.group;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

import in.lubble.app.GlideApp;
import in.lubble.app.R;
import in.lubble.app.models.ChatData;

import static in.lubble.app.utils.StringUtils.isValidString;

/**
 * Created by ishaan on 21/1/18.
 */

public class ChatAdapter extends RecyclerView.Adapter {

    private static final int TYPE_RECEIVED = 0;
    private static final int TYPE_SENT = 1;

    private Context context;
    private ArrayList<ChatData> chatDataList;

    public ChatAdapter(Context context, ArrayList<ChatData> chatDataList) {
        this.context = context;
        this.chatDataList = chatDataList;
    }

    @Override
    public int getItemViewType(int position) {
        final ChatData chatData = chatDataList.get(position);
        if (chatData.getAuthorUid().equalsIgnoreCase(FirebaseAuth.getInstance().getUid())) {
            return TYPE_SENT;
        }
        return TYPE_RECEIVED;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_RECEIVED:
                return new RecvdChatViewHolder(LayoutInflater.from(context).inflate(R.layout.item_recvd_chat, parent, false));
            case TYPE_SENT:
                return new SentChatViewHolder(LayoutInflater.from(context).inflate(R.layout.item_sent_chat, parent, false));
            default:
                return new RecvdChatViewHolder(LayoutInflater.from(context).inflate(R.layout.item_recvd_chat, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SentChatViewHolder) {
            bindSentChatViewHolder(holder, position);
        } else {
            bindRecvdChatViewHolder(holder, position);
        }
    }

    private void bindSentChatViewHolder(RecyclerView.ViewHolder holder, int position) {
        final SentChatViewHolder sentChatViewHolder = (SentChatViewHolder) holder;
        ChatData chatData = chatDataList.get(position);

        sentChatViewHolder.messageTv.setText(chatData.getMessage());

        handleImage(sentChatViewHolder.chatIv, chatData);

    }

    private void bindRecvdChatViewHolder(RecyclerView.ViewHolder holder, int position) {
        RecvdChatViewHolder recvdChatViewHolder = (RecvdChatViewHolder) holder;
        ChatData chatData = chatDataList.get(position);

        recvdChatViewHolder.authorNameTv.setText(chatData.getAuthorName());
        recvdChatViewHolder.messageTv.setText(chatData.getMessage());

        handleImage(recvdChatViewHolder.chatIv, chatData);

    }

    private void handleImage(ImageView imageView, ChatData chatData) {
        if (isValidString(chatData.getImgUrl())) {
            imageView.setVisibility(View.VISIBLE);
            GlideApp.with(context)
                    .load(chatData.getImgUrl())
                    .centerCrop()
                    //.signature(new ObjectKey(imageFile.length() + "@" + imageFile.lastModified())) // What ? Why?
                    .into(imageView);
        } else {
            imageView.setVisibility(View.GONE);
        }
    }

    public void addChatData(@NonNull ChatData chatData) {
        final int size = chatDataList.size();
        chatDataList.add(chatData);
        notifyItemInserted(size + 1);
    }

    @Override
    public int getItemCount() {
        return chatDataList.size();
    }

    public class RecvdChatViewHolder extends RecyclerView.ViewHolder {

        private TextView authorNameTv;
        private TextView messageTv;
        private ImageView chatIv;

        public RecvdChatViewHolder(View itemView) {
            super(itemView);
            authorNameTv = itemView.findViewById(R.id.tv_author);
            messageTv = itemView.findViewById(R.id.tv_message);
            chatIv = itemView.findViewById(R.id.iv_chat_img);
        }

    }

    public class SentChatViewHolder extends RecyclerView.ViewHolder {

        private TextView messageTv;
        private ImageView chatIv;

        public SentChatViewHolder(View itemView) {
            super(itemView);
            messageTv = itemView.findViewById(R.id.tv_message);
            chatIv = itemView.findViewById(R.id.iv_chat_img);
        }

    }

}
