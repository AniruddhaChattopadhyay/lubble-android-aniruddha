package in.lubble.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import com.google.firebase.auth.FirebaseAuth;
import in.lubble.app.rewards.RewardsFrag;
import in.lubble.app.utils.FragUtils;

public class DebugActivity extends BaseActivity {

    private static final String TAG = "DebugActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        FrameLayout frameContent = findViewById(R.id.content_frame);
        FragUtils.replaceFrag(getSupportFragmentManager(), RewardsFrag.newInstance(), frameContent.getId());

    }

    public void iAmTouched(View view) {
        //ShareActiv.open(this);
    }

    private void sendInvite(Uri mInvitationUrl) {
        String referrerName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        String subject = String.format("%s wants you to play MyExampleGame!", referrerName);
        String invitationLink = mInvitationUrl.toString();
        String msg = "Let's play MyExampleGame together! Use my referrer link: "
                + invitationLink;
        try {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, subject);
            i.putExtra(Intent.EXTRA_TEXT, msg);
            startActivity(Intent.createChooser(i, "choose one"));
        } catch (Exception e) {
            Log.e(TAG, "sendInvite: " + e.toString());
        }
    }

}
