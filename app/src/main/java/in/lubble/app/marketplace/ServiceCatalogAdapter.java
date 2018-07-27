package in.lubble.app.marketplace;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import in.lubble.app.R;
import in.lubble.app.models.marketplace.ServiceData;

public class ServiceCatalogAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "ServiceCatalogAdapter";

    private final List<ServiceData> serviceDataList;

    public ServiceCatalogAdapter() {
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
        viewHolder.servicePriceTv.setText("₹ " + serviceData.getPrice());

    }

    @Override
    public int getItemCount() {
        return serviceDataList.size();
    }

    public void addData(ServiceData serviceData) {
        serviceDataList.add(serviceData);
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final TextView serviceNameTv;
        final TextView servicePriceTv;

        ViewHolder(View view) {
            super(view);
            serviceNameTv = view.findViewById(R.id.tv_service_name);
            servicePriceTv = view.findViewById(R.id.tv_service_price);
        }

    }

}
