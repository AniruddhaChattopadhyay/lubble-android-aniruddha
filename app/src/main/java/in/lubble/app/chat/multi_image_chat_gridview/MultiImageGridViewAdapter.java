package in.lubble.app.chat.multi_image_chat_gridview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import in.lubble.app.R;

public class MultiImageGridViewAdapter extends RecyclerView.Adapter<MultiImageGridViewAdapter.MyViewHolder> {

    private List<String> ImageUrlList;
    private Context mContext;
    private boolean isFromChat;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private ImageView iv;
        private TextView countViewTv;
        private RelativeLayout countViewRl;

        public MyViewHolder(View view) {
            super(view);
            iv =  view.findViewById(R.id.multi_imgView);
            countViewRl = view.findViewById(R.id.count_view);
            countViewTv = view.findViewById(R.id.count_view_tv);
        }
    }


    public MultiImageGridViewAdapter(Context context, List<String> ImageUrlList,boolean isFromChat) {
        this.ImageUrlList = ImageUrlList;
        this.mContext = context;
        this.isFromChat = isFromChat;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.multi_image_attach_inside_recycleview_layout, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        String imgUrl = ImageUrlList.get(position);
        Glide.with(mContext).load(imgUrl).into(holder.iv);
        if(!isFromChat){
//            RelativeLayout.LayoutParams layoutParams = new
//                    RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            RelativeLayout.LayoutParams layoutParams = new
                    RelativeLayout.LayoutParams(1000, 1000);
            holder.iv.setLayoutParams(layoutParams);

        }
        if (isFromChat && position==3) {
            holder.countViewRl.setVisibility(View.VISIBLE);
            holder.countViewTv.setText("+3");
        }
    }

    @Override
    public int getItemCount() {
        return ImageUrlList.size();
    }
}
