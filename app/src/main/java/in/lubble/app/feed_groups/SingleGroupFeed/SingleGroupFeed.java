package in.lubble.app.feed_groups.SingleGroupFeed;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.net.MalformedURLException;
import java.util.List;

import in.lubble.app.LubbleSharedPrefs;
import in.lubble.app.R;
import in.lubble.app.feed.AddPostForFeed;
import in.lubble.app.feed.FeedAdaptor;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import in.lubble.app.services.FeedServices;
import io.getstream.core.exceptions.StreamException;
import io.getstream.core.models.Activity;
import io.getstream.core.options.Limit;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;

public class SingleGroupFeed extends Fragment {

    private FloatingActionButton postBtn;
    private RecyclerView feedRV;
    private List<Activity> activities = null;
    private static final int REQUEST_CODE_POST = 800;
    public SingleGroupFeed() {
        // Required empty public constructor
    }

    public static SingleGroupFeed newInstance() {
        SingleGroupFeed fragment = new SingleGroupFeed();
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

        View rootView = inflater.inflate(R.layout.fragment_single_group_feed, container, false);
        postBtn = rootView.findViewById(R.id.btn_new_post);
        feedRV =  rootView.findViewById(R.id.feed_recyclerview);
        postBtn.setOnClickListener(v -> {
            startActivityForResult(new Intent(getContext(), AddPostForFeed.class), REQUEST_CODE_POST);
        });
        getCredentials();
        return rootView;
    }

    void getCredentials(){
        final Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
        Call<Endpoints.StreamCredentials> call = endpoints.getStreamCredentials("Badminton_" + LubbleSharedPrefs.getInstance().getLubbleName());
        call.enqueue(new Callback<Endpoints.StreamCredentials>() {
            @Override
            public void onResponse(Call<Endpoints.StreamCredentials> call, Response<Endpoints.StreamCredentials> response) {
                if (response.isSuccessful()) {
                    //Toast.makeText(getContext(), R.string.upload_success, Toast.LENGTH_SHORT).show();
                    assert response.body() != null;
                    final Endpoints.StreamCredentials credentials = response.body();
                    try {
                        FeedServices.init(credentials.getApi_key(), credentials.getUser_token());

                        initRecyclerView();

                    } catch (MalformedURLException | StreamException e) {
                        e.printStackTrace();
                    }

                } else {
                    Toast.makeText(getContext(), R.string.all_try_again, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Endpoints.StreamCredentials> call, Throwable t) {
                Toast.makeText(getContext(), R.string.all_try_again, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initRecyclerView() throws StreamException {
        activities = FeedServices.client.flatFeed("group","Badminton_"+ LubbleSharedPrefs.getInstance().getLubbleName())
                .getActivities(new Limit(25))
                .join();
        Log.d("hey","hey");
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        feedRV.setVisibility(View.VISIBLE);
        feedRV.setLayoutManager(layoutManager);
        FeedAdaptor adapter = new FeedAdaptor(getContext(),activities);
        feedRV.setAdapter(adapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_POST && resultCode == RESULT_OK){
            try {
                initRecyclerView();
            } catch (StreamException e) {
                e.printStackTrace();
            }
        }
    }
}