package in.lubble.app.profile;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import in.lubble.app.R;

public class StatusBottomSheetAdapter extends RecyclerView.Adapter<StatusBottomSheetAdapter.MyViewHolder> {

    private List<String> statusList;
    private int selectedPosition = -1;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, year, genre;

        public MyViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.title);
        }
    }


    public StatusBottomSheetAdapter(List<String> statusList) {
        this.statusList = statusList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.status_bottomsheet_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
//        Movie movie = statusList.get(position);
//        holder.title.setText(movie.getTitle());
        holder.title.setText(statusList.get(position));
        if(selectedPosition==position)
            holder.itemView.setBackgroundColor(Color.parseColor("#008CF9"));
        else
            holder.itemView.setBackgroundColor(Color.parseColor("#ffffff"));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedPosition=position;
                notifyDataSetChanged();

            }
        });
    }

    @Override
    public int getItemCount() {
        return statusList.size();
    }
}
