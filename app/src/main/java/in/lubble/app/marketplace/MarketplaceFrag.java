package in.lubble.app.marketplace;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import in.lubble.app.R;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MarketplaceFrag#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MarketplaceFrag extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public MarketplaceFrag() {
        // Required empty public constructor
    }

    public static MarketplaceFrag newInstance(String param1, String param2) {
        MarketplaceFrag fragment = new MarketplaceFrag();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_marketplace, container, false);

        view.findViewById(R.id.layout_cat1);
        view.findViewById(R.id.layout_cat2);
        RecyclerView recyclerView = view.findViewById(R.id.rv_all_items);



        return view;
    }

}
