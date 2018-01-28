package in.lubble.app.groups;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import in.lubble.app.R;
import in.lubble.app.chat.ChatFragment;

import static in.lubble.app.utils.FragUtils.replaceFrag;

public class NewGroupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group);

        replaceFrag(getSupportFragmentManager(), NewGroupFragment.newInstance(), R.id.frame_fragContainer);

    }
}
