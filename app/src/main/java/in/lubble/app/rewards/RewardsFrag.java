package in.lubble.app.rewards;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.cooltechworks.views.shimmer.ShimmerRecyclerView;
import in.lubble.app.GlideApp;
import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import in.lubble.app.rewards.data.RewardsAirtableData;
import in.lubble.app.rewards.data.RewardsRecordData;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RewardsFrag extends Fragment {

    private static final String TAG = "RewardsFrag";

    private OnListFragmentInteractionListener mListener;
    private TextView noRewardsTv;
    private ShimmerRecyclerView shimmerRecyclerView;

    public RewardsFrag() {
    }

    @SuppressWarnings("unused")
    public static RewardsFrag newInstance() {
        RewardsFrag fragment = new RewardsFrag();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_rewards, container, false);

        noRewardsTv = view.findViewById(R.id.tv_no_rewards);
        shimmerRecyclerView = view.findViewById(R.id.rv_rewards);
        shimmerRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        fetchRewards();

        return view;
    }


    private void fetchRewards() {
        shimmerRecyclerView.showShimmerAdapter();
        noRewardsTv.setVisibility(View.GONE);
        String formula = "LubbleId=\'" + LubbleSharedPrefs.getInstance().getLubbleId() + "\'";
        String url = "https://api.airtable.com/v0/appbhSWmy7ZS6UeTy/Rewards?filterByFormula=" + formula + "&view=Grid%20view";

        final Endpoints endpoints = ServiceGenerator.createAirtableService(Endpoints.class);
        endpoints.fetchRewards(url).enqueue(new Callback<RewardsAirtableData>() {
            @Override
            public void onResponse(Call<RewardsAirtableData> call, Response<RewardsAirtableData> response) {
                final RewardsAirtableData airtableData = response.body();
                if (response.isSuccessful() && airtableData != null && isAdded() && isVisible()) {
                    if (airtableData.getRecords().size() > 0) {
                        final String photoUrl = airtableData.getRecords().get(0).getFields().getPhoto();
                        GlideApp.with(requireContext()).load(photoUrl).diskCacheStrategy(DiskCacheStrategy.NONE).listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                shimmerRecyclerView.hideShimmerAdapter();
                                Toast.makeText(requireContext(), R.string.all_try_again, Toast.LENGTH_SHORT).show();
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                shimmerRecyclerView.hideShimmerAdapter();
                                shimmerRecyclerView.setAdapter(new RewardsAdapter(airtableData.getRecords(), GlideApp.with(requireContext())));
                                return false;
                            }
                        }).preload();
                    } else {
                        noRewardsTv.setVisibility(View.VISIBLE);
                    }

                } else {
                    if (isAdded() && isVisible()) {
                        shimmerRecyclerView.hideShimmerAdapter();
                        Toast.makeText(requireContext(), R.string.all_try_again, Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<RewardsAirtableData> call, Throwable t) {
                if (isAdded() && isVisible()) {
                    Toast.makeText(requireContext(), R.string.check_internet, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "onFailure: ");
                    shimmerRecyclerView.hideShimmerAdapter();
                }
            }
        });
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(RewardsRecordData item);
    }
}
