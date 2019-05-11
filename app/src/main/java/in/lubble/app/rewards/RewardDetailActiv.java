package in.lubble.app.rewards;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.text.HtmlCompat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.crashlytics.android.Crashlytics;
import in.lubble.app.BaseActivity;
import in.lubble.app.GlideApp;
import in.lubble.app.R;
import in.lubble.app.rewards.data.RewardsData;

import java.util.MissingFormatArgumentException;

public class RewardDetailActiv extends BaseActivity {

    private static final String TAG = "RewardDetailActiv";
    private static final String ARG_REWARD_DATA = "ARG_REWARD_DATA";

    private RewardsData rewardsData;
    private ImageView logoIv;
    private TextView detailsTv;
    private TextView brandNameTv;
    private TextView titleTv;
    private TextView descTv;
    private TextView costTv;
    private TextView detailBrandName;
    private TextView detailTitleTv;
    private TextView detailDescTv;
    private TextView tncTv;

    public static void open(Context context, RewardsData rewardsData) {
        final Intent intent = new Intent(context, RewardDetailActiv.class);
        intent.putExtra(ARG_REWARD_DATA, rewardsData);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reward_detail);

        logoIv = findViewById(R.id.iv_logo);
        brandNameTv = findViewById(R.id.tv_brand_name);
        titleTv = findViewById(R.id.tv_reward_title);
        descTv = findViewById(R.id.tv_reward_desc);
        costTv = findViewById(R.id.tv_cost);
        detailBrandName = findViewById(R.id.tv_detail_brand_name);
        detailTitleTv = findViewById(R.id.tv_detail_title);
        detailDescTv = findViewById(R.id.tv_detail_desc);
        detailsTv = findViewById(R.id.tv_details);
        tncTv = findViewById(R.id.tv_tnc);

        rewardsData = (RewardsData) getIntent().getSerializableExtra(ARG_REWARD_DATA);
        if (rewardsData == null) {
            Toast.makeText(this, "No Data Found", Toast.LENGTH_SHORT).show();
            Crashlytics.logException(new MissingFormatArgumentException("no rewards data found"));
            finish();
            return;
        }

        GlideApp.with(this).load(rewardsData.getBrandLogo()).diskCacheStrategy(DiskCacheStrategy.NONE).into(logoIv);
        brandNameTv.setText(rewardsData.getBrand());
        detailBrandName.setText(rewardsData.getBrand());
        titleTv.setText(rewardsData.getTitle());
        detailTitleTv.setText(rewardsData.getTitle());
        descTv.setText(rewardsData.getDescription());
        detailDescTv.setText(rewardsData.getDescription());
        costTv.setText(rewardsData.getCost() + " el coins");

        detailsTv.setText(HtmlCompat.fromHtml(rewardsData.getDetails(), HtmlCompat.FROM_HTML_MODE_LEGACY));
        detailsTv.setMovementMethod(LinkMovementMethod.getInstance());

        tncTv.setText(HtmlCompat.fromHtml(rewardsData.getTnc(), HtmlCompat.FROM_HTML_MODE_LEGACY));
        tncTv.setMovementMethod(LinkMovementMethod.getInstance());

    }
}
