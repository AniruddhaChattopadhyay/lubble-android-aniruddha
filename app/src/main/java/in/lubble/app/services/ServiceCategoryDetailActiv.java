package in.lubble.app.services;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;

import com.crashlytics.android.Crashlytics;

import in.lubble.app.R;

import static in.lubble.app.utils.FragUtils.replaceFrag;

public class ServiceCategoryDetailActiv extends BaseActivity {

    private static final String ARG_CAT_ID = "ARG_CAT_ID";

    public static void open(Context context, int catId) {
        final Intent intent = new Intent(context, ServiceCategoryDetailActiv.class);
        intent.putExtra(ARG_CAT_ID, catId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_category_detail);

        Toolbar toolbar = findViewById(R.id.text_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        int catId = -1;

        Intent intent = getIntent();
        Uri data = intent.getData();
        if (data != null) {
            try {
                catId = Integer.parseInt(data.getQueryParameter("id"));
            } catch (Exception e) {
                e.printStackTrace();
                Crashlytics.logException(e);
                finish();
            }
        } else {
            catId = getIntent().getIntExtra(ARG_CAT_ID, -1);
        }
        replaceFrag(getSupportFragmentManager(), ServiceCategoryDetailFrag.newInstance(catId), R.id.frame_fragContainer);
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
