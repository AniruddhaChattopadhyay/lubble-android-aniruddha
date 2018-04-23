package in.lubble.app.chat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import in.lubble.app.GlideApp;
import in.lubble.app.R;
import in.lubble.app.models.ChatData;
import in.lubble.app.profile.ProfileActivity;
import in.lubble.app.utils.DateTimeUtils;
import in.lubble.app.utils.FullScreenImageActivity;

import static in.lubble.app.firebase.RealtimeDbHelper.getMessagesRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getUserInfoRef;
import static in.lubble.app.models.ChatData.HIDDEN;
import static in.lubble.app.models.ChatData.LINK;
import static in.lubble.app.models.ChatData.SYSTEM;
import static in.lubble.app.models.ChatData.UNREAD;
import static in.lubble.app.utils.StringUtils.isValidString;

/**
 * Created by ishaan on 21/1/18.
 */

public class ChatAdapter extends RecyclerView.Adapter {

    private static final String TAG = "ChatAdapter";
    private static final int TYPE_RECEIVED = 0;
    private static final int TYPE_SENT = 1;
    private static final int TYPE_SYSTEM = 2;
    private static final int TYPE_UNREAD = 3;

    private Activity activity;
    private Context context;
    private RecyclerView recyclerView;
    private ArrayList<ChatData> chatDataList;

    public ChatAdapter(Activity activity, Context context, ArrayList<ChatData> chatDataList, RecyclerView recyclerView) {
        this.activity = activity;
        this.context = context;
        this.chatDataList = chatDataList;
        this.recyclerView = recyclerView;
    }

    @Override
    public int getItemViewType(int position) {
        final ChatData chatData = chatDataList.get(position);
        if (isValidString(chatData.getType()) && chatData.getType().equalsIgnoreCase(SYSTEM)) {
            return TYPE_SYSTEM;
        } else if (isValidString(chatData.getType()) && chatData.getType().equalsIgnoreCase(UNREAD)) {
            return TYPE_UNREAD;
        } else if (chatData.getAuthorUid().equalsIgnoreCase(FirebaseAuth.getInstance().getUid())) {
            return TYPE_SENT;
        } else {
            return TYPE_RECEIVED;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_RECEIVED:
                return new RecvdChatViewHolder(LayoutInflater.from(context).inflate(R.layout.item_recvd_chat, parent, false));
            case TYPE_SENT:
                return new SentChatViewHolder(LayoutInflater.from(context).inflate(R.layout.item_sent_chat, parent, false));
            case TYPE_SYSTEM:
                return new SystemChatViewHolder(LayoutInflater.from(context).inflate(R.layout.item_system, parent, false));
            case TYPE_UNREAD:
                return new UnreadChatViewHolder(LayoutInflater.from(context).inflate(R.layout.item_unread, parent, false));
            default:
                return new RecvdChatViewHolder(LayoutInflater.from(context).inflate(R.layout.item_recvd_chat, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SentChatViewHolder) {
            bindSentChatViewHolder(holder, position);
        } else if (holder instanceof SystemChatViewHolder) {
            bindSystemViewHolder(holder, position);
        } else if (holder instanceof UnreadChatViewHolder) {
            // no data to bind to any view
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

        sentChatViewHolder.dateTv.setText(DateTimeUtils.getTimeFromLong(chatData.getCreatedTimestamp()));
        if (chatData.getType().equalsIgnoreCase(LINK)) {
            sentChatViewHolder.linkContainer.setVisibility(View.VISIBLE);
            sentChatViewHolder.linkTitleTv.setText(chatData.getLinkTitle());
            sentChatViewHolder.linkDescTv.setText(chatData.getLinkDesc());
        } else {
            sentChatViewHolder.linkContainer.setVisibility(View.GONE);
        }

        handleImage(sentChatViewHolder.imgContainer, sentChatViewHolder.progressBar, sentChatViewHolder.chatIv, chatData);

    }

    private void bindRecvdChatViewHolder(RecyclerView.ViewHolder holder, int position) {
        final RecvdChatViewHolder recvdChatViewHolder = (RecvdChatViewHolder) holder;
        ChatData chatData = chatDataList.get(position);

        if (isValidString(chatData.getMessage())) {
            recvdChatViewHolder.messageTv.setVisibility(View.VISIBLE);
            recvdChatViewHolder.messageTv.setText(chatData.getMessage());
        } else {
            recvdChatViewHolder.messageTv.setVisibility(View.GONE);
        }
        recvdChatViewHolder.dateTv.setText(DateTimeUtils.getTimeFromLong(chatData.getCreatedTimestamp()));
        recvdChatViewHolder.lubbCount.setText(String.valueOf(chatData.getLubbCount()));
        if (chatData.getLubbers().containsKey(FirebaseAuth.getInstance().getUid())) {
            recvdChatViewHolder.lubbIcon.setImageResource(R.drawable.ic_favorite_24dp);
        } else {
            recvdChatViewHolder.lubbIcon.setImageResource(R.drawable.ic_favorite_border_24dp);
        }

        if (chatData.getType().equalsIgnoreCase(LINK)) {
            recvdChatViewHolder.linkContainer.setVisibility(View.VISIBLE);
            recvdChatViewHolder.linkTitleTv.setText(chatData.getLinkTitle());
            recvdChatViewHolder.linkDescTv.setText(chatData.getLinkDesc());
        } else {
            recvdChatViewHolder.linkContainer.setVisibility(View.GONE);
        }

        handleImage(recvdChatViewHolder.imgContainer, recvdChatViewHolder.progressBar, recvdChatViewHolder.chatIv, chatData);

        showDpAndName(recvdChatViewHolder, chatData);
    }

    private void bindSystemViewHolder(RecyclerView.ViewHolder holder, int position) {
        final SystemChatViewHolder systemChatViewHolder = (SystemChatViewHolder) holder;
        ChatData chatData = chatDataList.get(position);
        if (chatData.getType().equalsIgnoreCase(SYSTEM)) {
            systemChatViewHolder.messageTv.setText(chatData.getMessage());
        }
    }

    private void showDpAndName(final RecvdChatViewHolder recvdChatViewHolder, ChatData chatData) {
        // single as its very difficult otherwise to keep track of all listeners for every user
        // plus we don't really need realtime updation of user DP and/or name in chat
        getUserInfoRef(chatData.getAuthorUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, String> map = (HashMap<String, String>) dataSnapshot.getValue();
                if (map != null && recvdChatViewHolder.itemView.getContext() != null) {
                    GlideApp.with(recvdChatViewHolder.itemView.getContext())
                            .load(map.get("thumbnail"))
                            .circleCrop()
                            .placeholder(R.drawable.ic_account_circle_black_no_padding)
                            .error(R.drawable.ic_account_circle_black_no_padding)
                            .into(recvdChatViewHolder.dpIv);
                    recvdChatViewHolder.authorNameTv.setText(map.get("name"));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void handleImage(FrameLayout imgContainer, final ProgressBar progressBar, final ImageView imageView, final ChatData chatData) {
        if (isValidString(chatData.getImgUrl())) {
            imageView.setOnClickListener(null);
            imgContainer.setVisibility(View.VISIBLE);
            GlideApp.with(context)
                    .load(chatData.getImgUrl())
                    .centerCrop()
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            imageView.setOnClickListener(null);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);

                            imageView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (isValidString(chatData.getImgUrl())) {
                                        FullScreenImageActivity.open(activity, context, chatData.getImgUrl(), imageView, null);
                                    }
                                }
                            });
                            return false;
                        }
                    })
                    .into(imageView);
        } else {
            imgContainer.setVisibility(View.GONE);
        }
    }

    public void addChatData(@NonNull ChatData chatData) {
        if (!chatData.getType().equalsIgnoreCase(HIDDEN)) {
            final int size = chatDataList.size();
            chatDataList.add(chatData);
            notifyItemInserted(size);
        }
    }

    public void addChatData(int pos, @NonNull ChatData chatData) {
        if (!chatData.getType().equalsIgnoreCase(HIDDEN)) {
            chatDataList.add(pos, chatData);
            notifyItemInserted(pos);
        }
    }

    public void updateChatData(@NonNull ChatData chatData) {
        if (!chatData.getType().equalsIgnoreCase(HIDDEN)) {
            final int pos = chatDataList.indexOf(chatData);
            if (pos != -1) {
                chatDataList.set(pos, chatData);
                notifyItemChanged(pos);
            }
        }
    }

    public ChatData getChatMsgAt(int pos) {
        return chatDataList.get(pos);
    }

    @Override
    public int getItemCount() {
        return chatDataList.size();
    }

    private void toggleLubb(int pos, String groupId) {
        getMessagesRef().child(groupId).child(chatDataList.get(pos).getId())
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
        private LinearLayout linkContainer;
        private TextView linkTitleTv;
        private TextView linkDescTv;
        private FrameLayout imgContainer;
        private ProgressBar progressBar;
        private ImageView chatIv;
        private TextView dateTv;
        private LinearLayout lubbContainer;
        private ImageView lubbIcon;
        private TextView lubbCount;
        private ImageView dpIv;

        public RecvdChatViewHolder(View itemView) {
            super(itemView);
            authorNameTv = itemView.findViewById(R.id.tv_author);
            messageTv = itemView.findViewById(R.id.tv_message);
            linkContainer = itemView.findViewById(R.id.link_meta_container);
            linkTitleTv = itemView.findViewById(R.id.tv_link_title);
            linkDescTv = itemView.findViewById(R.id.tv_link_desc);
            imgContainer = itemView.findViewById(R.id.img_container);
            progressBar = itemView.findViewById(R.id.progressbar_img);
            chatIv = itemView.findViewById(R.id.iv_chat_img);
            dateTv = itemView.findViewById(R.id.tv_date);
            lubbContainer = itemView.findViewById(R.id.linearLayout_lubb_container);
            lubbIcon = itemView.findViewById(R.id.iv_lubb);
            lubbCount = itemView.findViewById(R.id.tv_lubb_count);
            dpIv = itemView.findViewById(R.id.iv_dp);
            dpIv.setOnClickListener(this);
            lubbContainer.setOnClickListener(this);
            chatIv.setOnClickListener(null);
            linkContainer.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.iv_dp:
                    ProfileActivity.open(context, chatDataList.get(getAdapterPosition()).getAuthorUid());
                    break;
                case R.id.linearLayout_lubb_container:
                    //toggleLubb(getAdapterPosition(), chatDataList.get(getAdapterPosition()).getId());
                    break;
                case R.id.link_meta_container:
                    final URLSpan[] urls = messageTv.getUrls();
                    final String url = urls[0].getURL();
                    if (isValidString(url)) {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        context.startActivity(i);
                    }
                    break;
            }
        }
    }

    public class SentChatViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView messageTv;
        private LinearLayout linkContainer;
        private TextView linkTitleTv;
        private TextView linkDescTv;
        private FrameLayout imgContainer;
        private ProgressBar progressBar;
        private ImageView chatIv;
        private TextView dateTv;
        private LinearLayout lubbContainer;
        private ImageView lubbIcon;
        private TextView lubbCount;

        SentChatViewHolder(View itemView) {
            super(itemView);
            messageTv = itemView.findViewById(R.id.tv_message);
            linkContainer = itemView.findViewById(R.id.link_meta_container);
            linkTitleTv = itemView.findViewById(R.id.tv_link_title);
            linkDescTv = itemView.findViewById(R.id.tv_link_desc);
            imgContainer = itemView.findViewById(R.id.img_container);
            progressBar = itemView.findViewById(R.id.progressbar_img);
            chatIv = itemView.findViewById(R.id.iv_chat_img);
            dateTv = itemView.findViewById(R.id.tv_date);
            lubbContainer = itemView.findViewById(R.id.linearLayout_lubb_container);
            lubbIcon = itemView.findViewById(R.id.iv_lubb);
            lubbCount = itemView.findViewById(R.id.tv_lubb_count);
            linkContainer.setOnClickListener(this);
            lubbContainer.setOnClickListener(this);
            chatIv.setOnClickListener(null);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.linearLayout_lubb_container:
                    //toggleLubb(getAdapterPosition(), chatDataList.get(getAdapterPosition()).getId());
                    break;
                case R.id.link_meta_container:
                    final URLSpan[] urls = messageTv.getUrls();
                    final String url = urls[0].getURL();
                    if (isValidString(url)) {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        context.startActivity(i);
                    }
                    break;
            }
        }
    }

    public class SystemChatViewHolder extends RecyclerView.ViewHolder {

        private TextView messageTv;

        SystemChatViewHolder(View itemView) {
            super(itemView);
            messageTv = itemView.findViewById(R.id.tv_system_msg);
        }
    }

    public class UnreadChatViewHolder extends RecyclerView.ViewHolder {

        UnreadChatViewHolder(View itemView) {
            super(itemView);
        }
    }

}
