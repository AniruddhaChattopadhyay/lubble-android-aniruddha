package in.lubble.app.chat.horizontalImageRecyclerView;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.List;

import in.lubble.app.GlideApp;
import in.lubble.app.R;

public class MultiImageAttachmentAdapter extends RecyclerView.Adapter<MultiImageAttachmentAdapter.MyViewHolder> {

    private List<Uri> moviesList;
    private Context mContext;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView iv;

        public MyViewHolder(View view) {
            super(view);
            iv =  view.findViewById(R.id.multi_imgView);
        }
    }


    public MultiImageAttachmentAdapter(Context context, List<Uri> moviesList) {
        this.moviesList = moviesList;
        this.mContext = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.multi_image_attach_inside_recycleview_layout, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Uri uri = moviesList.get(position);
        GlideApp.with(mContext).load(new File(uri.getPath())).into(holder.iv);
    }

    @Override
    public int getItemCount() {
        return moviesList.size();
    }
}
