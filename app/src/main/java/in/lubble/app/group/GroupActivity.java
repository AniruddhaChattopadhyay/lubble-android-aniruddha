package in.lubble.app.group;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import in.lubble.app.R;

import static in.lubble.app.utils.FragUtils.addFrag;

public class GroupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        addFrag(getSupportFragmentManager(), R.id.frame_fragContainer, ChatFragment.newInstance());
    }
}
