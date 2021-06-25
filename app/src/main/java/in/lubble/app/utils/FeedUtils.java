package in.lubble.app.utils;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import in.lubble.app.GlideRequests;
import in.lubble.app.LubbleApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;
import io.getstream.core.models.Content;
import io.getstream.core.models.EnrichedActivity;

import static in.lubble.app.Constants.MSG_WATERMARK_TEXT;
import static in.lubble.app.utils.FileUtils.saveImageInGallery;
import static in.lubble.app.utils.UiUtils.dpToPx;

public class FeedUtils {

    public interface ShareListener {
        void onShareReady(Intent sharingIntent);
    }

    public static void requestPostShareIntent(GlideRequests glide, EnrichedActivity activity, Map<String, Object> extras, ShareListener shareListener) {
        final String msgShareUrl = LubbleSharedPrefs.getInstance().getMsgShareUrl();
        if (!TextUtils.isEmpty(msgShareUrl)) {
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Join your neighbourhood on Lubble");
            String message = "";
            if (extras != null) {
                message = String.valueOf(extras.get("message") == null ? "" : extras.get("message"));
            }
            Object actorName = activity.getActor().getData().get("name");
            String suffix = getMsgSuffix(actorName, LubbleSharedPrefs.getInstance().getMsgCopyShareUrl());
            sharingIntent.putExtra(Intent.EXTRA_TEXT, message + suffix);
            if (extras != null) {
                Object photoLinkExtra = extras.get("photoLink");
                if (photoLinkExtra != null && !TextUtils.isEmpty(String.valueOf(photoLinkExtra))) {
                    String photoLinkStr = (String) photoLinkExtra;
                    LubbleApp appContext = LubbleApp.getAppContext();
                    glide.asBitmap()
                            .load(photoLinkStr)
                            .transform(new RoundedCornersTransformation(dpToPx(8), 0))
                            .into(new CustomTarget<Bitmap>() {
                                @Override
                                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                    String savedPath = saveImageInGallery(resource, activity.getID(), appContext, Uri.parse(photoLinkStr));
                                    if (!TextUtils.isEmpty(savedPath)) {
                                        sharingIntent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(appContext, appContext.getPackageName() + ".fileprovider", new File(savedPath)));
                                        sharingIntent.setType("image/*");
                                        sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                        shareListener.onShareReady(sharingIntent);
                                    }
                                }

                                @Override
                                public void onLoadCleared(@Nullable Drawable placeholder) {
                                    shareListener.onShareReady(sharingIntent);
                                }
                            });
                } else {
                    shareListener.onShareReady(sharingIntent);
                }
            } else {
                shareListener.onShareReady(sharingIntent);
            }
        }
    }

    public static String getMsgSuffix(Object actorName, String shareUrl) {
        String suffix = "";
        if (shareUrl != null && !TextUtils.isEmpty(shareUrl)) {
            suffix = FirebaseRemoteConfig.getInstance().getString(MSG_WATERMARK_TEXT);
            shareUrl = shareUrl.replace("https://", "");
            suffix = "\n" + replaceSuffixKeys(suffix, actorName) + shareUrl;
        }
        return suffix;
    }

    public static String replaceSuffixKeys(String suffix, Object actorName) {
        final String authorNameKey = "{authorname}";
        final String lubbleNameKey = "{lubble}";
        if (suffix.contains(authorNameKey)) {
            if (actorName != null) {
                suffix = suffix.replace(authorNameKey, actorName.toString().split(" ")[0]);
            } else {
                suffix = suffix.replace(authorNameKey, "shared");
            }
        }
        if (suffix.contains(lubbleNameKey)) {
            suffix = suffix.replace(lubbleNameKey, LubbleSharedPrefs.getInstance().getLubbleName());
        }
        return suffix;
    }

    public static void processTrackedPosts(List<EnrichedActivity> enrichedActivities, VisibleState visibleState, @Nullable String feedName, String location) {
        new Handler().post(() -> {
            try {
                ArrayList<Content> contentList = new ArrayList<>();
                ArrayList<String> foreignIdList = new ArrayList<>();
                int firstPos = visibleState.getFirstCompletelyVisible();
                int lastPos = visibleState.getLastCompletelyVisible();
                if (firstPos > 0 && lastPos > 0) {
                    if (firstPos == lastPos) {
                        contentList.add(new Content(enrichedActivities.get(firstPos).getForeignID()));
                        foreignIdList.add(enrichedActivities.get(firstPos).getForeignID());
                    } else {
                        List<EnrichedActivity> subList = enrichedActivities.subList(firstPos, lastPos);
                        contentList.addAll(Lists.transform(subList, input -> new Content(input.getForeignID())));
                        foreignIdList.addAll(Lists.transform(subList, input -> input.getForeignID()));
                    }
                    Analytics.triggerFeedImpression(contentList, feedName, location);
                    Bundle bundle = new Bundle();
                    bundle.putStringArrayList("foreignIdList", foreignIdList);
                    bundle.putString("feedName", feedName);
                    bundle.putString("location", location);
                    Analytics.triggerEvent(AnalyticsEvents.FEED_POST_IMPRESSION, bundle, LubbleApp.getAppContext());
                }
            } catch (Exception e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        });
    }

}
