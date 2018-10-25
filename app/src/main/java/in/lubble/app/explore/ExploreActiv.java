package in.lubble.app.explore;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.utils.FragUtils;

import java.util.ArrayList;

public class ExploreActiv extends AppCompatActivity implements ExploreGroupAdapter.OnListFragmentInteractionListener {

    private ArrayList<ExploreGroupData> selectedGroupList = new ArrayList<>();
    private Button joinBtn;

    public static void open(Context context) {
        context.startActivity(new Intent(context, ExploreActiv.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);

        joinBtn = findViewById(R.id.btn_join);

        Analytics.triggerScreenEvent(this, this.getClass());

        FragUtils.replaceFrag(getSupportFragmentManager(), ExploreFrag.newInstance(), R.id.frag_container);

        joinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

    @Override
    public void onListFragmentInteraction(ExploreGroupData item) {
        if (selectedGroupList.contains(item)) {
            selectedGroupList.remove(item);
            if (joinBtn.getVisibility() == View.VISIBLE && selectedGroupList.size() == 0) {
                joinBtn.setVisibility(View.GONE);
            }
        } else {
            selectedGroupList.add(item);
            if (joinBtn.getVisibility() != View.VISIBLE) {
                joinBtn.setVisibility(View.VISIBLE);
            }
        }
    }
}