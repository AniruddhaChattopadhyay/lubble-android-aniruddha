package in.lubble.app.groups;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.widget.Toolbar;
import in.lubble.app.BaseActivity;
import in.lubble.app.R;

import static in.lubble.app.utils.FragUtils.replaceFrag;

public class NewGroupActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group);

        Toolbar toolbar = findViewById(R.id.text_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        replaceFrag(getSupportFragmentManager(), NewGroupFragment.newInstance(), R.id.frame_fragContainer);

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
