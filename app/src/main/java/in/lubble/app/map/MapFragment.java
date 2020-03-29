package in.lubble.app.map;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;

import static in.lubble.app.Constants.MAP_BTN_URL;
import static in.lubble.app.Constants.MAP_HTML;
import static in.lubble.app.Constants.MAP_SHARE_TEXT;

public class MapFragment extends Fragment {

    private static final String TAG = "MapFragment";

    private WebView mapWebView;
    private RelativeLayout btnsContainer;
    private MaterialCardView disclaimerCv;
    private MaterialButton submitBtn, shareBtn, dismissDisclaimerBtn;

    private String mapViewHtml, mapShareText, btnLink;

    public MapFragment() {
        // Required empty public constructor
    }

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        mapWebView = view.findViewById(R.id.webview_map);
        submitBtn = view.findViewById(R.id.btn_submit);
        shareBtn = view.findViewById(R.id.btn_whatsapp_share);
        dismissDisclaimerBtn = view.findViewById(R.id.btn_dismiss_disclaimer);
        btnsContainer = view.findViewById(R.id.container_map_btns);
        disclaimerCv = view.findViewById(R.id.cv_map_disclaimer);

        if (LubbleSharedPrefs.getInstance().getIsMapDisclaimerClosed()) {
            disclaimerCv.setVisibility(View.GONE);
            btnsContainer.setVisibility(View.VISIBLE);
        } else {
            disclaimerCv.setVisibility(View.VISIBLE);
            btnsContainer.setVisibility(View.GONE);
        }

        mapViewHtml = FirebaseRemoteConfig.getInstance().getString(MAP_HTML);
        mapShareText = FirebaseRemoteConfig.getInstance().getString(MAP_SHARE_TEXT);

        btnLink = FirebaseRemoteConfig.getInstance().getString(MAP_BTN_URL);
        if (btnLink.contains("^^")) {
            btnLink = btnLink.replace("^^uid", FirebaseAuth.getInstance().getUid());
            btnLink = btnLink.replace("^^username", FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        }

        mapWebView.getSettings().setJavaScriptEnabled(true);

        mapWebView.loadData(mapViewHtml, "text/html", null);
        mapWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }
        });

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder();
                intentBuilder.setToolbarColor(ContextCompat.getColor(requireContext(), R.color.colorAccent));
                intentBuilder.setSecondaryToolbarColor(ContextCompat.getColor(requireContext(), R.color.dk_colorAccent));
                intentBuilder.enableUrlBarHiding();
                intentBuilder.setShowTitle(true);
                CustomTabsIntent customTabsIntent = intentBuilder.build();
                customTabsIntent.launchUrl(requireContext(), Uri.parse(btnLink));
            }
        });

        dismissDisclaimerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LubbleSharedPrefs.getInstance().setIsMapDisclaimerClosed(true);
                disclaimerCv.setVisibility(View.GONE);
                btnsContainer.setVisibility(View.VISIBLE);
            }
        });

        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(Intent.EXTRA_TEXT, mapShareText);

                PackageManager pm = getContext().getPackageManager();
                try {
                    PackageInfo info = pm.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);
                    //Check if package exists or not. If not then code
                    //in catch block will be called
                    sharingIntent.setPackage("com.whatsapp");

                    startActivity(Intent.createChooser(sharingIntent, getString(R.string.refer_share_title)));

                    Analytics.triggerEvent(AnalyticsEvents.MAP_SHARE, getContext());

                } catch (PackageManager.NameNotFoundException e) {
                    Toast.makeText(getContext(), "Whatsapp not found on your phone", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }
}
