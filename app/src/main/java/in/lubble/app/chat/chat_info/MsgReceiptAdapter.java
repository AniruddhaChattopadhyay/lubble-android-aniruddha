package in.lubble.app.chat.chat_info;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import in.lubble.app.GlideRequests;
import in.lubble.app.R;
import in.lubble.app.models.MsgInfoData;

import static in.lubble.app.utils.DateTimeUtils.getHumanTimestampWithTime;

public class MsgReceiptAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "MsgReceiptAdapter";

    private final List<MsgInfoData> msgInfoList;
    private final GlideRequests glide;

    public MsgReceiptAdapter(GlideRequests glide) {
        msgInfoList = new ArrayList<>();
        this.glide = glide;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_msg_info, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        final ViewHolder viewHolder = (ViewHolder) holder;
        final MsgInfoData msgInfoData = msgInfoList.get(position);
        glide.load(msgInfoData.getProfileInfo().getThumbnail())
                .placeholder(R.drawable.ic_account_circle_black_no_padding)
                .circleCrop()
                .into(viewHolder.dpIv);
        viewHolder.nameTv.setText(msgInfoData.getProfileInfo().getName());
        viewHolder.timestampTv.setText(getHumanTimestampWithTime(msgInfoData.getTimestamp()));

    }

    @Override
    public int getItemCount() {
        return msgInfoList.size();
    }

    public void addData(MsgInfoData msgInfoData) {
        msgInfoList.add(msgInfoData);
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final ImageView dpIv;
        final TextView nameTv;
        final TextView timestampTv;

        ViewHolder(View view) {
            super(view);
            mView = view;
            dpIv = view.findViewById(R.id.iv_dp);
            nameTv = view.findViewById(R.id.tv_name);
            timestampTv = view.findViewById(R.id.tv_timestamp);
        }
    }


}
