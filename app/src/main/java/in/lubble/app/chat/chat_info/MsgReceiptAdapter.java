package in.lubble.app.chat.chat_info;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import in.lubble.app.GlideRequests;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.models.MsgInfoData;

import static in.lubble.app.analytics.AnalyticsEvents.EXPAND_LIKES;
import static in.lubble.app.utils.DateTimeUtils.getHumanTimestampWithTime;

public class MsgReceiptAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "MsgReceiptAdapter";
    private static final int TYPE_ROW = 543;
    private static final int TYPE_COLLAPSED = 726;

    private final List<MsgInfoData> msgInfoList;
    private final GlideRequests glide;
    // number of rows to show when recyclerView is collapsed
    // if -1 then no limit
    private int initCount = -1;

    public MsgReceiptAdapter(GlideRequests glide, int initCount) {
        msgInfoList = new ArrayList<>();
        this.glide = glide;
        this.initCount = initCount;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == initCount) {
            return TYPE_COLLAPSED;
        } else {
            return TYPE_ROW;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_COLLAPSED) {
            return new CollapsedViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_recyclerview_collapsed, parent, false));
        } else {
            return new ViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_msg_info, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewHolder) {
            final ViewHolder viewHolder = (ViewHolder) holder;
            final MsgInfoData msgInfoData = msgInfoList.get(position);
            glide.load(msgInfoData.getProfileInfo().getThumbnail())
                    .placeholder(R.drawable.ic_account_circle_black_no_padding)
                    .circleCrop()
                    .into(viewHolder.dpIv);
            viewHolder.nameTv.setText(msgInfoData.getProfileInfo().getName());
            viewHolder.timestampTv.setText(getHumanTimestampWithTime(msgInfoData.getTimestamp()));
        } else {
            final CollapsedViewHolder collapsedViewHolder = (CollapsedViewHolder) holder;
            collapsedViewHolder.collapsedNumberTv.setText("+" + (msgInfoList.size() - initCount) + " more");
        }
    }

    @Override
    public int getItemCount() {
        if (initCount == -1) {
            return msgInfoList.size();
        } else {
            return msgInfoList.size() < initCount ? msgInfoList.size() : (initCount + 1);
        }
    }

    public void addData(MsgInfoData msgInfoData) {
        msgInfoList.add(msgInfoData);
        notifyDataSetChanged();
    }

    public void clear() {
        msgInfoList.clear();
        notifyDataSetChanged();
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        private final View mView;
        private final ImageView dpIv;
        private final TextView nameTv;
        private final TextView timestampTv;

        ViewHolder(View view) {
            super(view);
            mView = view;
            dpIv = view.findViewById(R.id.iv_dp);
            nameTv = view.findViewById(R.id.tv_name);
            timestampTv = view.findViewById(R.id.tv_timestamp);
        }
    }

    private class CollapsedViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final View mView;
        private final TextView collapsedNumberTv;

        CollapsedViewHolder(View view) {
            super(view);
            mView = view;
            collapsedNumberTv = view.findViewById(R.id.tv_collapsed_count);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Analytics.triggerEvent(EXPAND_LIKES, v.getContext());
            initCount = -1;
            notifyDataSetChanged();
        }
    }


}
