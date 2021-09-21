package in.lubble.app.feed_user_search;

import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import in.lubble.app.LubbleApp;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;
import in.lubble.app.chat.ChatMoreFragment;
import in.lubble.app.chat.ShareActiv;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.models.FeedGroupData;
import in.lubble.app.profile.RecyclerTouchListener;
import in.lubble.app.profile.StatusBottomSheetAdapter;
import in.lubble.app.receivers.ShareSheetReceiver;
import in.lubble.app.user_search.OnUserSelectedListener;
import in.lubble.app.user_search.SelectedUserAdapter;
import in.lubble.app.user_search.UserAdapter;
import in.lubble.app.utils.UiUtils;
import io.branch.referral.Branch;
import io.branch.referral.BranchError;

import static in.lubble.app.utils.ReferralUtils.getReferralIntentForFeedGroup;


public class FeedUserShareBottomSheetFrag extends BottomSheetDialogFragment {

    private static final String TAG = "UserSearchFrag";
    private OnUserSelectedListener mListener;
    private static final String ARG_LUBBLE_ID = "FeedUserSearchFrag_ARG_LUBBLE_ID";
    private static final String ARG_FEED_GROUP_ID = "FeedUserSearchFrag_ARG_GROUP_ID";
    private static final String ARG_FEED_GROU_DATA = "FeedUserSearchFrag_ARG_GROUP_DATA";

    private String lubbleId;
    private String feedGroupId;
    private FeedGroupData feedGroupData;
    private String sharingUrl = null;

    private LinearLayout fbContainer;
    private LinearLayout whatsappContainer;
    private LinearLayout moreContainer;
    private LinearLayout copyLinkContainer;
    private ProgressDialog sharingProgressDialog;

    private LinearLayout sendGroupContainer;
    private LinearLayout inviteLinksContainer;
    private View selectedMembersDiv;




    public FeedUserShareBottomSheetFrag( String feedGroupId, FeedGroupData feedGroupData) {
        // Required empty public constructor
        this.feedGroupId = feedGroupId;
        //lubbleId = lubbleId;
        this.feedGroupData = feedGroupData;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_feed_user_search, container, false);
        View view = inflater.inflate(R.layout.fragment_feed_user_search, container, false);
        inviteLinksContainer = view.findViewById(R.id.container_invite_links);
        sendGroupContainer = view.findViewById(R.id.container_send);
        fbContainer = view.findViewById(R.id.container_fb);
        whatsappContainer = view.findViewById(R.id.container_whatsapp);
        moreContainer = view.findViewById(R.id.container_more);
        copyLinkContainer = view.findViewById(R.id.container_copy_link);
        sharingProgressDialog = new ProgressDialog(getContext());
        initClickHandlers(feedGroupData);
        return view;
    }

    private void initClickHandlers(final FeedGroupData feedGroupData) {
        copyLinkContainer.setOnClickListener(v -> {
            Intent referralIntent = getReferralIntentForFeedGroup(getContext(), sharingUrl, sharingProgressDialog, feedGroupData, linkCreateListener);
            if (referralIntent != null) {
                ClipboardManager clipboard = (ClipboardManager) LubbleApp.getAppContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("lubble_share_link", sharingUrl);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(requireContext(), "LINK COPIED", Toast.LENGTH_SHORT).show();
                Analytics.triggerEvent(AnalyticsEvents.REFERRAL_COPY_LINK, getContext());
            } else {
                Toast.makeText(requireContext(), "RETRY", Toast.LENGTH_SHORT).show();
            }
        });
        fbContainer.setOnClickListener(v -> {
            Intent referralIntent = getReferralIntentForFeedGroup(getContext(), sharingUrl, sharingProgressDialog, feedGroupData, linkCreateListener);
            if (referralIntent != null) {
                boolean facebookAppFound = false;
                List<ResolveInfo> matches = getContext().getPackageManager().queryIntentActivities(referralIntent, 0);
                for (ResolveInfo info : matches) {
                    if (info.activityInfo.packageName.toLowerCase().startsWith("com.facebook.katana")) {
                        referralIntent.setPackage(info.activityInfo.packageName);
                        facebookAppFound = true;
                        break;
                    }
                }
                if (!facebookAppFound) {
                    String sharerUrl = "https://www.facebook.com/sharer/sharer.php?u=" + sharingUrl;
                    referralIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(sharerUrl));
                }
                startActivity(referralIntent);
                Analytics.triggerEvent(AnalyticsEvents.REFERRAL_FB_SHARE, getContext());
            }
        });
        whatsappContainer.setOnClickListener(v -> {
            final Intent referralIntent = getReferralIntentForFeedGroup(getContext(), sharingUrl, sharingProgressDialog, feedGroupData, linkCreateListener);
            if (referralIntent != null) {
                PackageManager pm = getContext().getPackageManager();
                try {
                    PackageInfo info = pm.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);
                    //Check if package exists or not. If not then code
                    //in catch block will be called
                    referralIntent.setPackage("com.whatsapp");

                    startActivity(Intent.createChooser(referralIntent, getString(R.string.refer_share_title)));

                    Analytics.triggerEvent(AnalyticsEvents.REFERRAL_WA_SHARE, getContext());

                } catch (PackageManager.NameNotFoundException e) {
                    Toast.makeText(getContext(), "You don't have WhatsApp! Please share it with another app", Toast.LENGTH_SHORT).show();
                }
            }
        });
        moreContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent referralIntent = getReferralIntentForFeedGroup(getContext(), sharingUrl, sharingProgressDialog, feedGroupData, linkCreateListener);
                if (referralIntent != null) {
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(
                            getContext(), 21,
                            new Intent(getContext(), ShareSheetReceiver.class),
                            PendingIntent.FLAG_UPDATE_CURRENT);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                        startActivity(Intent.createChooser(referralIntent, getString(R.string.refer_share_title), pendingIntent.getIntentSender()));
                    } else {
                        startActivity(Intent.createChooser(referralIntent, getString(R.string.refer_share_title)));
                    }
                    Analytics.triggerEvent(AnalyticsEvents.REFERRAL_MORE_SHARE, getContext());
                }
            }
        });
    }

    final Branch.BranchLinkCreateListener linkCreateListener = new Branch.BranchLinkCreateListener() {
        @Override
        public void onLinkCreate(String url, BranchError error) {
            if (url != null) {
                Log.d(TAG, "got my Branch link to share: " + url);
                sharingUrl = url;
                if (sharingProgressDialog != null && sharingProgressDialog.isShowing()) {
                    sharingProgressDialog.dismiss();
                }
            } else {
                Log.e(TAG, "Branch onLinkCreate: " + error.getMessage());
                FirebaseCrashlytics.getInstance().recordException(new IllegalStateException(error.getMessage()));
                if (isAdded() && isVisible()) {
                    Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    @Override
    public int getTheme() {
        return R.style.RoundedBottomSheetDialog;
    }

//    @NonNull
//    @Override
//    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        Dialog dialog = super.onCreateDialog(savedInstanceState);
//
//        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
//        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
//            @Override
//            public void onShow(final DialogInterface dialog) {
//                new Handler().post(new Runnable() {
//                    @Override
//                    public void run() {
//                        FrameLayout bottomSheet = (FrameLayout) ((BottomSheetDialog) dialog).findViewById(R.id.design_bottom_sheet);
//                        BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
//                    }
//                });
//            }
//        });
//        return dialog;
//    }
//
//
//
//    @Override
//    public void dismiss() {
//        super.dismiss();
//    }

}