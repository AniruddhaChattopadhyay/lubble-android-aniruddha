package in.lubble.app.map;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.widget.Toolbar;

import in.lubble.app.BaseActivity;
import in.lubble.app.R;

public class LubbleMapActivity extends BaseActivity {

    public static void open(Context context) {
        context.startActivity(new Intent(context, LubbleMapActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lubble_map);

        Toolbar toolbar = findViewById(R.id.text_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Neighbourhood Map");

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, MapFragment.newInstance())
                .commitNow();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Respond to the action bar's Up/Home button
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}