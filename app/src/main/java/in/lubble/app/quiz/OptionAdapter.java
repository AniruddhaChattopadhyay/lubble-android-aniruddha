package in.lubble.app.quiz;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import in.lubble.app.R;
import in.lubble.app.quiz.OptionFrag.OnListFragmentInteractionListener;

public class OptionAdapter extends RecyclerView.Adapter<OptionAdapter.ViewHolder> {

    private final List<OptionData> optionDataList;
    private final OnListFragmentInteractionListener mListener;
    private int SELECTED_ROW = -1;
    private int quesId;

    public OptionAdapter(int id, List<OptionData> items, OnListFragmentInteractionListener listener) {
        optionDataList = items;
        mListener = listener;
        quesId = id;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_option, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final OptionData optionData = optionDataList.get(position);
        holder.mItem = optionData;
        holder.optionTv.setText(optionData.getValue());

        if (SELECTED_ROW == position) {
            holder.mView.setBackgroundColor(ContextCompat.getColor(holder.mView.getContext(), R.color.light_green));
        } else {
            holder.mView.setBackgroundColor(ContextCompat.getColor(holder.mView.getContext(), R.color.white));
        }

    }

    @Override
    public int getItemCount() {
        return optionDataList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final View mView;
        public final TextView optionTv;
        public OptionData mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            optionTv = view.findViewById(R.id.tv_option);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            SELECTED_ROW = getAdapterPosition();
            mListener.onListFragmentInteraction(quesId, optionDataList.get(getAdapterPosition()));
            notifyDataSetChanged();
        }

    }
}
