package in.lubble.app.announcements;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import in.lubble.app.R;

import static in.lubble.app.utils.FragUtils.replaceFrag;

public class NewAnnouncementActivity extends AppCompatActivity {

    public static void newInstance(Context context) {

        final Intent intent = new Intent(context, NewAnnouncementActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_announcement);

        replaceFrag(getSupportFragmentManager(), NewAnnouncementFragment.newInstance(), R.id.frame_fragContainer);

    }
}
