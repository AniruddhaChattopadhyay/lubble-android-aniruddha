package in.lubble.app.explore;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import in.lubble.app.GlideApp;
import in.lubble.app.R;
import in.lubble.app.analytics.Analytics;
import in.lubble.app.explore.dummy.DummyContent;
import in.lubble.app.explore.dummy.DummyContent.DummyItem;

public class ExploreFrag extends Fragment implements ExploreGroupAdapter.OnListFragmentInteractionListener {

    private ExploreGroupAdapter.OnListFragmentInteractionListener mListener;

    public ExploreFrag() {
    }

    public static ExploreFrag newInstance() {
        return new ExploreFrag();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Analytics.triggerScreenEvent(getContext(), this.getClass());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_explore, container, false);

        Context context = view.getContext();
        RecyclerView recyclerView = (RecyclerView) view;
        recyclerView.setLayoutManager(new GridLayoutManager(context, 2));
        recyclerView.setAdapter(new ExploreGroupAdapter(DummyContent.ITEMS, mListener, GlideApp.with(getContext())));

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = this;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onListFragmentInteraction(DummyItem item) {

    }

}
