package in.lubble.app.chat;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import in.lubble.app.R;

import java.util.ArrayList;

public class AttachmentListDialogFrag extends BottomSheetDialogFragment implements AttachmentClickListener {

    private static final int ITEM_COUNT = 3;
    private AttachmentClickListener mListener;

    public static AttachmentListDialogFrag newInstance() {
        final AttachmentListDialogFrag fragment = new AttachmentListDialogFrag();
        final Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_attachment_list_dialog, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        final RecyclerView recyclerView = (RecyclerView) view;
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        recyclerView.setAdapter(new AttachmentAdapter(getAttachmentList()));
    }

    private ArrayList<AttachmentData> getAttachmentList() {
        final ArrayList<AttachmentData> list = new ArrayList<>();
        AttachmentData attachmentData = new AttachmentData();
        attachmentData.setIcon(R.drawable.ic_landscape);
        attachmentData.setText("Gallery");
        list.add(attachmentData);
        attachmentData = new AttachmentData();
        attachmentData.setIcon(R.drawable.ic_poll);
        attachmentData.setText("Poll");
        list.add(attachmentData);
        return list;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        final Fragment parent = getParentFragment();
        if (parent != null) {
            mListener = (AttachmentClickListener) parent;
        } else {
            mListener = (AttachmentClickListener) context;
        }
        mListener = this;
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    @Override
    public void onAttachmentClicked(int position) {

    }

    private class ViewHolder extends RecyclerView.ViewHolder {

        final ImageView attachmentIconIv;
        final TextView attachmentTextTv;

        ViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.item_attachment, parent, false));
            attachmentIconIv = itemView.findViewById(R.id.iv_attachment_icon);
            attachmentTextTv = itemView.findViewById(R.id.tv_attachment_text);
            parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onAttachmentClicked(getAdapterPosition());
                        dismiss();
                    }
                }
            });
        }

    }

    private class AttachmentAdapter extends RecyclerView.Adapter<ViewHolder> {

        private ArrayList<AttachmentData> attachmentList;

        AttachmentAdapter(ArrayList<AttachmentData> attachmentList) {
            this.attachmentList = attachmentList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()), parent);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final AttachmentData attachmentData = attachmentList.get(position);
            holder.attachmentIconIv.setImageResource(attachmentData.getIcon());
            holder.attachmentTextTv.setText(attachmentData.getText());
        }

        @Override
        public int getItemCount() {
            return attachmentList.size();
        }

    }

}
