package in.lubble.app.explore;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.utils.FragUtils;

public class ExploreActiv extends AppCompatActivity {

    public static void open(Context context) {
        context.startActivity(new Intent(context, ExploreActiv.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explore);

        Analytics.triggerScreenEvent(this, this.getClass());

        FragUtils.addFrag(getSupportFragmentManager(), R.id.frag_container, ExploreFrag.newInstance());

    }
}
