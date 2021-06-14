package in.lubble.app.explore;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.util.HashMap;

import in.lubble.app.BaseActivity;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.analytics.AnalyticsEvents;
import in.lubble.app.chat.GroupPromptSharedPrefs;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.utils.FragUtils;

import static in.lubble.app.analytics.AnalyticsEvents.EXPLORE_CONTINUE_CLICKED;
import static in.lubble.app.analytics.AnalyticsEvents.EXPLORE_DIALOG_SHOWN;
import static in.lubble.app.firebase.RealtimeDbHelper.bulkJoinGroupV2Ref;

public class ExploreActiv extends BaseActivity implements ExploreGroupAdapter.OnListFragmentInteractionListener {

    private static final String IS_NEW_USER = "IS_NEW_USER";

    private HashMap<String, Boolean> selectedGroupIdMap = new HashMap<>();
    private ImageView crossIv;
    private Button joinBtn;
    private boolean isNewUser;

    public static void open(Context context, boolean isNewUser) {
        final Intent intent = new Intent(context, ExploreActiv.class);
        intent.putExtra(IS_NEW_USER, isNewUser);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);

        crossIv = findViewById(R.id.iv_cross);
        joinBtn = findViewById(R.id.btn_join);
        TextView subtitleTv = findViewById(R.id.tv_subtitle);
        subtitleTv.setVisibility(View.GONE);

        Analytics.triggerScreenEvent(this, this.getClass());

        isNewUser = getIntent().getBooleanExtra(IS_NEW_USER, false);

        FragUtils.replaceFrag(getSupportFragmentManager(), ExploreFrag.newInstance(), R.id.frag_container);

        joinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                joinBtn.setEnabled(false);
                Analytics.triggerEvent(EXPLORE_CONTINUE_CLICKED, ExploreActiv.this);
                String groupIdList = "";
                for (String groupId : selectedGroupIdMap.keySet()) {
                    groupIdList = groupIdList + ',' + groupId;
                    GroupPromptSharedPrefs.getInstance().putGroupId(groupId);
                    Bundle bundle = new Bundle();
                    bundle.putString("group_id", groupId);
                    Analytics.triggerEvent(AnalyticsEvents.JOIN_GROUP, bundle, ExploreActiv.this);
                }
                groupIdList = groupIdList.substring(groupIdList.indexOf(',') + 1);
                bulkJoinGroupV2Ref().child(groupIdList).setValue(true);
                final ProgressDialog progressDialog = new ProgressDialog(ExploreActiv.this);
                progressDialog.setTitle("Joining " + selectedGroupIdMap.size() + " groups");
                progressDialog.setMessage("You're about to join a lovely & helpful community, please be respectful & enjoy :)");
                progressDialog.setCancelable(false);
                progressDialog.show();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!isFinishing()) {
                            progressDialog.dismiss();
                            finish();
                            overridePendingTransition(R.anim.none, R.anim.slide_to_bottom);
                        }
                    }
                }, 4000);
            }
        });

        if (!isNewUser) {
            crossIv.setColorFilter(ContextCompat.getColor(this, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN);
        }

        crossIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.none, R.anim.slide_to_bottom);
            }
        });

        LubbleSharedPrefs.getInstance().setIsExploreShown(true);
        RealtimeDbHelper.getThisUserRef().child("isExploreShown").setValue(true);
        Analytics.triggerEvent(EXPLORE_DIALOG_SHOWN, this);

    }

    @Override
    public void onListFragmentInteraction(ExploreGroupData item, boolean isAdded) {
        if (isAdded) {
            selectedGroupIdMap.put(item.getFirebaseGroupId(), true);
            joinBtn.setVisibility(View.VISIBLE);

        } else {
            selectedGroupIdMap.remove(item.getFirebaseGroupId());
            if (selectedGroupIdMap.size() == 0) {
                joinBtn.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (!isNewUser) {
            // allow old user to go back
            super.onBackPressed();
            overridePendingTransition(R.anim.none, R.anim.slide_to_bottom);
        }
    }
}
