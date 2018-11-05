package in.lubble.app.marketplace;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.crashlytics.android.Crashlytics;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.chat.ChatActivity;
import in.lubble.app.models.marketplace.SellerData;
import in.lubble.app.models.marketplace.ServiceData;

import java.util.ArrayList;
import java.util.List;

public class ServiceCatalogAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "ServiceCatalogAdapter";

    private final List<ServiceData> serviceDataList;
    private Context context;
    private SellerData sellerData;
    private String dmId;

    public ServiceCatalogAdapter(Context context, @NonNull SellerData sellerData) {
        this.context = context;
        this.sellerData = sellerData;
        serviceDataList = new ArrayList<>();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ServiceCatalogAdapter.ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_service_catalog, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        final ServiceCatalogAdapter.ViewHolder viewHolder = (ServiceCatalogAdapter.ViewHolder) holder;

        final ServiceData serviceData = serviceDataList.get(position);

        viewHolder.serviceNameTv.setText(serviceData.getTitle());
        final Integer price = serviceData.getPrice();
        if (price == null || price < 0) {
            viewHolder.servicePriceTv.setText("Ask for price");
            viewHolder.servicePriceTv.setTextColor(ContextCompat.getColor(context, R.color.link_blue));
            viewHolder.servicePriceTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (sellerData.getId() != LubbleSharedPrefs.getInstance().getSellerId()) {
                        //allow chat only if the seller is not same as viewing user
                        if (!TextUtils.isEmpty(dmId)) {
                            ChatActivity.openForDm(context, dmId, null, serviceData.getTitle());
                        } else {
                            if (sellerData != null) {
                                ChatActivity.openForEmptyDm(
                                        context,
                                        String.valueOf(sellerData.getId()),
                                        sellerData.getName(),
                                        sellerData.getPhotoUrl(),
                                        serviceData.getTitle()
                                );
                            } else {
                                final IllegalArgumentException throwable = new IllegalArgumentException("Service Data is NULL when trying to request service price");
                                Log.e(TAG, "onClick: ", throwable);
                                Crashlytics.logException(throwable);
                            }
                        }
                    } else {
                        Toast.makeText(context, "You cannot chat with yourself :)", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else if (price == 0) {
            viewHolder.servicePriceTv.setText("FREE");
            viewHolder.servicePriceTv.setTextColor(ContextCompat.getColor(context, R.color.black));
            viewHolder.servicePriceTv.setOnClickListener(null);
        } else {
            viewHolder.servicePriceTv.setText("₹ " + price);
            viewHolder.servicePriceTv.setTextColor(ContextCompat.getColor(context, R.color.black));
            viewHolder.servicePriceTv.setOnClickListener(null);
        }

        if (position % 2 == 0) {
            viewHolder.view.setBackgroundColor(ContextCompat.getColor(context, R.color.very_light_gray));
        } else {
            viewHolder.view.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
        }

    }

    @Override
    public int getItemCount() {
        return serviceDataList.size();
    }

    public void addData(ServiceData serviceData) {
        serviceDataList.add(serviceData);
        notifyDataSetChanged();
    }

    public void updateDmId(String dmId) {
        this.dmId = dmId;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final View view;
        final TextView serviceNameTv;
        final TextView servicePriceTv;

        ViewHolder(View view) {
            super(view);
            this.view = view;
            serviceNameTv = view.findViewById(R.id.tv_service_name);
            servicePriceTv = view.findViewById(R.id.tv_service_price);
        }

    }

}
