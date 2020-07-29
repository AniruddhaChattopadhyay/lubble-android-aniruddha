package in.lubble.app.user_search;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.widget.Toolbar;

import in.lubble.app.BaseActivity;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;

import static in.lubble.app.utils.FragUtils.replaceFrag;

public class UserSearchActivity extends BaseActivity {

    private static final String EXTRA_GROUP_ID = "UserSearchActivity_GroupId";

    public static void newInstance(Context context, String groupId) {
        Intent intent = new Intent(context, UserSearchActivity.class);
        intent.putExtra(EXTRA_GROUP_ID, groupId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_search);


        Toolbar toolbar = findViewById(R.id.text_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final String groupId = getIntent().getStringExtra(EXTRA_GROUP_ID);
        replaceFrag(getSupportFragmentManager(), UserSearchFrag.newInstance(LubbleSharedPrefs.getInstance().requireLubbleId(), groupId), R.id.frame_fragContainer);

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
