package in.lubble.app.announcements.announcementHistory;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import in.lubble.app.R;
import in.lubble.app.models.AnnouncementData;
import in.lubble.app.utils.DateTimeUtils;

public class AnnouncementsAdapter extends RecyclerView.Adapter<AnnouncementsAdapter.AnnouncementViewHolder> {

    private List<AnnouncementData> announcementDataList;

    public AnnouncementsAdapter() {
        announcementDataList = new ArrayList<>();
    }

    @Override
    public AnnouncementViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_announcement, parent, false);
        return new AnnouncementViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final AnnouncementViewHolder holder, int position) {
        final AnnouncementData announcementData = announcementDataList.get(position);

        final long time = announcementData.getCreatedTimestamp();
        if (time > 0) {
            holder.timeTv.setVisibility(View.VISIBLE);
            holder.timeTv.setText(DateTimeUtils.getHumanTimestamp(time));
        } else {
            holder.timeTv.setVisibility(View.GONE);
        }
        holder.titleTv.setText(announcementData.getTitle());
        holder.messageTv.setText(announcementData.getMessage());

    }

    public void addAnnouncement(AnnouncementData announcementData) {
        announcementDataList.add(0, announcementData);
        notifyDataSetChanged();
    }

    public void clear() {
        announcementDataList.clear();
    }

    @Override
    public int getItemCount() {
        return announcementDataList.size();
    }

    class AnnouncementViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView timeTv;
        final TextView titleTv;
        final TextView messageTv;

        public AnnouncementViewHolder(View view) {
            super(view);
            mView = view;
            timeTv = view.findViewById(R.id.tv_phone);
            titleTv = view.findViewById(R.id.tv_title);
            messageTv = view.findViewById(R.id.tv_message);
        }
    }
}
