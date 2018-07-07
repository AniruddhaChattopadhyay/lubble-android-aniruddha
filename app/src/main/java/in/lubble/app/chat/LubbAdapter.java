package in.lubble.app.chat;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import in.lubble.app.GlideRequests;
import in.lubble.app.R;

public class LubbAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "LubbAdapter";

    private final List<String> dpUrlList;
    private final GlideRequests glide;

    public LubbAdapter(GlideRequests glide) {
        dpUrlList = new ArrayList<>();
        this.glide = glide;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new LubbViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lubb_head, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        final LubbViewHolder viewHolder = (LubbViewHolder) holder;
        final String thumbnailUrl = dpUrlList.get(position);
        glide.load(thumbnailUrl)
                .placeholder(R.drawable.ic_account_circle_black_no_padding)
                .circleCrop()
                .into(viewHolder.dpIv);

    }

    @Override
    public int getItemCount() {
        return dpUrlList.size();
    }

    public void addData(String thumbmailUrl) {
        dpUrlList.add(thumbmailUrl);
        notifyDataSetChanged();
    }

    class LubbViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final ImageView dpIv;

        LubbViewHolder(View view) {
            super(view);
            mView = view;
            dpIv = view.findViewById(R.id.iv_dp);
        }
    }


}
