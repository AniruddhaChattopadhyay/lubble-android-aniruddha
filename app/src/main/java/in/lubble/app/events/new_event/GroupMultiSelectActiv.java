package in.lubble.app.events.new_event;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import in.lubble.app.BaseActivity;
import in.lubble.app.GlideApp;
import in.lubble.app.GlideRequests;
import in.lubble.app.R;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.models.GroupData;

public class GroupMultiSelectActiv extends BaseActivity {

    private static final String TAG = "GroupMultiSelectActiv";

    private ProgressBar progressbar;
    private RecyclerView recyclerView;
    private Query query;
    private Button doneBtn;
    private ValueEventListener valueEventListener;
    private HashMap<String, Boolean> selectedGroupsMap = new HashMap<>();

    public static Intent getIntent(Context context) {
        return new Intent(context, GroupMultiSelectActiv.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiselect_group);

        Toolbar toolbar = findViewById(R.id.text_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle("Select Related Public Groups");

        progressbar = findViewById(R.id.progressbar_group_picker);
        recyclerView = findViewById(R.id.rv_group_picker);
        doneBtn = findViewById(R.id.btn_done);

        doneBtn.setText("SELECT SOME GROUPS");
        doneBtn.setAlpha(0.3f);
        doneBtn.setEnabled(false);

        progressbar.setVisibility(View.VISIBLE);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration itemDecor = new DividerItemDecoration(this, LinearLayoutManager.VERTICAL);
        recyclerView.addItemDecoration(itemDecor);

        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ArrayList<String> selectedIdList = new ArrayList<>(selectedGroupsMap.keySet());
                final Intent intent = new Intent();
                intent.putExtra("selected_list", selectedIdList);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        recyclerView.setAdapter(new MultiGroupPickerAdapter(new ArrayList<GroupData>(), GlideApp.with(GroupMultiSelectActiv.this)));

        /*query = RealtimeDbHelper.getLubbleGroupsRef().orderByChild("lastMessageTimestamp");
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final ArrayList<GroupData> groupDataList = new ArrayList<>();

                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    final GroupData groupData = child.getValue(GroupData.class);
                    if (groupData != null && !groupData.getIsPrivate()) {
                        groupDataList.add(groupData);
                    }
                }
                Collections.reverse(groupDataList);
                progressbar.setVisibility(View.GONE);
                recyclerView.setAdapter(new MultiGroupPickerAdapter(groupDataList, GlideApp.with(GroupMultiSelectActiv.this)));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        query.addValueEventListener(valueEventListener);*/
    }

    private class MultiGroupPickerAdapter extends RecyclerView.Adapter<MultiGroupPickerAdapter.ViewHolder> {

        private ArrayList<GroupData> groupList;
        private GlideRequests glideApp;

        MultiGroupPickerAdapter(ArrayList<GroupData> groupList, GlideRequests glideApp) {
            this.groupList = groupList;
            this.glideApp = glideApp;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()), parent);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final GroupData groupData = groupList.get(position);
            if (selectedGroupsMap.containsKey(groupData.getId())) {
                holder.groupDpIv.setImageResource(R.drawable.ic_check_circle_green_on_white);
            } else {
                glideApp.load(groupData.getThumbnail())
                        .placeholder(R.drawable.ic_circle_group_24dp)
                        .error(R.drawable.ic_circle_group_24dp)
                        .circleCrop()
                        .into(holder.groupDpIv);
            }
            holder.groupNameTv.setText(groupData.getTitle());
            holder.groupDescTv.setText(groupData.getDescription());
            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!selectedGroupsMap.containsKey(groupData.getId())) {
                        holder.groupDpIv.setImageResource(R.drawable.ic_check_circle_green_on_white);
                        selectedGroupsMap.put(groupData.getId(), Boolean.TRUE);
                    } else {
                        glideApp.load(groupData.getThumbnail())
                                .placeholder(R.drawable.ic_circle_group_24dp)
                                .error(R.drawable.ic_circle_group_24dp)
                                .circleCrop()
                                .into(holder.groupDpIv);
                        selectedGroupsMap.remove(groupData.getId());
                    }
                    final int selectedGroupsCount = selectedGroupsMap.size();
                    if (selectedGroupsCount == 0) {
                        doneBtn.setText("SELECT SOME GROUPS");
                        doneBtn.setAlpha(0.3f);
                        doneBtn.setEnabled(false);
                    } else {
                        doneBtn.setText(String.format("SELECT %s", getResources().getQuantityString(R.plurals.group_count, selectedGroupsCount, selectedGroupsCount)));
                        doneBtn.setAlpha(1f);
                        doneBtn.setEnabled(true);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return groupList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            final ImageView groupDpIv;
            final TextView groupNameTv;
            final TextView groupDescTv;
            final View view;

            ViewHolder(LayoutInflater inflater, ViewGroup parent) {
                super(inflater.inflate(R.layout.item_multi_group_picker, parent, false));
                groupDpIv = itemView.findViewById(R.id.iv_group_dp);
                groupNameTv = itemView.findViewById(R.id.tv_group_name);
                groupDescTv = itemView.findViewById(R.id.tv_group_desc);
                view = itemView;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (valueEventListener != null) {
            query.removeEventListener(valueEventListener);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
