package in.lubble.app.feed;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import in.lubble.app.R;

public class GroupSelectionActiv extends AppCompatActivity {

    public static Intent getIntent(Context context) {
        return new Intent(context, GroupSelectionActiv.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_selection);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, GroupSelectionFrag.newInstance())
                    .commitNow();
        }
    }
}