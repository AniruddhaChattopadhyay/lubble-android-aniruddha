package in.lubble.app.groups.group_info;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import in.lubble.app.R;

import static in.lubble.app.utils.FragUtils.replaceFrag;

public class GroupInfoActivity extends AppCompatActivity {

    private static final String EXTRA_GROUP_ID = "GroupInfoActivity_GroupId";

    public static void newInstance(Context context, String groupId) {
        final Intent intent = new Intent(context, GroupInfoActivity.class);
        intent.putExtra(EXTRA_GROUP_ID, groupId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);

        Toolbar toolbar = findViewById(R.id.transparent_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("");

        replaceFrag(
                getSupportFragmentManager(),
                GroupInfoFragment.newInstance(getIntent().getStringExtra(EXTRA_GROUP_ID)), R.id.frame_fragContainer
        );
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
