package in.lubble.app.map;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import in.lubble.app.R;

import static in.lubble.app.Constants.MAP_BTN_URL;
import static in.lubble.app.Constants.MAP_HTML;

public class MapFragment extends Fragment {

    private static final String TAG = "MapFragment";

    private WebView mapWebView;
    private MaterialButton submitBtn;

    private String mapViewHtml;
    private String btnLink;

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

        mapViewHtml = FirebaseRemoteConfig.getInstance().getString(MAP_HTML);;
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

        return view;
    }
}
