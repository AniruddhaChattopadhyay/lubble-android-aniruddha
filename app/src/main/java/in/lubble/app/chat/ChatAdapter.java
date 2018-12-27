package in.lubble.app.chat;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.core.content.ContextCompat;
import androidx.emoji.widget.EmojiTextView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.crashlytics.android.Crashlytics;
import com.google.android.youtube.player.YouTubeIntents;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import in.lubble.app.GlideApp;
import in.lubble.app.GlideRequests;
import in.lubble.app.LubbleApp;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;
import in.lubble.app.models.ChatData;
import in.lubble.app.models.ChoiceData;
import in.lubble.app.models.ProfileInfo;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import in.lubble.app.profile.ProfileActivity;
import in.lubble.app.utils.*;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.HashMap;

import static in.lubble.app.firebase.RealtimeDbHelper.*;
import static in.lubble.app.models.ChatData.*;
import static in.lubble.app.utils.FileUtils.deleteCache;
import static in.lubble.app.utils.FileUtils.getSavedImageForMsgId;
import static in.lubble.app.utils.StringUtils.extractFirstLink;
import static in.lubble.app.utils.StringUtils.isValidString;
import static in.lubble.app.utils.UiUtils.dpToPx;
import static in.lubble.app.utils.YoutubeUtils.extractYoutubeId;

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
    private final GlideRequests glide;
    private RecyclerView recyclerView;
    private ArrayList<ChatData> chatDataList;
    private ChatFragment chatFragment;
    private String selectedChatId = null;
    @Nullable
    private String groupId;
    private int highlightedPos = -1;
    private int posToFlash = -1;
    private boolean shownLubbHintForLastMsg;
    private static HashMap<String, ProfileInfo> profileInfoMap = new HashMap<>();
    private String authorId = FirebaseAuth.getInstance().getUid();
    @Nullable
    private String dmId;// Allows to remember the last item shown on screen

    public ChatAdapter(Activity activity, Context context, String groupId,
                       RecyclerView recyclerView, ChatFragment chatFragment, GlideRequests glide) {
        this.chatDataList = new ArrayList<>();
        this.activity = activity;
        this.context = context;
        this.groupId = groupId;
        this.recyclerView = recyclerView;
        this.chatFragment = chatFragment;
        this.glide = glide;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public void setDmId(@NonNull String dmId) {
        this.dmId = dmId;
    }

    @Override
    public int getItemViewType(int position) {
        final ChatData chatData = chatDataList.get(position);
        if (isValidString(chatData.getType()) && chatData.getType().equalsIgnoreCase(SYSTEM)) {
            return TYPE_SYSTEM;
        } else if (isValidString(chatData.getType()) && chatData.getType().equalsIgnoreCase(UNREAD)) {
            return TYPE_UNREAD;
        } else if (chatData.getAuthorUid().equalsIgnoreCase(authorId)) {
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

        if (posToFlash == position) {
            UiUtils.animateColor(sentChatViewHolder.itemView, ContextCompat.getColor(context, R.color.trans_colorAccent), Color.TRANSPARENT);
            posToFlash = -1;
        } else {
            sentChatViewHolder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }

        if (highlightedPos == position) {
            sentChatViewHolder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.trans_colorAccent));
            sentChatViewHolder.lubbPopOutContainer.setVisibility(View.VISIBLE);
            toggleLubbPopOutContainer(sentChatViewHolder.lubbIv,
                    sentChatViewHolder.lubbHintTv,
                    chatData.getLubbReceipts().containsKey(authorId));
        } else {
            sentChatViewHolder.itemView.setBackgroundColor(Color.TRANSPARENT);
            sentChatViewHolder.lubbPopOutContainer.setVisibility(View.GONE);
        }

        if (isValidString(chatData.getMessage())) {
            sentChatViewHolder.messageTv.setVisibility(View.VISIBLE);
            sentChatViewHolder.messageTv.setText(chatData.getMessage());
        } else {
            sentChatViewHolder.messageTv.setVisibility(View.GONE);
        }
        sentChatViewHolder.lubbCount.setText(String.valueOf(chatData.getLubbCount()));
        if (chatData.getLubbReceipts().containsKey(authorId)) {
            sentChatViewHolder.lubbIcon.setImageResource(R.drawable.ic_favorite_24dp);
            if (position == chatDataList.size() - 1) {
                // scroll to bottom if liked last msg to show that like icon and count
                recyclerView.smoothScrollToPosition(chatDataList.size() - 1 > -1 ? chatDataList.size() - 1 : 0);
            }
        } else {
            sentChatViewHolder.lubbIcon.setImageResource(R.drawable.ic_favorite_border_24dp);
        }

        sentChatViewHolder.dateTv.setText(DateTimeUtils.getTimeFromLong(chatData.getServerTimestampInLong()));
        if (chatData.getType().equalsIgnoreCase(LINK)) {
            sentChatViewHolder.linkContainer.setVisibility(View.VISIBLE);
            sentChatViewHolder.linkTitleTv.setText(chatData.getLinkTitle());
            sentChatViewHolder.linkDescTv.setText(chatData.getLinkDesc());
        } else if (chatData.getType().equalsIgnoreCase(REPLY) && isValidString(chatData.getReplyMsgId())) {
            sentChatViewHolder.linkContainer.setVisibility(View.VISIBLE);
            addReplyData(chatData.getReplyMsgId(), sentChatViewHolder.linkTitleTv, sentChatViewHolder.linkDescTv);
            /*sentChatViewHolder.itemView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    // to have the link container fill whole chat bubble
                    sentChatViewHolder.itemView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    final int textWidth = sentChatViewHolder.textContainer.getWidth();
                    if (textWidth > sentChatViewHolder.linkContainer.getWidth()) {
                        final ViewGroup.LayoutParams layoutParams = sentChatViewHolder.linkContainer.getLayoutParams();
                        layoutParams.width = textWidth;
                        sentChatViewHolder.linkContainer.setLayoutParams(layoutParams);
                    }
                }
            });*/
        } else {
            sentChatViewHolder.linkContainer.setVisibility(View.GONE);
        }

        handleImage(sentChatViewHolder.imgContainer, sentChatViewHolder.progressBar, sentChatViewHolder.chatIv, chatData, null);

        sentChatViewHolder.lubbHeadsContainer.setVisibility(chatData.getLubbCount() > 0 ? View.VISIBLE : View.GONE);
        sentChatViewHolder.lubbContainer.setVisibility(chatData.getLubbCount() > 0 ? View.VISIBLE : View.GONE);

        int i = 0;
        sentChatViewHolder.lubbHeadsContainer.removeAllViews();
        for (String uid : chatData.getLubbReceipts().keySet()) {
            if (i++ < 4) {
                // show a max of 4 heads
                // todo sort?
                if (profileInfoMap.containsKey(uid)) {
                    final ImageView lubbHeadIv = new ImageView(context);
                    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(dpToPx(16), dpToPx(16));
                    lubbHeadIv.setLayoutParams(lp);
                    glide.load(profileInfoMap.get(uid).getThumbnail())
                            .placeholder(R.drawable.ic_account_circle_black_no_padding)
                            .circleCrop()
                            .into(lubbHeadIv);
                    sentChatViewHolder.lubbHeadsContainer.addView(lubbHeadIv);
                } else {
                    updateProfileInfoMap(getUserInfoRef(uid), uid, sentChatViewHolder.getAdapterPosition());
                }
            } else {
                break;
            }
        }

        handleYoutube(sentChatViewHolder, chatData.getMessage(), position);

        if (chatData.getType().equalsIgnoreCase(ChatData.POLL) && chatData.getChoiceList() != null && !chatData.getChoiceList().isEmpty()) {
            sentChatViewHolder.messageTv.setVisibility(View.GONE);
            sentChatViewHolder.pollContainer.setVisibility(View.VISIBLE);
            ((TextView) sentChatViewHolder.pollContainer.findViewById(R.id.tv_poll_ques)).setText(chatData.getPollQues());

            showPollResults(sentChatViewHolder, chatData);

        } else {
            sentChatViewHolder.pollContainer.setVisibility(View.GONE);
        }
    }

    private void bindRecvdChatViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final RecvdChatViewHolder recvdChatViewHolder = (RecvdChatViewHolder) holder;
        ChatData chatData = chatDataList.get(position);

        if (!chatData.getIsDm()) {
            showDpAndName(recvdChatViewHolder, chatData);
        } else {
            recvdChatViewHolder.authorNameTv.setVisibility(View.GONE);
            recvdChatViewHolder.dpIv.setVisibility(View.GONE);
        }

        if (posToFlash == position) {
            UiUtils.animateColor(recvdChatViewHolder.itemView, ContextCompat.getColor(context, R.color.trans_colorAccent), Color.TRANSPARENT);
            posToFlash = -1;
        } else {
            recvdChatViewHolder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }

        if (highlightedPos == position) {
            recvdChatViewHolder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.trans_colorAccent));
            recvdChatViewHolder.lubbPopOutContainer.setVisibility(View.VISIBLE);
            toggleLubbPopOutContainer(recvdChatViewHolder.lubbIv,
                    recvdChatViewHolder.lubbHintTv,
                    chatData.getLubbReceipts().containsKey(authorId));
        } else {
            recvdChatViewHolder.itemView.setBackgroundColor(Color.TRANSPARENT);
            recvdChatViewHolder.lubbPopOutContainer.setVisibility(View.GONE);

        }

        if (isValidString(chatData.getMessage())) {
            recvdChatViewHolder.messageTv.setVisibility(View.VISIBLE);
            recvdChatViewHolder.messageTv.setText(chatData.getMessage());
        } else {
            recvdChatViewHolder.messageTv.setVisibility(View.GONE);
        }
        recvdChatViewHolder.dateTv.setText(DateTimeUtils.getTimeFromLong(chatData.getServerTimestampInLong()));
        recvdChatViewHolder.lubbCount.setText(String.valueOf(chatData.getLubbCount()));
        if (chatData.getLubbReceipts().containsKey(authorId)) {
            recvdChatViewHolder.lubbIcon.setImageResource(R.drawable.ic_favorite_24dp);
            if (position == chatDataList.size() - 1) {
                // scroll to bottom if liked last msg to show that like icon and count
                recyclerView.smoothScrollToPosition(chatDataList.size() - 1 > -1 ? chatDataList.size() - 1 : 0);
            }
        } else {
            recvdChatViewHolder.lubbIcon.setImageResource(R.drawable.ic_favorite_border_24dp);
        }

        if (chatData.getType().equalsIgnoreCase(LINK)) {
            recvdChatViewHolder.linkContainer.setVisibility(View.VISIBLE);
            recvdChatViewHolder.linkTitleTv.setText(chatData.getLinkTitle());
            recvdChatViewHolder.linkDescTv.setText(chatData.getLinkDesc());
        } else if (chatData.getType().equalsIgnoreCase(REPLY) && isValidString(chatData.getReplyMsgId())) {
            recvdChatViewHolder.linkContainer.setVisibility(View.VISIBLE);
            addReplyData(chatData.getReplyMsgId(), recvdChatViewHolder.linkTitleTv, recvdChatViewHolder.linkDescTv);
            /*recvdChatViewHolder.itemView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    // to have the link container fill whole chat bubble
                    recvdChatViewHolder.itemView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    final int textWidth = recvdChatViewHolder.textContainer.getWidth();
                    if (textWidth > recvdChatViewHolder.linkContainer.getWidth()) {
                        final ViewGroup.LayoutParams layoutParams = recvdChatViewHolder.linkContainer.getLayoutParams();
                        layoutParams.width = textWidth;
                        recvdChatViewHolder.linkContainer.setLayoutParams(layoutParams);
                    } else {
                        final ViewGroup.LayoutParams layoutParams = recvdChatViewHolder.linkContainer.getLayoutParams();
                        layoutParams.width = recvdChatViewHolder.linkTitleTv.getWidth() > recvdChatViewHolder.linkDescTv.getWidth()
                                ? recvdChatViewHolder.linkTitleTv.getWidth()
                                : recvdChatViewHolder.linkDescTv.getWidth();
                        recvdChatViewHolder.linkContainer.setLayoutParams(layoutParams);
                    }
                }
            });*/
        } else {
            recvdChatViewHolder.linkContainer.setVisibility(View.GONE);
        }

        handleImage(recvdChatViewHolder.imgContainer, recvdChatViewHolder.progressBar, recvdChatViewHolder.chatIv, chatData, recvdChatViewHolder.downloadIv);
        showLubbHintIfLastMsg(position, chatData, recvdChatViewHolder);
        handleYoutube(recvdChatViewHolder, chatData.getMessage(), position);

        if (chatData.getType().equalsIgnoreCase(ChatData.POLL) && chatData.getChoiceList() != null && !chatData.getChoiceList().isEmpty()) {
            recvdChatViewHolder.messageTv.setVisibility(View.GONE);
            recvdChatViewHolder.pollContainer.setVisibility(View.VISIBLE);
            ((TextView) recvdChatViewHolder.pollContainer.findViewById(R.id.tv_poll_ques)).setText(chatData.getPollQues());

            if (chatData.getPollReceipts().containsKey(FirebaseAuth.getInstance().getUid())) {
                // this user has voted. Show results UI
                showPollResults(recvdChatViewHolder, chatData);
            } else {
                showPollButtons(recvdChatViewHolder, chatData);
            }
        } else {
            recvdChatViewHolder.pollContainer.setVisibility(View.GONE);
        }

    }

    private void showPollButtons(final RecyclerView.ViewHolder baseViewHolder, final ChatData chatData) {
        final LinearLayout pollContainer;
        if (baseViewHolder instanceof RecvdChatViewHolder) {
            pollContainer = ((RecvdChatViewHolder) baseViewHolder).pollContainer;
        } else {
            pollContainer = ((SentChatViewHolder) baseViewHolder).pollContainer;
        }
        pollContainer.findViewById(R.id.container_poll_results).setVisibility(View.GONE);
        setPollCount(chatData, pollContainer);
        final LinearLayout buttonsContainer = pollContainer.findViewById(R.id.container_poll_btns);
        buttonsContainer.setVisibility(View.VISIBLE);
        if (buttonsContainer.getChildCount() > 0) {
            buttonsContainer.removeAllViews();
        }
        final ArrayList<ChoiceData> choiceList = chatData.getChoiceList();
        for (int i = 0; i < choiceList.size(); i++) {
            ChoiceData choiceData = choiceList.get(i);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View btnContainer = inflater.inflate(R.layout.layout_poll_choice_btn, null);
            final EmojiTextView choiceBtnTv = btnContainer.findViewById(R.id.tv_poll_btn);
            choiceBtnTv.setText(choiceData.getTitle());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, dpToPx(4), 0, 0);
            choiceBtnTv.setLayoutParams(params);
            buttonsContainer.addView(btnContainer);
            choiceBtnTv.setTag(i);

            choiceBtnTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    if (groupId != null) {
                        DatabaseReference pollRef = getMessagesRef().child(groupId).child(chatData.getId())/*.child("choiceList").child(String.valueOf(v.getTag()))*/;
                        pollRef.runTransaction(new Transaction.Handler() {
                            @Override
                            public Transaction.Result doTransaction(MutableData mutableData) {
                                ChatData chatData = mutableData.getValue(ChatData.class);
                                if (chatData == null) {
                                    return Transaction.success(mutableData);
                                }
                                final ChoiceData choiceData1 = chatData.getChoiceList().get((Integer) v.getTag());
                                choiceData1.setCount(choiceData1.getCount() + 1);
                                chatData.getPollReceipts().put(FirebaseAuth.getInstance().getUid(), (Integer) v.getTag());
                                // Set value and report transaction success
                                mutableData.setValue(chatData);
                                return Transaction.success(mutableData);
                            }

                            @Override
                            public void onComplete(DatabaseError databaseError, boolean isCompleted, DataSnapshot dataSnapshot) {
                                // Transaction completed
                                Log.d(TAG, "postTransaction:onComplete:" + databaseError);
                                if (isCompleted) {
                                    showPollResults(baseViewHolder, chatData);
                                } else {
                                    if (databaseError != null) {
                                        Crashlytics.logException(new Exception("poll button click error code: " + databaseError.getCode()));
                                        Toast.makeText(context, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(context, R.string.all_try_again, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    private void showPollResults(RecyclerView.ViewHolder baseViewHolder, ChatData chatData) {
        final LinearLayout pollContainer;
        if (baseViewHolder instanceof RecvdChatViewHolder) {
            pollContainer = ((RecvdChatViewHolder) baseViewHolder).pollContainer;
        } else {
            pollContainer = ((SentChatViewHolder) baseViewHolder).pollContainer;
        }
        setPollCount(chatData, pollContainer);
        final LinearLayout resultsView = pollContainer.findViewById(R.id.container_poll_results);
        resultsView.setVisibility(View.VISIBLE);
        pollContainer.findViewById(R.id.container_poll_btns).setVisibility(View.GONE);
        if (resultsView.getChildCount() > 0) {
            resultsView.removeAllViews();
        }
        @Nullable Integer votedIndex = chatData.getPollReceipts().get(FirebaseAuth.getInstance().getUid());
        // add choices with bar graph
        ArrayList<ChoiceData> choiceList = chatData.getChoiceList();
        for (int i = 0; i < choiceList.size(); i++) {
            ChoiceData choiceData = choiceList.get(i);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View choiceContainer = inflater.inflate(R.layout.layout_poll_choice, null);
            final EmojiTextView choiceTv = choiceContainer.findViewById(R.id.tv_choice_text);
            choiceTv.setText(choiceData.getTitle());
            int percent = chatData.getPollReceipts().size() == 0 ? 0 : (int) ((((double) choiceData.getCount() / (double) chatData.getPollReceipts().size())) * 100);
            final TextView percentTv = choiceContainer.findViewById(R.id.tv_choice_percent);
            percentTv.setText(percent + "%");
            if (percent > 0) {
                final View choiceBackground = choiceContainer.findViewById(R.id.iv_choice_background);
                final LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) choiceBackground.getLayoutParams();
                layoutParams.weight = percent;
                choiceBackground.setLayoutParams(layoutParams);
                if (votedIndex != null && votedIndex == i) {
                    // tint the choice voted by user to differentiate it from rest of the choices
                    choiceTv.setTypeface(choiceTv.getTypeface(), Typeface.BOLD);
                    percentTv.setTypeface(percentTv.getTypeface(), Typeface.BOLD);
                    ((ImageView) choiceBackground).setColorFilter(ContextCompat.getColor(context, R.color.trans_colorAccent), android.graphics.PorterDuff.Mode.SRC_IN);
                }
            } else {
                // percent is ZERO
                choiceTv.setBackground(ContextCompat.getDrawable(context, R.drawable.rounded_rect_trans_border));
                choiceContainer.findViewById(R.id.iv_choice_background).setVisibility(View.GONE);
            }


            resultsView.addView(choiceContainer);
        }
    }

    private void setPollCount(ChatData chatData, LinearLayout pollContainer) {
        final TextView voteCountTv = pollContainer.findViewById(R.id.tv_poll_vote_count);
        final int voteCount = chatData.getPollReceipts().size();
        String voteCountStr = "No votes";
        if (voteCount > 0) {
            voteCountStr = context.getResources().getQuantityString(R.plurals.vote_count, voteCount, voteCount);
        }
        voteCountTv.setText(voteCountStr);
    }

    private void handleYoutube(RecyclerView.ViewHolder baseViewHolder, final String message, final int position) {
        /**
         * init
         */
        final ImageView youTubeThumbnailIv;
        final ImageView youtubePlayIv;
        final ProgressBar youtubeProgressBar;
        LinearLayout linkContainer;
        final RelativeLayout youtubeContainer;
        final TextView titleTv;
        if (baseViewHolder instanceof RecvdChatViewHolder) {
            final RecvdChatViewHolder recvdChatViewHolder = (RecvdChatViewHolder) baseViewHolder;
            youTubeThumbnailIv = recvdChatViewHolder.youtubeThumbnailView;
            youtubePlayIv = recvdChatViewHolder.youtubePlayIv;
            youtubeProgressBar = recvdChatViewHolder.youtubeProgressBar;
            linkContainer = recvdChatViewHolder.linkContainer;
            youtubeContainer = recvdChatViewHolder.youtubeContainer;
            titleTv = recvdChatViewHolder.youtubeTitleTv;
        } else {
            final SentChatViewHolder sentChatViewHolder = (SentChatViewHolder) baseViewHolder;
            youTubeThumbnailIv = sentChatViewHolder.youtubeThumbnailView;
            youtubePlayIv = sentChatViewHolder.youtubePlayIv;
            youtubeProgressBar = sentChatViewHolder.youtubeProgressBar;
            linkContainer = sentChatViewHolder.linkContainer;
            youtubeContainer = sentChatViewHolder.youtubeContainer;
            titleTv = sentChatViewHolder.youtubeTitleTv;
        }
        youtubeProgressBar.setVisibility(View.GONE);
        /**
         * logic
         */
        final String extractedLink = extractFirstLink(message);
        if (!TextUtils.isEmpty(extractedLink) && extractYoutubeId(extractedLink) != null) {
            // has a youtube link
            // STOP VIDEO - SHOW THUMBNAIL
            youtubeContainer.setVisibility(View.VISIBLE);
            youTubeThumbnailIv.setVisibility(View.VISIBLE);
            linkContainer.setVisibility(View.GONE);
            youtubeProgressBar.setVisibility(View.VISIBLE);

            final Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
            endpoints.getYoutubeData("https://www.youtube.com/oembed?url=" + extractedLink).enqueue(new Callback<YoutubeData>() {
                @Override
                public void onResponse(Call<YoutubeData> call, Response<YoutubeData> response) {
                    final YoutubeData youtubeData = response.body();
                    if (response.isSuccessful() && youtubeData != null) {
                        Log.d(TAG, "onResponse: ");
                        youtubePlayIv.setVisibility(View.VISIBLE);
                        glide.load(youtubeData.getThumbnailUrl()).listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                youtubeProgressBar.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                youtubeProgressBar.setVisibility(View.GONE);
                                return false;
                            }
                        }).error(R.drawable.rounded_rect_gray)
                                .into(youTubeThumbnailIv);

                        if (!TextUtils.isEmpty(youtubeData.getTitle())) {
                            titleTv.setText(youtubeData.getTitle());
                        }

                    } else if (activity != null && context != null && !activity.isFinishing()) {
                        Crashlytics.log("youtube thumbnail failed to fetch with error code: " + response.code());
                        youtubeProgressBar.setVisibility(View.GONE);
                        youtubePlayIv.setImageResource(R.drawable.ic_error_outline_black_24dp);
                    }
                }

                @Override
                public void onFailure(Call<YoutubeData> call, Throwable t) {
                    if (activity != null && context != null && !activity.isFinishing()) {
                        Log.e(TAG, "onFailure: " + t);
                        youtubeProgressBar.setVisibility(View.GONE);
                        youtubePlayIv.setImageResource(R.drawable.ic_error_outline_black_24dp);
                    }
                }
            });

            youTubeThumbnailIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (YouTubeIntents.isYouTubeInstalled(context) && YouTubeIntents.canResolvePlayVideoIntent(context)) {
                        FullscreenYoutubeActiv.open(context, extractYoutubeId(extractedLink));
                    } else {
                        YoutubeUtils.openYoutubeAppOrWeb(activity, message);
                    }
                }
            });
        } else {
            youtubeContainer.setVisibility(View.GONE);
        }
    }

    private void handleLubbs(RecvdChatViewHolder recvdChatViewHolder, ChatData chatData, boolean toAnimate) {

        if (chatData.getLubbCount() > 0) {
            if (toAnimate) {
                UiUtils.animateSlideDownShow(context, recvdChatViewHolder.lubbContainer);
            } else {
                recvdChatViewHolder.lubbContainer.setVisibility(View.VISIBLE);
            }
        } else {
            recvdChatViewHolder.lubbContainer.setVisibility(View.GONE);
        }

        //recvdChatViewHolder.lubbContainer.setVisibility(chatData.getLubbCount() > 0 ? View.VISIBLE : View.GONE);
        recvdChatViewHolder.lubbHeadsContainer.setVisibility(chatData.getLubbCount() > 0 ? View.VISIBLE : View.GONE);

        int i = 0;
        recvdChatViewHolder.lubbHeadsContainer.removeAllViews();
        for (String uid : chatData.getLubbReceipts().keySet()) {
            if (i++ < 4) {
                // show a max of 4 heads
                // todo sort?
                if (profileInfoMap.containsKey(uid)) {
                    final ImageView lubbHeadIv = new ImageView(context);
                    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(dpToPx(16), dpToPx(16));
                    lubbHeadIv.setLayoutParams(lp);
                    GlideApp.with(context).load(profileInfoMap.get(uid).getThumbnail())
                            .placeholder(R.drawable.ic_account_circle_black_no_padding)
                            .circleCrop()
                            .into(lubbHeadIv);
                    recvdChatViewHolder.lubbHeadsContainer.addView(lubbHeadIv);
                } else {
                    updateProfileInfoMap(getUserInfoRef(uid), uid, recvdChatViewHolder.getAdapterPosition());
                }
            } else {
                break;
            }
        }
    }

    private void showLubbHintIfLastMsg(int position, final ChatData chatData, final RecvdChatViewHolder recvdChatViewHolder) {
        if (position == chatDataList.size() - 1) {
            UiUtils.animateSlideDownShow(context, recvdChatViewHolder.lubbLastHintContainer);
            //recvdChatViewHolder.lubbLastHintContainer.setVisibility(View.VISIBLE);
            recvdChatViewHolder.lubbContainer.setVisibility(View.GONE);
            shownLubbHintForLastMsg = true;
            if (chatData.getLubbCount() > 0) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (chatFragment != null && chatFragment.isAdded() && chatFragment.isVisible()) {
                            //recvdChatViewHolder.lubbLastHintContainer.setVisibility(View.GONE);
                            UiUtils.animateSlideDownHide(context, recvdChatViewHolder.lubbLastHintContainer);
                            handleLubbs(recvdChatViewHolder, chatData, true);
                        }
                    }
                }, 2000);
            }
        } else {
            recvdChatViewHolder.lubbLastHintContainer.setVisibility(View.GONE);
            handleLubbs(recvdChatViewHolder, chatData, false);
        }
    }

    private void updateProfileInfoMap(DatabaseReference userInfoRef, final String uid, final int pos) {
        // single as its very difficult otherwise to keep track of all listeners for every user
        // plus we don't really need realtime updation of user DP and/or name in chat
        userInfoRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final ProfileInfo profileInfo = dataSnapshot.getValue(ProfileInfo.class);
                if (profileInfo != null) {
                    profileInfo.setId(dataSnapshot.getRef().getParent().getKey()); // this works. Don't touch.
                    profileInfoMap.put(profileInfo.getId(), profileInfo);
                    notifyItemChanged(pos);
                } else {
                    updateProfileInfoMap(getSellerInfoRef(uid), uid, pos);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void addReplyData(String replyMsgId, TextView linkTitleTv, TextView linkDescTv) {
        ChatData emptyReplyChatData = new ChatData();
        emptyReplyChatData.setId(replyMsgId);
        int index = chatDataList.indexOf(emptyReplyChatData);
        if (index > -1) {
            ChatData quotedChatData = chatDataList.get(index);
            String desc = "";
            if (isValidString(quotedChatData.getImgUrl())) {
                desc = desc.concat("\uD83D\uDCF7 ");
                if (!isValidString(quotedChatData.getMessage())) {
                    // add the word photo if there is no caption
                    desc = desc.concat("Photo ");
                }
            }
            desc = desc.concat(quotedChatData.getMessage());
            linkDescTv.setText(desc);
            showName(linkTitleTv, quotedChatData.getAuthorUid());
        }
    }

    private void bindSystemViewHolder(RecyclerView.ViewHolder holder, int position) {
        final SystemChatViewHolder systemChatViewHolder = (SystemChatViewHolder) holder;
        ChatData chatData = chatDataList.get(position);
        if (chatData.getType().equalsIgnoreCase(SYSTEM)) {
            systemChatViewHolder.messageTv.setText(chatData.getMessage());
        }
    }

    private void showName(final TextView authorNameTv, String authorUid) {
        getUserInfoRef(authorUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                HashMap<String, String> map = (HashMap<String, String>) dataSnapshot.getValue();
                if (map != null) {
                    authorNameTv.setText(map.get("name"));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void showDpAndName(final RecvdChatViewHolder recvdChatViewHolder, ChatData chatData) {
        if (profileInfoMap.containsKey(chatData.getAuthorUid())) {
            recvdChatViewHolder.authorNameTv.setVisibility(View.VISIBLE);
            recvdChatViewHolder.dpIv.setVisibility(View.VISIBLE);
            final ProfileInfo profileInfo = profileInfoMap.get(chatData.getAuthorUid());
            glide.load(profileInfo.getThumbnail())
                    .circleCrop()
                    .placeholder(R.drawable.ic_account_circle_black_no_padding)
                    .error(R.drawable.ic_account_circle_black_no_padding)
                    .into(recvdChatViewHolder.dpIv);
            recvdChatViewHolder.authorNameTv.setText(profileInfo.getName());
        } else {
            updateProfileInfoMap(getUserInfoRef(chatData.getAuthorUid()), chatData.getAuthorUid(), recvdChatViewHolder.getAdapterPosition());
        }
    }

    private void handleImage(FrameLayout imgContainer, final ProgressBar progressBar, final ImageView imageView, final ChatData chatData, @Nullable ImageView downloadIv) {
        if (isValidString(chatData.getImgUrl())) {
            imageView.setOnClickListener(null);
            imgContainer.setVisibility(View.VISIBLE);
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && downloadIv != null) {
                // Permission is not granted
                glide.load(chatData.getImgUrl()).override(18, 18).diskCacheStrategy(DiskCacheStrategy.NONE).centerCrop().into(imageView);
                downloadIv.setVisibility(View.VISIBLE);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // ask for external storage perm
                        ChatFragmentPermissionsDispatcher
                                .getWritePermWithPermissionCheck(chatFragment);
                    }
                });
            } else {
                if (downloadIv != null) {
                    downloadIv.setVisibility(View.GONE);
                }
                String savedPath = getSavedImageForMsgId(context, chatData.getId());
                if (savedPath != null) {
                    progressBar.setVisibility(View.GONE);
                    glide.load(savedPath).centerCrop().diskCacheStrategy(DiskCacheStrategy.NONE).into(imageView);
                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (isValidString(chatData.getImgUrl())) {
                                FullScreenImageActivity.open(activity, context, chatData.getImgUrl(), imageView, null, R.drawable.ic_cancel_black_24dp);
                            }
                        }
                    });
                } else {
                    downloadAndSavePic(progressBar, imageView, chatData);
                }
            }
        } else {
            imgContainer.setVisibility(View.GONE);
        }
    }

    private void downloadAndSavePic(final ProgressBar progressBar, final ImageView imageView, final ChatData chatData) {
        glide.asBitmap()
                .load(chatData.getImgUrl())
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        FileUtils.saveImageInGallery(resource, chatData.getId(), context);
                        progressBar.setVisibility(View.GONE);
                        glide.load(resource).centerCrop().diskCacheStrategy(DiskCacheStrategy.NONE).into(imageView);
                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (isValidString(chatData.getImgUrl())) {
                                    FullScreenImageActivity.open(activity, context, chatData.getImgUrl(), imageView, null, R.drawable.ic_cancel_black_24dp);
                                }
                            }
                        });
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        progressBar.setVisibility(View.GONE);
                        imageView.setOnClickListener(null);
                    }
                });
    }

    public void addChatData(@NonNull ChatData chatData) {
        if (!chatData.getType().equalsIgnoreCase(HIDDEN)) {
            final int size = chatDataList.size();
            chatDataList.add(chatData);
            if (size - 1 >= 0) {
                // remove the last msg lubb hint
                notifyItemChanged(size - 1);
            }
            notifyItemInserted(size);
        }
    }

    public void addChatData(int pos, @NonNull ChatData chatData) {
        if (!chatData.getType().equalsIgnoreCase(HIDDEN)) {
            chatDataList.add(pos, chatData);
            if (pos - 1 >= 0) {
                // remove the last msg lubb hint
                notifyItemChanged(pos - 1);
            }
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

    public int getIndexOfChatMsg(String msgId) {
        final ChatData chatDataToFind = new ChatData();
        chatDataToFind.setId(msgId);
        return chatDataList.indexOf(chatDataToFind);
    }

    public void setPosToFlash(int pos) {
        this.posToFlash = pos;
    }

    @Override
    public int getItemCount() {
        return chatDataList.size();
    }

    private void toggleLubb(int pos) {
        final ChatData chatData = chatDataList.get(pos);
        DatabaseReference lubbRef;
        if (chatData.getIsDm()) {
            lubbRef = getDmMessagesRef().child(dmId).child(chatData.getId());
        } else {
            lubbRef = getMessagesRef().child(groupId).child(chatData.getId());
        }
        lubbRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                ChatData chatData = mutableData.getValue(ChatData.class);
                if (chatData == null) {
                    return Transaction.success(mutableData);
                }

                final String uid = authorId;
                if (chatData.getLubbReceipts().containsKey(uid)) {
                    // Unstar the message and remove self from lubbs
                    chatData.setLubbCount(chatData.getLubbCount() - 1);
                    chatData.getLubbReceipts().remove(uid);
                } else {
                    // Star the message and add self to lubbs
                    chatData.setLubbCount(chatData.getLubbCount() + 1);
                    chatData.getLubbReceipts().put(uid, System.currentTimeMillis());
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

    void writePermGranted() {
        deleteCache(context);
        Analytics.triggerEvent(AnalyticsEvents.WRITE_PERM_GRANTED, context);
        notifyDataSetChanged();
    }

    public class RecvdChatViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private TextView authorNameTv;
        private EmojiTextView messageTv;
        private LinearLayout linkContainer;
        private TextView linkTitleTv;
        private EmojiTextView linkDescTv;
        private FrameLayout imgContainer;
        private ProgressBar progressBar;
        private ImageView chatIv;
        private TextView dateTv;
        private MsgFlexBoxLayout textContainer;
        private LinearLayout lubbContainer;
        private LinearLayout lubbLastHintContainer;
        private TextView lubbAnyHintTv;
        private ImageView lubbIcon;
        private TextView lubbCount;
        private LinearLayout lubbHeadsContainer;
        private ImageView dpIv;
        private LinearLayout lubbPopOutContainer;
        @Nullable
        private ActionMode actionMode;
        private ImageView lubbIv;
        private TextView lubbHintTv;
        private ImageView youtubeThumbnailView;
        private ProgressBar youtubeProgressBar;
        private ImageView youtubePlayIv;
        private RelativeLayout youtubeContainer;
        private TextView youtubeTitleTv;
        private ImageView downloadIv;
        private LinearLayout pollContainer;

        public RecvdChatViewHolder(final View itemView) {
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
            textContainer = itemView.findViewById(R.id.msgFlexBox_text);
            lubbContainer = itemView.findViewById(R.id.linearLayout_lubb_container);
            lubbLastHintContainer = itemView.findViewById(R.id.linearLayout_lubb_hint_container);
            lubbAnyHintTv = itemView.findViewById(R.id.tv_any_lubb_hint);
            lubbIcon = itemView.findViewById(R.id.iv_lubb);
            lubbCount = itemView.findViewById(R.id.tv_lubb_count);
            lubbHeadsContainer = itemView.findViewById(R.id.linear_layout_lubb_heads);
            dpIv = itemView.findViewById(R.id.iv_dp);
            lubbPopOutContainer = itemView.findViewById(R.id.linear_layout_lubb_pop);
            lubbIv = itemView.findViewById(R.id.iv_lubb_icon);
            lubbHintTv = itemView.findViewById(R.id.tv_lubb_hint);
            youtubeThumbnailView = itemView.findViewById(R.id.youtube_thumbnail_view);
            youtubeProgressBar = itemView.findViewById(R.id.progressbar_youtube);
            youtubePlayIv = itemView.findViewById(R.id.iv_youtube_play);
            youtubeContainer = itemView.findViewById(R.id.relativelayout_youtube);
            youtubeTitleTv = itemView.findViewById(R.id.tv_yt_title);
            downloadIv = itemView.findViewById(R.id.iv_download);
            pollContainer = itemView.findViewById(R.id.container_polls);

            lubbAnyHintTv.setSelected(true);
            lubbAnyHintTv.setHorizontallyScrolling(true);

            lubbPopOutContainer.setOnClickListener(this);
            lubbHeadsContainer.setOnClickListener(this);
            dpIv.setOnClickListener(this);
            lubbContainer.setOnClickListener(this);
            chatIv.setOnClickListener(null);
            linkContainer.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            chatIv.setOnLongClickListener(this);

        }

        private ActionMode.Callback actionModeCallbacks = new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.menu_chat, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_reply:
                        if (null != selectedChatId) {
                            chatFragment.addReplyFor(selectedChatId);
                        }
                        break;
                    case R.id.action_copy:
                        ClipboardManager clipboard = (ClipboardManager) LubbleApp.getAppContext().getSystemService(Context.CLIPBOARD_SERVICE);
                        String message = chatDataList.get(highlightedPos).getMessage();
                        ClipData clip = ClipData.newPlainText("lubble_copied_text", message);
                        clipboard.setPrimaryClip(clip);
                        break;
                    case R.id.action_info:
                        chatFragment.openChatInfo(chatDataList.get(highlightedPos).getId(), false);
                        break;
                }
                mode.finish();
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                selectedChatId = null;
                //lubbPopOutContainer.setVisibility(View.GONE);
                if (highlightedPos != -1) {
                    notifyItemChanged(highlightedPos);
                    highlightedPos = -1;
                }
            }
        };

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.iv_dp:
                    ProfileActivity.open(context, chatDataList.get(getAdapterPosition()).getAuthorUid());
                    break;
                case R.id.linear_layout_lubb_pop:
                    toggleLubb(getAdapterPosition());
                    Analytics.triggerEvent(AnalyticsEvents.POP_LIKE_CLICK, v.getContext());
                    break;
                case R.id.linearLayout_lubb_container:
                    toggleLubb(getAdapterPosition());
                    break;
                case R.id.link_meta_container:
                    ChatData chatData = chatDataList.get(getAdapterPosition());
                    if (LINK.equalsIgnoreCase(chatData.getType())) {
                        final URLSpan[] urls = messageTv.getUrls();
                        final String url = urls[0].getURL();
                        if (isValidString(url)) {
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(Uri.parse(url));
                            context.startActivity(i);
                        }
                    } else if (REPLY.equalsIgnoreCase(chatData.getType())) {
                        ChatData emptyReplyChatData = new ChatData();
                        emptyReplyChatData.setId(chatData.getReplyMsgId());
                        int pos = chatDataList.indexOf(emptyReplyChatData);
                        if (pos != -1) {
                            recyclerView.scrollToPosition(pos);
                            posToFlash = pos;
                            notifyItemChanged(pos);
                        }
                    }
                    break;
                case R.id.linear_layout_lubb_heads:
                    chatFragment.openChatInfo(chatDataList.get(getAdapterPosition()).getId(), false);
                    break;
            }
            if (actionMode != null) {
                actionMode.finish();
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (getAdapterPosition() != highlightedPos) {
                actionMode = ((AppCompatActivity) v.getContext()).startSupportActionMode(actionModeCallbacks);
                lubbPopOutContainer.setVisibility(View.VISIBLE);
                toggleLubbPopOutContainer(lubbIv, lubbHintTv, chatDataList.get(getAdapterPosition()).getLubbReceipts().containsKey(authorId));
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.trans_colorAccent));
                if (highlightedPos != -1) {
                    // another item was highlighted, remove its highlight
                    notifyItemChanged(highlightedPos);
                }
                highlightedPos = getAdapterPosition();
                selectedChatId = chatDataList.get(getAdapterPosition()).getId();
            } else {
                if (actionMode != null) {
                    actionMode.finish();
                }
            }
            return true;
        }
    }

    public class SentChatViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private EmojiTextView messageTv;
        private LinearLayout linkContainer;
        private TextView linkTitleTv;
        private EmojiTextView linkDescTv;
        private FrameLayout imgContainer;
        private ProgressBar progressBar;
        private ImageView chatIv;
        private TextView dateTv;
        private MsgFlexBoxLayout textContainer;
        private LinearLayout lubbContainer;
        private ImageView lubbIcon;
        private LinearLayout lubbHeadsContainer;
        private LinearLayout lubbPopOutContainer;
        private TextView lubbCount;
        @Nullable
        private ActionMode actionMode;
        private ImageView lubbIv;
        private TextView lubbHintTv;
        private ImageView youtubeThumbnailView;
        private ProgressBar youtubeProgressBar;
        private ImageView youtubePlayIv;
        private RelativeLayout youtubeContainer;
        private TextView youtubeTitleTv;
        private LinearLayout pollContainer;

        SentChatViewHolder(final View itemView) {
            super(itemView);
            messageTv = itemView.findViewById(R.id.tv_message);
            linkContainer = itemView.findViewById(R.id.link_meta_container);
            linkTitleTv = itemView.findViewById(R.id.tv_link_title);
            linkDescTv = itemView.findViewById(R.id.tv_link_desc);
            imgContainer = itemView.findViewById(R.id.img_container);
            progressBar = itemView.findViewById(R.id.progressbar_img);
            chatIv = itemView.findViewById(R.id.iv_chat_img);
            dateTv = itemView.findViewById(R.id.tv_date);
            textContainer = itemView.findViewById(R.id.msgFlexBox_text);
            lubbContainer = itemView.findViewById(R.id.linearLayout_lubb_container);
            lubbIcon = itemView.findViewById(R.id.iv_lubb);
            lubbCount = itemView.findViewById(R.id.tv_lubb_count);
            lubbHeadsContainer = itemView.findViewById(R.id.linear_layout_lubb_heads);
            lubbPopOutContainer = itemView.findViewById(R.id.linear_layout_lubb_pop);
            lubbIv = itemView.findViewById(R.id.iv_lubb_icon);
            lubbHintTv = itemView.findViewById(R.id.tv_lubb_hint);
            youtubeThumbnailView = itemView.findViewById(R.id.youtube_thumbnail_view);
            youtubeProgressBar = itemView.findViewById(R.id.progressbar_youtube);
            youtubePlayIv = itemView.findViewById(R.id.iv_youtube_play);
            youtubeContainer = itemView.findViewById(R.id.relativelayout_youtube);
            youtubeTitleTv = itemView.findViewById(R.id.tv_yt_title);
            pollContainer = itemView.findViewById(R.id.container_polls);

            linkContainer.setOnClickListener(this);
            lubbContainer.setOnClickListener(this);
            lubbHeadsContainer.setOnClickListener(this);
            lubbPopOutContainer.setOnClickListener(this);
            chatIv.setOnClickListener(null);
            chatIv.setOnLongClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        private ActionMode.Callback actionModeCallbacks = new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.menu_chat, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_reply:
                        chatFragment.addReplyFor(selectedChatId);
                        break;
                    case R.id.action_copy:
                        ClipboardManager clipboard = (ClipboardManager) LubbleApp.getAppContext().getSystemService(Context.CLIPBOARD_SERVICE);
                        String message = chatDataList.get(highlightedPos).getMessage();
                        ClipData clip = ClipData.newPlainText("lubble_copied_text", message);
                        clipboard.setPrimaryClip(clip);
                        break;
                    case R.id.action_info:
                        chatFragment.openChatInfo(chatDataList.get(highlightedPos).getId(), true);
                        break;
                }
                mode.finish();
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                selectedChatId = null;
                //lubbPopOutContainer.setVisibility(View.GONE);
                if (highlightedPos != -1) {
                    notifyItemChanged(highlightedPos);
                    highlightedPos = -1;
                }
            }
        };

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.linearLayout_lubb_container:
                    toggleLubb(getAdapterPosition());
                    Analytics.triggerEvent(AnalyticsEvents.POP_LIKE_CLICK, v.getContext());
                    break;
                case R.id.linear_layout_lubb_pop:
                    toggleLubb(getAdapterPosition());
                    break;
                case R.id.link_meta_container:
                    ChatData chatData = chatDataList.get(getAdapterPosition());
                    if (LINK.equalsIgnoreCase(chatData.getType())) {
                        final URLSpan[] urls = messageTv.getUrls();
                        final String url = urls[0].getURL();
                        if (isValidString(url)) {
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(Uri.parse(url));
                            context.startActivity(i);
                        }
                    } else if (REPLY.equalsIgnoreCase(chatData.getType())) {
                        ChatData emptyReplyChatData = new ChatData();
                        emptyReplyChatData.setId(chatData.getReplyMsgId());
                        int pos = chatDataList.indexOf(emptyReplyChatData);
                        if (pos != -1) {
                            recyclerView.scrollToPosition(pos);
                            posToFlash = pos;
                            notifyItemChanged(pos);
                        }
                    }
                    break;
                case R.id.linear_layout_lubb_heads:
                    chatFragment.openChatInfo(chatDataList.get(getAdapterPosition()).getId(), true);
                    break;
            }
            if (actionMode != null) {
                actionMode.finish();
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (getAdapterPosition() != highlightedPos) {
                actionMode = ((AppCompatActivity) v.getContext()).startSupportActionMode(actionModeCallbacks);
                lubbPopOutContainer.setVisibility(View.VISIBLE);
                toggleLubbPopOutContainer(lubbIv, lubbHintTv, chatDataList.get(getAdapterPosition()).getLubbReceipts().containsKey(authorId));
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.trans_colorAccent));
                if (highlightedPos != -1) {
                    // another item was highlighted, remove its highlight
                    notifyItemChanged(highlightedPos);
                }
                highlightedPos = getAdapterPosition();
                selectedChatId = chatDataList.get(getAdapterPosition()).getId();
            } else {
                if (actionMode != null) {
                    actionMode.finish();
                }
            }
            return true;
        }
    }

    private void toggleLubbPopOutContainer(ImageView lubbIv, TextView lubbTv, boolean isLubbed) {
        if (isLubbed) {
            lubbIv.setImageResource(R.drawable.ic_favorite_24dp);
            lubbIv.setColorFilter(null);
            lubbTv.setText(R.string.liked);
        } else {
            lubbIv.setImageResource(R.drawable.ic_favorite_border_24dp);
            lubbIv.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.bright_red), PorterDuff.Mode.SRC_IN));
            lubbTv.setText(R.string.like);
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
