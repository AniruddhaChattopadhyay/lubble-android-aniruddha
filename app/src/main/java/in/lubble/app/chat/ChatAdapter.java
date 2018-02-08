package in.lubble.app.chat;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.util.ArrayList;

import in.lubble.app.GlideApp;
import in.lubble.app.R;
import in.lubble.app.models.ChatData;
import in.lubble.app.profile.ProfileActivity;

import static in.lubble.app.Constants.DEFAULT_LUBBLE;
import static in.lubble.app.utils.StringUtils.isValidString;

/**
 * Created by ishaan on 21/1/18.
 */

public class ChatAdapter extends RecyclerView.Adapter {

    private static final String TAG = "ChatAdapter";
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
        sentChatViewHolder.lubbCount.setText(String.valueOf(chatData.getLubbCount()));
        if (chatData.getLubbers().containsKey(FirebaseAuth.getInstance().getUid())) {
            sentChatViewHolder.lubbIcon.setImageResource(R.drawable.ic_favorite_24dp);
        } else {
            sentChatViewHolder.lubbIcon.setImageResource(R.drawable.ic_favorite_border_24dp);
        }

        handleImage(sentChatViewHolder.chatIv, chatData);

    }

    private void bindRecvdChatViewHolder(RecyclerView.ViewHolder holder, int position) {
        RecvdChatViewHolder recvdChatViewHolder = (RecvdChatViewHolder) holder;
        ChatData chatData = chatDataList.get(position);

        recvdChatViewHolder.authorNameTv.setText(chatData.getAuthorName());
        recvdChatViewHolder.messageTv.setText(chatData.getMessage());
        recvdChatViewHolder.lubbCount.setText(String.valueOf(chatData.getLubbCount()));
        if (chatData.getLubbers().containsKey(FirebaseAuth.getInstance().getUid())) {
            recvdChatViewHolder.lubbIcon.setImageResource(R.drawable.ic_favorite_24dp);
        } else {
            recvdChatViewHolder.lubbIcon.setImageResource(R.drawable.ic_favorite_border_24dp);
        }

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

    public void updateChatData(@NonNull ChatData chatData) {
        final int pos = chatDataList.indexOf(chatData);
        chatDataList.set(pos, chatData);
        notifyItemChanged(pos);
    }

    @Override
    public int getItemCount() {
        return chatDataList.size();
    }

    private void toggleLubb(int pos) {
        FirebaseDatabase.getInstance().getReference("messages/lubbles/" + DEFAULT_LUBBLE + "/groups/0/").child(chatDataList.get(pos).getId())
                .runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        ChatData chatData = mutableData.getValue(ChatData.class);
                        if (chatData == null) {
                            return Transaction.success(mutableData);
                        }

                        final String uid = FirebaseAuth.getInstance().getUid();
                        if (chatData.getLubbers().containsKey(uid)) {
                            // Unstar the message and remove self from lubbs
                            chatData.setLubbCount(chatData.getLubbCount() - 1);
                            chatData.getLubbers().remove(uid);
                        } else {
                            // Star the message and add self to lubbs
                            chatData.setLubbCount(chatData.getLubbCount() + 1);
                            chatData.getLubbers().put(uid, true);
                        }

                        // Set value and report transaction success
                        mutableData.setValue(chatData);
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                        // Transaction completed
                        Log.d(TAG, "postTransaction:onComplete:" + databaseError);
                    }
                });
    }

    public class RecvdChatViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView authorNameTv;
        private TextView messageTv;
        private ImageView chatIv;
        private LinearLayout lubbContainer;
        private ImageView lubbIcon;
        private TextView lubbCount;

        public RecvdChatViewHolder(View itemView) {
            super(itemView);
            authorNameTv = itemView.findViewById(R.id.tv_author);
            messageTv = itemView.findViewById(R.id.tv_message);
            chatIv = itemView.findViewById(R.id.iv_chat_img);
            lubbContainer = itemView.findViewById(R.id.linearLayout_lubb_container);
            lubbIcon = itemView.findViewById(R.id.iv_lubb);
            lubbCount = itemView.findViewById(R.id.tv_lubb_count);
            authorNameTv.setOnClickListener(this);
            lubbContainer.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.tv_author:
                    ProfileActivity.open(context, chatDataList.get(getAdapterPosition()).getAuthorUid());
                    break;
                case R.id.linearLayout_lubb_container:
                    toggleLubb(getAdapterPosition());
                    break;
            }
        }
    }

    public class SentChatViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView messageTv;
        private ImageView chatIv;
        private LinearLayout lubbContainer;
        private ImageView lubbIcon;
        private TextView lubbCount;

        public SentChatViewHolder(View itemView) {
            super(itemView);
            messageTv = itemView.findViewById(R.id.tv_message);
            chatIv = itemView.findViewById(R.id.iv_chat_img);
            lubbContainer = itemView.findViewById(R.id.linearLayout_lubb_container);
            lubbIcon = itemView.findViewById(R.id.iv_lubb);
            lubbCount = itemView.findViewById(R.id.tv_lubb_count);

            lubbContainer.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            toggleLubb(getAdapterPosition());
        }
    }

}
