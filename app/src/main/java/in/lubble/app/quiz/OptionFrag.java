package in.lubble.app.quiz;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import in.lubble.app.R;

public class OptionFrag extends Fragment {

    private OnListFragmentInteractionListener mListener;
    private QuestionData questionData;
    private TextView quesTv;

    public OptionFrag() {
    }

    public static OptionFrag newInstance(QuestionData questionData) {
        OptionFrag fragment = new OptionFrag();
        Bundle args = new Bundle();
        args.putSerializable("ques", questionData);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            questionData = (QuestionData) getArguments().getSerializable("ques");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_option_list, container, false);

        // Set the adapter
        Context context = view.getContext();
        quesTv = view.findViewById(R.id.tv_quiz_title);
        RecyclerView recyclerView = view.findViewById(R.id.rv_quiz_options);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(new OptionAdapter(questionData.getQuesId(), questionData.getOptions(), mListener));

        quesTv.setText(questionData.getQuesName());

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
        void onListFragmentInteraction(int quesId, OptionData optionData);
    }
}
