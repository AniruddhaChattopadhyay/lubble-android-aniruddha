package in.lubble.app.groups.group_info;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import in.lubble.app.GlideApp;
import in.lubble.app.R;
import in.lubble.app.models.GroupData;
import in.lubble.app.user_search.UserSearchActivity;
import in.lubble.app.utils.FullScreenImageActivity;

import static in.lubble.app.Constants.DEFAULT_LUBBLE;
import static in.lubble.app.firebase.RealtimeDbHelper.getLubbleGroupsRef;
import static in.lubble.app.utils.UiUtils.dpToPx;

public class GroupInfoFragment extends Fragment {
    private static final String ARG_GROUP_ID = "GroupInfoFragment_GroupId";
    private String groupId;
    private ImageView groupIv;
    private TextView titleTv;
    private TextView descTv;
    private LinearLayout inviteMembersContainer;
    private RecyclerView recyclerView;
    private GroupMembersAdapter adapter;
    private ArrayList<String> memberList;

    public GroupInfoFragment() {
        // Required empty public constructor
    }

    public static GroupInfoFragment newInstance(String groupId) {
        GroupInfoFragment fragment = new GroupInfoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_GROUP_ID, groupId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            groupId = getArguments().getString(ARG_GROUP_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_group_info, container, false);

        groupIv = view.findViewById(R.id.iv_group_image);
        titleTv = view.findViewById(R.id.tv_group_title);
        descTv = view.findViewById(R.id.tv_group_desc);
        inviteMembersContainer = view.findViewById(R.id.linearLayout_invite_container);
        recyclerView = view.findViewById(R.id.rv_group_members);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new GroupMembersAdapter();
        recyclerView.setAdapter(adapter);

        inviteMembersContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserSearchActivity.newInstance(getContext(), groupId);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        syncGroupInfo();
    }

    private void syncGroupInfo() {
        getLubbleGroupsRef().child(groupId)
                .addValueEventListener(groupInfoEventListener);
    }

    final ValueEventListener groupInfoEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            final GroupData groupData = dataSnapshot.getValue(GroupData.class);

            titleTv.setText(groupData.getTitle());
            descTv.setText(groupData.getDescription());
            GlideApp.with(getContext())
                    .load(groupData.getProfilePic())
                    .placeholder(R.drawable.ic_account_circle_black_no_padding)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            groupIv.setPadding(dpToPx(56), dpToPx(56), dpToPx(56), dpToPx(56));
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            groupIv.setPadding(0, 0, 0, 0);
                            return false;
                        }
                    })
                    .into(groupIv);
            memberList = new ArrayList<>(groupData.getMembers().keySet());
            adapter.addAllMembers(memberList);


            groupIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FullScreenImageActivity.open(getActivity(),
                            getContext(),
                            groupData.getProfilePic(),
                            groupIv,
                            "lubbles/" + DEFAULT_LUBBLE + "/groups/" + groupId
                    );
                }
            });
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    public void onPause() {
        super.onPause();
        getLubbleGroupsRef().child(groupId).removeEventListener(groupInfoEventListener);
    }
}
