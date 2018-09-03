package in.lubble.app.services;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import in.lubble.app.R;

import static in.lubble.app.utils.FragUtils.replaceFrag;

public class ServiceCategoryDetailActiv extends AppCompatActivity {

    private static final String ARG_CAT_ID = "ARG_CAT_ID";

    public static void open(Context context, String catId) {
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

        replaceFrag(getSupportFragmentManager(), ServiceCategoryDetailFrag.newInstance(getIntent().getStringExtra(ARG_CAT_ID)), R.id.frame_fragContainer);
    }
}
