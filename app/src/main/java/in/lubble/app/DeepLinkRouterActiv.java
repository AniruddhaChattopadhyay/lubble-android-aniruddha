package in.lubble.app;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;
import in.lubble.app.marketplace.ItemActivity;
import in.lubble.app.marketplace.ItemListActiv;
import in.lubble.app.marketplace.SellerDashActiv;
import in.lubble.app.models.marketplace.Item;
import in.lubble.app.profile.ProfileActivity;
import in.lubble.app.referrals.ReferralActivity;
import in.lubble.app.services.ServiceCategoryDetailActiv;

import java.util.List;

public class DeepLinkRouterActiv extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            // user is NOT logged in
            finish();
            Crashlytics.logException(new IllegalAccessException("tried to open deeplink without login"));
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
            Crashlytics.logException(new IllegalArgumentException("ILLEGAL INTENT for DeepLinkRouterActiv"));
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
                ReferralActivity.open(this);
                break;
            case "seller_dash":
                startActivity(SellerDashActiv.getIntent(this, LubbleSharedPrefs.getInstance().getSellerId(), false, Item.ITEM_PRODUCT));
                break;
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
