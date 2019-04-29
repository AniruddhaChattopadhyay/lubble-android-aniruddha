package in.lubble.app.chat.collections;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import in.lubble.app.GlideRequests;
import in.lubble.app.R;
import in.lubble.app.utils.StringUtils;

import java.util.Calendar;
import java.util.List;

public class CollectionPlacesAdapter extends RecyclerView.Adapter<CollectionPlacesAdapter.CollectionViewHolder> {

    private List<PlacesRecordData> placesDataList;
    private final GlideRequests glide;
    private Context context;

    public CollectionPlacesAdapter(Context context, GlideRequests glide, List<PlacesRecordData> placesDataList) {
        this.placesDataList = placesDataList;
        this.context = context;
        this.glide = glide;
    }

    @Override
    public CollectionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_place, parent, false);
        return new CollectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CollectionViewHolder holder, int position) {
        final PlacesData placesData = placesDataList.get(position).getPlacesData();

        holder.preTitleTv.setText(placesData.getName());
        holder.locationTv.setText(placesData.getLocality());

        final CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(context);
        circularProgressDrawable.setStyle(CircularProgressDrawable.DEFAULT);
        circularProgressDrawable.start();
        glide.load(placesData.getImage())
                .placeholder(circularProgressDrawable)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(holder.collectionIv);
        holder.descTv.setText(HtmlCompat.fromHtml(placesData.getDesc(), HtmlCompat.FROM_HTML_MODE_LEGACY));
        holder.descTv.setMovementMethod(LinkMovementMethod.getInstance());
        holder.ctaBtn.setText(placesData.getCTAText());
        holder.postTitleTv.setText(placesData.getName());
        if (placesData.getPrice() > 0) {
            holder.priceTv.setVisibility(View.VISIBLE);
            holder.priceHintTv.setVisibility(View.VISIBLE);
            holder.priceTv.setText("₹" + placesData.getPrice());
        } else {
            holder.priceTv.setVisibility(View.GONE);
            holder.priceHintTv.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(placesData.getTiming())) {
            holder.timingTv.setVisibility(View.VISIBLE);
            holder.timingHintTv.setVisibility(View.VISIBLE);
            final String[] weekTiming = placesData.getTiming().split(",");
            holder.timingTv.setText(weekTiming[Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1]);
        } else {
            holder.timingTv.setVisibility(View.GONE);
            holder.timingHintTv.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(placesData.getSpecial())) {
            holder.specialTv.setVisibility(View.VISIBLE);
            holder.specialTv.setText(placesData.getSpecial());
        } else {
            holder.specialTv.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(placesData.getRating())) {
            holder.ratingTv.setVisibility(View.VISIBLE);
            holder.ratingProviderTv.setVisibility(View.VISIBLE);
            holder.ratingTv.setText(placesData.getRating());
            holder.ratingProviderTv.setText(placesData.getRatingProvider());
        } else {
            holder.ratingTv.setVisibility(View.GONE);
            holder.ratingProviderTv.setVisibility(View.GONE);
        }

        holder.ctaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (StringUtils.isValidMobile(placesData.getCTALink())) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + placesData.getCTALink()));
                    context.startActivity(intent);
                } else {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(placesData.getCTALink()));
                    context.startActivity(intent);
                }
            }
        });

        if (placesData.getLatitude() != 0) {
            holder.mapIv.setVisibility(View.VISIBLE);
            holder.mapIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:" + placesData.getLatitude() +
                            "," + placesData.getLongitude() + "?q=" + placesData.getLatitude() + "," + placesData.getLongitude() + "(" + placesData.getName() + ")"));
                    context.startActivity(intent);
                }
            });
        } else {
            holder.mapIv.setVisibility(View.GONE);
        }

    }

    public void clear() {
        placesDataList.clear();
    }

    @Override
    public int getItemCount() {
        return placesDataList.size();
    }

    class CollectionViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final ImageView collectionIv;
        final TextView descTv;
        final TextView preTitleTv;
        final TextView locationTv;
        final TextView postTitleTv;
        final ImageView mapIv;
        final Button ctaBtn;
        final TextView ratingTv;
        final TextView ratingProviderTv;
        final TextView priceHintTv;
        final TextView priceTv;
        final TextView specialTv;
        final TextView timingHintTv;
        final TextView timingTv;

        public CollectionViewHolder(View view) {
            super(view);
            mView = view;
            collectionIv = view.findViewById(R.id.iv_place_hero);
            preTitleTv = view.findViewById(R.id.tv_pre_title);
            descTv = view.findViewById(R.id.tv_desc);
            postTitleTv = view.findViewById(R.id.tv_post_title);
            mapIv = view.findViewById(R.id.iv_map);
            ratingTv = view.findViewById(R.id.tv_rating);
            ratingProviderTv = view.findViewById(R.id.tv_rating_provider);
            priceHintTv = view.findViewById(R.id.tv_price_hint);
            priceTv = view.findViewById(R.id.tv_price);
            timingHintTv = view.findViewById(R.id.tv_timing_hint);
            timingTv = view.findViewById(R.id.tv_timing);
            locationTv = view.findViewById(R.id.tv_location);
            specialTv = view.findViewById(R.id.tv_special);
            ctaBtn = view.findViewById(R.id.btn_cta);
        }

    }
}
