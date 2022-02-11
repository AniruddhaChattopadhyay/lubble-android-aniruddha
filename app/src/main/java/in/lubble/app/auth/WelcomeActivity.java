package in.lubble.app.auth;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import in.lubble.app.BaseActivity;
import in.lubble.app.R;
import in.lubble.app.utils.FragUtils;
import android.os.Bundle;

public class WelcomeActivity extends BaseActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        FragUtils.replaceFrag(getSupportFragmentManager(), WelcomeFrag.newInstance(getIntent().getStringExtra("Link")), R.id.frame_fragContainer);
    }
}