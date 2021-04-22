package in.lubble.app.feed;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import in.lubble.app.R;
import in.lubble.app.feed.services.FeedServices;
import in.lubble.app.network.Endpoints;
import in.lubble.app.network.ServiceGenerator;
import io.getstream.core.exceptions.StreamException;
import io.getstream.core.models.Activity;
import io.getstream.core.options.Limit;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;

public class FeedFrag extends Fragment {
    private static final String TAG = "FeedFrag";
    private static final String user = "c4ZIgCriHdcU5avx70AgY0000jj1";
    private FloatingActionButton postBtn;
    private RecyclerView feedRV;
    private List<Activity> activities = null;
    private static final int REQUEST_CODE_POST = 800;

    public FeedFrag() {
        // Required empty public constructor
    }

    public static FeedFrag newInstance() {
        FeedFrag fragment = new FeedFrag();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_feed, container, false);
        postBtn = view.findViewById(R.id.btn_new_post);
        feedRV =  view.findViewById(R.id.feed_recyclerview);
        postBtn.setOnClickListener(v -> {
                startActivityForResult(new Intent(getContext(),AddPostForFeed.class), REQUEST_CODE_POST);
        });
        getCredentials();
        return view;
    }

    void getCredentials(){
        final Endpoints endpoints = ServiceGenerator.createService(Endpoints.class);
        Call<Endpoints.StreamCredentials> call = endpoints.getStreamCredentials();
        call.enqueue(new Callback<Endpoints.StreamCredentials>() {
            @Override
            public void onResponse(Call<Endpoints.StreamCredentials> call, Response<Endpoints.StreamCredentials> response) {
                if (response.isSuccessful()) {
                    //Toast.makeText(getContext(), R.string.upload_success, Toast.LENGTH_SHORT).show();
                    assert response.body() != null;
                    final Endpoints.StreamCredentials credentials = response.body();
                    /*try {
                        //FeedServices.init(credentials.getApi_key(), credentials.getUser_token());

                        initRecyclerView();

                    } catch (MalformedURLException | StreamException e) {
                        e.printStackTrace();
                    }*/

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
        activities = FeedServices.client.flatFeed("user")
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
