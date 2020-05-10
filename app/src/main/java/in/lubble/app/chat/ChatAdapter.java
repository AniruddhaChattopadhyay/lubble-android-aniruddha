package in.lubble.app.chat;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.BackgroundColorSpan;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.emoji.widget.EmojiTextView;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.crashlytics.android.Crashlytics;
import com.google.android.youtube.player.YouTubeIntents;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import org.jsoup.Jsoup;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import in.lubble.app.GlideRequests;
import in.lubble.app.LubbleApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;
import in.lubble.app.events.EventInfoActivity;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.models.ChatData;
import in.lubble.app.models.ChoiceData;
import in.lubble.app.models.GroupData;
import in.lubble.app.models.ProfileData;
import in.lubble.app.models.ProfileInfo;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import in.lubble.app.profile.ProfileActivity;
import in.lubble.app.utils.ChatUtils;
import in.lubble.app.utils.DateTimeUtils;
import in.lubble.app.utils.FullScreenImageActivity;
import in.lubble.app.utils.FullScreenVideoActivity;
import in.lubble.app.utils.UiUtils;
import in.lubble.app.utils.YoutubeData;
import in.lubble.app.utils.YoutubeUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static in.lubble.app.firebase.RealtimeDbHelper.getDmMessagesRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getMessagesRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getSellerRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getUserInfoRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getUserRef;
import static in.lubble.app.models.ChatData.EVENT;
import static in.lubble.app.models.ChatData.GROUP;
import static in.lubble.app.models.ChatData.GROUP_PROMPT;
import static in.lubble.app.models.ChatData.HIDDEN;
import static in.lubble.app.models.ChatData.LINK;
import static in.lubble.app.models.ChatData.REPLY;
import static in.lubble.app.models.ChatData.SYSTEM;
import static in.lubble.app.models.ChatData.UNREAD;
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
    private HashMap<String, ProfileData> profileDataMap = new HashMap<>();
    private String authorId = FirebaseAuth.getInstance().getUid();
    @Nullable
    private String dmId;// Allows to remember the last item shown on screen
    private HashMap<String, String> searchHighlightMap = new HashMap<>();


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
        final ChatData chatData = chatDataList.get(position);

        final String authorUid = chatData.getAuthorUid();
        if (profileDataMap.containsKey(authorUid)) {
            final ProfileData profileData = profileDataMap.get(authorUid);
            final ProfileInfo profileInfo = profileData.getInfo();
            sentChatViewHolder.senderTv.setVisibility(View.VISIBLE);
            sentChatViewHolder.senderTv.setText(profileInfo.getName());
            if (!chatData.getIsDm() && (!TextUtils.isEmpty(profileInfo.getBadge()) || !TextUtils.isEmpty(profileData.getGroupFlair()))) {
                String flair = !TextUtils.isEmpty(profileData.getGroupFlair()) ? profileData.getGroupFlair() : profileInfo.getBadge();
                sentChatViewHolder.badgeTextTv.setVisibility(View.VISIBLE);
                sentChatViewHolder.badgeTextTv.setText("\u2022 " + flair);
                sentChatViewHolder.badgeTextTv.setTextColor(ContextCompat.getColor(context, R.color.white));
            } else {
                sentChatViewHolder.badgeTextTv.setVisibility(View.GONE);
            }
        } else {
            sentChatViewHolder.senderTv.setVisibility(View.GONE);
            sentChatViewHolder.badgeTextTv.setVisibility(View.GONE);
            updateProfileInfoMap(getUserRef(authorUid), authorUid, position);
        }

        if (posToFlash == position) {
            UiUtils.animateColor(sentChatViewHolder.itemView, ContextCompat.getColor(context, R.color.trans_colorAccent), Color.TRANSPARENT);
            posToFlash = -1;
        } else {
            sentChatViewHolder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }

        if (highlightedPos == position) {
            sentChatViewHolder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.trans_colorAccent));
            sentChatViewHolder.dateTv.setVisibility(View.VISIBLE);
        } else {
            sentChatViewHolder.itemView.setBackgroundColor(Color.TRANSPARENT);
            sentChatViewHolder.dateTv.setVisibility(View.INVISIBLE);
        }

        if (isValidString(chatData.getMessage())) {
            sentChatViewHolder.messageTv.setVisibility(View.VISIBLE);
            sentChatViewHolder.messageTv.setText(chatData.getMessage());
        } else {
            sentChatViewHolder.messageTv.setVisibility(View.GONE);
        }
        sentChatViewHolder.messageTv.setLinkTextColor(ContextCompat.getColor(context, R.color.white));

        if (searchHighlightMap.containsKey(chatData.getId())) {
            setHighLightedText(sentChatViewHolder.messageTv, searchHighlightMap.get(chatData.getId()));
        }

        Linkify.addLinks(sentChatViewHolder.messageTv, Linkify.ALL);
        if (chatData.getTagged() != null && !chatData.getTagged().isEmpty()) {
            Pattern atMentionPattern = Pattern.compile("@([A-Za-z0-9_]+)");
            String atMentionScheme = "lubble://profile/";

            Linkify.TransformFilter transformFilter = new Linkify.TransformFilter() {
                //skip the first character to filter out '@'
                public String transformUrl(final Matcher match, String url) {
                    return ChatUtils.getKeyByValue(chatData.getTagged(), match.group(1));
                }
            };

            Linkify.addLinks(sentChatViewHolder.messageTv, atMentionPattern, atMentionScheme, null, transformFilter);
        }

        if (chatData.getLubbCount() == 0 || chatData.getIsDm()) {
            sentChatViewHolder.lubbCount.setText("");
        } else {
            sentChatViewHolder.lubbCount.setText(String.valueOf(chatData.getLubbCount()));
        }
        if (chatData.getIsDm()) {
            sentChatViewHolder.lubbIcon.setVisibility(View.GONE);
        } else {
            sentChatViewHolder.lubbIcon.setVisibility(View.VISIBLE);
            if (chatData.getLubbReceipts().containsKey(authorId)) {
                sentChatViewHolder.lubbIcon.setImageResource(R.drawable.ic_favorite_24dp);
                if (position == chatDataList.size() - 1) {
                    // scroll to bottom if liked last msg to show that like icon and count
                    recyclerView.smoothScrollToPosition(chatDataList.size() - 1 > -1 ? chatDataList.size() - 1 : 0);
                }
            } else {
                sentChatViewHolder.lubbIcon.setImageResource(R.drawable.ic_favorite_border_light);
            }
        }

        sentChatViewHolder.dateTv.setText(DateTimeUtils.getTimeFromLong(chatData.getServerTimestampInLong()));
        if (chatData.getType().equalsIgnoreCase(GROUP) && isValidString(chatData.getAttachedGroupId())) {
            sentChatViewHolder.linkContainer.setVisibility(View.VISIBLE);
            sentChatViewHolder.linkTitleTv.setText(chatData.getLinkTitle());
            sentChatViewHolder.linkTitleTv.setTextColor(ContextCompat.getColor(context, R.color.white));
            sentChatViewHolder.linkDescTv.setText(chatData.getLinkDesc());
            sentChatViewHolder.linkDescTv.setTextColor(ContextCompat.getColor(context, R.color.trans_white));
            glide.load(chatData.getLinkPicUrl())
                    .circleCrop()
                    .placeholder(R.drawable.ic_circle_group_24dp)
                    .error(R.drawable.ic_circle_group_24dp)
                    .into(sentChatViewHolder.attachPicIv);
            sentChatViewHolder.linkPicIv.setVisibility(View.GONE);
            sentChatViewHolder.attachPicIv.setVisibility(View.VISIBLE);

            setBgColor(sentChatViewHolder.linkContainer, chatData);

        } else if (chatData.getType().equalsIgnoreCase(EVENT) && isValidString(chatData.getAttachedGroupId())) {
            sentChatViewHolder.linkContainer.setVisibility(View.VISIBLE);
            sentChatViewHolder.linkTitleTv.setText("Event: " + chatData.getLinkTitle());
            sentChatViewHolder.linkTitleTv.setTextColor(ContextCompat.getColor(context, R.color.white));
            sentChatViewHolder.linkDescTv.setText(Jsoup.parse(chatData.getLinkDesc()).text());
            sentChatViewHolder.linkDescTv.setTextColor(ContextCompat.getColor(context, R.color.trans_white));
            glide.load(chatData.getLinkPicUrl())
                    .circleCrop()
                    .placeholder(R.drawable.ic_event)
                    .error(R.drawable.ic_event)
                    .into(sentChatViewHolder.attachPicIv);
            sentChatViewHolder.linkPicIv.setVisibility(View.GONE);
            sentChatViewHolder.attachPicIv.setVisibility(View.VISIBLE);

            setBgColor(sentChatViewHolder.linkContainer, chatData);

        } else if (chatData.getType().equalsIgnoreCase(REPLY) && isValidString(chatData.getReplyMsgId())) {
            sentChatViewHolder.linkContainer.setVisibility(View.VISIBLE);
            addReplyData(chatData.getReplyMsgId(), sentChatViewHolder.linkTitleTv, sentChatViewHolder.linkDescTv, chatData.getIsDm());
            final Drawable drawable = ContextCompat.getDrawable(context, R.drawable.rect_rounded_trans_white);
            DrawableCompat.setTintList(drawable, null);
            sentChatViewHolder.linkTitleTv.setTextColor(ContextCompat.getColor(context, R.color.black));
            sentChatViewHolder.linkDescTv.setTextColor(ContextCompat.getColor(context, R.color.link_text_color));
            sentChatViewHolder.linkContainer.setBackground(drawable);
            sentChatViewHolder.linkPicIv.setVisibility(View.GONE);
            sentChatViewHolder.attachPicIv.setVisibility(View.GONE);
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
        } else if (chatData.getType().equalsIgnoreCase(LINK)) {
            sentChatViewHolder.linkContainer.setVisibility(View.VISIBLE);
            sentChatViewHolder.linkTitleTv.setText(chatData.getLinkTitle());
            sentChatViewHolder.linkDescTv.setText(chatData.getLinkDesc());
            sentChatViewHolder.linkTitleTv.setTextColor(ContextCompat.getColor(context, R.color.black));
            sentChatViewHolder.linkDescTv.setTextColor(ContextCompat.getColor(context, R.color.link_text_color));
            sentChatViewHolder.attachPicIv.setVisibility(View.GONE);
            if (!TextUtils.isEmpty(chatData.getLinkPicUrl())) {
                glide.load(chatData.getLinkPicUrl())
                        .apply(RequestOptions.bitmapTransform(new RoundedCorners(UiUtils.dpToPx(44))))
                        .into(sentChatViewHolder.linkPicIv);
                sentChatViewHolder.linkPicIv.setVisibility(View.VISIBLE);
            } else {
                sentChatViewHolder.linkPicIv.setVisibility(View.GONE);
            }
            final Drawable drawable = ContextCompat.getDrawable(context, R.drawable.rect_rounded_trans_white);
            DrawableCompat.setTintList(drawable, null);
            sentChatViewHolder.linkContainer.setBackground(drawable);
        } else {
            sentChatViewHolder.linkContainer.setVisibility(View.GONE);
        }

        handleImage(sentChatViewHolder.imgContainer, sentChatViewHolder.progressBar, sentChatViewHolder.chatIv, chatData, null);
        handleVideo(sentChatViewHolder.vidContainer, sentChatViewHolder.progressBar_vid, sentChatViewHolder.playvidIv, sentChatViewHolder.vidThumbnailIv, chatData, null, position);

        handleYoutube(sentChatViewHolder, chatData.getMessage(), position);

        if (chatData.getType().equalsIgnoreCase(ChatData.POLL) && chatData.getChoiceList() != null && !chatData.getChoiceList().isEmpty()) {
            sentChatViewHolder.messageTv.setVisibility(View.GONE);
            sentChatViewHolder.pollContainer.setVisibility(View.VISIBLE);
            final TextView pollQuesTv = sentChatViewHolder.pollContainer.findViewById(R.id.tv_poll_ques);
            ((TextView) sentChatViewHolder.pollContainer.findViewById(R.id.tv_anon_poll_hint)).setTextColor(ContextCompat.getColor(context, R.color.trans_white));
            pollQuesTv.setText(chatData.getPollQues());
            pollQuesTv.setTextColor(ContextCompat.getColor(context, R.color.white));

            showPollResults(sentChatViewHolder, chatData);

        } else {
            sentChatViewHolder.pollContainer.setVisibility(View.GONE);
        }
    }

    private void setHighLightedText(TextView tv, String textToHighlight) {
        String tvt = tv.getText().toString().toLowerCase();
        int ofe = tvt.indexOf(textToHighlight.toLowerCase(), 0);
        Spannable wordToSpan = new SpannableString(tv.getText());
        for (int ofs = 0; ofs < tvt.length() && ofe != -1; ofs = ofe + 1) {
            ofe = tvt.indexOf(textToHighlight.toLowerCase(), ofs);
            if (ofe == -1)
                break;
            else {
                // set color here
                wordToSpan.setSpan(new BackgroundColorSpan(ContextCompat.getColor(context, R.color.trans_dark_gold)), ofe, ofe + textToHighlight.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                tv.setText(wordToSpan, TextView.BufferType.SPANNABLE);
            }
        }
    }

    private void setBgColor(final RelativeLayout linkContainer, ChatData chatData) {
        if (!TextUtils.isEmpty(chatData.getLinkPicUrl())) {
            glide.asBitmap().load(chatData.getLinkPicUrl()).into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    Palette.from(resource)
                            .maximumColorCount(8)
                            .addFilter(UiUtils.DEFAULT_FILTER)
                            .generate(new Palette.PaletteAsyncListener() {
                                public void onGenerated(Palette p) {
                                    // Use generated instance
                                    Drawable normalDrawable = context.getResources().getDrawable(R.drawable.rounded_rect_gray);
                                    Drawable wrapDrawable = DrawableCompat.wrap(normalDrawable);
                                    DrawableCompat.setTint(wrapDrawable, p.getDarkVibrantColor(ContextCompat.getColor(context, R.color.fb_color)));
                                    DrawableCompat.setTintMode(wrapDrawable, PorterDuff.Mode.MULTIPLY);
                                    linkContainer.setBackground(wrapDrawable);
                                }
                            });
                }
            });
        } else {
            Drawable normalDrawable = context.getResources().getDrawable(R.drawable.rounded_rect_gray);
            Drawable wrapDrawable = DrawableCompat.wrap(normalDrawable);
            DrawableCompat.setTint(wrapDrawable, ContextCompat.getColor(context, R.color.fb_color));
            DrawableCompat.setTintMode(wrapDrawable, PorterDuff.Mode.MULTIPLY);
            linkContainer.setBackground(wrapDrawable);
        }
    }

    private void bindRecvdChatViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final RecvdChatViewHolder recvdChatViewHolder = (RecvdChatViewHolder) holder;
        final ChatData chatData = chatDataList.get(position);

        showDpAndName(recvdChatViewHolder, chatData);

        recvdChatViewHolder.visibleToYouTv.setVisibility(chatData.getType().equalsIgnoreCase(GROUP_PROMPT) ? View.VISIBLE : View.GONE);
        recvdChatViewHolder.replyBottomTv.setVisibility(chatData.getType().equalsIgnoreCase(GROUP_PROMPT) ? View.VISIBLE : View.GONE);

        recvdChatViewHolder.replyBottomTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chatFragment.addReplyForPrompt(chatData.getId(), profileDataMap.get(chatData.getAuthorUid()).getInfo().getName(), chatData.getPromptQues());
                Analytics.triggerEvent(AnalyticsEvents.GROUP_PROMPT_REPLIED, context);
            }
        });

        if (posToFlash == position) {
            UiUtils.animateColor(recvdChatViewHolder.itemView, ContextCompat.getColor(context, R.color.trans_colorAccent), Color.TRANSPARENT);
            posToFlash = -1;
        } else {
            recvdChatViewHolder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }

        if (highlightedPos == position) {
            recvdChatViewHolder.itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.trans_colorAccent));
            recvdChatViewHolder.dateTv.setVisibility(View.VISIBLE);
        } else {
            recvdChatViewHolder.itemView.setBackgroundColor(Color.TRANSPARENT);
            recvdChatViewHolder.dateTv.setVisibility(View.INVISIBLE);

        }

        if (isValidString(chatData.getMessage())) {
            recvdChatViewHolder.messageTv.setVisibility(View.VISIBLE);
            recvdChatViewHolder.messageTv.setText(chatData.getMessage());
        } else {
            recvdChatViewHolder.messageTv.setVisibility(View.GONE);
        }
        recvdChatViewHolder.messageTv.setLinkTextColor(ContextCompat.getColor(context, R.color.colorAccent));

        if (searchHighlightMap.containsKey(chatData.getId())) {
            setHighLightedText(recvdChatViewHolder.messageTv, searchHighlightMap.get(chatData.getId()));
        }

        Linkify.addLinks(recvdChatViewHolder.messageTv, Linkify.ALL);

        if (chatData.getTagged() != null && !chatData.getTagged().isEmpty()) {
            Pattern atMentionPattern = Pattern.compile("@([A-Za-z0-9_]+)");
            String atMentionScheme = "lubble://profile/";

            Linkify.TransformFilter transformFilter = new Linkify.TransformFilter() {
                //skip the first character to filter out '@'
                public String transformUrl(final Matcher match, String url) {
                    return ChatUtils.getKeyByValue(chatData.getTagged(), match.group(1));
                }
            };

            Linkify.addLinks(recvdChatViewHolder.messageTv, atMentionPattern, atMentionScheme, null, transformFilter);
        }
        recvdChatViewHolder.dateTv.setText(DateTimeUtils.getTimeFromLong(chatData.getServerTimestampInLong()));
        if (chatData.getLubbCount() == 0 || chatData.getIsDm()) {
            recvdChatViewHolder.lubbCount.setText("");
        } else {
            recvdChatViewHolder.lubbCount.setText(String.valueOf(chatData.getLubbCount()));
        }
        if (chatData.getIsDm()) {
            recvdChatViewHolder.lubbIcon.setVisibility(View.GONE);
        } else {
            recvdChatViewHolder.lubbIcon.setVisibility(View.VISIBLE);
            if (chatData.getLubbReceipts().containsKey(authorId)) {
                recvdChatViewHolder.lubbIcon.setImageResource(R.drawable.ic_favorite_24dp);
                if (position == chatDataList.size() - 1) {
                    // scroll to bottom if liked last msg to show that like icon and count
                    recyclerView.smoothScrollToPosition(chatDataList.size() - 1 > -1 ? chatDataList.size() - 1 : 0);
                }
            } else {
                recvdChatViewHolder.lubbIcon.setImageResource(R.drawable.ic_favorite_border_light);
            }
        }
        if (chatData.getType().equalsIgnoreCase(GROUP) && isValidString(chatData.getAttachedGroupId())) {
            recvdChatViewHolder.linkContainer.setVisibility(View.VISIBLE);
            recvdChatViewHolder.linkTitleTv.setText(chatData.getLinkTitle());
            recvdChatViewHolder.linkTitleTv.setTextColor(ContextCompat.getColor(context, R.color.white));
            recvdChatViewHolder.linkDescTv.setText(chatData.getLinkDesc());
            recvdChatViewHolder.linkDescTv.setTextColor(ContextCompat.getColor(context, R.color.trans_white));
            glide.load(chatData.getLinkPicUrl())
                    .circleCrop()
                    .placeholder(R.drawable.ic_circle_group_24dp)
                    .error(R.drawable.ic_circle_group_24dp)
                    .into(recvdChatViewHolder.attachPicIv);
            recvdChatViewHolder.linkPicIv.setVisibility(View.GONE);
            recvdChatViewHolder.attachPicIv.setVisibility(View.VISIBLE);
            setBgColor(recvdChatViewHolder.linkContainer, chatData);
        } else if (chatData.getType().equalsIgnoreCase(EVENT) && isValidString(chatData.getAttachedGroupId())) {
            recvdChatViewHolder.linkContainer.setVisibility(View.VISIBLE);
            recvdChatViewHolder.linkTitleTv.setText("Event: " + chatData.getLinkTitle());
            recvdChatViewHolder.linkTitleTv.setTextColor(ContextCompat.getColor(context, R.color.white));
            recvdChatViewHolder.linkDescTv.setText(Jsoup.parse(chatData.getLinkDesc()).text());
            recvdChatViewHolder.linkDescTv.setTextColor(ContextCompat.getColor(context, R.color.trans_white));
            glide.load(chatData.getLinkPicUrl())
                    .circleCrop()
                    .placeholder(R.drawable.ic_event)
                    .error(R.drawable.ic_event)
                    .into(recvdChatViewHolder.attachPicIv);
            recvdChatViewHolder.linkPicIv.setVisibility(View.GONE);
            recvdChatViewHolder.attachPicIv.setVisibility(View.VISIBLE);
            setBgColor(recvdChatViewHolder.linkContainer, chatData);
        } else if (chatData.getType().equalsIgnoreCase(REPLY) && isValidString(chatData.getReplyMsgId())) {
            recvdChatViewHolder.linkContainer.setVisibility(View.VISIBLE);
            recvdChatViewHolder.linkTitleTv.setTextColor(ContextCompat.getColor(context, R.color.black));
            recvdChatViewHolder.linkDescTv.setTextColor(ContextCompat.getColor(context, R.color.link_text_color));
            addReplyData(chatData.getReplyMsgId(), recvdChatViewHolder.linkTitleTv, recvdChatViewHolder.linkDescTv, chatData.getIsDm());
            final Drawable drawable = ContextCompat.getDrawable(context, R.drawable.sent_chat_bubble_border);
            DrawableCompat.setTintList(drawable, null);
            recvdChatViewHolder.linkContainer.setBackground(drawable);
            recvdChatViewHolder.linkPicIv.setVisibility(View.GONE);
            recvdChatViewHolder.attachPicIv.setVisibility(View.GONE);
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
        } else if (chatData.getType().equalsIgnoreCase(LINK)) {
            recvdChatViewHolder.linkContainer.setVisibility(View.VISIBLE);
            recvdChatViewHolder.linkTitleTv.setText(chatData.getLinkTitle());
            recvdChatViewHolder.linkTitleTv.setTextColor(ContextCompat.getColor(context, R.color.black));
            recvdChatViewHolder.linkDescTv.setTextColor(ContextCompat.getColor(context, R.color.link_text_color));
            recvdChatViewHolder.linkDescTv.setText(chatData.getLinkDesc());
            recvdChatViewHolder.attachPicIv.setVisibility(View.GONE);

            if (!TextUtils.isEmpty(chatData.getLinkPicUrl())) {
                glide.load(chatData.getLinkPicUrl())
                        .apply(RequestOptions.bitmapTransform(new RoundedCorners(UiUtils.dpToPx(44))))
                        .into(recvdChatViewHolder.linkPicIv);
                recvdChatViewHolder.linkPicIv.setVisibility(View.VISIBLE);
            } else {
                recvdChatViewHolder.linkPicIv.setVisibility(View.GONE);
            }
            final Drawable drawable = ContextCompat.getDrawable(context, R.drawable.sent_chat_bubble_border);
            DrawableCompat.setTintList(drawable, null);
            recvdChatViewHolder.linkContainer.setBackground(drawable);
        } else {
            recvdChatViewHolder.linkContainer.setVisibility(View.GONE);
        }

        handleImage(recvdChatViewHolder.imgContainer, recvdChatViewHolder.progressBar, recvdChatViewHolder.chatIv, chatData, recvdChatViewHolder.downloadIv);
        handleVideo(recvdChatViewHolder.vidContainer, recvdChatViewHolder.progressBar_vid, recvdChatViewHolder.playvidIv, recvdChatViewHolder.vidThumbnailIv, chatData, recvdChatViewHolder.downloadIv, position);
        handleYoutube(recvdChatViewHolder, chatData.getMessage(), position);

        if (chatData.getType().equalsIgnoreCase(ChatData.POLL) && chatData.getChoiceList() != null && !chatData.getChoiceList().isEmpty()) {
            recvdChatViewHolder.messageTv.setVisibility(View.GONE);
            recvdChatViewHolder.pollContainer.setVisibility(View.VISIBLE);
            final TextView pollQuesTv = recvdChatViewHolder.pollContainer.findViewById(R.id.tv_poll_ques);
            ((TextView) recvdChatViewHolder.pollContainer.findViewById(R.id.tv_anon_poll_hint)).setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
            pollQuesTv.setText(chatData.getPollQues());
            pollQuesTv.setTextColor(ContextCompat.getColor(context, R.color.black));

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
        setPollCount(chatData, pollContainer, ContextCompat.getColor(context, R.color.white));
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
            setPollCount(chatData, pollContainer, ContextCompat.getColor(context, R.color.black));
        } else {
            pollContainer = ((SentChatViewHolder) baseViewHolder).pollContainer;
            setPollCount(chatData, pollContainer, ContextCompat.getColor(context, R.color.white));
        }
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
            final View choiceBackground = choiceContainer.findViewById(R.id.iv_choice_background);
            if (percent > 0) {
                final LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) choiceBackground.getLayoutParams();
                layoutParams.weight = percent;
                choiceBackground.setLayoutParams(layoutParams);
                if (votedIndex != null && votedIndex == i) {
                    // tint the choice voted by user to differentiate it from rest of the choices
                    choiceTv.setTypeface(choiceTv.getTypeface(), Typeface.BOLD);
                    percentTv.setTypeface(percentTv.getTypeface(), Typeface.BOLD);
                    if (baseViewHolder instanceof RecvdChatViewHolder) {
                        ((ImageView) choiceBackground).setColorFilter(ContextCompat.getColor(context, R.color.trans_colorAccent), android.graphics.PorterDuff.Mode.SRC_IN);
                    } else {
                        ((ImageView) choiceBackground).setColorFilter(ContextCompat.getColor(context, R.color.white), android.graphics.PorterDuff.Mode.SRC_IN);
                    }
                } else {
                    choiceTv.setTypeface(choiceTv.getTypeface(), Typeface.NORMAL);
                    percentTv.setTypeface(percentTv.getTypeface(), Typeface.NORMAL);
                    if (baseViewHolder instanceof RecvdChatViewHolder) {
                        ((ImageView) choiceBackground).setColorFilter(ContextCompat.getColor(context, R.color.medium_light_grey), android.graphics.PorterDuff.Mode.SRC_IN);
                    } else {
                        ((ImageView) choiceBackground).setColorFilter(ContextCompat.getColor(context, R.color.very_trans_white), android.graphics.PorterDuff.Mode.SRC_IN);
                    }
                }
            } else {
                // percent is ZERO
                if (baseViewHolder instanceof RecvdChatViewHolder) {
                    choiceTv.setBackground(ContextCompat.getDrawable(context, R.drawable.rounded_rect_trans_border));
                } else {
                    choiceTv.setBackground(ContextCompat.getDrawable(context, R.drawable.rounded_rect_white_border));
                }
                choiceContainer.findViewById(R.id.iv_choice_background).setVisibility(View.GONE);
            }
            if (baseViewHolder instanceof RecvdChatViewHolder) {
                choiceTv.setTextColor(ContextCompat.getColor(context, R.color.black));
                percentTv.setTextColor(ContextCompat.getColor(context, R.color.black));
            } else {
                choiceTv.setTextColor(ContextCompat.getColor(context, R.color.white));
                percentTv.setTextColor(ContextCompat.getColor(context, R.color.white));
            }

            resultsView.addView(choiceContainer);
        }
    }

    private void setPollCount(ChatData chatData, LinearLayout pollContainer, int color) {
        final TextView voteCountTv = pollContainer.findViewById(R.id.tv_poll_vote_count);
        final int voteCount = chatData.getPollReceipts().size();
        String voteCountStr = "No votes";
        if (voteCount > 0) {
            voteCountStr = context.getResources().getQuantityString(R.plurals.vote_count, voteCount, voteCount);
        }
        voteCountTv.setText(voteCountStr);
        voteCountTv.setTextColor(color);
    }

    private void handleYoutube(RecyclerView.ViewHolder baseViewHolder, final String message, final int position) {
        /**
         * init
         */
        final ImageView youTubeThumbnailIv;
        final ImageView youtubePlayIv;
        final ProgressBar youtubeProgressBar;
        RelativeLayout linkContainer;
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

        /*if (chatData.getLubbCount() > 0) {
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
        }*/
    }

    private void updateProfileInfoMap(DatabaseReference userRef, final String uid, final int pos) {
        // single as its very difficult otherwise to keep track of all listeners for every user
        // plus we don't really need realtime updation of user DP and/or name in chat
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final ProfileData profileData = dataSnapshot.getValue(ProfileData.class);
                if (profileData != null) {
                    profileData.setId(dataSnapshot.getKey());
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                        if (childSnapshot.getKey().equalsIgnoreCase("lubbles") && groupId != null) {
                            final String flair = childSnapshot.child(LubbleSharedPrefs.getInstance().requireLubbleId()).child("groups").child(groupId).child("flair").getValue(String.class);
                            profileData.setGroupFlair(flair);
                            break;
                        }
                    }

                    profileData.setId(dataSnapshot.getKey());
                    final ProfileInfo profileInfo = profileData.getInfo();
                    if (profileInfo != null) {
                        profileInfo.setId(dataSnapshot.getKey());
                        profileDataMap.put(profileData.getId(), profileData);
                        notifyItemChanged(pos);
                    }
                } else {
                    updateProfileInfoMap(getSellerRef(uid), uid, pos);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void addReplyData(String replyMsgId, final TextView linkTitleTv, final TextView linkDescTv, boolean isDm) {
        if (replyMsgId.equalsIgnoreCase("101")) {
            // for group prompt ques
            showGroupQues(linkTitleTv, linkDescTv);
        } else {
            ChatData emptyReplyChatData = new ChatData();
            emptyReplyChatData.setId(replyMsgId);
            int index = chatDataList.indexOf(emptyReplyChatData);
            if (index > -1) {
                ChatData quotedChatData = chatDataList.get(index);
                if (quotedChatData.getType().equalsIgnoreCase(GROUP_PROMPT)) {
                    showGroupQues(linkTitleTv, linkDescTv);
                } else {
                    String desc = getQuotedDesc(quotedChatData);
                    linkDescTv.setText(desc);
                    showName(linkTitleTv, quotedChatData.getAuthorUid());
                }
            } else {
                // chat not found, must have not been loaded yet due to pagination
                if (!isDm) {
                    RealtimeDbHelper.getMessagesRef().child(groupId).child(replyMsgId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getValue() != null) {
                                final ChatData quotedChatData = dataSnapshot.getValue(ChatData.class);
                                String desc = getQuotedDesc(quotedChatData);
                                linkDescTv.setText(desc);
                                showName(linkTitleTv, quotedChatData.getAuthorUid());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                } else {
                    RealtimeDbHelper.getDmMessagesRef().child(dmId).child(replyMsgId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getValue() != null) {
                                final ChatData quotedChatData = dataSnapshot.getValue(ChatData.class);
                                String desc = getQuotedDesc(quotedChatData);
                                linkDescTv.setText(desc);
                                showName(linkTitleTv, quotedChatData.getAuthorUid());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                }
            }
        }
    }

    private void showGroupQues(final TextView linkTitleTv, final TextView linkDescTv) {
        RealtimeDbHelper.getLubbleGroupsRef().child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    GroupData groupData = dataSnapshot.getValue(GroupData.class);
                    linkDescTv.setText(groupData.getQuestion());
                    showName(linkTitleTv, LubbleSharedPrefs.getInstance().getSupportUid());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private String getQuotedDesc(ChatData quotedChatData) {
        String desc = "";
        if (isValidString(quotedChatData.getVidUrl())) {
            desc = desc.concat("\ud83c\udfa5 ");
            if (!isValidString(quotedChatData.getMessage())) {
                // add the word video if there is no caption
                desc = desc.concat("Video ");
            }
        } else if (isValidString(quotedChatData.getImgUrl())) {
            desc = desc.concat("\uD83D\uDCF7 ");
            if (!isValidString(quotedChatData.getMessage())) {
                // add the word photo if there is no caption
                desc = desc.concat("Photo ");
            }
        }
        desc = desc.concat(quotedChatData.getMessage());
        return desc;
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
        if (profileDataMap.containsKey(chatData.getAuthorUid())) {
            recvdChatViewHolder.authorNameTv.setVisibility(View.VISIBLE);
            recvdChatViewHolder.dpIv.setVisibility(View.VISIBLE);
            final ProfileData profileData = profileDataMap.get(chatData.getAuthorUid());
            final ProfileInfo profileInfo = profileData.getInfo();
            glide.load(profileInfo.getThumbnail())
                    .circleCrop()
                    .placeholder(R.drawable.ic_account_circle_black_no_padding)
                    .error(R.drawable.ic_account_circle_black_no_padding)
                    .into(recvdChatViewHolder.dpIv);
            recvdChatViewHolder.authorNameTv.setText(profileInfo.getName().split(" ")[0]);
            recvdChatViewHolder.badgeTextTv.setVisibility(View.GONE);
            if (!TextUtils.isEmpty(profileInfo.getBadge()) || !TextUtils.isEmpty(profileData.getGroupFlair())) {
                String flair = !TextUtils.isEmpty(profileData.getGroupFlair()) ? profileData.getGroupFlair() : profileInfo.getBadge();
                recvdChatViewHolder.badgeTextTv.setVisibility(View.VISIBLE);
                recvdChatViewHolder.badgeTextTv.setText("\u2022 " + flair);
                recvdChatViewHolder.badgeTextTv.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
            } else {
                recvdChatViewHolder.badgeTextTv.setVisibility(View.GONE);
            }
        } else {
            updateProfileInfoMap(getUserRef(chatData.getAuthorUid()), chatData.getAuthorUid(), recvdChatViewHolder.getAdapterPosition());
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

            } else {
                if (downloadIv != null) {
                    downloadIv.setVisibility(View.GONE);
                }
                String savedPath = getSavedImageForMsgId(context, chatData.getId());
                if (savedPath != null) {
                    progressBar.setVisibility(View.GONE);
                    glide.load(savedPath).centerCrop().diskCacheStrategy(DiskCacheStrategy.NONE).into(imageView);
                } else {
                    downloadAndSavePic(progressBar, imageView, chatData);
                }
            }
        } else {
            imgContainer.setVisibility(View.GONE);
        }
    }

    private void handleVideo(FrameLayout vidContainer, final ProgressBar progressBar, final ImageView playvid, final ImageView imageView, final ChatData chatData, @Nullable ImageView downloadIv, int position) {

        if (isValidString(chatData.getVidUrl())) {
            progressBar.setVisibility(View.VISIBLE);
            imageView.setOnClickListener(null);
            vidContainer.setVisibility(View.VISIBLE);
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && downloadIv != null) {
                // Permission is not granted
                Log.d(TAG, "inside if of handle video");
                glide.load(chatData.getVidUrl()).override(18, 18).centerCrop().into(imageView);
                downloadIv.setVisibility(View.VISIBLE);
                playvid.setImageResource(R.drawable.ic_file_download_black_24dp);
                progressBar.setVisibility(View.GONE);
            } else {
                RequestOptions requestOptions = new RequestOptions();
                requestOptions.placeholder(R.color.black);
                requestOptions.error(R.color.black);
                playvid.setImageResource(R.drawable.ic_play_circle_outline_gray_24dp);
                glide.load(chatData.getVidUrl()).listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        Log.d(TAG, "progress bar hidden");
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                        .apply(requestOptions)
                        .into(imageView);
                Log.d(TAG, "inside lst else");
            }
        } else {
            vidContainer.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
        }
    }

    private void downloadAndSavePic(final ProgressBar progressBar, final ImageView imageView, final ChatData chatData) {
        glide.download(chatData.getImgUrl())
                .listener(new RequestListener<File>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<File> target, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        imageView.setOnClickListener(null);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(File resource, Object model, Target<File> target, DataSource dataSource, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        glide.load(resource).centerCrop().into(imageView);
                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (isValidString(chatData.getImgUrl())) {
                                    FullScreenImageActivity.open(activity, context, chatData.getImgUrl(), imageView, null, R.drawable.ic_cancel_black_24dp);
                                }
                            }
                        });
                        return false;
                    }
                }).submit();
    }

    public void addChatData(@NonNull ChatData chatData) {
        if (!chatData.getType().equalsIgnoreCase(HIDDEN)) {
            final int size = chatDataList.size();
            if (chatData.getReporters() != null) {
                if (chatData.getReporters().containsKey(authorId)) {
                    chatData.setType(SYSTEM);
                    chatData.setMessage("Message has been reported");
                }
            }
            chatDataList.add(chatData);
            notifyItemInserted(size);
        }
    }

    public void addChatData(int pos, @NonNull ChatData chatData) {
        if (!chatData.getType().equalsIgnoreCase(HIDDEN)) {
            if (chatData.getReporters() != null) {
                if (chatData.getReporters().containsKey(authorId)) {
                    chatData.setType(SYSTEM);
                    chatData.setMessage("Message has been reported");
                }
            }
            chatDataList.add(pos, chatData);
            notifyItemInserted(pos);
        }
    }

    public void updateChatData(@NonNull ChatData chatData) {
        if (!chatData.getType().equalsIgnoreCase(HIDDEN)) {
            final int pos = chatDataList.indexOf(chatData);
            if (pos != -1) {
                if (chatData.getReporters() != null) {
                    if (chatData.getReporters().containsKey(authorId)) {
                        chatData.setType(SYSTEM);
                        chatData.setMessage("Message has been reported");
                    }
                }
                chatDataList.set(pos, chatData);
                notifyItemChanged(pos);
            }
        }
    }

    public void addPersonalChatData(@NonNull ChatData chatData) {
        if (!chatData.getType().equalsIgnoreCase(HIDDEN) &&
                (chatDataList.size() == 0 || !chatDataList.get(chatDataList.size() - 1).getId().equalsIgnoreCase("101"))) {
            final int size = chatDataList.size();
            chatDataList.add(chatData);
            notifyItemInserted(size);
        }
    }

    void updateFlair(ProfileData thisUserProfileData) {
        profileDataMap.put(thisUserProfileData.getId(), thisUserProfileData);
        notifyDataSetChanged();
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

    private void toggleLubb(int pos, boolean isSrcDoubleTap) {
        if (pos == -1) {
            Toast.makeText(activity, "Please try liking again", Toast.LENGTH_SHORT).show();
            Crashlytics.logException(new ArrayIndexOutOfBoundsException("while lubbing a msg. length = " + chatDataList.size() + " and index = " + pos));
            return;
        }
        final ChatData chatData = chatDataList.get(pos);
        if (chatData.getType().equalsIgnoreCase(GROUP_PROMPT) || chatData.getIsDm()) {
            return;
        }
        // add or remove like to chat msg
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
            public void onComplete(DatabaseError databaseError, boolean isCommitted, DataSnapshot dataSnapshot) {
                // Transaction completed
                Log.d(TAG, "postLikeTransaction:onComplete:" + databaseError);
                if (isCommitted) {
                    // add or remove like to user profile
                    // this logic is inverse of the one for post likes
                    // here, the chatData has been successfully liked and contains liker uid
                    ChatData updatedChatData = dataSnapshot.getValue(ChatData.class);
                    updatedChatData.setId(dataSnapshot.getKey());
                    chatFragment.updateMsgId(updatedChatData.getId());
                    if (updatedChatData != null) {
                        if (updatedChatData.getLubbReceipts().containsKey(authorId)) {
                            addLikeToAuthorProfile(updatedChatData);
                        } else {
                            removeLikeFromAuthorProfile(updatedChatData);
                        }
                    }
                }
            }
        });
        final Bundle bundle = new Bundle();
        if (isSrcDoubleTap) {
            bundle.putBoolean("is_src_double_tap", true);
        } else {
            bundle.putBoolean("is_src_double_tap", false);
            Toast.makeText(context, "You can also double tap a message to like it", Toast.LENGTH_SHORT).show();
        }
        Analytics.triggerEvent(AnalyticsEvents.POP_LIKE_CLICK, bundle, context);
        LubbleSharedPrefs.getInstance().setShowRatingDialog(true);
    }

    private void addLikeToAuthorProfile(final ChatData chatData) {
        getUserRef(chatData.getAuthorUid()).runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                ProfileData profileData = mutableData.getValue(ProfileData.class);
                if (profileData == null) {
                    return Transaction.success(mutableData);
                }

                // Set value and report transaction success
                mutableData.child("likes").setValue(profileData.getLikes() + 1);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {

            }
        });
    }

    private void removeLikeFromAuthorProfile(final ChatData chatData) {
        getUserRef(chatData.getAuthorUid()).runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                ProfileData profileData = mutableData.getValue(ProfileData.class);
                if (profileData == null) {
                    return Transaction.success(mutableData);
                }
                // Set value and report transaction success
                mutableData.child("likes").setValue(profileData.getLikes() - 1);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {

            }
        });
    }

    void writePermGranted() {
        deleteCache(context);
        Analytics.triggerEvent(AnalyticsEvents.WRITE_PERM_GRANTED, context);
        notifyDataSetChanged();
    }

    void scrollToChatId(String targetChatId, @Nullable String highlightText) {
        if (!TextUtils.isEmpty(highlightText)) {
            searchHighlightMap.put(targetChatId, highlightText);
        }
        ChatData emptyReplyChatData = new ChatData();
        emptyReplyChatData.setId(targetChatId);
        int pos = chatDataList.indexOf(emptyReplyChatData);
        if (pos != -1) {
            recyclerView.scrollToPosition(pos);
            posToFlash = pos;
            notifyItemChanged(pos);
        } else {
            // load more paginated chats
            chatFragment.moreMsgListener(targetChatId);
        }
    }

    void removeSearchHighlights() {
        if (searchHighlightMap != null && !searchHighlightMap.isEmpty()) {
            Iterator<String> iterator = searchHighlightMap.keySet().iterator();
            while (iterator.hasNext()) {
                String chatIdToRemove = iterator.next();
                iterator.remove();
                ChatData emptyReplyChatData = new ChatData();
                emptyReplyChatData.setId(chatIdToRemove);
                int pos = chatDataList.indexOf(emptyReplyChatData);
                if (pos != -1) {
                    notifyItemChanged(pos);
                }
            }
            searchHighlightMap.clear();
        }
    }

    public class RecvdChatViewHolder extends RecyclerView.ViewHolder implements View.OnTouchListener {

        private RelativeLayout rootLayout;
        private TextView authorNameTv, visibleToYouTv, replyBottomTv;
        private EmojiTextView messageTv;
        private RelativeLayout linkContainer;
        private ImageView linkPicIv, attachPicIv;
        private TextView linkTitleTv;
        private EmojiTextView linkDescTv;
        private FrameLayout imgContainer;
        private FrameLayout vidContainer;
        private ProgressBar progressBar_vid;
        private ImageView vidThumbnailIv;
        private ImageView playvidIv;
        private ProgressBar progressBar;
        private ImageView chatIv;
        private TextView dateTv;
        private ImageView lubbIcon;
        private TextView lubbCount;
        private ImageView dpIv;
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
        private LinearLayout lubbContainer;
        private EmojiTextView badgeTextTv;

        public RecvdChatViewHolder(final View itemView) {
            super(itemView);
            rootLayout = itemView.findViewById(R.id.root_layout_chat_recvd);
            authorNameTv = itemView.findViewById(R.id.tv_author);
            visibleToYouTv = itemView.findViewById(R.id.tv_msg_visible_to_you);
            replyBottomTv = itemView.findViewById(R.id.tv_reply_bottom);
            messageTv = itemView.findViewById(R.id.tv_message);
            linkContainer = itemView.findViewById(R.id.link_meta_container);
            linkPicIv = itemView.findViewById(R.id.iv_link_pic);
            attachPicIv = itemView.findViewById(R.id.iv_attach_pic);
            linkTitleTv = itemView.findViewById(R.id.tv_link_title);
            linkDescTv = itemView.findViewById(R.id.tv_link_desc);
            imgContainer = itemView.findViewById(R.id.img_container);
            vidContainer = itemView.findViewById(R.id.vid_container);
            progressBar_vid = itemView.findViewById(R.id.progressbar_img_vid);
            vidThumbnailIv = itemView.findViewById(R.id.iv_vid_img);
            playvidIv = itemView.findViewById(R.id.iv_play_vid);
            lubbContainer = itemView.findViewById(R.id.container_lubb);
            progressBar = itemView.findViewById(R.id.progressbar_img);
            chatIv = itemView.findViewById(R.id.iv_chat_img);
            dateTv = itemView.findViewById(R.id.tv_date);
            lubbIcon = itemView.findViewById(R.id.iv_lubb);
            lubbCount = itemView.findViewById(R.id.tv_lubb_count);
            dpIv = itemView.findViewById(R.id.iv_dp);
            youtubeThumbnailView = itemView.findViewById(R.id.youtube_thumbnail_view);
            youtubeProgressBar = itemView.findViewById(R.id.progressbar_youtube);
            youtubePlayIv = itemView.findViewById(R.id.iv_youtube_play);
            youtubeContainer = itemView.findViewById(R.id.relativelayout_youtube);
            youtubeTitleTv = itemView.findViewById(R.id.tv_yt_title);
            downloadIv = itemView.findViewById(R.id.iv_download);
            pollContainer = itemView.findViewById(R.id.container_polls);

            badgeTextTv = itemView.findViewById(R.id.tv_badge_text);

            dpIv.setOnTouchListener(this);
            linkContainer.setOnTouchListener(this);
            pollContainer.setOnTouchListener(this);
            messageTv.setOnTouchListener(this);
            rootLayout.setOnTouchListener(this);
            chatIv.setOnTouchListener(this);
            vidThumbnailIv.setOnTouchListener(this);
            lubbContainer.setOnTouchListener(this);
        }

        private ActionMode.Callback actionModeCallbacks = new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.menu_chat, menu);
                menu.findItem(R.id.action_spam).setVisible(true);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_spam:
                        if (null != selectedChatId) {
                            chatFragment.markSpam(selectedChatId, chatDataList.get(getAdapterPosition()).getMessage());
                        }
                        break;
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
                dateTv.setVisibility(View.INVISIBLE);
                if (highlightedPos != -1) {
                    notifyItemChanged(highlightedPos);
                    highlightedPos = -1;
                }
            }
        };

        private View touchedView;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            touchedView = v;
            return gestureDetector.onTouchEvent(event);
        }

        private GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onDown(MotionEvent event) {
                // triggers first for both single tap and long press
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                toggleLubb(getAdapterPosition(), true);
                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (getAdapterPosition() == RecyclerView.NO_POSITION) {
                    Toast.makeText(activity, "Something went wrong, please try again", Toast.LENGTH_SHORT).show();
                    Crashlytics.logException(new ArrayIndexOutOfBoundsException("index = -1"));
                    return true;
                }
                switch (touchedView.getId()) {
                    case R.id.iv_dp:
                        ProfileActivity.open(context, chatDataList.get(getAdapterPosition()).getAuthorUid());
                        break;
                    case R.id.container_lubb:
                        toggleLubb(getAdapterPosition(), false);
                        break;
                    case R.id.link_meta_container:
                        ChatData chatData = chatDataList.get(getAdapterPosition());
                        if (GROUP.equalsIgnoreCase(chatData.getType())) {
                            ChatActivity.openForGroup(context, chatData.getAttachedGroupId(), false, null);
                        } else if (EVENT.equalsIgnoreCase(chatData.getType())) {
                            EventInfoActivity.open(context, chatData.getAttachedGroupId());
                        } else if (REPLY.equalsIgnoreCase(chatData.getType())) {
                            scrollToChatId(chatData.getReplyMsgId(), null);
                        } else if (LINK.equalsIgnoreCase(chatData.getType())) {
                            final URLSpan[] urls = messageTv.getUrls();
                            final String url = urls[0].getURL();
                            if (isValidString(url)) {
                                Intent i = new Intent(Intent.ACTION_VIEW);
                                i.setData(Uri.parse(url));
                                context.startActivity(i);
                            }
                        }
                        break;
                    case R.id.iv_chat_img:
                        ChatData imgChatData = chatDataList.get(getAdapterPosition());
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && downloadIv != null) {
                            // ask for external storage perm
                            ChatFragmentPermissionsDispatcher
                                    .getWritePermWithPermissionCheck(chatFragment);
                        } else if (isValidString(imgChatData.getImgUrl())) {
                            FullScreenImageActivity.open(activity, context, imgChatData.getImgUrl(), chatIv, null, R.drawable.ic_cancel_black_24dp);
                        }
                        break;
                    case R.id.iv_vid_img:
                        ChatData vidChatData = chatDataList.get(getAdapterPosition());
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && downloadIv != null) {
                            // ask for external storage perm
                            ChatFragmentPermissionsDispatcher
                                    .getWritePermWithPermissionCheck(chatFragment);
                        } else if (isValidString(vidChatData.getVidUrl())) {
                            FullScreenVideoActivity.open(activity, context, vidChatData.getVidUrl());
                        }
                        break;
                }
                if (actionMode != null) {
                    actionMode.finish();
                }
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                if (chatDataList.get(getAdapterPosition()).getType().equalsIgnoreCase(GROUP_PROMPT)) {
                    if (actionMode != null) {
                        actionMode.finish();
                    }

                } else if (getAdapterPosition() != highlightedPos) {
                    actionMode = ((AppCompatActivity) context).startSupportActionMode(actionModeCallbacks);
                    dateTv.setVisibility(View.VISIBLE);
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
                super.onLongPress(e);
            }
        });

    }

    public class SentChatViewHolder extends RecyclerView.ViewHolder implements View.OnTouchListener {

        private RelativeLayout rootLayout;
        private EmojiTextView messageTv;
        private RelativeLayout linkContainer;
        private ImageView linkPicIv, attachPicIv;
        private TextView linkTitleTv;
        private EmojiTextView linkDescTv;
        private FrameLayout imgContainer;
        private FrameLayout vidContainer;
        private ImageView vidThumbnailIv;
        private ImageView playvidIv;
        private ProgressBar progressBar_vid;
        private ProgressBar progressBar;
        private ImageView chatIv;
        private TextView dateTv;
        private ImageView lubbIcon;
        private TextView lubbCount;
        @Nullable
        private ActionMode actionMode;
        private ImageView youtubeThumbnailView;
        private ProgressBar youtubeProgressBar;
        private ImageView youtubePlayIv;
        private RelativeLayout youtubeContainer;
        private TextView youtubeTitleTv;
        private LinearLayout pollContainer;
        private LinearLayout lubbContainer;
        private EmojiTextView badgeTextTv;
        private TextView senderTv;

        SentChatViewHolder(final View itemView) {
            super(itemView);
            rootLayout = itemView.findViewById(R.id.root_layout_chat_sent);
            messageTv = itemView.findViewById(R.id.tv_message);
            linkContainer = itemView.findViewById(R.id.link_meta_container);
            linkPicIv = itemView.findViewById(R.id.iv_link_pic);
            attachPicIv = itemView.findViewById(R.id.iv_attach_pic);
            linkTitleTv = itemView.findViewById(R.id.tv_link_title);
            linkDescTv = itemView.findViewById(R.id.tv_link_desc);
            imgContainer = itemView.findViewById(R.id.img_container);
            vidContainer = itemView.findViewById(R.id.vid_container);
            vidThumbnailIv = itemView.findViewById(R.id.iv_vid_img);
            playvidIv = itemView.findViewById(R.id.iv_play_vid);
            progressBar_vid = itemView.findViewById(R.id.progressbar_img_vid);
            progressBar = itemView.findViewById(R.id.progressbar_img);
            chatIv = itemView.findViewById(R.id.iv_chat_img);
            dateTv = itemView.findViewById(R.id.tv_date);
            lubbIcon = itemView.findViewById(R.id.iv_lubb);
            lubbCount = itemView.findViewById(R.id.tv_lubb_count);
            lubbContainer = itemView.findViewById(R.id.container_lubb);
            youtubeThumbnailView = itemView.findViewById(R.id.youtube_thumbnail_view);
            youtubeProgressBar = itemView.findViewById(R.id.progressbar_youtube);
            youtubePlayIv = itemView.findViewById(R.id.iv_youtube_play);
            youtubeContainer = itemView.findViewById(R.id.relativelayout_youtube);
            youtubeTitleTv = itemView.findViewById(R.id.tv_yt_title);
            pollContainer = itemView.findViewById(R.id.container_polls);
            senderTv = itemView.findViewById(R.id.tv_sender_name);
            badgeTextTv = itemView.findViewById(R.id.tv_badge_text);

            linkContainer.setOnTouchListener(this);
            pollContainer.setOnTouchListener(this);
            messageTv.setOnTouchListener(this);
            chatIv.setOnTouchListener(this);
            vidThumbnailIv.setOnTouchListener(this);
            rootLayout.setOnTouchListener(this);
            lubbContainer.setOnTouchListener(this);
        }

        private ActionMode.Callback actionModeCallbacks = new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.menu_chat, menu);
                menu.findItem(R.id.action_spam).setVisible(false);
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
                dateTv.setVisibility(View.INVISIBLE);
                if (highlightedPos != -1) {
                    notifyItemChanged(highlightedPos);
                    highlightedPos = -1;
                }
            }
        };

        private View touchedView;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            touchedView = v;
            return gestureDetector.onTouchEvent(event);
        }

        private GestureDetector gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onDown(MotionEvent event) {
                // triggers first for both single tap and long press
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                toggleLubb(getAdapterPosition(), true);
                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                switch (touchedView.getId()) {
                    case R.id.iv_dp:
                        ProfileActivity.open(context, chatDataList.get(getAdapterPosition()).getAuthorUid());
                        break;
                    case R.id.container_lubb:
                        toggleLubb(getAdapterPosition(), false);
                        break;
                    case R.id.link_meta_container:
                        ChatData chatData = chatDataList.get(getAdapterPosition());
                        if (GROUP.equalsIgnoreCase(chatData.getType())) {
                            ChatActivity.openForGroup(context, chatData.getAttachedGroupId(), false, null);
                        } else if (EVENT.equalsIgnoreCase(chatData.getType())) {
                            EventInfoActivity.open(context, chatData.getAttachedGroupId());
                        } else if (REPLY.equalsIgnoreCase(chatData.getType())) {
                            scrollToChatId(chatData.getReplyMsgId(), null);
                        } else if (LINK.equalsIgnoreCase(chatData.getType())) {
                            final URLSpan[] urls = messageTv.getUrls();
                            final String url = urls[0].getURL();
                            if (isValidString(url)) {
                                Intent i = new Intent(Intent.ACTION_VIEW);
                                i.setData(Uri.parse(url));
                                context.startActivity(i);
                            }
                        }
                        break;
                    case R.id.iv_chat_img:
                        ChatData imgChatData = chatDataList.get(getAdapterPosition());
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            // ask for external storage perm
                            ChatFragmentPermissionsDispatcher
                                    .getWritePermWithPermissionCheck(chatFragment);
                        } else if (isValidString(imgChatData.getImgUrl())) {
                            FullScreenImageActivity.open(activity, context, imgChatData.getImgUrl(), chatIv, null, R.drawable.ic_cancel_black_24dp);
                        }
                        break;
                    case R.id.iv_vid_img:
                        ChatData vidChatData = chatDataList.get(getAdapterPosition());
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            // ask for external storage perm
                            ChatFragmentPermissionsDispatcher
                                    .getWritePermWithPermissionCheck(chatFragment);
                        } else if (isValidString(vidChatData.getVidUrl())) {
                            FullScreenVideoActivity.open(activity, context, vidChatData.getVidUrl());
                        }
                        break;
                }
                if (actionMode != null) {
                    actionMode.finish();
                }
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                if (chatDataList.get(getAdapterPosition()).getType().equalsIgnoreCase(GROUP_PROMPT)) {
                    if (actionMode != null) {
                        actionMode.finish();
                    }

                } else if (getAdapterPosition() != highlightedPos) {
                    actionMode = ((AppCompatActivity) context).startSupportActionMode(actionModeCallbacks);
                    dateTv.setVisibility(View.VISIBLE);
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
                super.onLongPress(e);
            }
        });


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
