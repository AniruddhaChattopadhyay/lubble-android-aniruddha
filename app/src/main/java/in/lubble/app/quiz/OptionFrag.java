package in.lubble.app.quiz;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import in.lubble.app.R;
import in.lubble.app.quiz.dummy.DummyContent;
import in.lubble.app.quiz.dummy.DummyContent.DummyItem;

public class OptionFrag extends Fragment {

    private OnListFragmentInteractionListener mListener;

    public OptionFrag() {
    }

    public static OptionFrag newInstance(int columnCount) {
        OptionFrag fragment = new OptionFrag();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_option_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = view.findViewById(R.id.rv_quiz_options);
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(new OptionAdapter(DummyContent.ITEMS, mListener));
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(DummyItem item);
    }
}
