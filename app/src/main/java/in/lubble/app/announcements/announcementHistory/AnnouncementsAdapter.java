package in.lubble.app.announcements.announcementHistory;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import in.lubble.app.R;
import in.lubble.app.models.AnnouncementData;

public class AnnouncementsAdapter extends RecyclerView.Adapter<AnnouncementsAdapter.AnnouncementViewHolder> {

    private List<AnnouncementData> announcementDataList;

    public AnnouncementsAdapter() {
        announcementDataList=new ArrayList<>();
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

        holder.titleTv.setText(announcementData.getTitle());
        holder.messageTv.setText(announcementData.getMessage());

    }

    public void addAnnouncement(AnnouncementData announcementData) {
        announcementDataList.add(0,announcementData);
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
        final TextView titleTv;
        final TextView messageTv;

        public AnnouncementViewHolder(View view) {
            super(view);
            mView = view;
            titleTv = view.findViewById(R.id.tv_title);
            messageTv = view.findViewById(R.id.tv_message);
        }
    }
}
