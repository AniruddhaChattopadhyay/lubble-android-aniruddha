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
                .setContentDescription("Explore your neighbourhood, buy or sell items around you & so much more")
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
            String message = FirebaseRemoteConfig.getInstance().getString(Constants.REFER_MSG);
            sharingIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(context.getString(R.string.refer_msg)) + sharingUrl);
            return sharingIntent;
        }
    }

}
