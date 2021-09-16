package in.lubble.app.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import in.lubble.app.Constants;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.models.FeedGroupData;
import in.lubble.app.models.GroupData;
import io.branch.indexing.BranchUniversalObject;
import io.branch.referral.Branch;
import io.branch.referral.util.ContentMetadata;
import io.branch.referral.util.LinkProperties;
import io.getstream.client.Feed;

public class ReferralUtils {

    private static final String TAG = "ReferralUtils";

    public static void generateBranchUrl(Context context, Branch.BranchLinkCreateListener callback) {
        generateBranchUrl(context, null, true, callback);
    }

    public static void generateBranchUrl(Context context, @Nullable String campaignName, boolean showLinkMetaData, Branch.BranchLinkCreateListener callback) {
        BranchUniversalObject branchUniversalObject = new BranchUniversalObject()
                .setCanonicalIdentifier("lbl/referralCode/" + FirebaseAuth.getInstance().getUid())
                .setContentMetadata(new ContentMetadata().addCustomMetadata("referrer_uid", FirebaseAuth.getInstance().getUid()));

        if (showLinkMetaData) {
            branchUniversalObject.setTitle("Join your neighbours on Lubble")
                    .setContentDescription("Connect with your neighbourhood, get advice, help & be a part of the local community")
                    .setContentImageUrl("https://i.imgur.com/JFsrCOs.png")
                    .setContentIndexingMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
                    .setLocalIndexMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC);
        } else {
            branchUniversalObject.setContentIndexingMode(BranchUniversalObject.CONTENT_INDEX_MODE.PRIVATE);
            branchUniversalObject.setLocalIndexMode(BranchUniversalObject.CONTENT_INDEX_MODE.PRIVATE);
        }

        final LinkProperties linkProperties = new LinkProperties()
                .setChannel("Android")
                .setFeature("Referral")
                .addControlParameter("$desktop_url", "https://lubble.in")
                .addControlParameter("$ios_url", "https://lubble.in");

        if (!TextUtils.isEmpty(campaignName)) {
            linkProperties.setCampaign(campaignName);
        }
        branchUniversalObject.generateShortUrl(context, linkProperties, callback);
    }

    @Nullable
    public static Intent getReferralIntent(Context context, String sharingUrl, ProgressDialog sharingProgressDialog, Branch.BranchLinkCreateListener callback) {
        if (TextUtils.isEmpty(sharingUrl)) {
            sharingProgressDialog.setTitle("Generating Invite Link");
            sharingProgressDialog.setMessage(context.getString(R.string.all_please_wait));
            sharingProgressDialog.show();
            generateBranchUrl(context, callback);
            return null;
        } else {
            // URL is ready to be wrapped in an Intent
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Join me & your neighbours on Lubble");
            String message = FirebaseRemoteConfig.getInstance().getString(Constants.REFER_MSG);
            sharingIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(context.getString(R.string.refer_msg)) + sharingUrl);
            return sharingIntent;
        }
    }

    public static void generateBranchUrlForGroup(Context context, Branch.BranchLinkCreateListener callback, GroupData groupData) {
        BranchUniversalObject branchUniversalObject = new BranchUniversalObject()
                .setCanonicalIdentifier("lbl/groupInvite/" + groupData.getId())
                .setTitle("Invite to join " + groupData.getTitle() + " group")
                .setContentDescription("Join me in " + groupData.getTitle() + " group for " + LubbleSharedPrefs.getInstance().getLubbleName())
                .setContentImageUrl(groupData.getThumbnail())
                .setContentIndexingMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
                .setLocalIndexMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
                .setContentMetadata(new ContentMetadata().addCustomMetadata("referrer_uid", FirebaseAuth.getInstance().getUid()).addCustomMetadata("group_id", groupData.getId()));

        final LinkProperties linkProperties = new LinkProperties()
                .setChannel("Android")
                .setFeature("GroupInvite")
                .addControlParameter("$desktop_url", "https://play.google.com/store/apps/details?id=in.lubble.app")
                .addControlParameter("$ios_url", "https://play.google.com/store/apps/details?id=in.lubble.app");

        branchUniversalObject.generateShortUrl(context, linkProperties, callback);
    }

    public static void generateBranchUrlForFeedGroup(Context context, Branch.BranchLinkCreateListener callback, FeedGroupData feedGroupData) {
        BranchUniversalObject branchUniversalObject = new BranchUniversalObject()
                .setCanonicalIdentifier("lbl/groupInvite/" + feedGroupData.getId())
                .setTitle("Invite to join " + feedGroupData.getName() + " group")
                .setContentDescription("Join me in " + feedGroupData.getName() + " group for " + LubbleSharedPrefs.getInstance().getLubbleName())
                .setContentImageUrl(feedGroupData.getPhotoUrl())
                .setContentIndexingMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
                .setLocalIndexMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
                .setContentMetadata(new ContentMetadata().addCustomMetadata("referrer_uid", FirebaseAuth.getInstance().getUid()).addCustomMetadata("group_id", Integer.toString(feedGroupData.getId())));

        final LinkProperties linkProperties = new LinkProperties()
                .setChannel("Android")
                .setFeature("GroupInvite")
                .addControlParameter("$desktop_url", "https://play.google.com/store/apps/details?id=in.lubble.app")
                .addControlParameter("$ios_url", "https://play.google.com/store/apps/details?id=in.lubble.app");

        branchUniversalObject.generateShortUrl(context, linkProperties, callback);
    }

    @Nullable
    public static Intent getReferralIntentForGroup(Context context, String sharingUrl, ProgressDialog sharingProgressDialog, GroupData groupData, Branch.BranchLinkCreateListener callback) {
        if (TextUtils.isEmpty(sharingUrl)) {
            sharingProgressDialog.setTitle("Generating Invite Link");
            sharingProgressDialog.setMessage(context.getString(R.string.all_please_wait));
            sharingProgressDialog.show();
            generateBranchUrlForGroup(context, callback, groupData);
            return null;
        } else {
            // URL is ready to be wrapped in an Intent
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Join me & your neighbours on Lubble");
            String message = FirebaseRemoteConfig.getInstance().getString(Constants.REFER_MSG);
            final String lubbleName = LubbleSharedPrefs.getInstance().getLubbleName();
            sharingIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(String.format(
                    context.getString(R.string.refer_msg_group), groupData.getTitle(), lubbleName
            )) + sharingUrl);
            return sharingIntent;
        }
    }

    @Nullable
    public static Intent getReferralIntentForFeedGroup(Context context, String sharingUrl, ProgressDialog sharingProgressDialog, FeedGroupData feedGroupData, Branch.BranchLinkCreateListener callback) {
        if (TextUtils.isEmpty(sharingUrl)) {
            sharingProgressDialog.setTitle("Generating Invite Link");
            sharingProgressDialog.setMessage(context.getString(R.string.all_please_wait));
            sharingProgressDialog.show();
            generateBranchUrlForFeedGroup(context,callback,feedGroupData);
            return null;
        } else {
            // URL is ready to be wrapped in an Intent
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Join me & your neighbours on Lubble");
            String message = FirebaseRemoteConfig.getInstance().getString(Constants.REFER_MSG);
            final String lubbleName = LubbleSharedPrefs.getInstance().getLubbleName();
            sharingIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(String.format(
                    context.getString(R.string.refer_msg_group), feedGroupData.getName(), lubbleName
            )) + sharingUrl);
            return sharingIntent;
        }
    }

}
