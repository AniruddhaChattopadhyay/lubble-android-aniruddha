package in.lubble.app.groups;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import in.lubble.app.R;

public class MySpinnerAdapter extends ArrayAdapter<String> {

    private Context mContext;

    public MySpinnerAdapter(Context context, int textViewResourceId, String[] objects) {
        super(context, textViewResourceId, objects);
        mContext = context;
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

        if (position == 0) {
            holder.icon.setImageResource(R.drawable.ic_public_black_24dp);
            holder.txt01.setText("Public Group");
            holder.txt02.setText("Visible to everyone. Anyone can join");
        } else {
            holder.icon.setImageResource(R.drawable.ic_lock_black_24dp);
            holder.txt01.setText("Private Group");
            holder.txt02.setText("Only invited people can join & see messages");
        }
        return convertView;
    }

    class ViewHolder {
        ImageView icon;
        TextView txt01;
        TextView txt02;
    }

}
