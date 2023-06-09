package in.lubble.app;

import static in.lubble.app.MainActivity.EXTRA_TAB_NAME;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.TaskStackBuilder;

import com.clevertap.android.sdk.CleverTapAPI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.json.JSONObject;

import java.util.List;

import in.lubble.app.chat.ChatActivity;
import in.lubble.app.events.EventInfoActivity;
import in.lubble.app.feed_groups.SingleGroupFeed.GroupFeedActivity;
import in.lubble.app.feed_post.FeedPostActivity;
import in.lubble.app.leaderboard.LeaderboardActivity;
import in.lubble.app.lubble_info.LubbleActivity;
import in.lubble.app.marketplace.ItemActivity;
import in.lubble.app.marketplace.ItemListActiv;
import in.lubble.app.marketplace.SellerDashActiv;
import in.lubble.app.models.FeedGroupData;
import in.lubble.app.models.marketplace.Item;
import in.lubble.app.profile.ProfileActivity;
import in.lubble.app.referrals.ReferralActivity;
import in.lubble.app.services.ServiceCategoryDetailActiv;
import io.branch.indexing.BranchUniversalObject;
import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import io.branch.referral.util.LinkProperties;

public class DeepLinkRouterActiv extends BaseActivity {

    private static final String TAG = "DeepLinkRouterActiv";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            // user is NOT logged in
            startActivity(new Intent(this, MainActivity.class));
            FirebaseCrashlytics.getInstance().recordException(new IllegalAccessException("tried to open deeplink without login"));
            finish();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        final Uri uri = getIntent().getData();
        Branch branch = Branch.getInstance();
        CleverTapAPI clevertapInstance = CleverTapAPI.getDefaultInstance(this);
        if (clevertapInstance != null) {
            branch.setRequestMetadata("$clevertap_attribution_id",
                    clevertapInstance.getCleverTapAttributionIdentifier());
        }

        if (uri != null) {
            final String scheme = uri.getScheme().toLowerCase();
            final String host = uri.getHost().toLowerCase();

            if ("lubble".equals(scheme)) {
                openCustomSchemeLink(uri);
            } else if ("https".equals(scheme) && "www.shop.lubble.in".equals(host) || "shop.lubble.in".equals(host)) {
                openShopWebLink(uri);
            } else {
                // if activity is in foreground (or in backstack but partially visible) launching the same
                // activity will skip onStart, handle this case with reInitSession
                Branch.BranchUniversalReferralInitListener branchReferralInitListener = (branchUniversalObject, linkProperties, error) -> {
                    // do something with branchUniversalObject/linkProperties..
                    if (linkProperties != null && error == null) {
                        if (branchUniversalObject != null && branchUniversalObject.getCanonicalIdentifier() != null) {
                            openCustomSchemeLink(Uri.parse(branchUniversalObject.getCanonicalIdentifier()));
                        } else {
                            startActivity(new Intent(DeepLinkRouterActiv.this, MainActivity.class));
                            finish();
                        }
                    } else {
                        if (error != null) {
                            Log.e(TAG, "onInitFinished: " + error.toString());
                        }
                        startActivity(new Intent(DeepLinkRouterActiv.this, MainActivity.class));
                        finish();
                    }
                };
                getIntent().putExtra("branch_force_new_session", true);
                Branch.sessionBuilder(this).withCallback(branchReferralInitListener).withData(getIntent().getData()).reInit();
            }
        } else {
            FirebaseCrashlytics.getInstance().recordException(new IllegalArgumentException("ILLEGAL INTENT for DeepLinkRouterActiv"));
            startActivity(new Intent(DeepLinkRouterActiv.this, MainActivity.class));
            finish();
        }
    }

    private void openCustomSchemeLink(Uri uri) {
        final String host = uri.getHost().toLowerCase();

        switch (host) {
            case "profile":
                ProfileActivity.open(this, uri.getLastPathSegment());
                break;
            case "market.item":
                startActivity(ItemActivity.getIntent(this, Integer.parseInt(uri.getLastPathSegment())));
                break;
            case "market.item_list":
                if (uri.toString().contains("category")) {
                    ItemListActiv.open(this, false, Integer.parseInt(uri.getLastPathSegment()));
                } else {
                    ItemListActiv.open(this, true, uri.getLastPathSegment());
                }
                break;
            case "service.category":
                ServiceCategoryDetailActiv.open(this, Integer.parseInt(uri.getLastPathSegment()));
                break;
            case "referrals":
                ReferralActivity.open(this, true);
                break;
            case "rewards":
                ReferralActivity.open(this);
                break;
            case "seller_dash":
                startActivity(SellerDashActiv.getIntent(this, LubbleSharedPrefs.getInstance().getSellerId(), false, Item.ITEM_PRODUCT));
                break;
            case "wiki":
                LubbleActivity.open(this);
                break;
            case "chats":
                final String groupId = uri.getQueryParameter("id");
                if (groupId != null) {
                    ChatActivity.openForGroup(this, groupId, false, null);
                } else {
                    startActivity(new Intent(this, MainActivity.class));
                }
                break;
            case "chats.more":
                final String id = uri.getQueryParameter("id");
                if (id != null) {
                    ChatActivity.openGroupMore(this, id);
                } else {
                    startActivity(new Intent(this, MainActivity.class));
                }
                break;
            case "games":
                final Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra(EXTRA_TAB_NAME, "games");
                startActivity(intent);
                break;
            case "services":
                final Intent serviceIntent = new Intent(this, MainActivity.class);
                serviceIntent.putExtra(EXTRA_TAB_NAME, "services");
                startActivity(serviceIntent);
                break;
            case "market":
                final Intent marketIntent = new Intent(this, MainActivity.class);
                marketIntent.putExtra(EXTRA_TAB_NAME, "mplace");
                startActivity(marketIntent);
                break;
            case "explore":
                final Intent exploreIntent = new Intent(this, MainActivity.class);
                exploreIntent.putExtra(EXTRA_TAB_NAME, "explore");
                startActivity(exploreIntent);
                break;
            case "feed":
                final Intent feedIntent = new Intent(this, MainActivity.class);
                feedIntent.putExtra(EXTRA_TAB_NAME, "feed");
                startActivity(feedIntent);
                break;
            case "events":
                final String event_id = uri.getQueryParameter("id");
                if (event_id != null) {
                    EventInfoActivity.open(this, event_id);
                } else {
                    final Intent eventsIntent = new Intent(this, MainActivity.class);
                    eventsIntent.putExtra(EXTRA_TAB_NAME, "events");
                    startActivity(eventsIntent);
                }
                break;
            case "leaderboard":
                LeaderboardActivity.open(this);
                break;
            case "feed_post":
                final String postId = uri.getQueryParameter("id");
                final Intent feedFallbackIntent = new Intent(this, MainActivity.class);
                feedFallbackIntent.putExtra(EXTRA_TAB_NAME, "feed");
                if (postId != null) {
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                    stackBuilder.addParentStack(MainActivity.class);
                    stackBuilder.addNextIntent(feedFallbackIntent);
                    stackBuilder.addNextIntent(FeedPostActivity.getIntent(this, postId));
                    stackBuilder.startActivities();
                } else {
                    startActivity(feedFallbackIntent);
                }
                break;
            case "feed_group":
                final String feedGroupName = uri.getQueryParameter("name");
                final Intent feedIntent1 = new Intent(this, MainActivity.class);
                feedIntent1.putExtra(EXTRA_TAB_NAME, "feed");
                if (feedGroupName != null) {
                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                    stackBuilder.addParentStack(MainActivity.class);
                    stackBuilder.addNextIntent(feedIntent1);
                    stackBuilder.addNextIntent(GroupFeedActivity.getIntent(this, feedGroupName));
                    stackBuilder.startActivities();
                } else {
                    startActivity(feedIntent1);
                }
                break;
            default:
                startActivity(new Intent(this, MainActivity.class));
        }
        finish();
    }

    private void openShopWebLink(Uri uri) {
        final List<String> pathSegments = uri.getPathSegments();

        switch (pathSegments.get(0).toLowerCase()) {
            case "item":
                startActivity(ItemActivity.getIntent(this, Integer.parseInt(pathSegments.get(1).toLowerCase())));
                break;
            case "category":
                ItemListActiv.open(this, false, Integer.parseInt(uri.getLastPathSegment()));
                break;
            default:
                ItemListActiv.open(this, true, uri.getLastPathSegment());
                break;
        }
        finish();
    }

}
