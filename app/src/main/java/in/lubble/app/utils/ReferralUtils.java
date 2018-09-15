package in.lubble.app.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.firebase.auth.FirebaseAuth;

import in.lubble.app.R;
import io.branch.indexing.BranchUniversalObject;
import io.branch.referral.Branch;
import io.branch.referral.util.ContentMetadata;
import io.branch.referral.util.LinkProperties;

public class ReferralUtils {

    private static final String TAG = "ReferralUtils";

    public static void generateBranchUrl(Context context, Branch.BranchLinkCreateListener callback) {
        BranchUniversalObject branchUniversalObject = new BranchUniversalObject()
                .setCanonicalIdentifier("lbl/referralCode/" + FirebaseAuth.getInstance().getUid())
                .setTitle("Join your neighbours on Lubble")
                .setContentDescription("Know what's happening in your neighbourhood, buy or sell items around you")
                .setContentImageUrl("https://i.imgur.com/JFsrCOs.png")
                .setContentIndexingMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
                .setLocalIndexMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
                .setContentMetadata(new ContentMetadata().addCustomMetadata("referrer_uid", FirebaseAuth.getInstance().getUid()));

        final LinkProperties linkProperties = new LinkProperties()
                .setChannel("Android")
                .setFeature("Referral")
                .addControlParameter("$desktop_url", "https://lubble.in")
                .addControlParameter("$ios_url", "https://lubble.in");

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
            String message = "Hey,\n\nI would love to invite you to Lubble, a private social network just for you & your neighbours " +
                    "living together in the same society.\n\nJoin Now: " + sharingUrl + "\n\nJoin the Lubble app to" +
                    "\n- Connect & interact with your neighbours" +
                    "\n- Buy & Sell items around you" +
                    "\n- Get nearby recommendations for plumbers & such services" +
                    "\n and get to know the lastest happenings around you!\n\n";
            sharingIntent.putExtra(Intent.EXTRA_TEXT, message + "Check it out: " + sharingUrl);
            return sharingIntent;
        }
    }

}
