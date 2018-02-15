package in.lubble.app.profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import in.lubble.app.R;
import in.lubble.app.utils.UserUtils;


public class ProfileActivity extends AppCompatActivity {

    public static final String KEY_USER_ID = "profileActivUserId";

    public static void open(Context context, String userId) {
        Intent intent = new Intent(context, ProfileActivity.class);
        intent.putExtra(KEY_USER_ID, userId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        setContentView(R.layout.activity_profile);

        setTitle("");

        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
                .replace(R.id.frameLayout_fragContainer,
                        ProfileFrag.newInstance(getIntent().getStringExtra(KEY_USER_ID)))
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sign_out: {
                UserUtils.logout(ProfileActivity.this);
                break;
            }
            case R.id.action_clear_db: {
                //deleteDatabase(SqliteHelper.DATABASE_NAME);
                break;
            }
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return false;
    }

}
