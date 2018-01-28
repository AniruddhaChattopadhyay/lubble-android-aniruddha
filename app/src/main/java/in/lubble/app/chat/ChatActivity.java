package in.lubble.app.chat;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import in.lubble.app.R;

import static in.lubble.app.utils.FragUtils.replaceFrag;

public class ChatActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        replaceFrag(getSupportFragmentManager(), ChatFragment.newInstance(), R.id.frame_fragContainer);
    }
}
