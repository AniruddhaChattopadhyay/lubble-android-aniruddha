package in.lubble.app.marketplace;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import in.lubble.app.GlideApp;
import in.lubble.app.R;

public class SlidePageFrag extends Fragment {
    private static final String ARG_SLIDER_DATA = "ARG_SLIDER_DATA";

    private SliderData sliderData;

    public SlidePageFrag() {
        // Required empty public constructor
    }

    public static SlidePageFrag newInstance(SliderData sliderData) {
        SlidePageFrag fragment = new SlidePageFrag();
        Bundle args = new Bundle();
        args.putSerializable(ARG_SLIDER_DATA, sliderData);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            sliderData = (SliderData) getArguments().getSerializable(ARG_SLIDER_DATA);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_slide_page, container, false);

        ImageView slideIv = view.findViewById(R.id.iv_slide_image);

        GlideApp.with(getContext()).load(sliderData.getUrl()).into(slideIv);

        return view;
    }

}
