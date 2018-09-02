package in.lubble.app.marketplace;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import in.lubble.app.GlideApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;

import static in.lubble.app.marketplace.SliderData.CATEGORY;
import static in.lubble.app.marketplace.SliderData.DASH;
import static in.lubble.app.marketplace.SliderData.ITEM;
import static in.lubble.app.marketplace.SliderData.SELLER;

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
        ImageView scrimIv = view.findViewById(R.id.iv_scrim);
        TextView titleTv = view.findViewById(R.id.tv_slide_title);
        TextView descTv = view.findViewById(R.id.tv_slide_desc);

        GlideApp.with(getContext()).load(sliderData.getUrl()).into(slideIv);
        if (TextUtils.isEmpty(sliderData.getTitle()) && TextUtils.isEmpty(sliderData.getDesc())) {
            scrimIv.setVisibility(View.GONE);
            titleTv.setVisibility(View.GONE);
            descTv.setVisibility(View.GONE);
        } else {
            scrimIv.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(sliderData.getTitle())) {
                titleTv.setVisibility(View.VISIBLE);
                titleTv.setText(sliderData.getTitle());
            }
            if (!TextUtils.isEmpty(sliderData.getDesc())) {
                descTv.setVisibility(View.VISIBLE);
                descTv.setText(sliderData.getDesc());
            }
        }
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int clickType = sliderData.getClickType();
                final int clickId = sliderData.getClickId();
                if (clickType != -1) {
                    switch (clickType) {
                        case ITEM:
                            getContext().startActivity(ItemActivity.getIntent(getContext(), clickId));
                            break;
                        case CATEGORY:
                            ItemListActiv.open(getContext(), false, clickId);
                            break;
                        case SELLER:
                            ItemListActiv.open(getContext(), true, clickId);
                            break;
                        case DASH:
                            final int sellerId = LubbleSharedPrefs.getInstance().getSellerId();
                            if (sellerId == -1) {
                                // no seller ID found, start activ to create a new seller
                                SellerEditActiv.open(getContext());
                            } else {
                                // seller ID found, open dashboard
                                getContext().startActivity(SellerDashActiv.getIntent(getContext(), sellerId, false));
                            }
                            break;
                    }
                }
            }
        });

        return view;
    }

}
