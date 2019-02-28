package in.lubble.app.quiz;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import in.lubble.app.R;
import in.lubble.app.quiz.OptionFrag.OnListFragmentInteractionListener;
import in.lubble.app.quiz.dummy.DummyContent.DummyItem;

import java.util.List;

public class OptionAdapter extends RecyclerView.Adapter<OptionAdapter.ViewHolder> {

    private final List<DummyItem> mValues;
    private final OnListFragmentInteractionListener mListener;

    public OptionAdapter(List<DummyItem> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_option, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.optionTv.setText(mValues.get(position).content);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView optionTv;
        public DummyItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            optionTv = view.findViewById(R.id.tv_option);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + optionTv.getText() + "'";
        }
    }
}
