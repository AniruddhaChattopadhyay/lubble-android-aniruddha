package in.lubble.app.domestic_directory;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import in.lubble.app.R;

public class DomesticAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 389;
    private static final int TYPE_ROW = 193;

    private List<DomesticHelpData> domesticList;
    private Context context;

    public DomesticAdapter(Context context) {
        domesticList = new ArrayList<>();
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        if (domesticList.get(position).getPhone() == 0) {
            return TYPE_HEADER;
        } else {
            return TYPE_ROW;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            return new DomesticHeaderViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_header, parent, false));
        } else {
            return new DomesticViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_domestic_help, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        final DomesticHelpData domesticHelpData = domesticList.get(position);
        if (holder instanceof DomesticHeaderViewHolder) {
            final DomesticHeaderViewHolder headerViewHolder = (DomesticHeaderViewHolder) holder;
            headerViewHolder.catNameTv.setText(domesticList.get(position).getName());
        } else {
            final DomesticViewHolder domesticViewHolder = (DomesticViewHolder) holder;
            domesticViewHolder.nameTv.setText(domesticHelpData.getName());
        }
    }

    void addAll(ArrayList<DomesticHelpData> categoryList) {
        domesticList.clear();
        domesticList.addAll(categoryList);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return domesticList.size();
    }

    class DomesticViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final View mView;
        final TextView nameTv;
        final ImageView callIcon;

        DomesticViewHolder(View view) {
            super(view);
            mView = view;
            nameTv = view.findViewById(R.id.tv_name);
            callIcon = view.findViewById(R.id.iv_call);
            callIcon.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            final long phoneNo = domesticList.get(getAdapterPosition()).getPhone();
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + String.valueOf(phoneNo)));
            context.startActivity(intent);
        }
    }

    class DomesticHeaderViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView catNameTv;

        DomesticHeaderViewHolder(View view) {
            super(view);
            mView = view;
            catNameTv = view.findViewById(R.id.tv_cat_name);
        }

    }
}
