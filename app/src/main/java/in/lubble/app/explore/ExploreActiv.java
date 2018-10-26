package in.lubble.app.explore;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.utils.FragUtils;

import java.util.HashMap;

import static in.lubble.app.firebase.RealtimeDbHelper.getCreateOrJoinGroupRef;

public class ExploreActiv extends AppCompatActivity implements ExploreGroupAdapter.OnListFragmentInteractionListener {

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

        Analytics.triggerScreenEvent(this, this.getClass());

        isNewUser = getIntent().getBooleanExtra(IS_NEW_USER, false);

        FragUtils.replaceFrag(getSupportFragmentManager(), ExploreFrag.newInstance(), R.id.frag_container);

        joinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                joinBtn.setEnabled(false);
                for (String groupId : selectedGroupIdMap.keySet()) {
                    getCreateOrJoinGroupRef().child(groupId).setValue(true);
                }
                finish();
                overridePendingTransition(R.anim.none, R.anim.slide_to_bottom);
            }
        });

        if (isNewUser) {
            crossIv.setColorFilter(ContextCompat.getColor(this, R.color.black), android.graphics.PorterDuff.Mode.SRC_IN);
        }

        crossIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.none, R.anim.slide_to_bottom);
            }
        });

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
        }
    }
}
