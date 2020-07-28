package in.lubble.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import in.lubble.app.chat.ChatActivity;
import in.lubble.app.events.EventInfoActivity;
import in.lubble.app.leaderboard.LeaderboardActivity;
import in.lubble.app.marketplace.ItemActivity;
import in.lubble.app.marketplace.ItemListActiv;
import in.lubble.app.marketplace.SellerDashActiv;
import in.lubble.app.models.marketplace.Item;
import in.lubble.app.profile.ProfileActivity;
import in.lubble.app.referrals.ReferralActivity;
import in.lubble.app.services.ServiceCategoryDetailActiv;

import static in.lubble.app.MainActivity.EXTRA_TAB_NAME;

public class DeepLinkRouterActiv extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            // user is NOT logged in
            finish();
            FirebaseCrashlytics.getInstance().recordException(new IllegalAccessException("tried to open deeplink without login"));
            return;
        }

        final Uri uri = getIntent().getData();

        if (uri != null) {
            final String scheme = uri.getScheme().toLowerCase();
            final String host = uri.getHost().toLowerCase();

            if ("lubble".equals(scheme)) {
                openCustomSchemeLink(uri);
            } else if ("https".equals(scheme) && "www.shop.lubble.in".equals(host) || "shop.lubble.in".equals(host)) {
                openShopWebLink(uri);
            }
        } else {
            FirebaseCrashlytics.getInstance().recordException(new IllegalArgumentException("ILLEGAL INTENT for DeepLinkRouterActiv"));
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
            default:
                startActivity(new Intent(this, MainActivity.class));
        }
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
    }

}
