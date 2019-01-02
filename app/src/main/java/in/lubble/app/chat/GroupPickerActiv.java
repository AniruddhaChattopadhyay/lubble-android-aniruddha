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
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import in.lubble.app.GlideApp;
import in.lubble.app.GlideRequests;
import in.lubble.app.R;
import in.lubble.app.firebase.RealtimeDbHelper;
import in.lubble.app.models.GroupData;

import java.util.ArrayList;

public class GroupPickerActiv extends AppCompatActivity {

    private static final String TAG = "GroupPickerActiv";

    private ProgressBar progressbar;
    private RecyclerView recyclerView;
    private Query query;
    private ValueEventListener valueEventListener;

    public static Intent getIntent(Context context) {
        return new Intent(context, GroupPickerActiv.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_picker);

        progressbar = findViewById(R.id.progressbar_group_picker);
        recyclerView = findViewById(R.id.rv_group_picker);

        progressbar.setVisibility(View.VISIBLE);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        recyclerView.setAdapter(new GroupPickerAdapter(new ArrayList<GroupData>(), GlideApp.with(GroupPickerActiv.this)));

        query = RealtimeDbHelper.getLubbleGroupsRef().orderByChild("lastMessageTimestamp");
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final ArrayList<GroupData> groupDataList = new ArrayList<>();

                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    final GroupData groupData = child.getValue(GroupData.class);
                    if (groupData.getMembers().containsKey(FirebaseAuth.getInstance().getUid()) && !groupData.getIsPrivate()) {
                        groupDataList.add(groupData);
                    }
                }
                progressbar.setVisibility(View.GONE);
                recyclerView.setAdapter(new GroupPickerAdapter(groupDataList, GlideApp.with(GroupPickerActiv.this)));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        query.addValueEventListener(valueEventListener);
    }

    private class GroupPickerAdapter extends RecyclerView.Adapter<GroupPickerAdapter.ViewHolder> {

        private ArrayList<GroupData> groupList;
        private GlideRequests glideApp;

        GroupPickerAdapter(ArrayList<GroupData> groupList, GlideRequests glideApp) {
            this.groupList = groupList;
            this.glideApp = glideApp;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()), parent);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final GroupData groupData = groupList.get(position);
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

    @Override
    protected void onPause() {
        super.onPause();
        if (valueEventListener != null) {
            query.removeEventListener(valueEventListener);
        }
    }
}
