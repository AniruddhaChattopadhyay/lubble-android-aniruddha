package in.lubble.app.domestic_directory;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import in.lubble.app.R;

import static in.lubble.app.utils.FragUtils.replaceFrag;

public class DomesticDirectoryActivity extends AppCompatActivity {

    public static void newInstance(Context context) {
        final Intent intent = new Intent(context, DomesticDirectoryActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doesmtic_help);

        Toolbar toolbar = findViewById(R.id.lubble_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        replaceFrag(getSupportFragmentManager(), DomesticDirectoryFrag.newInstance(), R.id.frame_fragContainer);

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
