package in.lubble.app.explore;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.utils.FragUtils;

import java.util.HashMap;

import static in.lubble.app.firebase.RealtimeDbHelper.getCreateOrJoinGroupRef;

public class ExploreActiv extends AppCompatActivity implements ExploreGroupAdapter.OnListFragmentInteractionListener {

    private HashMap<String, Boolean> selectedGroupIdMap = new HashMap<>();
    private ImageView crossIv;
    private Button joinBtn;

    public static void open(Context context) {
        context.startActivity(new Intent(context, ExploreActiv.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);

        crossIv = findViewById(R.id.iv_cross);
        joinBtn = findViewById(R.id.btn_join);

        Analytics.triggerScreenEvent(this, this.getClass());

        FragUtils.replaceFrag(getSupportFragmentManager(), ExploreFrag.newInstance(), R.id.frag_container);

        joinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                joinBtn.setEnabled(false);
                for (String groupId : selectedGroupIdMap.keySet()) {
                    getCreateOrJoinGroupRef().child(groupId).setValue(true);
                }
                finish();
            }
        });

        crossIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
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
    }
}
