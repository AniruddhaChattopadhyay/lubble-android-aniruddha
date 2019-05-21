package in.lubble.app.chat.books;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import in.lubble.app.BaseActivity;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.models.ProfileData;
import in.lubble.app.referrals.ReferralActivity;

public class OrderDoneActiv extends BaseActivity {

    private static final String TAG = "OrderDoneActiv";

    private ImageView crossIv;
    private TextView coinsTv;
    private TextView earnCoinsTv;
    private TextView addBooksTv;

    public static void open(Context context) {
        context.startActivity(new Intent(context, OrderDoneActiv.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_done);

        crossIv = findViewById(R.id.iv_cross);
        coinsTv = findViewById(R.id.tv_coins);
        earnCoinsTv = findViewById(R.id.tv_earn);
        addBooksTv = findViewById(R.id.tv_add_books);
        Analytics.triggerScreenEvent(this, this.getClass());

        crossIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        RealtimeDbHelper.getThisUserRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final ProfileData profileData = dataSnapshot.getValue(ProfileData.class);
                if (profileData != null) {
                    coinsTv.setText(String.valueOf(profileData.getCoins()));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        earnCoinsTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReferralActivity.open(OrderDoneActiv.this, true);
                finish();
            }
        });

        addBooksTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BookSearchActiv.open(OrderDoneActiv.this);
                finish();
            }
        });

    }


}
