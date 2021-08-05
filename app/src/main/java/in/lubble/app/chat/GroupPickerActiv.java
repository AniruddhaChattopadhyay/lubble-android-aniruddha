package in.lubble.app.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import in.lubble.app.BaseActivity;
import in.lubble.app.GlideApp;
import in.lubble.app.GlideRequests;
import in.lubble.app.R;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.models.GroupData;
import in.lubble.app.models.GroupInfoData;
import in.lubble.app.models.UserGroupData;

import static in.lubble.app.firebase.RealtimeDbHelper.getLubbleGroupInfoRef;
import static in.lubble.app.firebase.RealtimeDbHelper.getUserGroupsRef;

public class GroupPickerActiv extends BaseActivity {

    private static final String TAG = "GroupPickerActiv";

    private ProgressBar progressbar;
    private RecyclerView recyclerView;
    private HashMap<String, UserGroupData> userGroupDataMap;
    private GroupPickerAdapter groupPickerAdapter;

    public static Intent getIntent(Context context) {
        return new Intent(context, GroupPickerActiv.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_picker);

        progressbar = findViewById(R.id.progressbar_group_picker);
        recyclerView = findViewById(R.id.rv_group_picker);
        ImageView closeIv = findViewById(R.id.iv_group_picker_close);

        progressbar.setVisibility(View.VISIBLE);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration itemDecor = new DividerItemDecoration(this, LinearLayoutManager.VERTICAL);
        recyclerView.addItemDecoration(itemDecor);
        groupPickerAdapter = new GroupPickerAdapter(GlideApp.with(GroupPickerActiv.this));
        recyclerView.setAdapter(groupPickerAdapter);

        closeIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        userGroupDataMap = new HashMap<>();
        syncUserGroupIds();
    }

    private void syncUserGroupIds() {
        // gets list of group IDs joined by the user
        getUserGroupsRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot dataSnapshot) {
                progressbar.setVisibility(View.GONE);
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    final UserGroupData userGroupData = snapshot.getValue(UserGroupData.class);
                    if (userGroupData != null) {
                        userGroupDataMap.put(snapshot.getKey(), userGroupData);
                        if (userGroupData.isJoined()) {
                            syncJoinedGroups(snapshot.getKey());
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                FirebaseCrashlytics.getInstance().recordException(error.toException());
                progressbar.setVisibility(View.GONE);
            }
        });
    }


    private void syncJoinedGroups(String groupId) {
        // get meta data of the groups joined by the user
        getLubbleGroupInfoRef(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GroupInfoData groupData = dataSnapshot.getValue(GroupInfoData.class);
                String groupId = dataSnapshot.getRef().getParent().getKey();
                final UserGroupData userGroupData = userGroupDataMap.get(groupId);
                if (groupData != null && userGroupData != null) {
                    groupData.setId(groupId);
                    groupPickerAdapter.addJoinedGroup(groupData);
                }
            }

            @Override
            public void onCancelled(@NotNull DatabaseError error) {
                FirebaseCrashlytics.getInstance().recordException(error.toException());
            }
        });
    }

    private class GroupPickerAdapter extends RecyclerView.Adapter<GroupPickerAdapter.ViewHolder> {
        private final ArrayList<GroupInfoData> groupList;
        private final GlideRequests glideApp;

        GroupPickerAdapter(GlideRequests glideApp) {
            this.groupList = new ArrayList<>();
            this.glideApp = glideApp;
        }

        void addJoinedGroup(GroupInfoData groupInfoData) {
            groupList.add(groupInfoData);
            notifyItemInserted(groupList.size());
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()), parent);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final GroupInfoData groupData = groupList.get(position);
            glideApp.load(groupData.getThumbnail())
                    .placeholder(R.drawable.ic_circle_group_24dp)
                    .error(R.drawable.ic_circle_group_24dp)
                    .circleCrop()
                    .into(holder.groupDpIv);
            holder.groupNameTv.setText(groupData.getTitle());
            holder.groupDescTv.setText(groupData.getDescription());
        }

        @Override
        public int getItemCount() {
            return groupList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            final ImageView groupDpIv;
            final TextView groupNameTv;
            final TextView groupDescTv;

            ViewHolder(LayoutInflater inflater, ViewGroup parent) {
                super(inflater.inflate(R.layout.item_group_picker, parent, false));
                groupDpIv = itemView.findViewById(R.id.iv_group_dp);
                groupNameTv = itemView.findViewById(R.id.tv_group_name);
                groupDescTv = itemView.findViewById(R.id.tv_group_desc);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final Intent intent = new Intent();
                        intent.putExtra("group_id", groupList.get(getAdapterPosition()).getId());
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                });
            }
        }

    }
}
