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

import java.util.ArrayList;

import static in.lubble.app.firebase.RealtimeDbHelper.getCreateOrJoinGroupRef;

public class ExploreActiv extends AppCompatActivity implements ExploreGroupAdapter.OnListFragmentInteractionListener {

    private ArrayList<ExploreGroupData> selectedGroupList = new ArrayList<>();
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
                for (ExploreGroupData groupData : selectedGroupList) {
                    getCreateOrJoinGroupRef().child(groupData.getFirebaseGroupId()).setValue(true);
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
    public void onListFragmentInteraction(ExploreGroupData item) {
        if (selectedGroupList.contains(item)) {
            selectedGroupList.remove(item);
            if (selectedGroupList.size() == 0) {
                joinBtn.setVisibility(View.GONE);
            }
        } else {
            selectedGroupList.add(item);
            joinBtn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
    }
}
