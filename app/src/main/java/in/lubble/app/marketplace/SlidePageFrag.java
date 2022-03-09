package in.lubble.app.marketplace;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import in.lubble.app.GlideApp;
import in.lubble.app.R;
import in.lubble.app.utils.UiUtils;

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
        TextView titleTv = view.findViewById(R.id.tv_slider_title);

        RequestOptions requestOptions = new RequestOptions();

        if (!TextUtils.isEmpty(sliderData.getTitle())) {
            titleTv.setVisibility(View.VISIBLE);
            titleTv.setText(sliderData.getTitle());
            requestOptions = requestOptions.transform(new RoundedCorners(UiUtils.dpToPx(8)));
        } else {
            titleTv.setVisibility(View.GONE);
            requestOptions = requestOptions.transform(new CenterCrop(), new RoundedCorners(UiUtils.dpToPx(8)));
        }

        GlideApp.with(requireContext()).load(sliderData.getUrl()).placeholder(R.color.gray).error(R.color.gray)
                .apply(requestOptions)
                .into(slideIv);

        view.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(sliderData.getDeepLink())) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(sliderData.getDeepLink()));
                startActivity(intent);
            }
        });

        return view;
    }

}
