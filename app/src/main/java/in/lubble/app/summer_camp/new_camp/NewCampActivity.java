package in.lubble.app.summer_camp.new_camp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import in.lubble.app.R;

public class NewCampActivity extends AppCompatActivity {

    public static void open(Context context) {
        context.startActivity(new Intent(context, NewCampActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_camp);
    }
}
