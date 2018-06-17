package in.lubble.app.events.new_event;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import in.lubble.app.GlideRequests;
import in.lubble.app.R;
import in.lubble.app.models.GroupData;

public class EventGroupSpinnerAdapter extends ArrayAdapter<GroupData> {

    private Context mContext;
    private ArrayList<GroupData> groupDataList;
    private final GlideRequests glide;

    public EventGroupSpinnerAdapter(Context context, int textViewResourceId, ArrayList<GroupData> groupDataList, GlideRequests glide) {
        super(context, textViewResourceId, groupDataList);
        mContext = context;
        this.groupDataList = groupDataList;
        this.glide = glide;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    public View getCustomView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater =
                (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_privacy_spinner, null);
            holder = new ViewHolder();
            holder.icon = convertView.findViewById(R.id.iv_icon);
            holder.txt01 = convertView.findViewById(R.id.tv_title);
            holder.txt02 = convertView.findViewById(R.id.tv_subtitle);

            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final GroupData groupData = groupDataList.get(position);

        if (groupData != null) {
            holder.icon.setImageResource(R.drawable.ic_public_black_24dp);
            glide.load(groupData.getThumbnail())
                    .circleCrop()
                    .placeholder(R.drawable.ic_circle_group_24dp)
                    .error(R.drawable.ic_circle_group_24dp)
                    .into(holder.icon);
            holder.txt01.setText(groupData.getTitle());
            holder.txt02.setVisibility(View.GONE);
        }

        return convertView;
    }

    class ViewHolder {
        ImageView icon;
        TextView txt01;
        TextView txt02;
    }

}
